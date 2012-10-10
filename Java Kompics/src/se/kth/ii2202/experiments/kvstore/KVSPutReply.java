package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.Event;

/**
 *
 * @author carbone
 */
public class KVSPutReply extends Event{
    private final String key;

    public KVSPutReply(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
    
}
