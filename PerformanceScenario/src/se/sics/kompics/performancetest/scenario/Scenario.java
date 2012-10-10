package se.sics.kompics.performancetest.scenario;

import java.io.Serializable;

public class Scenario implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6873399744413723984L;

	private String globalCmd = "";
	private Long warmUpTime = 0l;
	private Long measureTime = 0l;
	private Integer readQuorum = 0;
	private Integer writeQuorum = 0;
	private Long antiEntropyInterval = 0l;
	
	public Integer getReadQuorum() {
		return this.readQuorum;
	}

	public void setReadQuorum(Integer readQuorum) {
		this.readQuorum = readQuorum;
	}

	public Integer getWriteQuorum() {
		return this.writeQuorum;
	}

	public void setWriteQuorum(Integer writeQuorum) {
		this.writeQuorum = writeQuorum;
	}

	public Long getAntiEntropyInterval() {
		return this.antiEntropyInterval;
	}

	public void setAntiEntropyInterval(Long antiEntropyInterval) {
		this.antiEntropyInterval = antiEntropyInterval;
	}

	public void command(int id, String cmd) {
		throw new RuntimeException("Not yet implemented!");
	}
	
	public void globalCommand(String cmd) {
		globalCmd = cmd;
	}
	
	public void warmUp(Long ms) {
		warmUpTime = ms;
	}
	
	public void measure(Long ms) {
		measureTime = ms;
	}
	
	public Long getWarmUpTime() {
		return warmUpTime;
	}
	
	public Long getMeasureTime() {
		return measureTime;
	}
	
	public String getCommand(int id) {
		if (globalCmd != null) {
			return globalCmd;
		}
		throw new RuntimeException("Per-node-commands not yet implemented!");
	}
	
}
