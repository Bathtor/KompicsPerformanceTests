/**
 *
 */
package se.sics.kompics.scala.performancetest.akka.kvstore

import akka.actor.ActorSystem
import akka.actor.Props
import se.sics.kompics.scala.performancetest.akka.ProfilingMessage
import se.sics.kompics.scala.performancetest.akka.ProfilerSubscriber

/**
 * The <code>Bootstrap</code> .
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 *
 */
object Bootstrap extends App {
	val n = if ((args != null) && (args.size > 0)) {
		args(0).toInt
	} else {
		2;
	}
	
	
	val system = ActorSystem("testSystem");
	val profiler = system.actorOf(Props(new ProfilerSubscriber("KVS " + n + "p")), name = "profiler");
	//system.eventStream.subscribe(profiler, classOf[ProfilingMessage]);
	
	
	println("Starting " + n + " processes");
	val scenario = system.actorOf(Props(new ScenarioActor(n, 0.8f, 0.3f, profiler)).withDispatcher("stash-dispatcher"), name = "scenario");
	scenario ! WarmUp(30000);
	scenario ! Measure(60000);
}