package se.kth.ii2202.experiments.nnar;

import se.sics.kompics.Event;

/**
 *
 * @author carbone
 */
public class NNARreadreturn extends Event{
    private Object value;
    
    public NNARreadreturn(Object value)
    {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
    
}
