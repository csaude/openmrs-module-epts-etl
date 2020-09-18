/**
 * 
 */
package org.openmrs.module.eptssync.utilities.concurrent;

/**
 * @author jpboane
 *
 */
public interface TimeCountDownInitializer {
	public void onFinish();
	
	public String getThreadNamingPattern();
}
