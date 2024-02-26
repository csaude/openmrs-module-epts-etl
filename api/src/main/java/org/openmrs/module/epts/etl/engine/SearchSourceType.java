package org.openmrs.module.epts.etl.engine;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;

/**
 * On an {@link EtlConfiguration} Specify the search source type. The type could be SOURCE OR
 * TARGET. SOURCE tells that the search will be performed on source table, and TARGET tell that the
 * search will be performed on target table
 */
public enum SearchSourceType {
	
	/**
	 * Sorce table
	 */
	SOURCE,
	/**
	 * Target or Destination table
	 */
	TARGET;
	
	public boolean isSource() {
		return this.equals(SOURCE);
	}
	
	public boolean isTarget() {
		return this.equals(TARGET);
	}
}
