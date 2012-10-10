package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.Event;

/**
 *
 * @author carbone
 */
public class KVSGetReply extends Event{
    private final String key;
    private final Object val;

    public KVSGetReply(String key, Object val) {
        this.key = key;
        this.val = val;
    }

    public String getKey() {
        return key;
    }

    public Object getVal() {
        return val;
    }
    
}
