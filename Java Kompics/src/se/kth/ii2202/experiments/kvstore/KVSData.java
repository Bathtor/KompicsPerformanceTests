package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;

/**
 *
 * @author carbone
 */
public class KVSData implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1270876751792728948L;
	
	
	private final int nodeId;
    private final int valueTS;
    private final String key;
    private final Object value;

    public KVSData(int nodeId, int valueTS, String key, Object value) {
        this.nodeId = nodeId;
        this.valueTS = valueTS;
        this.key = key;
        this.value = value;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getValueTS() {
        return valueTS;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public static KVSData noData(String key) {
        return new KVSData(-1, -1, key, null);
    }
}
