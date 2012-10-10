package se.kth.ii2202.experiments.common;

import java.util.Set;
import se.kth.ii2202.experiments.beb.BEBInit;
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
public class PerfectUnicast extends ComponentDefinition {

    private Negative<PerfectPointToPointLink> pp2p = provides(PerfectPointToPointLink.class);
    private Positive<Network> network = requires(Network.class);
    private Address selfAddr;
    private TopologyView topologyView;
    private Set<Address> neighbors;

    public PerfectUnicast() {
        subscribe(onInit, control);
        subscribe(onSendRequest, pp2p);
        subscribe(onMessageReceived, network);
    }
    Handler<Pp2pInit> onInit = new Handler<Pp2pInit>() {
        @Override
        public void handle(Pp2pInit e) {
            PerfectUnicast.this.topologyView = e.getTopology();
            PerfectUnicast.this.selfAddr = topologyView.getSelfAddress();
        }
    };
    Handler<Pp2pSend> onSendRequest = new Handler<Pp2pSend>() {
        @Override
        public void handle(Pp2pSend e) {
        	//System.out.println("Sending message of type " + e.getMessage().getClass().getCanonicalName());
			trigger(new Pp2pMessage(selfAddr, e.getDestination(), e.getMessage()), network);
        }
    };
    Handler<Pp2pMessage> onMessageReceived = new Handler<Pp2pMessage>() {
        @Override
        public void handle(Pp2pMessage e) {
            trigger(e.getMsg(), pp2p);
        }
    };
}
