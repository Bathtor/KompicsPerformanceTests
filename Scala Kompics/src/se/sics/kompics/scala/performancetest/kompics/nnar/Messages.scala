package se.sics.kompics.scala.performancetest.kompics.nnar

import se.sics.kompics.Event


sealed trait ARMessage extends Event

// Scenario messages
case class WarmUp(time: Int) extends ARMessage
case class Measure(time: Int) extends ARMessage