package se.sics.kompics.scala.performancetest.akka.nnar

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.BroadcastRouter
import akka.routing.CurrentRoutees
import akka.routing.RouterRoutees
import scala.util.Random
import se.sics.kompics.scala.performancetest.akka.ProfilingMessage
import se.sics.kompics.scala.performancetest.akka.MemoryProfiler

class Main() extends Actor {
	
	val nnar = context.actorOf(Props[RIWC]);
	//println("MainMePath: " + context.self.path);
	//val scenario = context.actorFor("akka://testSystem/user/scenario");
	var scenario: ActorRef = null;

	val startTime = System.nanoTime();
	var stageEnd: Long = 0;
	var warmup: Boolean = false;
	var reqStart: Long = 0;
	
	val rand = new Random();
	
	def receive = {
		case Setup(scenarioActor, profiler) =>
			scenario = scenarioActor;
			scenario ! Register(nnar);
			context.system.eventStream.subscribe(profiler, classOf[ProfilingMessage]);
		case WarmUp(time) =>
			stageEnd = System.nanoTime() + (time.toLong)*1000*1000;
			warmup = true;
			startOp();
		case Measure(time) =>
			stageEnd = System.nanoTime() + (time.toLong)*1000*1000;
			warmup = false;
			startOp();
		case NNARReadReturn(v) =>
			timeOp("READ");
			startOp();
		case NNARWriteReturn =>
			timeOp("WRITE");
			startOp();
		case any => println("Received unkown message: "+any.toString);
	}
	
	private def startOp() = {
		if (System.nanoTime() < stageEnd) {
			val read = rand.nextBoolean;
			if (read) {
				if (!warmup) {
					reqStart = System.nanoTime();
				}
				nnar ! NNARRead;
			} else {
				val value = rand.nextString(16);
				if (!warmup) {
					reqStart = System.nanoTime();
				}
				nnar ! NNARWrite(value);
			}
		}
	}
	
	private def timeOp(opType: String) = {
		//println("Finished " + opType + "op!");
		if (!warmup) {
			val endTime = System.nanoTime();
			val diff = endTime - reqStart;
			val startDiff = endTime - startTime;
			//println(startDiff + " " + opType + " completed in " + diff + "ns");
			context.system.eventStream.publish(ProfilingMessage(startDiff, opType, diff));
		}
	}
}