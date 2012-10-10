package se.kth.ii2202.experiments.common;

import se.sics.kompics.PortType;

/**
 *
 * @author carbone
 */
public class PerfectFailureDetector extends PortType{
    {
        indication(DownEvent.class);
    }
}
