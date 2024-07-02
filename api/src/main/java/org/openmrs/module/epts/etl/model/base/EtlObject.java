package org.openmrs.module.epts.etl.model.base;

import org.openmrs.module.epts.etl.utilities.parseToCSV;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface EtlObject extends VO {
	
	parseToCSV utils = parseToCSV.getInstance();
	
	@JsonIgnore
	default String getObjectName() {
		return this.getClass().getName();
	}
}
