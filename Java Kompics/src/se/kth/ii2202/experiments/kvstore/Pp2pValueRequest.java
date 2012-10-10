package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;
import java.math.BigInteger;
import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class Pp2pValueRequest extends Pp2pDeliver implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5386480825890068576L;
	
	
	private final BigInteger key;
    private final Object val;

    public Pp2pValueRequest(BigInteger key, Object val, Address src) {
        super(src);
        this.key = key;
        this.val = val;
    }

    public BigInteger getKey() {
        return key;
    }

    public Object getVal() {
        return val;
    }
}
