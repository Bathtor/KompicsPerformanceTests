package se.kth.ii2202.experiments.common;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

/**
 *
 * @author carbone
 */
public class MeasureTimeout extends Timeout {

    public MeasureTimeout(ScheduleTimeout s) {
        super(s);
    }
}
