package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;
import java.math.BigInteger;
import se.kth.ii2202.experiments.beb.BroadcastDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class BSRootRequest extends BroadcastDeliver implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7341663017408869222L;
	
	
	private final BigInteger root;
    
    public BSRootRequest(Address src, BigInteger root) {
        super(src);
        this.root = root;
    }

    public BigInteger getRoot() {
        return root;
    }
    
    
}
