package se.sics.kompics.scala.performancetest.akka

import akka.actor.Actor
import akka.util.duration._

case class Timeout

class MemoryProfiler extends Actor {
	import context._
	
	context.system.scheduler.schedule(10 milliseconds, 1000 milliseconds, self, Timeout);
	val startTime = System.nanoTime();
	
	def receive = {
		case Timeout =>
			val endTime = System.nanoTime();
			val rt = Runtime.getRuntime();
			val heap = rt.totalMemory();
			val free = rt.freeMemory();
			val used = (heap - free)/1000;
			val startDiff = endTime - startTime;
			//println("Currently using " + used + "KB of memory");
			system.eventStream.publish(ProfilingMessage(startDiff, "HEAP", used));
	}
}