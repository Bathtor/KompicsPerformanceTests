package se.kth.ii2202.experiments.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.performancetest.scenario.TopologyView;
    
/**
 *
 * @author carbone
 */
public class SimpleBroadcast extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(SimpleBroadcast.class);
    Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
    Positive<Network> network = requires(Network.class);
    Address selfAddr;
    TopologyView topology;

    public SimpleBroadcast() {
        subscribe(onInit, control);
        subscribe(onUnBroadcast, beb);
        subscribe(onMessageReceived, network);
    }
    Handler<BEBInit> onInit = new Handler<BEBInit>() {
        @Override
        public void handle(BEBInit e) {
            SimpleBroadcast.this.topology = e.getTopology();
            SimpleBroadcast.this.selfAddr = topology.getSelfAddress();
        }
    };
    Handler<BroadcastRequest> onUnBroadcast = new Handler<BroadcastRequest>() {
        @Override
        public void handle(BroadcastRequest e) {
        	//System.out.println("Broadcasting message of type " + e.getMsg().getClass().getCanonicalName());
            for (Address addr : topology.getNeighbours()) {
                trigger(new BroadcastMessage(selfAddr, addr, e.getMsg()), network);
            }
        }
    };
    Handler<BroadcastMessage> onMessageReceived = new Handler<BroadcastMessage>() {
        @Override
        public void handle(BroadcastMessage e) {
            trigger(e.getMsg(), beb);
        }
    };
}
