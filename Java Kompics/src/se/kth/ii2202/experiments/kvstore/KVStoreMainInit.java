package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.Scenario;
import se.sics.kompics.performancetest.scenario.TopologyView;

public class KVStoreMainInit extends Init {

    private final Scenario scenario;
    private final TopologyView topology;

    public KVStoreMainInit(Scenario scenario, TopologyView topology) {
        this.scenario = scenario;
        this.topology = topology;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public TopologyView getTopology() {
        return topology;
    }
}
