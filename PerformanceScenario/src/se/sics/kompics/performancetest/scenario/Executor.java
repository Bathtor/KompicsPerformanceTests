/**
 * 
 */
package se.sics.kompics.performancetest.scenario;

import java.net.InetAddress;

/**
 * The <code>Executor</code> .
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 *
 */
public interface Executor {
	public void run(Topology t, Scenario s, Profiler p, InetAddress ip);
}
