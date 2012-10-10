package se.kth.ii2202.experiments.kvstore;

import java.io.Serializable;

import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class Pp2pPutReply extends Pp2pDeliver implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 8808439451468764292L;
	
	
	private String key;

    public Pp2pPutReply(String key, Address src) {
        super(src);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
    
    
    
}
