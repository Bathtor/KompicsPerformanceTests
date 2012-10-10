package se.kth.ii2202.experiments.kvstore;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.kth.ii2202.experiments.beb.BestEffortBroadcast;
import se.kth.ii2202.experiments.beb.BroadcastRequest;
import se.kth.ii2202.experiments.common.PerfectPointToPointLink;
import se.kth.ii2202.experiments.common.Pp2pSend;
import se.kth.ii2202.experiments.common.Utils;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

/**
 * 
 * @author carbone
 */
public class QuorumStore extends ComponentDefinition {

	private Negative<KeyValueStore> kvs = provides(KeyValueStore.class);
	private Positive<ReplicatedStore> rs = requires(ReplicatedStore.class);
	private Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
	private Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
	private int selfId;
	private Address selfAddr;
	private int ts;
	private int wq;
	private int rq;
	private Map<String, Value> store = new HashMap<String, Value>();
	private Map<String, Set<Address>> writeSet = new HashMap<String, Set<Address>>();
	private Map<String, Set<NodeData>> readSet = new HashMap<String, Set<NodeData>>();
	private Map<String, Set<Address>> pendingRead = new HashMap<String, Set<Address>>();
	private Map<String, Set<WriteData>> pendingWrite = new HashMap<String, Set<WriteData>>();
	private Map<String, Boolean> pendingGetReply = new HashMap<String, Boolean>();
	private Map<String, Boolean> pendingPutReply = new HashMap<String, Boolean>();

	public QuorumStore() {
		subscribe(onInit, control);
		subscribe(onKVSPut, kvs);
		subscribe(onKVSGet, kvs);
		subscribe(onRSvalue, rs);
		subscribe(onPutRequest, beb);
		subscribe(onGetRequest, beb);
		subscribe(onPutReply, pp2p);
		subscribe(onGetReply, pp2p);
	}

