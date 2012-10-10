package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.scala.ComponentDefinition
import se.sics.kompics.performancetest.scenario.Profiler
import se.sics.kompics.performancetest.scenario.Scenario
import se.sics.kompics.performancetest.scenario.Topology
import java.net.InetAddress
import scala.collection.JavaConverters._
import se.sics.kompics.scala.Init
import se.sics.kompics.network.netty.NettyNetworkInit
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.network.netty.NettyNetwork
import se.sics.kompics.scala.performancetest.kompics.PerfectUnicast
import se.sics.kompics.scala.performancetest.kompics.SimpleBroadcast
import se.sics.kompics.scala.ScalaComponent._
import se.sics.kompics.network.Network
import se.sics.kompics.scala.performancetest.kompics.PingTimeoutFD
import se.sics.kompics.timer.Timer
import se.sics.kompics.scala.performancetest.kompics.PerfectPointToPointLink
import se.sics.kompics.scala.performancetest.kompics.BasicBroadcast
import se.sics.kompics.scala.performancetest.kompics.PerfectFailureDetector

class Application extends ComponentDefinition {
	
	println("Starting Application");
	
	val nodes = Application.topology.getLocalNodes(Application.ip).asScala.toSet;
	nodes foreach {
		node => {
			val view = Application.topology.getView(node);
			val self = view.getSelfAddress();
			
			println("Setting up node " + node + " with address " + self.toString());
			
			val time = init(Init(), classOf[JavaTimer]);
			val network = init(new NettyNetworkInit(self, 5), classOf[NettyNetwork]);
			val pp2p = init(Init(view), classOf[PerfectUnicast]);
			val beb = init(Init(view), classOf[SimpleBroadcast]);
			val rs = init(Init(Application.scenario.getAntiEntropyInterval()), classOf[GRMT]);
			val kvs = init(Init(Application.scenario.getWriteQuorum(), Application.scenario.getReadQuorum(), self), classOf[QuorumStore]);
			val main = init(Init(Application.scenario, view), classOf[Main]);
			
			network ++ classOf[Network] -- (pp2p, beb);
			time ++ classOf[Timer] -- (main, rs);
			pp2p ++ PerfectPointToPointLink -- (kvs, rs);
			beb ++ BasicBroadcast -- (kvs, rs);
			rs ++ ReplicatedStore -- kvs;
			kvs ++ KeyValueStore -- main;		
		}
	}
	
	
}

object Application {
	
	var profiler: Profiler = null;
	var scenario: Scenario = null;
	var topology: Topology = null;
	var ip: InetAddress = null;
	
	def log(ts: Long, opType: String, time: Long) = {
		if (profiler != null) {
			profiler.log(ts, opType, time);
		}
	}
}