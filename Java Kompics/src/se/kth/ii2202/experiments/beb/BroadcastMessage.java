/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.ii2202.experiments.beb;

import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;
import se.sics.kompics.network.Transport;

/**
 *
 * @author carbone
 */
public class BroadcastMessage extends Message {

    private final BroadcastDeliver msg;

    BroadcastMessage(Address src, Address dest, BroadcastDeliver msg) {
        super(src, dest, Transport.TCP);
        this.msg = msg;
    }

    public BroadcastDeliver getMsg() {
        return msg;
    }
}

