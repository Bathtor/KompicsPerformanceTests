package se.kth.ii2202.experiments.beb;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.TopologyView;

public class BEBInit extends Init {

    final TopologyView topology;

    public BEBInit(TopologyView topology) {
        this.topology = topology;
    }

    public TopologyView getTopology() {
        return topology;
    }

}
