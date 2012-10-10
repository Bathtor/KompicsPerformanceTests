package se.sics.kompics.scala.performancetest.kompics

import se.sics.kompics.Event
import se.sics.kompics.address.Address
import se.sics.kompics.PortType

case class SBSend(val msg: Any) extends Event
case class SBDeliver(val src: Address, val m: Any) extends Event

object BasicBroadcast extends PortType {
	PortType.preloadInstance(this);
	request(classOf[SBSend]);
	indication(classOf[SBDeliver]);	
}