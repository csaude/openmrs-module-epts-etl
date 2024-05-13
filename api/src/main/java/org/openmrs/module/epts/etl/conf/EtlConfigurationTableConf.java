package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;

/**
 * Represents any table related to etl configuration. Ex: "table_operation_progress_info",
 * "inconsistence_info"
 */
public class EtlConfigurationTableConf extends AbstractTableConfiguration {
	
	private EtlConfiguration relatedEtlConfiguration;
	
	public EtlConfigurationTableConf(String tableName, EtlConfiguration relatedConf) {
		super.setTableName(tableName);
		
		this.relatedEtlConfiguration = relatedConf;
	}
	
	@Override
	public EtlConfiguration getRelatedSyncConfiguration() {
		return this.relatedEtlConfiguration;
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return this.relatedEtlConfiguration.getMainApp();
	}
	
	@Override
	public Class<? extends EtlDatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		return GenericDatabaseObject.class;
	}
}
