package se.kth.ii2202.experiments.beb;

import java.io.Serializable;
import se.sics.kompics.Event;

public class BroadcastRequest extends Event implements Serializable {

    private final BroadcastDeliver msg;

    public BroadcastRequest(BroadcastDeliver msg) {
        this.msg = msg;
    }

    public BroadcastDeliver getMsg() {
        return msg;
    }

}
