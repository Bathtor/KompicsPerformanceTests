package se.kth.ii2202.experiments.common;

import java.io.Serializable;

import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class HeartbeatResponse extends Pp2pDeliver implements Serializable {

    public HeartbeatResponse(Address src) {
        super(src);
    }
    
    
    
}
