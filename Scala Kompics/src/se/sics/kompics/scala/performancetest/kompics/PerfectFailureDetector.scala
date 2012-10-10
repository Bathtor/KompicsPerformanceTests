package se.sics.kompics.scala.performancetest.kompics

import se.sics.kompics.address.Address
import se.sics.kompics.Event
import se.sics.kompics.PortType

case class Terminated(node: Address) extends Event

object PerfectFailureDetector extends PortType {
	PortType.preloadInstance(this);
	
	indication(classOf[Terminated]);
}