package se.sics.kompics.scala.performancetest.akka.nnar

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.BroadcastRouter
import scala.collection.immutable.Vector
import collection.breakOut
import akka.actor.Stash
import akka.util.duration._
import akka.actor.PoisonPill
import akka.actor.Address
import scala.collection.JavaConverters._
import akka.actor.AddressFromURIString
import akka.routing.RemoteRouterConfig
import se.sics.kompics.scala.performancetest.akka.MemoryProfiler

case class Timeout;
case class Setup(s: ActorRef, p: ActorRef);

class ScenarioActor(n: Int, profiler: ActorRef) extends Actor with Stash {
	
	println("Starting Init-Phase (" + n + " processes)");
	var group = Set.empty[ActorRef];
	
	val addresses = getAddresses();
	val memProfilerRouter = context.actorOf(Props[MemoryProfiler].withRouter(RemoteRouterConfig(BroadcastRouter(nrOfInstances = addresses.size), addresses)), name="memProfiler");
	
	
	var mainRouter = context.actorOf(Props[Main].withRouter(RemoteRouterConfig(BroadcastRouter(nrOfInstances = n), addresses)));
	mainRouter ! Setup(self, profiler);
	var bcRouter: ActorRef = null;
	var initializing = true;
	
	def receive = {
		case Register(ref) =>
			//println("Got registration!");
			group += ref;
			if (group.size == n) {
				val groupVec: Vector[ActorRef] = group.map(f => f)(breakOut);
				bcRouter = context.actorOf(Props().withRouter(BroadcastRouter(routees = groupVec)));
				bcRouter ! RIWCInit(group, bcRouter);
				unstashAll();
				println("Finished Init-Phase");
				context.become(warmup);
			}
		case _ =>
			stash();
		
	}
	
	def warmup: Receive = {
		case x @ WarmUp(time) =>
			mainRouter ! x;
			context.system.scheduler.scheduleOnce(time milliseconds, self, Timeout);
			println("Starting Warmup-Phase");
		case Timeout =>
			unstashAll();
			println("Finished Warmup-Phase");
			context.become(measure);
		case _ => stash();
	}
	
	def measure: Receive = {
		case x @ Measure(time) =>
			mainRouter ! x;
			context.system.scheduler.scheduleOnce(time milliseconds, self, Timeout);
			println("Starting Measure-Phase");
		case Timeout =>
			unstashAll();
			mainRouter ! PoisonPill;
			self ! PoisonPill;
			println("Finished Measure-Phase");
			context.become(finalise);
		case _ => stash();
	}
	
	def finalise: Receive = {
		case x => println("Unhandled stashed message: " + x.toString);
	}
	
	private def getAddresses(): Iterable[Address] = {
		val strings = context.system.settings.config.getStringList("tests.remote.nodes").asScala.toList;
		var adrs = Set.empty[Address];
		strings foreach {
			s => {
				val adr = AddressFromURIString(s);
				adrs += adr;
				println("Created address with host: " + adr.host.get);
			}
		}
//		adrs += Address("akka", "testSystem", "192.168.0.105", 2555);
//		adrs += Address("akka", "testSystem", "192.168.0.106", 2556);
		return adrs;
	}
}