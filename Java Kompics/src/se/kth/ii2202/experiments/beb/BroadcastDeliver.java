package se.kth.ii2202.experiments.beb;

import java.io.Serializable;
import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class BroadcastDeliver extends Pp2pDeliver implements Serializable {

	
    public BroadcastDeliver(Address src) {
        super(src);
    }
}
