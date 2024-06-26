package org.openmrs.module.epts.etl.model.base;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface EtlObject extends VO {
	
	CommonUtilities utils = CommonUtilities.getInstance();
	
	@JsonIgnore
	default String getObjectName() {
		return this.getClass().getName();
	}
}
