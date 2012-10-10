package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.Event

//case class Timeout;

sealed trait KVMessage extends Event

case class RSInit(timeDelay: Int) extends KVMessage

// Key-Value Store messages


case class KVSInit(writeQuorum: Int, readQuorum: Int) extends KVMessage

// Scenario messages
case class WarmUp(time: Int) extends KVMessage
case class Measure(time: Int) extends KVMessage


case class MainInit(rsTimeDelay: Int, writeQuorum: Int, readQuorum: Int) extends KVMessage