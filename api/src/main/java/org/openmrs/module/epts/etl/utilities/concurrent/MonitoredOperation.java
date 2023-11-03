package org.openmrs.module.epts.etl.utilities.concurrent;

/**
 * Representa uma operacao monitorada 
 * 
 * @author JPBOANE
 *
 */
public interface MonitoredOperation extends Runnable{
	public static final int STATUS_NOT_INITIALIZED=0;
	public static final int STATUS_RUNNING=1;
	public static final int STATUS_PAUSED = 2;
	public static final int STATUS_STOPPED=3;
	public static final int STATUS_SLEEPING=4;
	//public static final int STATUS_FINISHING=5;
	public static final int STATUS_FINISHED=6;
	
	public TimeController getTimer();
	
	public void requestStop();
	
	public boolean stopRequested();
	
	public boolean isNotInitialized();
	public boolean isRunning();
	public boolean isStopped();
	public boolean isFinished() ;
	public boolean isPaused() ;
	public boolean isSleeping();
	//public boolean isFinishing();
	
	public void changeStatusToRunning();
	public void changeStatusToStopped();
	public void changeStatusToFinished();
	public void changeStatusToPaused();
	public void changeStatusToSleeping();
	//public void changeStatusToFinishing();
	
	public abstract void onStart();
	public abstract void onSleep();
	public abstract void onStop();
	public abstract void onFinish();
	
	public abstract int getWaitTimeToCheckStatus();
	
}
