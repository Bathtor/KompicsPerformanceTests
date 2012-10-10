package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.PortType;

/**
 *
 * @author carbone
 */
public class ReplicatedStore extends PortType {

    {
        request(RSstore.class);
        request(RSretrieve.class);
        indication(RSvalue.class);
    }
}
