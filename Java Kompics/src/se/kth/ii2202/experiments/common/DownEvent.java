package se.kth.ii2202.experiments.common;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class DownEvent extends Event{
    private Address addr;

    public DownEvent(Address addr) {
        this.addr = addr;
    }
    
    public Address getAddr() {
        return addr;
    }

    public void setAddr(Address addr) {
        this.addr = addr;
    }
    
}
