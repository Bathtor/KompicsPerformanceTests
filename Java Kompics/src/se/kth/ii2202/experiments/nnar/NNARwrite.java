package se.kth.ii2202.experiments.nnar;

import se.sics.kompics.Event;

/**
 *
 * @author carbone
 */
public class NNARwrite extends Event {
    private Object value;

    public NNARwrite(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    
}
