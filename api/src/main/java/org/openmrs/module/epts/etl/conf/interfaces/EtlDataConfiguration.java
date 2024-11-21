package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;

public interface EtlDataConfiguration extends BaseConfiguration {
	
	EtlConfiguration getRelatedEtlConf();
	
	EtlDataConfiguration getParentConf();
	
	void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration);
	
	default DBConnectionInfo getSrcConnInfo() {
		return this.getRelatedEtlConf().getSrcConnInfo();
	}
	
	void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc);
	
}
