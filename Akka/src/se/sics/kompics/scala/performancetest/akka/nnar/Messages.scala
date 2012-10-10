package se.sics.kompics.scala.performancetest.akka.nnar

import akka.actor.ActorRef

sealed trait ARMessage

// Read from register
case class NNARRead extends ARMessage

// Write value to register
case class NNARWrite(value: Any) extends ARMessage

// Return read value from register
case class NNARReadReturn(value: Any) extends ARMessage

// Confirm write to register
case class NNARWriteReturn() extends ARMessage

// Setup messages
case class RIWCInit(group: Set[ActorRef], router: ActorRef) extends ARMessage

// Scenario messages
case class WarmUp(time: Int) extends ARMessage
case class Measure(time: Int) extends ARMessage
case class Register(r: ActorRef) extends ARMessage