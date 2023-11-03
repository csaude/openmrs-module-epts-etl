package org.openmrs.module.epts.etl.utilities.concurrent;

import java.util.concurrent.ExecutorService;

public abstract class BackgroundRunner implements Runnable{
	private boolean running;
	private ExecutorService executorService;
	private String taskName;
	private TimeController timer;
	
	public BackgroundRunner(String taskName){
		this.taskName = taskName;
	}
	
	public void runInBackground() {
		this.running = true;
		
		this.executorService = ThreadPoolService.getInstance().createNewThreadPoolExecutor(taskName);
		
		this.executorService.execute(this);
	}
	
	public TimeController getTimer() {
		return timer;
	}
	
	@Override
	public void run() {
		this.timer = new TimeController();
		this.timer.start();
		
		doRun();
		this.running = false;
		
		this.timer.stop();
	}
	
	public abstract void doRun();
	
	public boolean isRunning() {
		return running;
	}
	
}
