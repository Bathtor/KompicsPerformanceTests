package se.kth.ii2202.experiments.common;

import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;
import se.sics.kompics.network.Transport;

/**
 *
 * @author carbone
 */
public class Pp2pMessage extends Message {

    private Pp2pDeliver msg;

    Pp2pMessage(Address src, Address dest, Pp2pDeliver msg) {
        super(src, dest, Transport.TCP);
        this.msg = msg;
    }

    public Pp2pDeliver getMsg() {
        return msg;
    }

    public void setMsg(Pp2pDeliver msg) {
        this.msg = msg;
    }
}
