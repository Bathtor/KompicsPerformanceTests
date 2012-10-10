package se.kth.ii2202.experiments.common;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

/**
 *
 * @author carbone
 */
public class WarmUpTimeout extends Timeout {

    public WarmUpTimeout(ScheduleTimeout t) {
        super(t);
    }
}
