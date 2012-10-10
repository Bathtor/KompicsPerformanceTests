package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.PortType
import se.sics.kompics.Event

case class RSStore(value: Any) extends Event
case class RSRetrieve(key: BigInt) extends Event
case class RSValue(key: BigInt, value: Any) extends Event

object ReplicatedStore extends PortType {
	PortType.preloadInstance(this);
	request(classOf[RSStore]);
	request(classOf[RSRetrieve]);
	indication(classOf[RSValue]);
}