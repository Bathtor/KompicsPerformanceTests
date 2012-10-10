package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.scala.ComponentDefinition
import se.sics.kompics.scala.performancetest.kompics.BasicBroadcast
import se.sics.kompics.scala.performancetest.kompics.SBSend
import se.sics.kompics.scala.performancetest.kompics.SBDeliver
import se.sics.kompics.scala.performancetest.kompics.PerfectPointToPointLink
import se.sics.kompics.timer.Timer
import se.sics.kompics.scala.Init
import se.sics.kompics.timer.ScheduleTimeout
import se.sics.kompics.timer.Timeout
import com.larskroll.merkletree.MerkleTree
import se.sics.kompics.scala.performancetest.kompics.Pp2pSend
import se.sics.kompics.scala.performancetest.kompics.Pp2pDeliver

case class AETimeout(t: ScheduleTimeout) extends Timeout(t)

case class Root(hash: BigInt)
case class Tree(tree: MerkleTree)
case class Value(key: BigInt, value: Any)
case class RValue(key: BigInt)

class GRMT extends ComponentDefinition {
	val rs = ++(ReplicatedStore);
	val beb = --(BasicBroadcast);
	val pp2p = --(PerfectPointToPointLink);
	val timer = --(classOf[Timer]);
	
	var period = 0;
	var aet: ScheduleTimeout = null;
	val store = scala.collection.mutable.Map.empty[BigInt, Any];
	val tree = new MerkleTree;
	
	ctrl uponEvent {
		case Init(timeDelay: Int) => {() => 
			period = timeDelay;
			aet = new ScheduleTimeout(period);
			aet.setTimeoutEvent(AETimeout(aet));
			trigger(aet, timer);
		}
	}
	
	rs uponEvent {
		case RSStore(value) => {() => 
			val hash = calcHash(value);
			tree += hash;
			store += (hash -> value);
			trigger(RSValue(hash, value), rs);
		}
		case RSRetrieve(key) => {() =>
			trigger(RSValue(key, store(key)), rs);
		}
	}
	
	timer uponEvent {
		case AETimeout(_) => {() => 
			trigger(SBSend(Root(tree.root)), beb);
			// restart timer
			trigger(aet, timer);
		}
	}
	
	beb uponEvent {
		case SBDeliver(src, Root(hash)) => {() =>
			if (hash != tree.root) {
				trigger(Pp2pSend(src, Tree(tree)), pp2p);
			}
		}
	}
	
	pp2p uponEvent {
		case Pp2pDeliver(src, Tree(otherTree)) => {() =>
			val (requestValues, sendValues) = tree.compareWith(otherTree);
			requestValues foreach (v => {
				trigger(Pp2pSend(src, RValue(v)), pp2p);
			})
			sendValues foreach (v => {
				trigger(Pp2pSend(src, Value(v, store(v))), pp2p);
			})
		}
		case Pp2pDeliver(src, Value(key, value)) => {() =>
			tree += key;
			store += (key -> value);
			trigger(RSValue(key, value), rs);
		}
		case Pp2pDeliver(src, RValue(key)) => {() =>
			trigger(Pp2pSend(src, Value(key, store(key))), pp2p);
		}
	}
	
	private def calcHash(value: Any): BigInt = {
		return tree.shaHash(value);
	}
}