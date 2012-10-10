package se.kth.ii2202.experiments.nnar;

import java.net.InetAddress;
import se.sics.kompics.Kompics;
import se.sics.kompics.performancetest.scenario.Profiler;
import se.sics.kompics.performancetest.scenario.Scenario;
import se.sics.kompics.performancetest.scenario.Topology;

/**
 *
 * @author carbone
 */
public class Executor implements se.sics.kompics.performancetest.scenario.Executor {

    @Override
    public void run(Topology t, Scenario s, Profiler p, InetAddress ip) {
        Application.profiler = p;
        Application.topology = t;
        Application.scenario = s;
        Application.ip = ip;
        Kompics.createAndStart(Application.class, 12);
    }
}
