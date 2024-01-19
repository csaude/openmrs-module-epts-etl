package org.openmrs.module.epts.etl.utilities;

import java.util.Date;

import org.apache.log4j.ConsoleAppender;
import org.openmrs.module.epts.etl.Main;
import org.slf4j.event.Level;

public class Logger {
	
	CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date lastLogDate;
	
	org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);
	
	private Level level;
	
	public Logger(org.slf4j.Logger logger, Level level) {
		this.logger = logger;
		this.level = level;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public org.slf4j.Logger getLogger() {
		return logger;
	}
	
	/**
	 * Inteligent logwarn. It only logs if the elapsed time (in seconds) after the last log is
	 * greater that #logInterval
	 * 
	 * @param msg the message to log
	 * @param logInterval the max log interval permited fore repited logs
	 */
	public void logWarn(String msg, double logInterval) {
		double elapsedTime = getLastLogElapsedTime();
		
		if (elapsedTime < 0 || elapsedTime >= logInterval) {
			logWarn(msg);
		}
	}
	
	public void logWarn(String msg) {
		if (Level.WARN.compareTo(level) <= 0) {
			msg = putAdditionalInfoOnLog(msg);
			
			logger.warn(msg);
			
			updateLastLogDate();
		}
	}
	
	public void logInfo(String msg) {
		if (Level.INFO.compareTo(level) <= 0) {
			msg = putAdditionalInfoOnLog(msg);
			
			logger.info(msg);
			//logger.error(msg);
			
			updateLastLogDate();
		}
	}
	
	public void logErr(String msg) {
		if (Level.ERROR.compareTo(level) <= 0) {
			
			msg = putAdditionalInfoOnLog(msg);
			
			logger.error(msg);
			
			updateLastLogDate();
		}
	}
	
	public void logDebug(String msg) {
		if (Level.DEBUG.compareTo(level) <= 0) {
			
			msg = putAdditionalInfoOnLog(msg);
			
			logger.debug(msg);
			//logger.error(msg);
			
			updateLastLogDate();
		}
	}
	
	String putAdditionalInfoOnLog(String msg) {
		return msg += " At: " + utilities.formatDateToDDMMYYYY_HHMISS(utilities.getCurrentDate());
	}
	
	void updateLastLogDate() {
		this.lastLogDate = utilities.getCurrentDate();
	}
	
	double getLastLogElapsedTime() {
		if (this.lastLogDate == null) {
			return -1;
		}
		
		return DateAndTimeUtilities.dateDiff(utilities.getCurrentDate(), this.lastLogDate,
		    DateAndTimeUtilities.SECOND_FORMAT);
	}
}
