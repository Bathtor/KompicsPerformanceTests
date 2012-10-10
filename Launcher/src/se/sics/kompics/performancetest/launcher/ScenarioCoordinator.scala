package se.sics.kompics.performancetest.launcher

import akka.actor.Actor
import se.sics.kompics.performancetest.scenario.Topology
import se.sics.kompics.performancetest.scenario.Scenario
import akka.actor.Address
import scala.Math._
import akka.actor.Props
import akka.routing.RemoteRouterConfig
import akka.routing.BroadcastRouter
import akka.actor.ActorRef
import scala.collection.JavaConverters._
import akka.actor.AddressFromURIString

case class Launch(t: Topology, s: Scenario, c: String, profiler: ActorRef);

class ScenarioCoordinator(className: String, n: Int, profiler: ActorRef) extends Actor {
	import context._

	val addresses = getAddresses();

	val scenarioRouter = context.actorOf(Props[ScenarioActor].withRouter(RemoteRouterConfig(BroadcastRouter(nrOfInstances = addresses.size), addresses)));

	val t = generateTopology();
	val s = generateScenario();

	scenarioRouter ! Launch(t, s, className, profiler);

	def receive = {
		case x => println("ScenarioCoordinator got: " + x);
	}

	private def generateTopology(): Topology = {
		val topo = new Topology();
		var ips = Set.empty[String];
		addresses foreach {
			adr =>
				{
					val ip: String = adr.host.get;
					ips += ip;
				}
		}
		val numNodes = ips.size;
		val perNode = n / numNodes;
		println("Creating " + perNode + " nodes per system");
		val rest = n % numNodes;
		println("Creating " + rest + " additional nodes");
		var id = 0;
		val basePort = 22000;

		ips foreach {
			ip =>
				for (i <- 1 to perNode) {
					topo.node(id, ip, basePort + id);
					println("Created regular node ("+id+","+ip+","+(basePort+id)+")");
					id += 1;
				}

		}

		val restIps = ips.take(rest);
		restIps foreach {
			ip =>
				{
					topo.node(id, ip, basePort + id);
					println("Created extra node ("+id+","+ip+","+(basePort+id)+")");
					id += 1;
				}
		}

		return topo;
	}

	private def generateScenario(): Scenario = {
		val s = new Scenario();
		s.warmUp(30000);
		s.measure(60000);
		s.setAntiEntropyInterval(1000);
		s.setReadQuorum(round(0.3f*n + 0.5f)); // 30% read quorum
		s.setWriteQuorum(round(0.8f*n + 0.5f)); // 80% write quorum
		
		return s;
	}

	private def getAddresses(): Iterable[Address] = {
		val strings = system.settings.config.getStringList("tests.remote.nodes").asScala.toList;
		var adrs = Set.empty[Address];
		strings foreach {
			s => {
				val adr = AddressFromURIString(s);
				adrs += adr;
				println("Created address with host: " + adr.host.get);
			}
		}
//		adrs += Address("akka", "testSystem", "192.168.0.105", 2555);
//		adrs += Address("akka", "testSystem", "192.168.0.106", 2556);
		return adrs;
	}
}