package se.sics.kompics.performancetest.scenario;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import se.sics.kompics.address.Address;

public class Topology implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4359167872517260750L;
	

	private Integer n = 0;
	private final Map<Integer, Address> addresses = new TreeMap<Integer, Address>();
	private final Map<InetAddress, Set<Integer>> ipToIds = new HashMap<InetAddress, Set<Integer>>();
	
	public void node(int id, String ip, int port) {
		if (addresses.containsKey(id)) {
			throw new RuntimeException("Can't define node with id " + id + " more than once!");
		}
		try {
			InetAddress ipadr = InetAddress.getByName(ip);
			Address adr = new Address(ipadr, port, id);
			addresses.put(id, adr);
			addToIpMap(adr);
			n++;
		} catch (UnknownHostException e) {
			throw new RuntimeException("Bad node IP or hostname", e);
		}
		
	}
	
	public TopologyView getView(int selfId) {
		Address selfAddress = addresses.get(selfId);
		if (selfAddress == null) {
			throw new RuntimeException("No node with id "+ selfId + " exists in this Topology!");
		}
		return new TopologyView(selfId, selfAddress, addresses.values());
	}
	
	public Set<Integer> getLocalNodes(InetAddress ip) {
		Set<Integer> nodes = ipToIds.get(ip);
		if (nodes == null) {
			return new TreeSet<Integer>(); // just return empty set to avoid exceptions
		}
		return nodes;
	}
	
	public Integer size() {
		return n;
	}
	
	private void addToIpMap(Address adr) {
		InetAddress ipadr = adr.getIp();
		Set<Integer> s = ipToIds.get(ipadr);
		if (s == null) {
			s = new TreeSet<Integer>();
			ipToIds.put(ipadr, s);
		}
		s.add(adr.getId());
	}
	
}
