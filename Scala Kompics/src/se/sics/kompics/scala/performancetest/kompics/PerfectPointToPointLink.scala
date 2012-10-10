package se.sics.kompics.scala.performancetest.kompics

import se.sics.kompics.PortType
import se.sics.kompics.address.Address
import se.sics.kompics.Event

case class Pp2pSend(val dest: Address, val msg: Any) extends Event
case class Pp2pDeliver(val src: Address, val m: Any) extends Event

object PerfectPointToPointLink extends PortType {
	PortType.preloadInstance(this);
	request(classOf[Pp2pSend]);
	indication(classOf[Pp2pDeliver]);	
}