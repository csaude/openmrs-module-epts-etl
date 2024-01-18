package org.openmrs.module.epts.etl.utilities.concurrent;

public abstract class ScheduledOperation implements Runnable {
	
	protected boolean inExecution;
	
	protected boolean aborted;
	
	/**
	 * Se a operacao foi executada com sucesso, isto é, se foi executada até ao fim. Ou por outras
	 * Se a operacaoo nao foi cancelada
	 */
	protected boolean executedSuccessfully;
	
	public ScheduledOperation() {
		inExecution = true;
		aborted = false;
		executedSuccessfully = false;
	}
	
	public boolean isInExecution() {
		return inExecution;
	}
	
	public boolean isAborted() {
		return aborted;
	}
	
	public boolean isExecutedSuccessfully() {
		return executedSuccessfully;
	}
	
	/**
	 * Destroi a operacao
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public abstract void destruct();
}
