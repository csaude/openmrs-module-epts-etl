package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;

public class GenericTabableConfiguration extends AbstractTableConfiguration {
	
	@Override
	public boolean isGeneric() {
		return true;
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		throw new ForbiddenOperationException("Method forbiden on generic table configuration!");
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		throw new ForbiddenOperationException("Method forbiden on generic table configuration!");
	}
	
	@Override
	public Class<DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		throw new ForbiddenOperationException("Method forbiden on generic table configuration!");
	}
}