	Handler<KVSInit> onInit = new Handler<KVSInit>() {
		@Override
		public void handle(KVSInit e) {
			QuorumStore.this.selfAddr = e.getTopology().getSelfAddress();
			QuorumStore.this.selfId = QuorumStore.this.selfAddr.getId();
			QuorumStore.this.wq = e.getWriteQuorum();
			QuorumStore.this.rq = e.getReadQuorum();
		}
	};
	Handler<KVSPut> onKVSPut = new Handler<KVSPut>() {
		@Override
		public void handle(KVSPut event) {
			//System.out.println("Got KVSPut for key " + event.getKey() + " with value " + event.getVal());
			
			writeSet.put(event.getKey(), new HashSet<Address>());
			KVSData data = new KVSData(selfId, ts, event.getKey(), event.getVal());
			ts++;
			trigger(new BroadcastRequest(new BebPutRequest(data, selfAddr)), beb);
			pendingPutReply.put(event.getKey(), true);
			
		}
	};
	Handler<KVSGet> onKVSGet = new Handler<KVSGet>() {
		@Override
		public void handle(KVSGet event) {
			//System.out.println("Got KVSGet for key " + event.getKey());
			
			readSet.put(event.getKey(), new HashSet<NodeData>());
			trigger(new BroadcastRequest(new BebGetRequest(event.getKey(), selfAddr)), beb);
			pendingGetReply.put(event.getKey(), true);
		}
	};
	Handler<RSvalue> onRSvalue = new Handler<RSvalue>() {
		@Override
		public void handle(RSvalue event) {
			//System.out.println("Got RSValue for key " + event.getKey() + " with value " + event.getValue());
			
			KVSData rsData = (KVSData) event.getValue();
			if (!store.containsKey(rsData.getKey())
					|| Utils.isLargerThan(rsData.getValueTS(), rsData.getNodeId(),
							store.get(rsData.getKey()).getTs(), selfId)) {
				ts = Utils.max(rsData.getValueTS(), ts);
				store.put(rsData.getKey(), new Value(rsData.getValueTS(), event.getKey()));
			}

			if (pendingRead.containsKey(rsData.getKey())) {
				for (Address addr : pendingRead.get(rsData.getKey())) {
					trigger(new Pp2pSend(addr, new Pp2pGetReply(rsData, selfAddr)), pp2p);
				}
				pendingRead.remove(rsData.getKey());
			}

			if (pendingWrite.containsKey(rsData.getKey())) {
				Set<WriteData> writersOrig = pendingWrite.get(rsData.getKey());
				// this way we avoid concurrent modification problems
				Set<WriteData> writersCopy = new HashSet<WriteData>();
				writersCopy.addAll(writersOrig);
				
				for (WriteData writer : writersCopy) {
					if ((writer.nodeId == rsData.getNodeId()) && (writer.ts == rsData.getValueTS())) {
						trigger(new Pp2pSend(writer.getAddr(), new Pp2pPutReply(rsData.getKey(), selfAddr)), pp2p);
						writersOrig.remove(writer);
					}
				}
			}
		}
	};
	Handler<BebPutRequest> onPutRequest = new Handler<BebPutRequest>() {
		@Override
		public void handle(BebPutRequest event) {
			//System.out.println("Got PutRequest for key " + event.getData().getKey() + " with value " + event.getData().getValue());
			
			trigger(new RSstore(event.getData()), rs);
			if (!pendingWrite.containsKey(event.getData().getKey())) {
				pendingWrite.put(event.getData().getKey(), new HashSet<WriteData>());
			}
			pendingWrite.get(event.getData().getKey()).add(
					new WriteData(event.getSrc(), event.getData().getNodeId(), event.getData()
							.getValueTS()));
		}
	};
	Handler<BebGetRequest> onGetRequest = new Handler<BebGetRequest>() {
		@Override
		public void handle(BebGetRequest event) {
			//System.out.println("Got GetRequest for key " + event.getKey());
			
			if (store.containsKey(event.getKey())) {
				trigger(new RSretrieve(store.get(event.getKey()).getHash()), rs);
				if (!pendingRead.containsKey(event.getKey())) {
					pendingRead.put(event.getKey(), new HashSet<Address>());
				}
				pendingRead.get(event.getKey()).add(event.getSrc());
			} else {
				trigger(new Pp2pSend(event.getSrc(), new Pp2pGetReply(
						KVSData.noData(event.getKey()), selfAddr)), pp2p);
			}
		}
	};
	Handler<Pp2pPutReply> onPutReply = new Handler<Pp2pPutReply>() {
		@Override
		public void handle(Pp2pPutReply event) {
			//System.out.println("Got PutReply for key " + event.getKey());
			String key = event.getKey();
			
			if (pendingPutReply.containsKey(key) && pendingPutReply.get(key)) {
				writeSet.get(key).add(event.getSrc());
				//System.out.println("Checking " + writeSet.get(key).size() + " against write quorum of " + wq);
				if (writeSet.get(key).size() >= wq) {
					//System.out.println("Write Quorum reached");
					
					trigger(new KVSPutReply(key), kvs);
					writeSet.remove(key);
					pendingPutReply.remove(key);
				}
			}
		}
	};
	Handler<Pp2pGetReply> onGetReply = new Handler<Pp2pGetReply>() {
		@Override
		public void handle(Pp2pGetReply event) {
			//System.out.println("Got GetReply for key " + event.getData().getKey() + " with value " + event.getData().getValue());
			KVSData data = event.getData();
			String key = data.getKey();
			
			if (pendingGetReply.containsKey(key) && pendingGetReply.get(key)) {
				readSet.get(key).add(new NodeData(event.getSrc(), data));
				//System.out.println("Checking " + readSet.get(key).size() + " against read quorum of " + rq);
				if (readSet.get(key).size() >= rq) {
					//System.out.println("Read Quorum reached");
					
					KVSData curValue = maxTSValue(readSet.get(key));
					trigger(new KVSGetReply(key, curValue.getValue()), kvs);
					readSet.remove(key);
					pendingGetReply.remove(key);
				}
			}
//			} else {
//				System.out.println("Key " + key + " was apparently not in " + pendingGetReply.toString());
//			}
		}
	};

	private KVSData maxTSValue(Set<NodeData> dataSet) {
		KVSData rdata = null;
		for (NodeData nodeData : dataSet) {
			if (rdata == null || nodeData.getData().getValueTS() > rdata.getValueTS()) {
				rdata = nodeData.getData();
			}
		}

		return rdata;
	}

	private static class Value {

		private int ts;
		private final BigInteger hash;

		public Value(int ts, BigInteger hash) {
			this.ts = ts;
			this.hash = hash;
		}

		public BigInteger getHash() {
			return hash;
		}

		public int getTs() {
			return ts;
		}

		public void setTs(int ts) {
			this.ts = ts;
		}
	}

	private static class NodeData {

		private Address addr;
		private KVSData data;

		public NodeData(Address addr, KVSData data) {
			this.addr = addr;
			this.data = data;
		}

		public Address getAddr() {
			return addr;
		}

		public KVSData getData() {
			return data;
		}
	}

	private static class WriteData {

		private Address addr;
		private int nodeId;
		private int ts;

		public WriteData(Address addr, int nodeId, int ts) {
			this.addr = addr;
			this.nodeId = nodeId;
			this.ts = ts;
		}

		public Address getAddr() {
			return addr;
		}

		public int getNodeId() {
			return nodeId;
		}

		public int getTs() {
			return ts;
		}
	}
}
