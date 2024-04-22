package org.openmrs.module.epts.etl.model.base;

/**
 * Allow the load of additional data to a {@link VO} when it is loaded from database
 */
public interface VOLoaderHelper {
	
	/**
	 * Load the additional data to the vo object passed by parameter before the load of object from
	 * database
	 * 
	 * @param vo the vo object to load data to
	 */
	void beforeLoad(VO vo);
	
	/**
	 * Load the additional data to the vo object passed by parameter after the load of object from
	 * database
	 * 
	 * @param vo the vo object to load data to
	 */
	void afterLoad(VO vo);
}
