package se.kth.ii2202.experiments.kvstore;

import java.net.InetAddress;
import java.util.Set;
import se.kth.ii2202.experiments.beb.BEBInit;
import se.kth.ii2202.experiments.beb.BestEffortBroadcast;
import se.kth.ii2202.experiments.beb.SimpleBroadcast;
import se.kth.ii2202.experiments.common.PerfectPointToPointLink;
import se.kth.ii2202.experiments.common.PerfectUnicast;
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
        System.out.println("Starting KVS Application...");
        Set<Integer> nodes = Application.topology.getLocalNodes(Application.ip);
        for (Integer node : nodes) {
            TopologyView view = Application.topology.getView(node);
            Address self = view.getSelfAddress();

            System.out.println("Setting up node " + node + " with address " + self);

            Component time = create(JavaTimer.class);
            Component network = create(NettyNetwork.class);
            Component pp2p = create(PerfectUnicast.class);
            Component beb = create(SimpleBroadcast.class);
            //System.out.println("creating replicated store component");
            Component rs = create(GRMT.class);
            System.out.println("creating quorum store component");
            Component kvs = create(QuorumStore.class);
            //System.out.println("creating main component");
            Component main = create(Main.class);

            //System.out.println("created components");
            
            subscribe(handleFault, time.control());
            subscribe(handleFault, network.control());
            subscribe(handleFault, pp2p.control());
            subscribe(handleFault, beb.control());
            subscribe(handleFault, rs.control());
            subscribe(handleFault, kvs.control());
            subscribe(handleFault, main.control());
            
            //System.out.println("subscribed faulthandler");

            trigger(new NettyNetworkInit(self, 5), network.control());
            trigger(new Pp2pInit(view), pp2p.control());
            trigger(new BEBInit(view), beb.control());
            trigger(new RSInit(Application.scenario.getAntiEntropyInterval(), view), rs.control());
            trigger(new KVSInit(view, Application.scenario.getWriteQuorum(), Application.scenario.getReadQuorum()), kvs.control());
            trigger(new KVStoreMainInit(Application.scenario, view), main.control());

            //System.out.println("triggered init events");
            
            connect(pp2p.required(Network.class), network.provided(Network.class));
            connect(beb.required(Network.class), network.provided(Network.class));
            connect(rs.required(Timer.class), time.provided(Timer.class));
            connect(main.required(Timer.class), time.provided(Timer.class));
            connect(rs.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));
            connect(kvs.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));
            connect(rs.required(BestEffortBroadcast.class), beb.provided(BestEffortBroadcast.class));
            connect(kvs.required(BestEffortBroadcast.class), beb.provided(BestEffortBroadcast.class));
            connect(kvs.required(ReplicatedStore.class), rs.provided(ReplicatedStore.class));
            connect(main.required(KeyValueStore.class), kvs.provided(KeyValueStore.class));

            //System.out.println("connected components");
        }

    }
}
