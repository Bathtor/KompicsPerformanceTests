package se.sics.kompics.scala.performancetest.akka.kvstore

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.BroadcastRouter
import akka.routing.CurrentRoutees
import akka.routing.RouterRoutees
import scala.util.Random
import se.sics.kompics.scala.performancetest.akka.ProfilingMessage
import scala.collection.immutable.SortedSet
import se.sics.kompics.scala.performancetest.akka.MemoryProfiler

class Main() extends Actor {
	
	val maxKeySetSize = 10;
	
	val rand = new Random();
	
	val kvs = context.actorOf(Props[QuorumStore]);
	val rs = context.actorOf(Props[GRMT]);
	
	//println("MainMePath: " + context.self.path);
	//val scenario = context.actorFor("akka://testSystem/user/scenario");
	var scenario:ActorRef = null;
	
	val startTime = System.nanoTime();
	var stageEnd: Long = 0;
	var warmup: Boolean = false;
	var reqStart: Long = 0;
	
	var keySet = Vector.fill(maxKeySetSize)(randFun);
	
	def randFun: String = {
		val r = rand.nextInt(99999);
		return r.toString();
	}
	
	
	
	def receive = {
		case Setup(scenarioActor, profiler) =>
			scenario = scenarioActor;
			scenario ! Register(kvs, rs);
			context.system.eventStream.subscribe(profiler, classOf[ProfilingMessage]);
		case MainInit(kvsRouter, rsRouter, rsTimeDelay, writeQuorum, readQuorum) =>
			kvs ! KVSInit(writeQuorum, readQuorum, kvsRouter, rs);
			rs ! RSInit(rsTimeDelay, rsRouter, kvs);
		case WarmUp(time) =>
			stageEnd = System.nanoTime() + (time.toLong)*1000*1000;
			warmup = true;
			startOp();
		case Measure(time) =>
			stageEnd = System.nanoTime() + (time.toLong)*1000*1000;
			warmup = false;
			startOp();
		case KVSGetReply(k, v) =>
			//println(self + " finished read on key " + k + " with value " + v);
			timeOp("READ");
			startOp();
		case KVSPutReply(k) =>
			//println(self + " finished write on key " + k);
			timeOp("WRITE");
			startOp();
		case any => println("Received unkown message: "+any.toString);
	}
	
	private def startOp() = {
		if (System.nanoTime() < stageEnd) {
			val key = keySet(rand.nextInt(maxKeySetSize));
			
			val read = rand.nextBoolean;
			if (read) {
				if (!warmup) {
					reqStart = System.nanoTime();
				}
				//println(self + " starting read on key " + key);
				kvs ! KVSGet(key);
			} else {
				val value = rand.nextString(16); // 16 character values
				if (!warmup) {
					reqStart = System.nanoTime();
				}
				//println(self + " starting write on key " + key + " with value " + value);
				kvs ! KVSPut(key, value);
			}
		}
	}
	
	private def timeOp(opType: String) = {
		//println("Finished " + opType + " op!");
		if (!warmup) {
			val endTime = System.nanoTime();
			val diff = endTime - reqStart;
			val startDiff = endTime - startTime;
			//println(startDiff + " " + opType + " completed in " + diff + "ns");
			context.system.eventStream.publish(ProfilingMessage(startDiff, opType, diff));
		}
	}
}