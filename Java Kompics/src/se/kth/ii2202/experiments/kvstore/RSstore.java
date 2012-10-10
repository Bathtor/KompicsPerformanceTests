package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.Event;

/**
 *
 * @author carbone
 */
public class RSstore extends Event{
    private final Object value;

    public RSstore(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
    
}
