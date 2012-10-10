package se.kth.ii2202.experiments.kvstore;

import java.math.BigInteger;
import se.sics.kompics.Event;

/**
 *
 * @author carbone
 */
public class RSvalue extends Event{
    
    private final BigInteger key;
    private final Object value;

    public RSvalue(BigInteger key, Object value) {
        this.key = key;
        this.value = value;
    }

    public BigInteger getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
    
    
    
}
