package se.sics.kompics.scala.performancetest.akka.kvstore

import akka.actor.Actor
import akka.util.duration._
import com.larskroll.merkletree.MerkleTree
import akka.actor.ActorRef

case class Root(hash: BigInt)
case class Tree(tree: MerkleTree)
case class Value(key: BigInt, value: Any)
case class RValue(key: BigInt)

class GRMT extends Actor {
	import context._
	var beb: ActorRef = null;
	var kvs: ActorRef = null;
	
	var period: Int = 0;
	val store = scala.collection.mutable.Map.empty[BigInt, Any];
	val tree = new MerkleTree;
	
	def receive = {
		case RSInit(timeDelay, router, kvsActor) =>
			beb = router;
			kvs = kvsActor;
			period = timeDelay;
			context.system.scheduler.schedule(period milliseconds, period milliseconds, self, Timeout);
		case RSStore(value) =>
			val hash = calcHash(value);
			tree += hash;
			store += (hash -> value);
			kvs ! RSValue(hash, value);
		case RSRetrieve(key) =>
			kvs ! RSValue(key, store(key));
		case Timeout =>
			beb ! Root(tree.root);
		case Root(hash) =>
			if (hash != tree.root) {
				sender ! Tree(tree);
			}
		case Tree(otherTree) =>
			val (requestValues, sendValues) = tree.compareWith(otherTree);
			requestValues foreach (v => {
				sender ! RValue(v);
			})
			sendValues foreach (v => {
				sender ! Value(v, store(v));
			})
		case Value(key, value) =>
			tree += key;
			store += (key -> value);
			kvs ! RSValue(key, value);
		case RValue(key) =>
			sender ! Value(key, store(key));
	}
	
	private def calcHash(value: Any): BigInt = {
		return tree.shaHash(value);
	}
	
}