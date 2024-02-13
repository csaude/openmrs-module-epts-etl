package org.openmrs.module.epts.etl.model.pojo.generic;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents a data base which can be represented by a Pojo. The {@link PojobleDatabaseObject} can
 * be a database table or a query result
 */
public interface PojobleDatabaseObject {
	
	boolean isFullLoaded();
	
	void fullLoad() throws DBException;
	
	void fullLoad(Connection conn) throws DBException;
	
	File getPOJOSourceFilesDirectory();
	
	String getClasspackage(AppInfo application);
	
	String generateFullPackageName(AppInfo application);
	
	String generateFullClassName(AppInfo application);
	
	String generateClassName();
	
	SyncConfiguration getRelatedSyncConfiguration();
	
	String getObjectName();
	
	List<Field> getFields();
	
	String getPrimaryKey();
	
	String getPrimaryKeyAsClassAtt();
	
	String getSharePkWith();
	
	boolean hasPK();
	
	boolean isNumericColumnType();
	
	List<RefInfo> getParents();
	
	List<RefInfo> getConditionalParents();
	
	boolean isMetadata();
	
	File getPOJOCopiledFilesDirectory();
	
	File getClassPath();
	
	Class<DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException;
	
	boolean isDestinationInstallationType();
	
	void generateRecordClass(AppInfo app, boolean fullClass);
}
