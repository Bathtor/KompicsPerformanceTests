package se.sics.kompics.performancetest.scenario;

public interface Profiler {
	public void log(Long timestamp, String opType, Long time);
}
