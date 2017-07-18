package se.sics.kompics.performancetest.kscala

import se.sics.kompics.{ KompicsEvent, Start, Kill, Stop }
import se.sics.kompics.sl._
import java.util.concurrent.CountDownLatch
import com.typesafe.scalalogging.StrictLogging

case object Ping extends KompicsEvent
case object Pong extends KompicsEvent

object PPPort extends Port {
  indication(Pong)
  request(Ping)
}

case class ParentInit(nClients: Int, startLatch: CountDownLatch, stopLatch: CountDownLatch, repeat: Long) extends se.sics.kompics.Init[TestParent]
case class ClientInit(latch: CountDownLatch, repeat: Long) extends se.sics.kompics.Init[TestClient]

class TestParent(init: ParentInit) extends ComponentDefinition {
  for (i <- 0 until init.nClients) {
    val dest = create(classOf[Destination], Init.NONE);
    val client = create(classOf[TestClient], ClientInit(init.stopLatch, init.repeat));
    connect(PPPort)(dest -> client);
  }

  ctrl uponEvent {
    case _: Start => handle {
      init.startLatch.countDown();
      logger.info(s"Parent is starting with ${init.nClients} clients.");
    }
    case _: Kill | _: Stop => handle {
      logger.info(s"Parent is stopping.");
    }
  }
}

class Destination() extends ComponentDefinition {
  val ppp = provides(PPPort);

  ppp uponEvent {
    case Ping => handle {
      trigger (Pong -> ppp);
    }
  }
}

class TestClient(init: ClientInit) extends ComponentDefinition {
  val ppp = requires(PPPort);

  private var sent = 0L
  private var received = 0L

  ctrl uponEvent {
    case _: Start => handle {
      for (i <- 0L until math.min(1000L, init.repeat)) {
        trigger (Ping -> ppp);
        sent += 1;
      }
    }
  }

  ppp uponEvent {
    case Pong => handle {
      received += 1;
      if (sent < init.repeat) {
        trigger (Ping -> ppp);
        sent += 1;
      } else if (received >= init.repeat) {
        init.latch.countDown();
      }
    }
  }
}
