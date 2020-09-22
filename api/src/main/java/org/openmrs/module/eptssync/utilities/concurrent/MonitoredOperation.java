package org.openmrs.module.eptssync.utilities.concurrent;

/**
 * Representa uma operacao monitorada 
 * 
 * @author JPBOANE
 *
 */
public interface MonitoredOperation {
	public static final int STATUS_NOT_INITIALIZED=0;
	public static final int STATUS_RUNNING=1;
	public static final int STATUS_PAUSED = 2;
	public static final int STATUS_STOPPED=3;
	public static final int STATUS_SLEEPENG=4;
	public static final int STATUS_FINISHED=5;
	
	public TimeController getTimer();
	
	public void requestStop();
	
	public boolean stopRequested();
	
	public boolean isNotInitialized();
	public boolean isRunning();
	public boolean isStopped();
	public boolean isFinished() ;
	public boolean isPaused() ;
	public boolean isSleeping();
	
	public void changeStatusToRunning();
	public void changeStatusToStopped();
	public void changeStatusToFinished();
	public void changeStatusToPaused();
	public void changeStatusToSleeping();
	
}
