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


case class SBMessage(src: Address, dest: Address, msg: Any) extends Message(src, dest, Transport.TCP)

class SimpleBroadcast extends ComponentDefinition {
	val beb = ++(BasicBroadcast);
	val network = --(classOf[Network]);

	private var topology: TopologyView = null;

	def self: Address = topology.getSelfAddress();
	def neighbours: Set[Address] = topology.neighbours;

	ctrl uponEvent {
		case Init(topology: TopologyView) => { () =>
			this.topology = topology;
		}
	}

	beb uponEvent {
		case SBSend(msg) => { () =>
			neighbours foreach {
				q => trigger(SBMessage(self, q, msg), network);
			}
		}
	}

	network uponEvent {
		case SBMessage(src, dest, msg) => { () =>
			trigger(SBDeliver(src, msg), beb);
		}
	}
}