package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;

public class GenericTableConfiguration extends AbstractTableConfiguration {
	
	private AbstractTableConfiguration relatedTableConf;
	
	public GenericTableConfiguration() {
	}
	
	public GenericTableConfiguration(AbstractTableConfiguration relatedTableConf) {
		this.relatedTableConf = relatedTableConf;
		
		if (this.relatedTableConf.isGeneric()) {
			throw new ForbiddenOperationException(
			        "The generic table Conf cannot be related to another generic configuration");
		}
		
	}
	
	@Override
	public boolean isGeneric() {
		return true;
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		
		if (this.relatedTableConf == null) {
			throw new ForbiddenOperationException("The generic table Conf should have a related to use this method");
			
		}
		
		return this.relatedTableConf.getRelatedAppInfo();
	}
	
	@Override
	public Class<? extends DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		return GenericDatabaseObject.class;
	}
}
