package se.kth.ii2202.experiments.nnar;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.Scenario;
import se.sics.kompics.performancetest.scenario.TopologyView;

/**
 *
 * @author carbone
 */
public class NnarMainInit extends Init {

    private final Scenario scenario;
    private final TopologyView topology;

    public NnarMainInit(Scenario scenario, TopologyView topology) {
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
