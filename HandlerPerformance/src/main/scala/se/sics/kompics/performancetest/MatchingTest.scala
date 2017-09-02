package se.sics.kompics.performancetest

import org.scalatest.{ WordSpec, Matchers }
import se.sics.kompics.{ Kompics, KompicsEvent, Start }
import se.sics.kompics.sl._

class MatchingTest extends WordSpec with Matchers {

  val repeat = 2L * 1000L * 1000L * 1000L;

  "type-only test" in {
    val h: Handler = (e: KompicsEvent) => e match {
      case _: Start => handle {
        ()
      }
    }
    runIndirectWith(Start.event, h);
    val h2: PartialFunction[KompicsEvent, Unit] = (e: KompicsEvent) => e match {
      case _: Start => ()
    }
    runDirectWith(Start.event, h2);
  }

  "single-match test" in {
    val h: Handler = (e: KompicsEvent) => e match {
      case SingleField(s) => handle {
        ()
      }
    }
    runIndirectWith(SingleField("test"), h);
    val h2: PartialFunction[KompicsEvent, Unit] = (e: KompicsEvent) => e match {
      case SingleField(s) => ()
    }
    runDirectWith(SingleField("test"), h2);
  }

  "multi-match test" in {
    val h: Handler = (e: KompicsEvent) => e match {
      case MultiField(s1, s2, s3, s4) => handle {
        ()
      }
    }
    runIndirectWith(MultiField("test1", "test2", "test3", "test4"), h);
    val h2: PartialFunction[KompicsEvent, Unit] = (e: KompicsEvent) => e match {
      case MultiField(s1, s2, s3, s4) => ()
    }
    runDirectWith(MultiField("test1", "test2", "test3", "test4"), h2);
  }

  "deep-match test" in {
    val h: Handler = (e: KompicsEvent) => e match {
      case DeepField(Some(s)) => handle {
        ()
      }
    }
    runIndirectWith(DeepField(Some("test")), h);
    val h2: PartialFunction[KompicsEvent, Unit] = (e: KompicsEvent) => e match {
      case DeepField(Some(s)) => ()
    }
    runDirectWith(DeepField(Some("test")), h2);
  }

  "deeper-match test" in {
    val h: Handler = (e: KompicsEvent) => e match {
      case DeeperField(DeepField(Some(s))) => handle {
        ()
      }
    }
    runIndirectWith(DeeperField(DeepField(Some("test"))), h);
    val h2: PartialFunction[KompicsEvent, Unit] = (e: KompicsEvent) => e match {
      case DeeperField(DeepField(Some(s))) => ()
    }
    runDirectWith(DeeperField(DeepField(Some("test"))), h2);
  }

  def runIndirectWith(event: KompicsEvent, handler: Handler) {
    val start = System.nanoTime;
    var i = 0L;
    while (i < repeat) {
      try {
        val c = handler(event);
        val r = c();
      } catch {
        case e: Throwable => println(e);
      }
      i += 1;
    }
    val durationNs = (System.nanoTime - start);
    logMeasurement("indirect", durationNs);
  }

  def runDirectWith(event: KompicsEvent, handler: PartialFunction[KompicsEvent, Unit]) {
    val start = System.nanoTime;
    var i = 0L;
    while (i < repeat) {
      try {
        val c = handler.isDefinedAt(event);
        val r = handler(event);
      } catch {
        case e: Throwable => println(e);
      }
      i += 1;
    }
    val durationNs = (System.nanoTime - start);
    logMeasurement("direct", durationNs);
  }

  def logMeasurement(name: String, durationNs: Long) {
    val durationS = durationNs.toDouble / 1000000000.0;
    val tps = (repeat.toDouble / durationS);

    println(s"$name: $tps matches/s")
  }

  case class SingleField(a: String) extends KompicsEvent
  case class MultiField(a: String, b: String, c: String, d: String) extends KompicsEvent
  case class DeepField(ao: Option[String]) extends KompicsEvent
  case class DeeperField(ado: DeepField) extends KompicsEvent

}
