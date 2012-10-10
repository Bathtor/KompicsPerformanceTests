package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;

import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class Pp2pGetReply extends Pp2pDeliver implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1560008833533144518L;
	
	
	private KVSData data;

    public Pp2pGetReply(KVSData data, Address src) {
        super(src);
        this.data = data;
    }

    public KVSData getData() {
        return data;
    }
    
}
