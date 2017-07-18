package se.sics.kompics.performancetest.akka

import akka.actor._
import java.util.concurrent.CountDownLatch

case object Run
case object Ping
case object Pong

class Destination extends Actor {
  def receive = {
    case Ping ⇒ sender ! Pong
  }
}

class TestClient(
    val actor: ActorRef,
    val latch: CountDownLatch,
    val repeat: Long) extends Actor {

  private var sent = 0L
  private var received = 0L

  def receive = {
    case Pong ⇒
      received += 1
      if (sent < repeat) {
        actor ! Ping
        sent += 1
      } else if (received >= repeat) {
        latch.countDown()
      }
    case Run ⇒
      for (i ← 0L until math.min(1000L, repeat)) {
        actor ! Ping
        sent += 1
      }
  }

}

//class BatchedTestClient(
//    val actor: ActorRef,
//    val latch: CountDownLatch,
//    val repeat: Long,
//    val batchSize: Long) extends Actor {
//
//  private var sent = 0L;
//  private var batchSent = 0L;
//  private var received = 0L;
//  private var batchReceived = 0L;
//
//  def receive = {
//    case Pong ⇒
//      batchReceived += 1;
//      received += 1;
//      if (received >= repeat) {
//        latch.countDown()
//      } else if (batchReceived > batchSize) {
//        batchReceived = 0L;
//        for (i <- 0L until math.min(batchSize, repeat)) {
//          if (sent < repeat) {
//            actor ! Ping
//            sent += 1
//          }
//        }
//      }
//    case Run ⇒
//      for (i ← 0L until math.min(1000L, repeat)) {
//        actor ! Ping
//        sent += 1
//      }
//  }
//
//}
