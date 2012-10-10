package se.sics.kompics.performancetest.scenario;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import se.sics.kompics.address.Address;

public class TopologyView {
	
	private final int selfId;
	private final Address self;
	private final Set<Address> neighbours;
	
	protected TopologyView(int selfId, Address self, Collection<Address> neighbours) {
		this.selfId = selfId;
		this.self = self;
		this.neighbours = new HashSet<Address>();
		this.neighbours.addAll(neighbours);
	}

	/**
	 * @return the selfId
	 */
	public int getSelfId() {
		return selfId;
	}

	/**
	 * @return the self
	 */
	public Address getSelf() {
		return self;
	}
	
	public Address getSelfAddress() {
		return getSelf();
	}

	/**
	 * @return the neighbours
	 */
	public Set<Address> getNeighbours() {
		return neighbours;
	}
	
}
