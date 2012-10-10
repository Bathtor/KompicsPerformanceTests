package se.sics.kompics.scala.performancetest.akka.kvstore

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import se.sics.kompics.scala.performancetest.akka.Utils._
import scala.Math._

class QuorumStore extends Actor {
	import context._
	import QuorumStore._

	private var beb: ActorRef = null;
	private var rs: ActorRef = null;

	private var selfId: BigInt = 0;
	private var ts = 0;
	private var wq = 0;
	private var rq = 0;

	private val store = Map.empty[String, Value];
	private val writeSet = Map.empty[String, Set[ActorRef]];
	private val readSet = Map.empty[String, Set[NodeData]];
	private val pendingRead = Map.empty[String, Set[ActorRef]];
	private val pendingWrite = Map.empty[String, Set[WriteData]];
	private val pendingGetReply = Map.empty[String, Boolean];
	private val pendingPutReply = Map.empty[String, Boolean];

	def receive = {
		case KVSInit(writeQuorum, readQuorum, router, rsActor) =>
			beb = router;
			rs = rsActor;
			wq = writeQuorum;
			rq = readQuorum;
			selfId = rank(self);
		case KVSPut(key, value) =>
			writeSet += (key -> Set.empty[ActorRef]);
			val sTS = ts; // don't give out var refs
			val data = Data(selfId, sTS, key, value);
			ts += 1;
			//println(self + " broadcasting PUT on key " + key + " with value " + value);
			beb ! Put(data);
			pendingPutReply += (key -> true);
		case KVSGet(key) =>
			readSet += (key -> Set.empty[NodeData]);
			//println(self + " broadcasting GET on key " + key);
			beb ! Get(key);
			pendingGetReply += (key -> true);
		case RSValue(hash, data @ Data(nodeId, valueTS, key, value)) =>
			//var debugStr = self + " got data: " + data + "\n";
			
			if ((store.get(key) == None) || ((valueTS, nodeId) largerThan (store(key).ts, selfId))) {
				//debugStr += "	updating local store \n";
				ts = max(valueTS, ts);
				store += (key -> Value(valueTS, hash));
			}
			if (pendingRead.get(key) != None) {
				val readers = pendingRead(key);
				for (reader <- readers) {
					//debugStr += "	replying to reader " + reader + "\n";
					reader ! GetReply(data);
				}
				pendingRead -= key;
			}
			if (pendingWrite.get(key) != None) {
				val writers = pendingWrite(key);
				for (writer <- writers) {
					writer match {
						case e @ WriteData(node, nodeId, valueTS) =>
							//debugStr += "	replying to writer " + writer + "\n";
							node ! PutReply(key);
							pendingWrite(key) -= e; // check if that modifies writers
					}
				}
			}
			//println(debugStr);
		//			val reader = pendingRead(key);
		//			val writer = pendingWrite(key)
		//			if (reader != None) {
		//				reader ! GetReply(data);
		//				pendingRead -= key;
		//			}
		//			if (writer != None) {
		//				writer ! PutReply(key);
		//				pendingWrite -= key;
		//			}
		case Put(data @ Data(nodeId, valueTS, key, value)) =>
			//println(self + " received PUT on key " + key + " with value " + value);
			rs ! RSStore(data);
			if (pendingWrite.get(key) == None)
				pendingWrite += (key -> Set.empty[WriteData]);
			pendingWrite(key) += WriteData(sender, nodeId, valueTS);

		case Get(key) =>
			//println(self + " received GET on key " + key);
			if (store.get(key) != None) {
				val item = store(key);
				rs ! RSRetrieve(item.hash);
				if (pendingRead.get(key) == None)
					pendingRead += (key -> Set.empty[ActorRef]);
				pendingRead(key) += sender;
			} else {
				sender ! GetReply(NoData(key));
			}
		case PutReply(key) =>
			//println(self + " received PUT_REPLY on key");
			if ((pendingPutReply.get(key) != None) && (pendingPutReply(key))) {
				writeSet(key) += sender;
				if (writeSet(key).size >= wq) {
					parent ! KVSPutReply(key);
					writeSet -= key;
					pendingPutReply -= key;
				}
			}
		case GetReply(data @ Data(nodeId, valueTS, key, value)) =>
			//println(self + " received GET_REPLY on key " + key + " with value " + value);
			if ((pendingGetReply.get(key) != None) && (pendingGetReply(key))) {
				readSet(key) += NodeData(sender, data);
				if (readSet(key).size >= rq) {
					val curValue = maxTSValue(readSet(key));
					parent ! KVSGetReply(key, curValue.value);
					readSet -= key;
					pendingGetReply -= key;
				}
			}
	}

	private def maxTSValue(rset: Set[NodeData]): Data = {
		var rData: Data = null;
		rset foreach (nd => {
			val d = nd.data;
			if ((rData == null) || (d.ts > rData.ts)) {
				rData = d;
			}
		});
		return rData;
	}

}

object QuorumStore {
	// private datatypes
	case class Value(ts: Int, hash: BigInt)
	case class Data(nodeId: BigInt, ts: Int, key: String, value: Any)
	case class NoData(override val key: String) extends Data(-1, -1, key, null)
	case class NodeData(node: ActorRef, data: Data)
	case class WriteData(node: ActorRef, nodeId: BigInt, ts: Int)
	// private messages
	case class Put(data: Data)
	case class PutReply(key: String)
	case class Get(key: String)
	case class GetReply(data: Data)
}