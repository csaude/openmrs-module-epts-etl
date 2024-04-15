package org.openmrs.module.epts.etl.controller.conf;

import java.util.List;

import org.openmrs.module.epts.etl.model.Field;

public class ConditionalParent extends SyncTableConfiguration {
	
	private List<Field> conditionalFields;
	
	public List<Field> getConditionalFields() {
		return conditionalFields;
	}
	
	public void setConditionalFields(List<Field> conditionalFields) {
		this.conditionalFields = conditionalFields;
	}
}
