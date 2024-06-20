package org.openmrs.module.epts.etl.etl.model;

public class EtlCounter {
	
	private int count;
	
	public synchronized void increese() {
		count++;
	}
	
	public int getCurrentCount() {
		return count;
	}
	
}
