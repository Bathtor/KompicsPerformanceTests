package se.kth.ii2202.experiments.nnar;

import java.io.Serializable;

import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author carbone
 */
public class Pp2pBCWrite extends Pp2pDeliver implements Serializable {
    private int ts;
    private int wr;
    private Object val;
    

    public Pp2pBCWrite(Address src, int ts, int wr, Object val) {
        super(src);
        this.ts = ts;
        this.wr = wr;
        this.val = val;
    }

    public int getTs() {
        return ts;
    }

    public Object getVal() {
        return val;
    }

    public int getWr() {
        return wr;
    }
}
