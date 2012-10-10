package se.sics.kompics.scala.performancetest.kompics.nnar

import se.sics.kompics.PortType
import se.sics.kompics.Event

// Read from register
case class NNARRead extends Event

// Write value to register
case class NNARWrite(value: Any) extends Event

// Return read value from register
case class NNARReadReturn(value: Any) extends Event

// Confirm write to register
case class NNARWriteReturn extends Event

object NNAR extends PortType {
	PortType.preloadInstance(this);
	request(classOf[NNARRead]);
	request(classOf[NNARWrite]);
	indication(classOf[NNARReadReturn]);
	indication(classOf[NNARWriteReturn]);
}