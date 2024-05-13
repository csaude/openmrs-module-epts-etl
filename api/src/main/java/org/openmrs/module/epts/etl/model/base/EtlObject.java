package org.openmrs.module.epts.etl.model.base;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public interface EtlObject extends VO {
	
	CommonUtilities utils = CommonUtilities.getInstance();
	
	default String getObjectName() {
		return this.getClass().getName();
	}
}
