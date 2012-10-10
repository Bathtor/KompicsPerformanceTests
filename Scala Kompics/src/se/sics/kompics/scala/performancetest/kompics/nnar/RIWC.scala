package se.sics.kompics.scala.performancetest.kompics.nnar

import se.sics.kompics.scala.ComponentDefinition
import se.sics.kompics.scala.Init
import se.sics.kompics.scala.performancetest.kompics.PerfectFailureDetector
import se.sics.kompics.scala.performancetest.kompics.BasicBroadcast
import se.sics.kompics.scala.performancetest.kompics.SBSend
import se.sics.kompics.scala.performancetest.kompics.SBDeliver
import se.sics.kompics.scala.performancetest.kompics.PerfectPointToPointLink
import se.sics.kompics.performancetest.launcher.ScalaTopology
import se.sics.kompics.performancetest.launcher.ScalaTopology._
import se.sics.kompics.performancetest.scenario.TopologyView
import se.sics.kompics.address.Address
import se.sics.kompics.scala.performancetest.kompics.Terminated
import se.sics.kompics.scala.performancetest.kompics.Utils._
import se.sics.kompics.scala.performancetest.kompics.Pp2pSend
import se.sics.kompics.scala.performancetest.kompics.Pp2pDeliver

/*
 * Private Messages
 */
private case class BCWrite(ts: Int, wr: Int, value: Any);
private case class UCAck;

class RIWC extends ComponentDefinition {

	val nnar = ++(NNAR);
	val pfd = --(PerfectFailureDetector);
	val beb = --(BasicBroadcast);
	val pp2p = --(PerfectPointToPointLink);
	
	private var topology: TopologyView = null;

	def self: Address = topology.getSelfAddress();
	def neighbours = topology.neighbours;
	
	var selfId: Int = 0;
	var ts: Int = 0;
	var wr: Int = 0;
	var value: Any = null;
	var correct = Set.empty[Address];
	var writeSet = Set.empty[Address];
	var readVal: Any = null;
	var reading = false;
	
	ctrl uponEvent {case Init(topo: TopologyView) => {() => 
		topology = topo;
		correct ++= neighbours;
		selfId = rank(self);
	}}
	
	pfd uponEvent {case Terminated(node) => {() =>
		correct -= node;
		checkWriteSet();
	}}
	
	nnar uponEvent {
		case NNARRead() => {() => 
			reading = true;
			readVal = value;
			trigger(SBSend(BCWrite(ts, wr, value)), beb);
		}
		case NNARWrite(v) => {() =>
			trigger(SBSend(BCWrite(ts+1, selfId, v)), beb);
		}
	}
	
	beb uponEvent {
		case SBDeliver(src, BCWrite(tsPrime, wrPrime, vPrime)) => {() => 
			if ((tsPrime, wrPrime) largerThan (ts, wr)) {
				ts = tsPrime;
				wr = wrPrime;
				value = vPrime;
			}
			trigger(Pp2pSend(src, UCAck), pp2p);
		}
	}
	
	pp2p uponEvent {
		case Pp2pDeliver(src, UCAck) => {() =>
			writeSet += src;
			checkWriteSet();
		}
	}
	
	private def checkWriteSet() = {
		if (correct subsetOf writeSet) {
			writeSet = Set.empty[Address];
			if (reading) {
				reading = false;
				trigger(NNARReadReturn(readVal), nnar);
			} else {
				trigger(NNARWriteReturn(), nnar);
			}
		}
	}
	
}