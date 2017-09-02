package se.sics.kompics.performancetest

import org.scalatest.{ WordSpec, Matchers }
import se.sics.kompics._
import java.util.concurrent.locks.ReentrantReadWriteLock;

class HandleTest extends WordSpec with Matchers {
  import HandleTest._

  val repeat = 2L * 1000L * 1000L;
  val nIter = 1000;

  "counter performance" in {
    val a = new MyActor();
    val res = measure {
      var i = 0;
      while (i < nIter) {
        try {
          a.handle(Req("req"));
        } catch {
          case e: Throwable => println(e);
        }
        i += 1;
      }
    }
    println(s"Counter: ${res._1} ns/1000iter (+- ${res._2})");
  }

  "counter performance static" in {
    val a = new MyActor();
    val res = measure {
      var i = 0;
      while (i < nIter) {
        try {
          a.handleStatic(Req("req"));
        } catch {
          case e: Throwable => println(e);
        }
        i += 1;
      }
    }
    println(s"Counter Static: ${res._1} ns/1000iter (+- ${res._2})");
  }

  def measure(f: => Unit): (Double, Double) = {
    val f2 = f _;
    var i = 0L;
    var sum = 0l;
    var max = 0l;
    var min = 0l;
    while (i < repeat) {
      val start = System.nanoTime;
      f2();
      val dur = (System.nanoTime - start);
      sum += dur;
      max = if (dur > max) dur else max;
      min = if (dur < min) dur else min;
      i += 1L;
    }
    val avg = sum.toDouble / repeat.toDouble;
    val maxdiff = max.toDouble - avg;
    val mindiff = avg - min.toDouble;
    (avg, Math.max(maxdiff, mindiff))
  }

}

object HandleTest {
  sealed trait MyMessages extends KompicsEvent
  case class Req(s: String) extends MyMessages
  case class Resp(s: String) extends MyMessages

  class MyActor {
    private val rwlock = new ReentrantReadWriteLock();
    private var counter = 0l;

    private val handler = new Handler[MyMessages]() {

      override def getEventType(): Class[MyMessages] = classOf[MyMessages];

      override def handle(event: MyMessages) {
        //println(s"Got $event");
        event match {
          case Req(s) => counter += 1l;
          case _      => println("Got wrong message type!");
        }
      }
    };

    private val handlers = Array[Handler[KompicsEvent]](handler.asInstanceOf[Handler[KompicsEvent]]);

    def handle(msg: KompicsEvent) {
      rwlock.writeLock().lock();
      try {
        for (h <- handlers) {
          if (h.getEventType.isAssignableFrom(msg.getClass)) {
            h.handle(msg)
          }
        }
      } finally {
        rwlock.writeLock().unlock();
      }
    }

    def handleStatic(msg: MyMessages) {
      rwlock.writeLock().lock();
      try {
        handler.handle(msg);
      } finally {
        rwlock.writeLock().unlock();
      }
    }

  }
}
