package se.kth.ii2202.experiments.common;

import java.io.Serializable;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class Pp2pDeliver extends Event implements Serializable {

    private Address src;
    
//    /**
//     * no arg constructor for deserialization
//     */
//    public Pp2pDeliver() {
//    	
//    }

    public Pp2pDeliver(Address src) {
        this.src = src;
    }

    public Address getSrc() {
        return src;
    }


    public void setSrc(Address src) {
        this.src = src;
    }
}