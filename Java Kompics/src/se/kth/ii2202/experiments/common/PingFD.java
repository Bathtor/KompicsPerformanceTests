package se.kth.ii2202.experiments.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.performancetest.scenario.TopologyView;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author carbone
 */
public class PingFD extends ComponentDefinition {

    Negative<PerfectFailureDetector> pfd = provides(PerfectFailureDetector.class);
    Positive<Timer> timer = requires(Timer.class);
    Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
    private long timeout = 0;
    private TopologyView topologyView;
    private Address selfAddr;
    private Collection<Address> neighbours;
    private Set<Address> alive = new HashSet<Address>();
    private Set<Address> detected = new HashSet<Address>();
    private ScheduleTimeout check;

    public PingFD() {
        subscribe(onInit, control);
        subscribe(onHeartbeatRequest, pp2p);
        subscribe(onHeartbeatResponse, pp2p);
        subscribe(onTimeout, timer);
    }
    Handler<PingFDInit> onInit = new Handler<PingFDInit>() {
        @Override
        public void handle(PingFDInit e) {
            PingFD.this.topologyView = e.getTopology();
            PingFD.this.selfAddr = topologyView.getSelfAddress();
            PingFD.this.timeout = e.getTimeout();
            check = new ScheduleTimeout(PingFD.this.timeout);
            check.setTimeoutEvent(new CheckTimeout(check));
            trigger(check, timer);
            alive.addAll(topologyView.getNeighbours());
        }
    };
    Handler<HeartbeatRequest> onHeartbeatRequest = new Handler<HeartbeatRequest>() {
        @Override
        public void handle(HeartbeatRequest event) {
            trigger(new Pp2pSend(event.getSrc(), new HeartbeatResponse(selfAddr)), pp2p);
        }
    };
    Handler<HeartbeatResponse> onHeartbeatResponse = new Handler<HeartbeatResponse>() {
        @Override
        public void handle(HeartbeatResponse event) {
            PingFD.this.alive.add(event.getSrc());
        }
    };
    Handler<CheckTimeout> onTimeout = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            for (Address addr : neighbours) {
                if (!alive.contains(addr) && !detected.contains(addr)) {
                    detected.add(addr);
                    trigger(new DownEvent(addr), pfd);
                } else if (!detected.contains(addr)) {
                    trigger(new Pp2pSend(addr, new HeartbeatRequest(selfAddr)), pp2p);
                }
            }
            alive.clear();
            trigger(check,timer);
        }
    };
}
