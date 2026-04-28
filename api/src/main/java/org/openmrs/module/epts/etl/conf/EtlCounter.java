package org.openmrs.module.epts.etl.conf;

public class EtlCounter {
	
	Integer counter;
	
	public EtlCounter() {
		counter = 0;
	}
	
	public synchronized void increase() {
		counter++;
	}
	
	@Override
	public String toString() {
		return "Counter = " + counter;
	}
	
	public Integer getCounter() {
		return this.counter;
	}
}
