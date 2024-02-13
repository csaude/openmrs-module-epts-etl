package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.SyncExtraDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.PojobleDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public interface SyncDataSource extends PojobleDatabaseObject {
	
	SyncExtraDataSource getRelatedSrcExtraDataSrc();
	
	void setRelatedSrcExtraDataSrc(SyncExtraDataSource relatedSrcExtraDataSrc);
	
	DatabaseObject loadRelatedSrcObject(DatabaseObject mainObject, Connection conn, AppInfo srcAppInfo) throws DBException;
	
	String getName();
	
	Class<DatabaseObject> getSyncRecordClass(AppInfo srcApplication) throws ForbiddenOperationException;
	
}
