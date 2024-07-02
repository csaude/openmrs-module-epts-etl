/**
 * TimeCountDown
 * Est� classe � respons�vel por um mecanismo de contagem decrescente do tempo. Quando o tempo se esgota, � rebentada uma excep��o
 * 
 */
package org.openmrs.module.epts.etl.utilities.concurrent;


import java.io.Serializable;
import java.util.Date;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.parseToCSV;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.FuncoesGenericas;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TimeController implements Runnable, Serializable{
	private static final long serialVersionUID = 1L;
	Thread timer = null;
	private long segundos;
	private long minutos;
	private long horas;
	private long dias;
	
	private Date startTime;
	
	private static final parseToCSV utilities = parseToCSV.getInstance();
	
	public TimeController(){
		segundos = 0;
		minutos = 0;
		horas = 0;
		dias=0;
		
		this.startTime = DateAndTimeUtilities.getCurrentDate();
	}
	
	/**
	 * 
	 * @param startTime
	 * @param elapsedTime in minutes
	 */
	public TimeController(Date startTime, double elapsedTime){
		int elapsedTimeInSeconds = (int) (60 * elapsedTime);
		
		Date currTime = DateAndTimeUtilities.addSecondsToDate(startTime, elapsedTimeInSeconds);
		
		dias = (long) DateAndTimeUtilities.dateDiff(currTime, startTime, DateAndTimeUtilities.DAY_FORMAT);
		
		horas = (long) (DateAndTimeUtilities.dateDiff(currTime, startTime, DateAndTimeUtilities.HOUR_FORMAT)%24);
		
		minutos = (long) (DateAndTimeUtilities.dateDiff(currTime, startTime, DateAndTimeUtilities.MINUTE_FORMAT)%60);
		
		segundos = (long) (DateAndTimeUtilities.dateDiff(currTime, startTime, DateAndTimeUtilities.SECOND_FORMAT)%60);
		
		this.startTime = startTime;
	}
	
	public static TimeController retrieveTimer(Date startDate, double elapsedTime) {
		TimeController timer = new TimeController(startDate, elapsedTime);
		
		return timer;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	@Override
	public void run() {
		try {
			while(timer != null){
				Thread.sleep(1000);
				
				segundos++;
				
				if (segundos == 60) {
					minutos++;
					segundos=0;
				}
				if (minutos== 60) {
					horas++;
					minutos=0;
				}
				if (horas== 24) {
					dias++;
					horas=0;
				}
				
				//System.out.println(horas+":"+minutos+":"+segundos);
			}
		} catch (InterruptedException e) {
	}	
	}

	public static final String DURACAO_IN_MINUTES = "MINUTES";
	public static final String DURACAO_IN_SECONDS= "SECONDS";
	public static final String DURACAO_IN_HOURS = "HOURS";
	public static final String DURACAO_IN_DAYS = "DAYS";
	
	public double getDuration(String durationType){
		if (!utilities.isStringIn(durationType, DURACAO_IN_MINUTES, DURACAO_IN_SECONDS, DURACAO_IN_HOURS, DURACAO_IN_DAYS)){
			throw new ForbiddenOperationException("Unsupported type!");
		}
		
		double diasInSeconds = this.dias*24*60*60;
		double hoursInSecond = this.horas*60*60;
		double minutesInSeconds = this.minutos*60;
		
		double totalInSeconds = diasInSeconds+hoursInSecond+minutesInSeconds+this.segundos;
		if (durationType.equals(DURACAO_IN_SECONDS)) return totalInSeconds;
		if (durationType.equals(DURACAO_IN_MINUTES)) return totalInSeconds/60;
		if (durationType.equals(DURACAO_IN_HOURS)) return totalInSeconds/60/60;
		 return totalInSeconds/60/60/24;
	}
	
	@JsonIgnore
	@Override
	public String toString() {
		return FuncoesGenericas.garantirXCaracterOnNumber(horas, 2)+":"+FuncoesGenericas.garantirXCaracterOnNumber(minutos, 2)+":"+FuncoesGenericas.garantirXCaracterOnNumber(segundos, 2);
	}
	public void start(){
		if (timer == null){
			timer = new Thread(this);
			
			if (this.startTime == null) this.startTime = DateAndTimeUtilities.getCurrentDate();
			
			timer.start();
			
		}
		else throw new ForbiddenOperationException("The timer is already running!!!");
	}
	
	public void restar(){
		timer = null;
		start();
	}

	public void stop(){
	   if (timer != null){
		   timer.interrupt();
		   timer = null;
	   }
	}

	public static double computeElapsedTime(Date startTime, Date stopTime) {
		return DateAndTimeUtilities.dateDiff(stopTime, startTime, DateAndTimeUtilities.MINUTE_FORMAT);
	}

}
