package org.openmrs.module.epts.etl.conf.datasource;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

public class ObjectDataSourceField extends DataSourceField {
	
	private static final long serialVersionUID = 6096484845018880232L;
	
	@Override
	public void setParent(EtlDataSource parent) {
		if (parent instanceof ObjectDataSource) {
			super.setParent(parent);
		} else {
			throw new ForbiddenOperationException("Only a ObjectDataSource is accepted for this method");
		}
	}
	
	@Override
	public ObjectDataSource getParent() {
		return (ObjectDataSource) super.getParent();
	}
}
