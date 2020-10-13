package org.openmrs.module.eptssync.engine;

import java.util.concurrent.ExecutorService;

public class RunningEngineInfo {
	private ExecutorService executorService;
	private Engine engine;
	
	public RunningEngineInfo(ExecutorService executorService, Engine engine) {
		this.executorService = executorService;
		this.engine = engine;
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	public Engine getEngine() {
		return engine;
	}
}
