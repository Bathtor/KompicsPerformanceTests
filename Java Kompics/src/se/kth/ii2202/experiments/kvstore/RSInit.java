package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.TopologyView;

/**
 *
 * @author carbone
 */
public class RSInit extends Init {

    private final long timeDelay;
    private final TopologyView topology;

    public RSInit(long timeDelay, TopologyView topology) {
        this.timeDelay = timeDelay;
        this.topology = topology;
    }

    public long getTimeDelay() {
        return timeDelay;
    }

    public TopologyView getTopology() {
        return topology;
    }
}
