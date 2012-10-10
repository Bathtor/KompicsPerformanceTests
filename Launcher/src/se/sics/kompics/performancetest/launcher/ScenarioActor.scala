package se.sics.kompics.performancetest.launcher

import akka.actor.Actor
import se.sics.kompics.performancetest.scenario.Executor
import scala.concurrent.ops._
import java.net.InetAddress
import akka.actor.Props

class ScenarioActor extends Actor {
	import context._
	
	val memProfiler = actorOf(Props[MemoryProfiler], name="memProfiler");
	
	def receive = {
		case Launch(t, s, c, profiler) => {
			system.eventStream.subscribe(profiler, classOf[ProfilingMessage]);
			
			val addr = system.settings.config.getString("akka.remote.netty.hostname");
			val ip = InetAddress.getByName(addr);
			println("My Hostname: " + ip);
			
			val iClass = this.getClass().getClassLoader().loadClass(c);
			val iObject = iClass.newInstance();
			val exec: Executor = iObject match {
				case x: Executor => x;
				case _ => throw new RuntimeException("Given class " + c + " is not a subtype of Executor!");
			}
			println("Loaded Executor");
			spawn {
				println("Running Executor");
				exec.run(t, s, new Profiler(system), ip);
			}
			
		}
	}
}