package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.performancetest.scenario.{Topology, Scenario, Profiler}
import se.sics.kompics.Kompics
import java.net.InetAddress

class Executor extends se.sics.kompics.performancetest.scenario.Executor {
	
	override def run(t: Topology, s: Scenario, p: Profiler, ip: InetAddress) = {
		Application.profiler = p;
		Application.topology = t;
		Application.scenario = s;
		Application.ip = ip;
		Kompics.createAndStart(classOf[Application], 12);
	}
	
}