package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public interface SyncDataSource extends DatabaseObjectConfiguration {
	public static final String MAIN_DATA_SRC = "main_ds";
	
	SrcConf getRelatedSrcConf();
	
	void setRelatedSrcConf(SrcConf relatedSrcConf);
	
	DatabaseObject loadRelatedSrcObject(DatabaseObject mainObject, Connection conn, AppInfo srcAppInfo) throws DBException;
	
	String getName();
	
	/**
	 * Tels weather this source is mandatory or not. If it is required and it returns an empty
	 * result then the main record will be ignored, i.e will not be loaded to destination table
	 * 
	 * @return true if this data source is required or false if not
	 */
	boolean isRequired();
	
	Class<? extends DatabaseObject> getSyncRecordClass(AppInfo srcApplication) throws ForbiddenOperationException;
	
	Class<? extends DatabaseObject> getSyncRecordClass() throws ForbiddenOperationException;
}
