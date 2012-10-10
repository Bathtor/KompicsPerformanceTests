package se.sics.kompics.scala.performancetest.akka.kvstore

import akka.actor.ActorRef

case class Timeout;

sealed trait KVMessage

// Replicated Store messages
case class RSStore(value: Any) extends KVMessage
case class RSRetrieve(key: BigInt) extends KVMessage
case class RSValue(key: BigInt, value: Any) extends KVMessage

case class RSInit(timeDelay: Int, router: ActorRef, kvs: ActorRef) extends KVMessage

// Key-Value Store messages
case class KVSPut(key: String, value: Any) extends KVMessage
case class KVSGet(key: String) extends KVMessage
case class KVSPutReply(key: String) extends KVMessage
case class KVSGetReply(key: String, value: Any) extends KVMessage

case class KVSInit(writeQuorum: Int, readQuorum: Int, router: ActorRef, store: ActorRef) extends KVMessage

// Scenario messages
case class WarmUp(time: Int) extends KVMessage
case class Measure(time: Int) extends KVMessage
case class Register(kvs: ActorRef, rs: ActorRef) extends KVMessage

case class MainInit(kvsRouter: ActorRef, rsRouter: ActorRef, rsTimeDelay: Int, writeQuorum: Int, readQuorum: Int) extends KVMessage