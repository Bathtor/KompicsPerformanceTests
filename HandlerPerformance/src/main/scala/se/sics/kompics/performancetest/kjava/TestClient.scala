package se.sics.kompics.performancetest.kjava;

import se.sics.kompics._
import java.util.concurrent.CountDownLatch
import com.typesafe.scalalogging.StrictLogging

case object Ping extends KompicsEvent
case object Pong extends KompicsEvent

class PPPort extends PortType {
  indication(Pong.getClass);
  request(Ping.getClass);
}

case class ParentInit(nClients: Int, startLatch: CountDownLatch, stopLatch: CountDownLatch, repeat: Long) extends Init[TestParent]
case class ClientInit(latch: CountDownLatch, repeat: Long) extends Init[TestClient]

class TestParent(init: ParentInit) extends ComponentDefinition {
  for (i <- 0 until init.nClients) {
    val dest = create(classOf[Destination], Init.NONE);
    val client = create(classOf[TestClient], ClientInit(init.stopLatch, init.repeat));
    connect(client.getNegative(classOf[PPPort]), dest.getPositive(classOf[PPPort]), Channel.TWO_WAY);
  }

  val startHandler = new Handler[Start]() {
    override def handle(event: Start) {
      init.startLatch.countDown();
      logger.info(s"Parent is starting with ${init.nClients} clients.");
    }
  };
  subscribe(startHandler, control);
  val stopHandler = new Handler[Stop]() {
    override def handle(event: Stop) {
      logger.info(s"Parent is stopping.");
    }
  };
  subscribe(stopHandler, control);
  val killHandler = new Handler[Kill]() {
    override def handle(event: Kill) {
      logger.info(s"Parent is stopping.");
    }
  };
  subscribe(killHandler, control);
}

class Destination() extends ComponentDefinition {
  val ppp = provides(classOf[PPPort]);

  val pingHandler = new Handler[Ping.type]() {
    override def handle(event: Ping.type) {
      trigger(Pong, ppp);
    }
  }
  subscribe(pingHandler, ppp);
}

class TestClient(init: ClientInit) extends ComponentDefinition {
  val ppp = requires(classOf[PPPort]);

  private var sent = 0L
  private var received = 0L

  val startHandler = new Handler[Start]() {
    override def handle(event: Start) {
      for (i <- 0L until math.min(1000L, init.repeat)) {
        trigger(Ping, ppp);
        sent += 1;
      }
    }
  };
  subscribe(startHandler, control);

  val pongHandler = new Handler[Pong.type]() {
    override def handle(event: Pong.type) {
      received += 1;
      if (sent < init.repeat) {
        trigger(Ping, ppp);
        sent += 1;
      } else if (received >= init.repeat) {
        init.latch.countDown();
      }
    }
  }
  subscribe(pongHandler, ppp);
}
