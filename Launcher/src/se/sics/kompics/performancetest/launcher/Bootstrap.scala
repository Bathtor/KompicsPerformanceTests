package se.sics.kompics.performancetest.launcher

import akka.actor.ActorSystem
import akka.actor.Props

object Bootstrap extends App {
	val c = if ((args != null) && (args.size > 0)) {
		args(0)	
	} else {
		throw new RuntimeException("Need to know which class to invoke!");
	}
	
	val n = if ((args != null) && (args.size > 1)) {
		args(1).toInt
	} else {
		2;
	}
	
	
	
	val system = ActorSystem("testSystem");
	val profiler = system.actorOf(Props(new ProfilerSubscriber("Class "+ c + " with " + n + "p")), name = "profiler");
	//system.eventStream.subscribe(profiler, classOf[ProfilingMessage]);
	
	val scenarioCoordinator = system.actorOf(Props(new ScenarioCoordinator(c, n, profiler)).withDispatcher("stash-dispatcher"), name = "scenario");
}