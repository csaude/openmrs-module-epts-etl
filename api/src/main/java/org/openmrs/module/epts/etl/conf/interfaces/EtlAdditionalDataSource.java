package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public interface EtlAdditionalDataSource extends EtlDataSource {
	
	SrcConf getRelatedSrcConf();
	
	void setRelatedSrcConf(SrcConf relatedSrcConf);
	
	EtlDatabaseObject loadRelatedSrcObject(List<EtlDatabaseObject> avaliableSrcObjects, Connection conn) throws DBException;
	
	/**
	 * Tells weather this source is mandatory or not. If it is required and it returns an empty
	 * result then the main dstRecord will be ignored, i.e will not be loaded to destination table
	 * 
	 * @return true if this data source is required or false if not
	 */
	boolean isRequired();
	
	default EtlDatabaseObject newInstance() {
		try {
			return getSyncRecordClass().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | ForbiddenOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	boolean allowMultipleSrcObjects();
}
