package se.kth.ii2202.experiments.kvstore;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import se.kth.ii2202.experiments.beb.BestEffortBroadcast;
import se.kth.ii2202.experiments.beb.BroadcastRequest;
import se.kth.ii2202.experiments.common.PerfectPointToPointLink;
import se.kth.ii2202.experiments.common.Pp2pSend;
import se.kth.ii2202.experiments.kvstore.MerkleLazy.Diff;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author carbone
 */
public class GRMT extends ComponentDefinition {

    private Negative<ReplicatedStore> rs = provides(ReplicatedStore.class);
    private Positive<Timer> timer = requires(Timer.class);
    private Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
    
    private long period;
    private Address selfAddr;
    private ScheduleTimeout aet;
    private Map<BigInteger, Object> store = new HashMap<BigInteger, Object>();
    private MerkleLazy mtree = new MerkleLazy();

    public GRMT() {
        subscribe(onInit, control);
        subscribe(onRSstore, rs);
        subscribe(onRSretrieve, rs);
        subscribe(onAEStimeout, timer);
        subscribe(onRootRequest, beb);
        subscribe(onTreeRequest, pp2p);
        subscribe(onValueRequest, pp2p);
        subscribe(onRValueRequest, pp2p);

    }
    Handler<RSInit> onInit = new Handler<RSInit>() {
        @Override
        public void handle(RSInit e) {
            GRMT.this.period = e.getTimeDelay();
            GRMT.this.selfAddr = e.getTopology().getSelfAddress();
            aet = new ScheduleTimeout(period);
            aet.setTimeoutEvent(new AETimeout(aet));
            trigger(aet, timer);
        }
    };
    Handler<RSstore> onRSstore = new Handler<RSstore>() {
        @Override
        public void handle(RSstore event) {
            BigInteger hash = mtree.calcHash(event.getValue());
            mtree.add(hash);
            store.put(hash, event.getValue());
            trigger(new RSvalue(hash, event.getValue()), rs);
        }
    };
    Handler<RSretrieve> onRSretrieve = new Handler<RSretrieve>() {
        @Override
        public void handle(RSretrieve event) {
            trigger(new RSvalue(event.getKey(), store.get(event.getKey())), rs);
        }
    };
    Handler<AETimeout> onAEStimeout = new Handler<AETimeout>() {
        @Override
        public void handle(AETimeout event) {
            trigger(new BroadcastRequest(new BSRootRequest(selfAddr, mtree.getRoothash())), beb);
            trigger(aet, timer);
        }
    };
    Handler<BSRootRequest> onRootRequest = new Handler<BSRootRequest>() {
        @Override
        public void handle(BSRootRequest event) {
            if (!mtree.getRoothash().equals(event.getRoot())) {
                trigger(new Pp2pSend(selfAddr, new Pp2pTreeRequest(mtree, selfAddr)), pp2p);
            }
        }
    };
    Handler<Pp2pTreeRequest> onTreeRequest = new Handler<Pp2pTreeRequest>() {
        @Override
        public void handle(Pp2pTreeRequest event) {
            Diff diff = mtree.diff(event.getTree());
            for (BigInteger rHash : diff.getMissing()) {
                trigger(new Pp2pSend(selfAddr, new Pp2pRValueRequest(rHash, selfAddr)), pp2p);
            }
            for (BigInteger exHash : diff.getExcess()) {
                trigger(new Pp2pSend(selfAddr, new Pp2pValueRequest(exHash, store.get(exHash), selfAddr)), pp2p);
            }
        }
    };
    Handler<Pp2pValueRequest> onValueRequest = new Handler<Pp2pValueRequest>() {
        @Override
        public void handle(Pp2pValueRequest event) {
            store.put(event.getKey(), event.getVal());
            mtree.add(event.getKey());
            trigger(new RSvalue(event.getKey(), event.getVal()), rs);
        }
    };
    Handler<Pp2pRValueRequest> onRValueRequest = new Handler<Pp2pRValueRequest>() {
        @Override
        public void handle(Pp2pRValueRequest event) {
            trigger(new Pp2pSend(selfAddr, new Pp2pValueRequest(event.getKey(), store.get(event.getKey()), selfAddr)), pp2p);
        }
    };
}
