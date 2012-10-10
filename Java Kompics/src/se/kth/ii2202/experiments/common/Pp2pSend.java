package se.kth.ii2202.experiments.common;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

/**
 * 
 * @author carbone
 */
public class Pp2pSend extends Event {

    private Address destination;
    private Pp2pDeliver message;

    public Pp2pSend(Address destination, Pp2pDeliver message) {
        this.destination = destination;
        this.message = message;
    }

    public Address getDestination() {
        return destination;
    }

    public Pp2pDeliver getMessage() {
        return message;
    }

    public void setDestination(Address destination) {
        this.destination = destination;
    }

    public void setMessage(Pp2pDeliver message) {
        this.message = message;
    }
}
