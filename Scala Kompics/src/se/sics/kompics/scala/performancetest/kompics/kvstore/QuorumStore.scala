package se.sics.kompics.scala.performancetest.kompics.kvstore

import se.sics.kompics.scala.ComponentDefinition
import se.sics.kompics.scala.performancetest.kompics.BasicBroadcast
import se.sics.kompics.scala.performancetest.kompics.SBSend
import se.sics.kompics.scala.performancetest.kompics.SBDeliver
import se.sics.kompics.scala.performancetest.kompics.PerfectPointToPointLink
import se.sics.kompics.address.Address
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import se.sics.kompics.scala.Init
import se.sics.kompics.scala.performancetest.kompics.Utils._
import scala.Math._
import se.sics.kompics.scala.performancetest.kompics.Pp2pSend
import se.sics.kompics.scala.performancetest.kompics.Pp2pDeliver

class QuorumStore extends ComponentDefinition {
	import QuorumStore._
	
	val kvs = ++(KeyValueStore);
	val rs = --(ReplicatedStore);
	val beb = --(BasicBroadcast);
	val pp2p = --(PerfectPointToPointLink);
	
	private var selfId = 0;
	private var ts = 0;
	private var wq = 0;
	private var rq = 0;

	private val store = Map.empty[String, Value];
	private val writeSet = Map.empty[String, Set[Address]];
	private val readSet = Map.empty[String, Set[NodeData]];
	private val pendingRead = Map.empty[String, Set[Address]];
	private val pendingWrite = Map.empty[String, Set[WriteData]];
	private val pendingGetReply = Map.empty[String, Boolean];
	private val pendingPutReply = Map.empty[String, Boolean];
	
	ctrl uponEvent {
		case Init(writeQuorum: Int, readQuorum: Int, self: Address) => {() =>
			wq = writeQuorum;
			rq = readQuorum;
			selfId = rank(self);
		}
	}
	
	kvs uponEvent {
		case KVSPut(key, value) => {() =>
			writeSet += (key -> Set.empty[Address]);
			val sTS = ts; // don't give out var refs
			val data = Data(selfId, sTS, key, value);
			ts += 1;
			//println(self + " broadcasting PUT on key " + key + " with value " + value);
			trigger(SBSend(Put(data)), beb);
			pendingPutReply += (key -> true);
		}
		case KVSGet(key) => {() =>
			readSet += (key -> Set.empty[NodeData]);
			//println(self + " broadcasting GET on key " + key);
			trigger(SBSend(Get(key)), beb);
			pendingGetReply += (key -> true);
		}
	}
	
	rs uponEvent {
		case RSValue(hash, data @ Data(nodeId, valueTS, key, value)) => {() =>
			if ((store.get(key) == None) || ((valueTS, nodeId) largerThan (store(key).ts, selfId))) {
				//debugStr += "	updating local store \n";
				ts = max(valueTS, ts);
				store += (key -> Value(valueTS, hash));
			}
			if (pendingRead.get(key) != None) {
				val readers = pendingRead(key);
				for (reader <- readers) {
					//debugStr += "	replying to reader " + reader + "\n";
					trigger(Pp2pSend(reader, GetReply(data)), pp2p);
				}
				pendingRead -= key;
			}
			if (pendingWrite.get(key) != None) {
				val writers = pendingWrite(key);
				for (writer <- writers) {
					writer match {
						case e @ WriteData(node, nodeId, valueTS) =>
							//debugStr += "	replying to writer " + writer + "\n";
							trigger(Pp2pSend(node, PutReply(key)), pp2p);
							pendingWrite(key) -= e; // check if that modifies writers
					}
				}
			}
		}
	}
	
	beb uponEvent {
		case SBDeliver(src, Put(data @ Data(nodeId, valueTS, key, value))) => {() =>
			trigger(RSStore(data), rs);
			if (pendingWrite.get(key) == None)
				pendingWrite += (key -> Set.empty[WriteData]);
			pendingWrite(key) += WriteData(src, nodeId, valueTS);
		}
		case SBDeliver(src, Get(key)) => {() => 
			if (store.get(key) != None) {
				val item = store(key);
				trigger(RSRetrieve(item.hash), rs);
				if (pendingRead.get(key) == None)
					pendingRead += (key -> Set.empty[Address]);
				pendingRead(key) += src;
			} else {
				trigger(Pp2pSend(src, GetReply(NoData(key))), pp2p);
			}
		}
	}
	
	pp2p uponEvent {
		case Pp2pDeliver(src, PutReply(key)) => {() => 
			if ((pendingPutReply.get(key) != None) && (pendingPutReply(key))) {
				writeSet(key) += src;
				if (writeSet(key).size >= wq) {
					trigger(KVSPutReply(key), kvs);
					writeSet -= key;
					pendingPutReply -= key;
				}
			}
		}
		case Pp2pDeliver(src, GetReply(data @ Data(nodeId, valueTS, key, value))) => {() =>
			if ((pendingGetReply.get(key) != None) && (pendingGetReply(key))) {
				readSet(key) += NodeData(src, data);
				if (readSet(key).size >= rq) {
					val curValue = maxTSValue(readSet(key));
					trigger(KVSGetReply(key, curValue.value), kvs);
					readSet -= key;
					pendingGetReply -= key;
				}
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
	case class Data(nodeId: Int, ts: Int, key: String, value: Any)
	case class NoData(override val key: String) extends Data(-1, -1, key, null)
	case class NodeData(node: Address, data: Data)
	case class WriteData(node: Address, nodeId: Int, ts: Int)
	// private messages
	case class Put(data: Data)
	case class PutReply(key: String)
	case class Get(key: String)
	case class GetReply(data: Data)
}