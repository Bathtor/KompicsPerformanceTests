package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.Init;
import se.sics.kompics.performancetest.scenario.TopologyView;

/**
 *
 * @author carbone
 */
public class KVSInit extends Init {
    
    private final TopologyView topology;
    private final int writeQuorum;
    private final int readQuorum;

    public KVSInit(TopologyView topology, int writeQuorum, int readQuorum) {
        this.topology = topology;
        this.writeQuorum = writeQuorum;
        this.readQuorum = readQuorum;
    }

    public int getReadQuorum() {
        return readQuorum;
    }

    public TopologyView getTopology() {
        return topology;
    }

    public int getWriteQuorum() {
        return writeQuorum;
    }
    
    
}
