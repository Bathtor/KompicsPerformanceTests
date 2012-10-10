package se.kth.ii2202.experiments.common;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.TopologyView;

/**
 *
 * @author carbone
 */
public class Pp2pInit extends Init{

    private final TopologyView topology;

    public Pp2pInit(TopologyView topology) {
        this.topology = topology;

    }

    public TopologyView getTopology() {
        return topology;
    }
}
