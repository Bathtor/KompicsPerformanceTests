package se.kth.ii2202.experiments.nnar;

import se.sics.kompics.PortType;

/**
 *
 * @author carbone
 */
public class NNAR extends PortType{
    {
        request(NNARread.class);
        request(NNARwrite.class);
        indication(NNARreadreturn.class);
        indication(NNARwritereturn.class);
    }
}
