/**
 * TimeCountDown
 * Est� classe � respons�vel por um mecanismo de contagem decrescente do tempo. Quando o tempo se esgota, � rebentada uma excep��o
 * 
 */
package org.openmrs.module.epts.etl.utilities.concurrent;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;

public class TimeCountDown extends ScheduledOperation {
	
	public static EptsEtlLogger logger = EptsEtlLogger.getLogger(TimeCountDown.class);
	
	private String message;
	
	Thread timer = null;
	
	private long totalTimeToCount;
	
	private long remainTime;
	
	private TimeCountDownInitializer initializer;
	
	private long intervalForMessage;
	
	public TimeCountDown(String message, long totalTimeToCount) {
		this.message = message;
		this.inExecution = true;
		this.totalTimeToCount = totalTimeToCount;
		this.remainTime = this.totalTimeToCount * 1000;
	}
	
	/**
	 * Initialize an instance of TimeCountDown which could be initialized using {@link #restart()}
	 * 
	 * @param initializer the initializer where this {@link TimeCountDown} was initialized
	 * @param message: the message to be shown during the process
	 * @param totalTimeToCount: total time to count is seconds
	 */
	public TimeCountDown(TimeCountDownInitializer initializer, String message, long totalTimeToCount) {
		this.message = message;
		this.inExecution = true;
		this.totalTimeToCount = totalTimeToCount;
		this.remainTime = this.totalTimeToCount * 1000;
		this.initializer = initializer;
	}
	
	/**
	 * Change the interval for displaying the message in seconds
	 * 
	 * @param intervalForMessage
	 */
	public void setIntervalForMessage(long intervalForMessage) {
		if (this.intervalForMessage > this.totalTimeToCount) {
			throw new ForbiddenOperationException("O intervalo para mensagens nao pode ser superior que o tempo total");
		}
		
		this.intervalForMessage = intervalForMessage;
	}
	
	public long getIntervalForMessage() {
		return intervalForMessage;
	}
	
	/**
	 * @return o valor do atributo {@link #remainTime}
	 */
	public long getRemainTime(String format) {
		if (format == DateAndTimeUtilities.SECOND_FORMAT) {
			return this.remainTime / 1000;
		}
		
		if (format == DateAndTimeUtilities.MINUTE_FORMAT) {
			return this.remainTime / 1000 / 60;
		}
		
		if (format == DateAndTimeUtilities.HOUR_FORMAT) {
			return this.remainTime / 1000 / 60 / 24;
		}
		
		if (format == DateAndTimeUtilities.MILLISECOND_FORMAT) {
			return this.remainTime;
		}
		
		throw new ForbiddenOperationException("Formato nao suportado!");
	}
	
	@Override
	public void run() {
		if (this.intervalForMessage == 0) this.intervalForMessage = 10;
		
		while (this.remainTime > 0) {
			try {
				Thread.sleep(this.intervalForMessage*1000);
				if (this.message != null && !this.message.isEmpty()) {
					logger.info(this.message+ "[Remain" + this.remainTime/1000 + "s] ");
				}
				
				this.remainTime = this.remainTime-this.intervalForMessage*1000;
			} catch (InterruptedException e) {
				return;
			}
		}
		
		inExecution = false;
		timer=null;
		
		if (initializer != null) initializer.onFinish();
		
		logger.info("Wait finihed " + this.message);
	}
	
	public void restart() {
		this.inExecution = true;
		this.remainTime = this.totalTimeToCount * 1000;
		
		//System.out.println(getThreadNamingPattern() +"[%d]");
		
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(getThreadNamingPattern()).execute(this);
	}
	
	/**
	 * @return
	 */
	private String getThreadNamingPattern() {
		return this.initializer != null ? this.initializer.getThreadNamingPattern() : "TimeCountDown[%d]";
	}
	
	/**
	 * Provoca a paragem da execucao do fluxo normal por "timeInSeconds" segundos
	 * 
	 * @param timeInSeconds
	 * @param msg
	 */
	public static TimeCountDown wait(long timeInSeconds, String msg) {
		TimeCountDown timer = new TimeCountDown(msg, timeInSeconds);
		
		timer.restart();
		
		return timer;
	}
	
	/**
	 * Provoca a paragem da execucao do fluxo normal por "timeInSeconds" segundos
	 * 
	 * @param timeInSeconds
	 * @param msg
	 */
	public static TimeCountDown wait(TimeCountDownInitializer initializer, long timeInSeconds, String msg) {
		TimeCountDown timer = new TimeCountDown(initializer, msg, timeInSeconds);
		
		timer.restart();
		
		return timer;
	}
	
	/**
	 * @see Thread#sleep(long)
	 * @param timeInSeconds
	 */
	public static void sleep(int timeInSeconds) {
		try {
			Thread.sleep(1000 * timeInSeconds);
		}
		catch (InterruptedException e) {
			return;
		}
	}
	
	@Override
	public void destruct() {
		inExecution = false;
		this.aborted = true;
	}
}
