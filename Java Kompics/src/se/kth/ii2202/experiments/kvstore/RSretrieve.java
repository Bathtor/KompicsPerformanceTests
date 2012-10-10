package se.kth.ii2202.experiments.kvstore;

import java.math.BigInteger;
import se.sics.kompics.Event;

/**
 *
 * @author carbone
 */
public class RSretrieve extends Event{
    
    private final BigInteger key;

    public RSretrieve(BigInteger key) {
        this.key = key;
    }

    public BigInteger getKey() {
        return key;
    }
    
}
