package org.openmrs.module.epts.etl.conf.types;

public enum ThreadingMode {
	
	MULTI,
	MULTITHREAD,
	MULTI_THREAD,
	SINGLE,
	SINGLETHREAD,
	SINGLE_THREAD;
	
	public boolean isMultiThread() {
		return this.compareTo(MULTI_THREAD) <= 0;
	}
	
	public boolean isSingleThread() {
		return this.compareTo(SINGLE) >= 0;
	}
}
