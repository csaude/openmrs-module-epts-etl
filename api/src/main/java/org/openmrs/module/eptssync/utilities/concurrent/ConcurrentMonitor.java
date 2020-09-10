package org.openmrs.module.eptssync.utilities.concurrent;

/**
 * Representa um controlador de operacoes concorrentes 
 * 
 * @author JPBOANE
 *
 */
public interface ConcurrentMonitor {
	public static final int STATUS_NOT_INITIALIZED=0;
	public static final int STATUS_RUNNING=1;
	public static final int STATUS_PAUSED = 2;
	public static final int STATUS_STOPPED=3;
	public static final int STATUS_FINISHED=4;
	
	public Relogio getRelogio();
	
	public void requestStop();
	
	public boolean stopRequested();
	
	public boolean isRunning();
	public boolean isStopped();
	public boolean isFinished() ;
	public boolean isPaused() ;
	public boolean isSleeping();
	
	public void chageStatusToRunning();
	public void chageStatusToStopped();
	public void chageStatusToFinished();
	public void chageStatusToPaused();
	public boolean isNotInitialized();
}
