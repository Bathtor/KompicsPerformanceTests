package se.kth.ii2202.experiments.nnar;

import java.net.InetAddress;
import java.util.Set;
import se.kth.ii2202.experiments.beb.BEBInit;
import se.kth.ii2202.experiments.beb.BestEffortBroadcast;
import se.kth.ii2202.experiments.beb.SimpleBroadcast;
import se.kth.ii2202.experiments.common.PerfectFailureDetector;
import se.kth.ii2202.experiments.common.PerfectPointToPointLink;
import se.kth.ii2202.experiments.common.PerfectUnicast;
import se.kth.ii2202.experiments.common.PingFD;
import se.kth.ii2202.experiments.common.Pp2pInit;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.network.netty.NettyNetworkInit;
import se.sics.kompics.performancetest.scenario.Profiler;
import se.sics.kompics.performancetest.scenario.Scenario;
import se.sics.kompics.performancetest.scenario.Topology;
import se.sics.kompics.performancetest.scenario.TopologyView;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

/**
 *
 * @author carbone
 */
public class Application extends ComponentDefinition {

    Handler<Fault> handleFault = new Handler<Fault>() {
        @Override
        public void handle(Fault fault) {
            fault.getFault().printStackTrace(System.err);
        }
    };

    public static void log(long ts, String opType, long time) {
        if (Application.profiler != null) {
            profiler.log(time, opType, time);
        }
    }
    protected static Profiler profiler;
    protected static Scenario scenario;
    protected static Topology topology;
    protected static InetAddress ip;

    {
        System.out.println("Starting NNAR Application...");
        Set<Integer> nodes = Application.topology.getLocalNodes(Application.ip);
        for (Integer node : nodes) {
            TopologyView view = Application.topology.getView(node);
            Address self = view.getSelfAddress();

            System.out.println("Setting up node " + node + " with address " + self);

            Component time = create(JavaTimer.class);
            Component network = create(NettyNetwork.class);
            Component pp2p = create(PerfectUnicast.class);
            Component beb = create(SimpleBroadcast.class);
            Component pfd = create(PingFD.class);
            Component nnar = create(RIWC.class);
            Component main = create(Main.class);

            subscribe(handleFault, time.control());
            subscribe(handleFault, network.control());
            subscribe(handleFault, pp2p.control());
            subscribe(handleFault, beb.control());
            subscribe(handleFault, pfd.control());
            subscribe(handleFault, nnar.control());
            subscribe(handleFault, main.control());

            //javaTimer init?
            trigger(new NettyNetworkInit(self, 5), network.control());
            trigger(new Pp2pInit(view), pp2p.control());
            trigger(new BEBInit(view), beb.control());
            trigger(new NNARInit(view), nnar.control());
            trigger(new NnarMainInit(Application.scenario, view), main.control());

            connect(pp2p.required(Network.class), network.provided(Network.class));
            connect(beb.required(Network.class), network.provided(Network.class));
            connect(pfd.required(Timer.class), time.provided(Timer.class));
            connect(main.required(Timer.class), time.provided(Timer.class));
            connect(nnar.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));
            connect(pfd.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));
            connect(nnar.required(BestEffortBroadcast.class), beb.provided(BestEffortBroadcast.class));
            connect(nnar.required(PerfectFailureDetector.class), pfd.provided(PerfectFailureDetector.class));
            connect(main.required(NNAR.class), nnar.provided(NNAR.class));

        }

    }
}
