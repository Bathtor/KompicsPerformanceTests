package se.sics.kompics.performancetest.launcher

import se.sics.kompics.performancetest.scenario.TopologyView
import se.sics.kompics.address.Address
import scala.collection.JavaConverters._

/**
 * The <code>ScalaTopology</code> class.
 * 
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 */
class ScalaTopology(original: TopologyView) {
	def neighbours() : Set[Address] = {		
			val neighs = original.getNeighbours();
			return neighs.asScala.toSet;
	}
}

/**
 * The <code>ScalaTopology</code> object.
 * 
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 */
object ScalaTopology {
	implicit def topology2scala(x: TopologyView): ScalaTopology = new ScalaTopology(x);
}