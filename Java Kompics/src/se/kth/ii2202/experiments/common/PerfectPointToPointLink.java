package se.kth.ii2202.experiments.common;

import se.sics.kompics.PortType;

/**
 *
 * @author carbone
 */
public class PerfectPointToPointLink extends PortType {

    {
        request(Pp2pSend.class);
        indication(Pp2pDeliver.class);
    }

    
}
