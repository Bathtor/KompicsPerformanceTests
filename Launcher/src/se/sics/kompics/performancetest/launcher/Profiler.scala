package se.sics.kompics.performancetest.launcher

import akka.actor.ActorSystem

class Profiler(system: ActorSystem) extends se.sics.kompics.performancetest.scenario.Profiler {
	
	val eventStream = system.eventStream;
	
	override def log(timestamp: java.lang.Long, opType: java.lang.String, time: java.lang.Long) = {
		eventStream.publish(ProfilingMessage(timestamp, opType, time));
	}
}