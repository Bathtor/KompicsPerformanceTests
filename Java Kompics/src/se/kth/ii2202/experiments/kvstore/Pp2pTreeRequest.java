package se.kth.ii2202.experiments.kvstore;

import se.kth.ii2202.experiments.common.Pp2pDeliver;
import se.sics.kompics.address.Address;
import java.io.Serializable;

/**
 *
 * @author carbone
 */
public class Pp2pTreeRequest extends Pp2pDeliver implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -6147848640266318284L;
	
	
	private final MerkleLazy tree;

    public Pp2pTreeRequest(MerkleLazy tree, Address src) {
        super(src);
        this.tree = tree;
    }

    public MerkleLazy getTree() {
        return tree;
    }
    
}
