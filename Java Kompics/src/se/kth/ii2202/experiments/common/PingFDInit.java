package se.kth.ii2202.experiments.common;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.TopologyView;

/**
 *
 * @author carbone
 */
public class PingFDInit extends Init {

    private final TopologyView topology;
    private final long timeout;

    public PingFDInit(TopologyView topology, int timeout) {
        this.topology = topology;
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public TopologyView getTopology() {
        return topology;
    }
}
