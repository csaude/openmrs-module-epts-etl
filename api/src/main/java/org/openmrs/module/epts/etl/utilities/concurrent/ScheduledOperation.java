package org.openmrs.module.epts.etl.utilities.concurrent;

import org.apache.log4j.Logger;

public abstract class ScheduledOperation implements Runnable{
	public static Logger logger = Logger.getLogger(ScheduledOperation.class);
	
	protected boolean inExecution;
	protected boolean aborted;
	/**
	 *Se a operacao foi executada com sucesso, isto  é, se foi executada até ao fim. Ou por outras
	 *Se a operacaoo nao foi cancelada 
	 */
	protected boolean executedSuccessfully;

	
	public ScheduledOperation(){
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
	 * @param conn
	 * @throws Exception
	 */
	public abstract void destruct();
}
