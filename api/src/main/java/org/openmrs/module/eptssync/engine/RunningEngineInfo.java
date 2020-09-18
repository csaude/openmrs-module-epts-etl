package org.openmrs.module.eptssync.engine;

import java.util.concurrent.ExecutorService;

public class RunningEngineInfo {
	private ExecutorService executorService;
	private SyncEngine engine;
	
	public RunningEngineInfo(ExecutorService executorService, SyncEngine engine) {
		this.executorService = executorService;
		this.engine = engine;
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	public SyncEngine getEngine() {
		return engine;
	}
}
