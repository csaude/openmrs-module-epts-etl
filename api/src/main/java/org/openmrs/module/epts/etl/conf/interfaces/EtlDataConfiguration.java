package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;

public interface EtlDataConfiguration extends BaseConfiguration {
	
	EtlConfiguration getRelatedEtlConf();
	
	void setRelatedSyncConfiguration(EtlConfiguration relatedSyncConfiguration);
	
	default DBConnectionInfo getSrcConnInfo() {
		return this.getRelatedEtlConf().getSrcConnInfo();
	}
	
}
