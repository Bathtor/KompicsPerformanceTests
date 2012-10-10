package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.scala.ComponentDefinition
import se.sics.kompics.timer.Timer
import se.sics.kompics.scala.Init
import se.sics.kompics.performancetest.scenario.Scenario
import se.sics.kompics.performancetest.scenario.TopologyView
import se.sics.kompics.timer.ScheduleTimeout
import se.sics.kompics.timer.Timeout
import scala.util.Random
import se.sics.kompics.Event


case class WarmUpTimeout(t: ScheduleTimeout) extends Timeout(t)
case class MeasureTimeout(t: ScheduleTimeout) extends Timeout(t)
case class FinishTimeout(t: ScheduleTimeout) extends Timeout(t)


class Main extends ComponentDefinition {
	val kvs = --(KeyValueStore);
	val timer = --(classOf[Timer]);
	
	val maxKeySetSize = 10;
	
	val preloadTime = 1000l;
	var preloadTimeout: ScheduleTimeout = null;
	var warmUpTime = 0l;
	var warmUpTimeout: ScheduleTimeout = null;
	var measureTime = 0l;
	var measureTimeout: ScheduleTimeout = null;
	var cmd = "";
	
	val startTime = System.nanoTime();
	var stageEnd: Long = 0;
	var warmUp = false;
	var reqStart: Long = 0;
	
	val rand = new Random();
	
	var keySet = Vector.fill(maxKeySetSize)(randFun);
	
	def randFun: String = {
		val r = rand.nextInt(99999);
		return r.toString();
	}
	
	var selfId = -1;
	
	ctrl uponEvent {
		case Init(scenario: Scenario, topology: TopologyView) => {() =>
			selfId = topology.getSelfId();
			
			//println("Node " + selfId + " initializing Main");
			warmUpTime = scenario.getWarmUpTime();
			measureTime = scenario.getMeasureTime();
			cmd = scenario.getCommand(topology.getSelfId());
			preloadTimeout = new ScheduleTimeout(preloadTime);
			preloadTimeout.setTimeoutEvent(WarmUpTimeout(preloadTimeout));
			warmUpTimeout = new ScheduleTimeout(warmUpTime);
			warmUpTimeout.setTimeoutEvent(MeasureTimeout(warmUpTimeout));
			measureTimeout = new ScheduleTimeout(measureTime);
			measureTimeout.setTimeoutEvent(FinishTimeout(measureTimeout));

			trigger(preloadTimeout, timer);
		}
	}
	
	timer uponEvent {
		case WarmUpTimeout(_) => {() =>
			println("Node " + selfId + " starting Warmup Phase");
			stageEnd = System.nanoTime() + warmUpTime*1000*1000;
			warmUp = true;
			trigger(warmUpTimeout, timer);
			startOp();
		}
		case MeasureTimeout(_) => {() =>
			println("Node " + selfId + " starting Measure Phase");
			stageEnd = System.nanoTime() + measureTime*1000*1000;
			warmUp = false;
			trigger(measureTimeout, timer);
			startOp();
		}
		case FinishTimeout(_) => {() =>
			println("Node " + selfId + " finished Measure Phase");
		}
	}
	
	kvs uponEvent {
		case KVSGetReply(k, v) => {() =>
			timeOp("READ");
			startOp();
		}
		case KVSPutReply(k) => {() =>
			timeOp("WRITE");
			startOp();
		}
	}
	
	private def startOp() = {
		if (System.nanoTime() < stageEnd) {
			val key = keySet(rand.nextInt(maxKeySetSize));
			
			val read = rand.nextBoolean;
			if (read) {
				if (!warmUp) {
					reqStart = System.nanoTime();
				}
				//println(self + " starting read on key " + key);
				trigger(KVSGet(key), kvs);
			} else {
				val value = rand.nextString(16); // 16 character values
				if (!warmUp) {
					reqStart = System.nanoTime();
				}
				//println(self + " starting write on key " + key + " with value " + value);
				trigger(KVSPut(key, value), kvs);
			}
		}
	}
	
	private def timeOp(opType: String) = {
		//println("Node " + selfId + " finished " + opType + "op!");
		if (!warmUp) {
			val endTime = System.nanoTime();
			val diff = endTime - reqStart;
			val startDiff = endTime - startTime;
			//println(startDiff + " " + opType + " completed in " + diff + "ns");
			Application.log(startDiff, opType, diff);
		}
	}
	
	
	
}