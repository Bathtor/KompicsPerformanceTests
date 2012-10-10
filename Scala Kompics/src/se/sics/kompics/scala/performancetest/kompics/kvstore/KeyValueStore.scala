package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.PortType
import se.sics.kompics.Event

case class KVSPut(key: String, value: Any) extends Event
case class KVSGet(key: String) extends Event
case class KVSPutReply(key: String) extends Event
case class KVSGetReply(key: String, value: Any) extends Event

object KeyValueStore extends PortType {
	PortType.preloadInstance(this);
	request(classOf[KVSPut]);
	request(classOf[KVSGet]);
	indication(classOf[KVSPutReply]);
	indication(classOf[KVSGetReply]);
}