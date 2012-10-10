package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;
import java.math.BigInteger;
import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class Pp2pRValueRequest extends Pp2pDeliver implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 3091623144432270460L;
	
	
	private final BigInteger key;

    public Pp2pRValueRequest(BigInteger key, Address src) {
        super(src);
        this.key = key;
    }

    public BigInteger getKey() {
        return key;
    }
    
    
}
