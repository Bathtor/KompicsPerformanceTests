package se.sics.kompics.scala.performancetest.kompics.nnar

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
	val nnar = --(NNAR);
	val timer = --(classOf[Timer]);
	
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
	
	var selfId = -1;
	
	ctrl uponEvent {
		case Init(scenario: Scenario, topology: TopologyView) => {() =>
			selfId = topology.getSelfId();
			
			println("Node " + selfId + " initializing Main");
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
	
	nnar uponEvent {
		case NNARReadReturn(v) => {() =>
			timeOp("READ");
			startOp();
		}
		case NNARWriteReturn() => {() =>
			timeOp("WRITE");
			startOp();
		}
	}
	
	private def startOp() = {
		
		if (System.nanoTime() < stageEnd) {
			val read = rand.nextBoolean;
			if (read) {
				if (!warmUp) {
					reqStart = System.nanoTime();
				}
				trigger(NNARRead(), nnar);
				//println("Node " + selfId + " starting Op: READ");
			} else {
				val value = rand.nextString(16);
				if (!warmUp) {
					reqStart = System.nanoTime();
				}
				trigger(NNARWrite(value), nnar);
				//println("Node " + selfId + " starting Op: WRITE");
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