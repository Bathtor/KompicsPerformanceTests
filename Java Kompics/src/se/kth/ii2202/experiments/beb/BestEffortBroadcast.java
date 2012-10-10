package se.kth.ii2202.experiments.beb;

import se.sics.kompics.PortType;


public class BestEffortBroadcast extends PortType {

    {
        request(BroadcastRequest.class);
        indication(BroadcastDeliver.class);
    }

}
