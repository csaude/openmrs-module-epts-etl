/**
 * 
 */
package org.openmrs.module.epts.etl.utilities.concurrent;

/**
 * @author jpboane
 *
 */
public interface TimeCountDownInitializer {
	public void onFinish();
	
	public String getThreadNamingPattern();
}
