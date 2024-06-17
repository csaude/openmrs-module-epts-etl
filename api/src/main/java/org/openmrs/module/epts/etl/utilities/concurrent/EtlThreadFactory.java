package org.openmrs.module.epts.etl.utilities.concurrent;

import java.util.concurrent.ThreadFactory;

public class EtlThreadFactory implements ThreadFactory {
	
	private ThreadLocal<String> taskId = new ThreadLocal<>();
	
	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, taskId.get());
	}
	
	public void setTaskId(String id) {
		taskId.set(id);
	}
	
	public ThreadLocal<String> getTaskId() {
		return taskId;
	}
}
