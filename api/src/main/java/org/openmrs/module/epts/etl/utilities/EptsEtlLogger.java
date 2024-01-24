package org.openmrs.module.epts.etl.utilities;

import java.util.Date;

import org.openmrs.module.epts.etl.Main;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class EptsEtlLogger {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date lastLogDate;
	
	Logger logger = LoggerFactory.getLogger(Main.class);
	
	private Level level;
	
	public <T> EptsEtlLogger(Class<T> clazz) {
		this.logger = LoggerFactory.getLogger(clazz);
		this.level = determineLogLevel();
	}
	
	public <T> EptsEtlLogger(Logger logger) {
		this.logger = logger;
		this.level = determineLogLevel();
	}
	
	public  static <T>   EptsEtlLogger getLogger(Class<T> clazz) {
		return new EptsEtlLogger(clazz);
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public static Level determineLogLevel() {
		String log = System.getProperty("log.level");
		
		if (!utilities.stringHasValue(log))
			return Level.INFO;
		
		if (log.equals("DEBUG"))
			return Level.DEBUG;
		if (log.equals("INFO"))
			return Level.INFO;
		if (log.equals("WARN"))
			return Level.WARN;
		if (log.equals("ERROR"))
			return Level.ERROR;
		
		throw new ForbiddenOperationException("Unsupported Log Level [" + log + "]");
	}
	
	/**
	 * Inteligent logwarn. It only logs if the elapsed time (in seconds) after the last log is
	 * greater that #logInterval
	 * 
	 * @param msg the message to log
	 * @param logInterval the max log interval permited fore repited logs
	 */
	public void warn(String msg, double logInterval) {
		double elapsedTime = getLastLogElapsedTime();
		
		if (elapsedTime < 0 || elapsedTime >= logInterval) {
			warn(msg);
		}
	}
	
	public void warn(String msg) {
		if (Level.WARN.compareTo(level) <= 0) {
			msg = putAdditionalInfoOnLog(msg);
			
			logger.warn(msg);
			
			updateLastLogDate();
		}
	}
	
	public void info(String msg) {
		if (Level.INFO.compareTo(level) <= 0) {
			msg = putAdditionalInfoOnLog(msg);
			
			logger.info(msg);
			//logger.error(msg);
			
			updateLastLogDate();
		}
	}
	
	public void error(String msg) {
		if (Level.ERROR.compareTo(level) <= 0) {
			
			msg = putAdditionalInfoOnLog(msg);
			
			logger.error(msg);
			
			updateLastLogDate();
		}
	}
	
	public void debug(String msg) {
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
