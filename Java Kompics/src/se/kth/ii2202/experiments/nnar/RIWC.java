package se.kth.ii2202.experiments.nnar;

import java.util.HashSet;
import java.util.Set;
import se.kth.ii2202.experiments.beb.BestEffortBroadcast;
import se.kth.ii2202.experiments.beb.BroadcastRequest;
import se.kth.ii2202.experiments.common.DownEvent;
import se.kth.ii2202.experiments.common.PerfectFailureDetector;
import se.kth.ii2202.experiments.common.PerfectPointToPointLink;
import se.kth.ii2202.experiments.common.Pp2pSend;
import se.kth.ii2202.experiments.common.Utils;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.performancetest.scenario.TopologyView;

/**
 *
 * @author carbone
 */
public class RIWC extends ComponentDefinition {

    Negative<NNAR> nnar = provides(NNAR.class);
    Positive<PerfectFailureDetector> pfd = requires(PerfectFailureDetector.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
    private TopologyView topology;
    private Address selfAddress;
    private Set<Address> neighbors = new HashSet<Address>();
    int selfId;
    int ts, wr;
    boolean reading;
    Object value;
    Object readVal;
    Set<Address> correct = new HashSet<Address>();
    Set<Address> writeSet = new HashSet<Address>();

    public RIWC() {
        subscribe(onInit, control);
        subscribe(onFailDetected, pfd);
        subscribe(onNNARread, nnar);
        subscribe(onNNARwrite, nnar);
        subscribe(onBCWrite, beb);
        subscribe(onUAack, pp2p);
    }
    Handler<NNARInit> onInit = new Handler<NNARInit>() {
        @Override
        public void handle(NNARInit e) {
            RIWC.this.topology = e.getTopology();
            RIWC.this.selfAddress = topology.getSelfAddress();
            RIWC.this.correct.addAll(e.getTopology().getNeighbours());
            RIWC.this.neighbors.addAll(topology.getNeighbours());
            RIWC.this.selfId = selfAddress.getId();
        }
    };
    Handler<DownEvent> onFailDetected = new Handler<DownEvent>() {
        @Override
        public void handle(DownEvent e) {
        	//System.out.println(selfId + " got Down for " + e.getAddr());
            correct.remove(e.getAddr());
            checkWriteSet();
        }
    };
    Handler<NNARread> onNNARread = new Handler<NNARread>() {
        @Override
        public void handle(NNARread event) {
            reading = true;
            readVal = value;
            trigger(new BroadcastRequest(new RbBCWrite(selfAddress, ts, wr, value)), beb);
        }
    };
    Handler<NNARwrite> onNNARwrite = new Handler<NNARwrite>() {
        @Override
        public void handle(NNARwrite event) {
            trigger(new BroadcastRequest(new RbBCWrite(selfAddress, ts+1, selfId, event.getValue())), beb);
        }
    };
    
    Handler<RbBCWrite> onBCWrite = new Handler<RbBCWrite>() {

        @Override
        public void handle(RbBCWrite event) {
        	//System.out.println(selfId + " got Write ");
            if (Utils.isLargerThan(event.getTs(), event.getWr(), ts,wr))
            {
                ts = event.getTs();
                wr = event.getWr();
                value = event.getVal();
            }
            trigger(new Pp2pSend(event.getSrc(), new UAack(selfAddress)),pp2p);
        }
    };
    
    Handler<UAack> onUAack = new Handler<UAack>(){

        @Override
        public void handle(UAack event) {
        	//System.out.println(selfId + " got Ack ");
            writeSet.add(event.getSrc());
            checkWriteSet();
        }
    };

    private void checkWriteSet() {
        if (writeSet.containsAll(correct)) {
        	//System.out.println(selfId + " replying ");
            writeSet.clear();
            if (reading) {
                reading = false;
                trigger(new NNARreadreturn(readVal), nnar);
            } else {
                trigger(new NNARwritereturn(), nnar);
            }
        }
    }
}
