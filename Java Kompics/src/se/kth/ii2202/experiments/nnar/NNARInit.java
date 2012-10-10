/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.ii2202.experiments.nnar;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.TopologyView;

public class NNARInit extends Init {

    final TopologyView topology;

    public NNARInit(TopologyView topology) {
        this.topology = topology;
    }

    public TopologyView getTopology() {
        return topology;
    }

}
