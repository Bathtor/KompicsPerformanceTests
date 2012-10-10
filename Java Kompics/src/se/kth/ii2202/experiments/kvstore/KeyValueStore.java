package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.PortType;

/**
 *
 * @author carbone
 */
public class KeyValueStore extends PortType{
    
    {
        request(KVSPut.class);
        request(KVSGet.class);
        indication(KVSPutReply.class);
        indication(KVSGetReply.class);
    }
    
}
