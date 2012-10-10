package se.kth.ii2202.experiments.nnar;

import java.io.Serializable;

import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class UAack extends Pp2pDeliver implements Serializable {

    public UAack(Address src) {
        super(src);
    }
}
