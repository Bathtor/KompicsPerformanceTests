package se.sics.kompics.scala.performancetest.kompics

import se.sics.kompics.scala.ComponentDefinition
import se.sics.kompics.timer.Timer
import se.sics.kompics.scala.Init
import se.sics.kompics.timer.Timeout
import se.sics.kompics.timer.ScheduleTimeout
import se.sics.kompics.address.Address
import scala.collection.mutable.Set
import se.sics.kompics.performancetest.launcher.ScalaTopology
import se.sics.kompics.performancetest.launcher.ScalaTopology._
import se.sics.kompics.performancetest.scenario.TopologyView

case class CheckTimeout(t: ScheduleTimeout) extends Timeout(t)
case class HeartbeatRequest
case class Heartbeat

class PingTimeoutFD extends ComponentDefinition {
	val pfd = ++(PerfectFailureDetector);
	val timer = --(classOf[Timer]);
	val pp2p = --(PerfectPointToPointLink);

	var t = 0;
	private var topology: TopologyView = null;

	def self: Address = topology.getSelfAddress();
	def neighbours = topology.neighbours;

	val alive = Set.empty[Address];
	val detected = Set.empty[Address];
	
	var check: ScheduleTimeout = null;

	ctrl uponEvent {
		case Init(timeout: Int, topo: TopologyView) => { () =>
			t = timeout;
			topology = topo;
			check = new ScheduleTimeout(t);
			check.setTimeoutEvent(CheckTimeout(check));
			trigger(check, timer);
			alive ++= neighbours;
		}
	}

	pp2p uponEvent {
		case Pp2pDeliver(src, HeartbeatRequest) => { () =>
			//println("PFD got HeartbeatRequest from "+src);
			trigger(Pp2pSend(src, Heartbeat), pp2p);
		}
		case Pp2pDeliver(src, Heartbeat) => { () =>
			//println("PFD got Heartbeat from "+src);
			alive += src;
		}
	}
	
	timer uponEvent {
		case CheckTimeout(_) => {() => 
			neighbours foreach {
				n => {
					if (!alive.contains(n) && !detected.contains(n)) {
						detected += n;
						trigger(Terminated(n), pfd);
					} else if (!detected.contains(n)) {
						trigger(Pp2pSend(n, HeartbeatRequest), pp2p);
					}
				}
			}
			alive.clear;
			trigger(check, timer);
		}
	}

}