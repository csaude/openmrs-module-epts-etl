package org.openmrs.module.epts.etl.model.pojo.generic;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

public class AuxEtlDataBaseObject extends GenericDatabaseObject {
	
	private String param;
	
	public AuxEtlDataBaseObject() {
	}
	
	public void setParam(String param) {
		this.param = param;
	}
	
	public String getParam() {
		return param;
	}
	
	@Override
	public Object getFieldValue(String fieldName) {
		if (fieldName.equals("param")) {
			return this.param;
		}
		
		throw new ForbiddenOperationException("Uknown field " + param);
	}
}
