/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.ii2202.experiments.kvstore;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

/**
 *
 * @author carbone
 */
public class AETimeout extends Timeout {

    public AETimeout(ScheduleTimeout schTimeout) {
        super(schTimeout);
    }
}
