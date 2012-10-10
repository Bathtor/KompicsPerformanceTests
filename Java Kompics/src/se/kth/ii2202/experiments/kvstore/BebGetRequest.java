package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;

import se.kth.ii2202.experiments.beb.BroadcastDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class BebGetRequest extends BroadcastDeliver implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 118805966564393259L;
	
	
	private final String key;

    public BebGetRequest(String key, Address src) {
        super(src);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
