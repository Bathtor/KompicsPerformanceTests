package se.sics.kompics.performancetest

import org.scalatest.{ WordSpec, Matchers }
import java.util.concurrent.CountDownLatch
import com.github.tototoshi.csv._
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, TimeoutException }

class HandlerTest extends WordSpec with Matchers {
  val repeat = 1000L * 1000L * 1000L;
  val maxRunDuration = Duration(1, TimeUnit.HOURS);
  val nCores = 8;
  val throughput = 50;
  val testid = System.currentTimeMillis();

  def performanceTest(runWarmup: Unit => Unit, runScenario: Int => Unit) {

    "warmup" in {
      runWarmup(())
    }
    "warmup more" in {
      runWarmup(())
    }
    "perform with load 1" in {
      runScenario(1)
    }
    "perform with load 2" in {
      runScenario(2)
    }
    "perform with load 4" in {
      runScenario(4)
    }
    "perform with load 6" in {
      runScenario(6)
    }
    "perform with load 8" in {
      runScenario(8)
    }
    "perform with load 10" in {
      runScenario(10)
    }
    "perform with load 12" in {
      runScenario(12)
    }
    "perform with load 14" in {
      runScenario(14)
    }
    "perform with load 16" in {
      runScenario(16)
    }
    "perform with load 18" in {
      runScenario(18)
    }
    "perform with load 20" in {
      runScenario(20)
    }
    "perform with load 22" in {
      runScenario(22)
    }
    "perform with load 24" in {
      runScenario(24)
    }
    //    "perform with load 26" in {
    //      runScenario(26)
    //    }
    //    "perform with load 28" in {
    //      runScenario(28)
    //    }
    //    "perform with load 30" in {
    //      runScenario(30)
    //    }
    //    "perform with load 32" in {
    //      runScenario(32)
    //    }
    //    "perform with load 34" in {
    //      runScenario(34)
    //    }
    //    "perform with load 36" in {
    //      runScenario(36)
    //    }
    //    "perform with load 38" in {
    //      runScenario(38)
    //    }
    //    "perform with load 40" in {
    //      runScenario(40)
    //    }
    //    "perform with load 42" in {
    //      runScenario(42)
    //    }
    //    "perform with load 44" in {
    //      runScenario(44)
    //    }
    //    "perform with load 46" in {
    //      runScenario(46)
    //    }
    //    "perform with load 48" in {
    //      runScenario(48)
    //    }

  }

  "Akka" should {
    behave like performanceTest(_ => runAkkaScenario(8, true), runAkkaScenario(_, false));
  }

  "Kompics Scala" should {
    behave like performanceTest(_ => runKScalaScenario(8, true), runKScalaScenario(_, false));
  }

  "Kompics Java" should {
    behave like performanceTest(_ => runKJavaScenario(8, true), runKJavaScenario(_, false));
  }

  def runAkkaScenario(load: Int, warmup: Boolean) {
    import _root_.akka.actor._;
    import com.typesafe.config.Config;
    import com.typesafe.config.ConfigFactory;
    import se.sics.kompics.performancetest.akka._

    val testConf: Config = ConfigFactory.parseString(s"""
      akka {
        loglevel = "WARNING"
        stdout-loglevel = "WARNING"
        actor {
          default-dispatcher {
            executor = "fork-join-executor"
            fork-join-executor {
              parallelism-min = ${nCores}
              //parallelism-factor = 2.0
              parallelism-max = ${nCores}
            }
            throughput = ${throughput}
          }
        }
      }
      """);
    val system = ActorSystem("test-system", testConf);

    val latch = new CountDownLatch(load)
    val repeatsPerClient = repeat / load;
    val destinations = for (i <- 0 until load)
      yield system.actorOf(Props(new Destination));
    val clients = for (dest <- destinations)
      yield system.actorOf(Props(new TestClient(dest, latch, repeatsPerClient)));

    val start = System.nanoTime;
    clients.foreach(_ ! Run);
    val ok = latch.await(maxRunDuration.toMillis, TimeUnit.MILLISECONDS);
    val durationNs = (System.nanoTime - start);

    if (!warmup) {
      ok shouldBe (true);
      val actualMessages = repeatsPerClient * load.toLong * 2L;
      logMeasurement(s"akka", load, durationNs, actualMessages);
    }
    clients.foreach(system.stop(_))
    destinations.foreach(system.stop(_))

    val tfuture = system.terminate();
    try Await.ready(tfuture, Duration(5, TimeUnit.SECONDS)) catch {
      case _: TimeoutException ⇒ system.log.warning("Failed to stop [{}] within 5 seconds", system.name)
    }
  }

  def runKScalaScenario(load: Int, warmup: Boolean) {
    import se.sics.kompics.performancetest.kscala._
    import se.sics.kompics.Kompics

    val startLatch = new CountDownLatch(1);
    val stopLatch = new CountDownLatch(load)
    val repeatsPerClient = repeat / load;
    try {
      Kompics.setScheduler(new se.sics.kompics.scheduler.ForkJoinScheduler(nCores));
      //Kompics.setScheduler(new se.sics.kompics.scheduler.ThreadPoolScheduler(nCores));
      Kompics.createAndStart(classOf[TestParent], ParentInit(load, startLatch, stopLatch, repeatsPerClient), nCores, throughput);

      if (startLatch.await(maxRunDuration.toMillis, TimeUnit.MILLISECONDS)) {
        val start = System.nanoTime;
        val ok = stopLatch.await(maxRunDuration.toMillis, TimeUnit.MILLISECONDS);
        val durationNs = (System.nanoTime - start);
        if (!warmup) {
          ok shouldBe (true);
          val actualMessages = repeatsPerClient * load.toLong * 2L;
          logMeasurement(s"kscala", load, durationNs, actualMessages);
        }

      } else {
        fail("Kompics did not start in time!");
      }
    } finally {
      Kompics.shutdown();
    }
  }

  def runKJavaScenario(load: Int, warmup: Boolean) {
    import se.sics.kompics.performancetest.kjava._
    import se.sics.kompics.Kompics

    val startLatch = new CountDownLatch(1);
    val stopLatch = new CountDownLatch(load)
    val repeatsPerClient = repeat / load;

    try {
      Kompics.setScheduler(new se.sics.kompics.scheduler.ForkJoinScheduler(nCores));
      //Kompics.setScheduler(new se.sics.kompics.scheduler.ThreadPoolScheduler(nCores));
      Kompics.createAndStart(classOf[TestParent], ParentInit(load, startLatch, stopLatch, repeatsPerClient), nCores, throughput);

      if (startLatch.await(maxRunDuration.toMillis, TimeUnit.MILLISECONDS)) {
        val start = System.nanoTime;
        val ok = stopLatch.await(maxRunDuration.toMillis, TimeUnit.MILLISECONDS);
        val durationNs = (System.nanoTime - start);
        if (!warmup) {
          ok shouldBe (true);
          val actualMessages = repeatsPerClient * load.toLong * 2L;
          logMeasurement(s"kjava", load, durationNs, actualMessages);
        }
        Kompics.shutdown();
      } else {
        fail("Kompics did not start in time!");
      }
    } finally {
      Kompics.shutdown();
    }
  }

  def logMeasurement(name: String, numberOfClients: Int, durationNs: Long, n: Long) {
    val durationS = durationNs.toDouble / 1000000000.0;
    val tps = (n.toDouble / durationS);

    val fName = s"${testid}-${name}-test.csv";
    val writer = CSVWriter.open(fName, append = true);
    try {
      writer.writeRow(List(numberOfClients, n, durationS, tps));
    } finally {
      writer.close();
    }
  }
}
