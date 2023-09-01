package org.openmrs.module.eptssync.utilities;

import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.openmrs.module.eptssync.controller.ProcessController;

public class Logger {
	
	CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date lastLogDate;
	
	private Log logger;
	
	private Level level;
	
	public Logger(Log logger, Level level) {
		this.logger = logger;
		this.level = level;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Log getLogger() {
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
		if (level.intValue() <= Level.WARNING.intValue()) {
			logger.warn(msg + " At: " + utilities.formatDateToDDMMYYYY_HHMISS(utilities.getCurrentDate()));
			
			updateLastLogDate();
		}
	}
	
	public void logInfo(String msg) {
		if (level.intValue() <= Level.INFO.intValue()) {
			logger.info(msg + " At: " + utilities.formatDateToDDMMYYYY_HHMISS(utilities.getCurrentDate()));
			
			updateLastLogDate();
		}
	}
	
	public void logErr(String msg) {
		if (level.intValue() <= Level.SEVERE.intValue()) {
			logger.error(msg + " At: " + utilities.formatDateToDDMMYYYY_HHMISS(utilities.getCurrentDate()));
			
			updateLastLogDate();
		}
	}
	
	public void logDebug(String msg) {
		if (level.intValue() <= Level.FINE.intValue()) {
			logger.debug(msg + " At: " + utilities.formatDateToDDMMYYYY_HHMISS(utilities.getCurrentDate()));
			
			updateLastLogDate();
		}
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
