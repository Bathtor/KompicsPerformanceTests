/**
 *
 */
package se.sics.kompics.scala.performancetest.akka.kvstore

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.BroadcastRouter
import scala.collection.immutable.Vector
import collection.breakOut
import akka.actor.Stash
import akka.util.duration._
import akka.actor.PoisonPill
import scala.math._
import akka.routing.RemoteRouterConfig
import akka.actor.Address
import scala.collection.JavaConverters._
import akka.actor.AddressFromURIString
import se.sics.kompics.scala.performancetest.akka.MemoryProfiler

/**
 * The <code>ScenarioActor</code> .
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 *
 */

case class Setup(s: ActorRef, p: ActorRef);

class ScenarioActor(n: Int, wq: Float, rq: Float, profiler: ActorRef) extends Actor with Stash {
	
	val rsTimeDelay = 1000;
	val writeQuorum: Int = round(wq*n + 0.5f);
	val readQuorum: Int = round(rq*n + 0.5f);
	
	val addresses = getAddresses();
	val memProfilerRouter = context.actorOf(Props[MemoryProfiler].withRouter(RemoteRouterConfig(BroadcastRouter(nrOfInstances = addresses.size), addresses)), name="memProfiler");
	
	
	println("Starting Init-Phase (" + n + " processes)");
	var kvsGroup = Set.empty[ActorRef];
	var rsGroup = Set.empty[ActorRef];
	var mainRouter = context.actorOf(Props[Main].withRouter(RemoteRouterConfig(BroadcastRouter(nrOfInstances = n), addresses)));
	mainRouter ! Setup(self, profiler);
	var kvsRouter: ActorRef = null;
	var rsRouter: ActorRef = null;
	var initializing = true;
	
	def receive = {
		case Register(kvs, rs) =>
			//println("Got registration!");
			kvsGroup += kvs;
			rsGroup += rs;
			if (kvsGroup.size == n) {
				val kvsGroupVec: Vector[ActorRef] = kvsGroup.map(f => f)(breakOut);
				val rsGroupVec: Vector[ActorRef] = rsGroup.map(f => f)(breakOut);
				kvsRouter = context.actorOf(Props().withRouter(BroadcastRouter(routees = kvsGroupVec)));
				rsRouter = context.actorOf(Props().withRouter(BroadcastRouter(routees = rsGroupVec)));
				mainRouter ! MainInit(kvsRouter, rsRouter, rsTimeDelay, writeQuorum, readQuorum);
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