/**
 *
 */
package se.sics.kompics.scala.performancetest.akka.nnar

import akka.actor.Actor
import se.sics.kompics.scala.performancetest.akka.Utils._
import scala.collection.immutable.Set.EmptySet
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.actor.ActorContext


/*
 * Private Messages
 */
private case class BCWrite(ts: Int, wr: BigInt, value: Any);
private case class UCAck;

/**
 * The <code>RIWC</code> .
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 *
 */
class RIWC extends Actor {
	import context._;
	
	var beb: ActorRef = null;
	
	var selfId: BigInt = 0;
	var ts: Int = 0;
	var wr: BigInt = 0;
	var value: Any = null;
	var correct = Set.empty[ActorRef];
	var writeSet = Set.empty[ActorRef];
	var readVal: Any = null;
	var reading = false;
	
	def receive = {
		case RIWCInit(group, router) => 
			correct = group;
			correct.foreach(p => {context.watch(p)});
			beb = router;
			selfId = rank(self);
		case Terminated(_) =>
			correct = correct.filterNot(p => {p.isTerminated});
			checkWriteSet(context);
		case NNARRead =>
			reading = true;
			readVal = value;
			beb ! BCWrite(ts, wr, value);
		case NNARWrite(v) =>
			beb ! BCWrite(ts+1, selfId, v);
		case BCWrite(tsPrime, wrPrime, vPrime) =>			
			if ((tsPrime, wrPrime) largerThan (ts, wr)) {
				ts = tsPrime;
				wr = wrPrime;
				value = vPrime;
			}
			context.sender ! UCAck;
		case UCAck =>
			writeSet += context.sender;
			checkWriteSet(context);
	}
	
	def checkWriteSet(context: ActorContext) = {
		if (correct subsetOf writeSet) {
			writeSet = Set.empty[ActorRef];
			if (reading) {
				reading = false;
				parent ! NNARReadReturn(readVal);
			} else {
				parent ! NNARWriteReturn;
			}
		}
	}
}
