package se.sics.kompics.scala.performancetest.kompics

import se.sics.kompics.scala.ComponentDefinition
import se.sics.kompics.network.Network
import se.sics.kompics.address.Address
import se.sics.kompics.performancetest.launcher.ScalaTopology
import se.sics.kompics.performancetest.launcher.ScalaTopology._
import se.sics.kompics.performancetest.scenario.TopologyView
import se.sics.kompics.scala.Init
import se.sics.kompics.network.Message
import se.sics.kompics.network.Transport

case class Pp2pMessage(src: Address, dest: Address, msg: Any) extends Message(src, dest, Transport.TCP)

class PerfectUnicast extends ComponentDefinition {
	val pp2p = ++(PerfectPointToPointLink);
	val network = --(classOf[Network]);
	
	private var topology : TopologyView = null;
	
	def self : Address = topology.getSelfAddress();
	def neighbours : Set[Address] = topology.neighbours;

	ctrl uponEvent {case Init(topology: TopologyView) => {() =>
		this.topology = topology;
	}}
	
	pp2p uponEvent {case Pp2pSend(dest, msg) => {() =>
		trigger(Pp2pMessage(self, dest, msg), network);
	}}
	
	network uponEvent {case Pp2pMessage(src, dest, msg) => {() => 
		trigger(Pp2pDeliver(src, msg), pp2p);
	}}
}