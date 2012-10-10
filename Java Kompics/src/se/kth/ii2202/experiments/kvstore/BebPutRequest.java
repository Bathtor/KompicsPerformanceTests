package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;

import se.kth.ii2202.experiments.beb.BroadcastDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class BebPutRequest extends BroadcastDeliver implements Serializable {
    
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2366881374871174511L;
	
	private final KVSData data;
    
    
    public BebPutRequest(KVSData data, Address src) {
        super(src);
        this.data = data;
    }

    public KVSData getData() {
        return data;
    }
}
