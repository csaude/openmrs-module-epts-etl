package org.openmrs.module.epts.etl.model.pojo.generic;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.ParentTable;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents a data base which can be represented by a Pojo. The
 * {@link DatabaseObjectConfiguration} can be a database table or a query result
 */
public interface DatabaseObjectConfiguration {
	
	boolean isFullLoaded();
	
	void fullLoad() throws DBException;
	
	void fullLoad(Connection conn) throws DBException;
	
	File getPOJOSourceFilesDirectory();
	
	String getClasspackage(AppInfo application);
	
	String generateFullPackageName(AppInfo application);
	
	String generateFullClassName(AppInfo application);
	
	String generateClassName();
	
	EtlDataConfiguration getParent();
	
	String getObjectName();
	
	List<Field> getFields();
	
	UniqueKeyInfo getPrimaryKey();
	
	String getSharePkWith();
	
	boolean hasPK();
	
	boolean isMetadata();
	
	File getPOJOCopiledFilesDirectory();
	
	File getClassPath();
	
	Class<? extends DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException;
	
	Class<? extends DatabaseObject> getSyncRecordClass() throws ForbiddenOperationException;
	
	boolean isDestinationInstallationType();
	
	void generateRecordClass(AppInfo app, boolean fullClass);
	
	EtlConfiguration getRelatedSyncConfiguration();
	
	List<ParentTable> getParentRefInfo();
	
	List<ChildTable> getChildRefInfo();
	
	boolean hasDateFields();
	
	DatabaseObjectLoaderHelper getLoadHealper();
	
	List<Field> cloneFields();
	
	default boolean containsField(String fieldName) {
		for (Field f : this.getFields()) {
			if (f.getName().equals(fieldName)) {
				return true;
			}
		}
		
		return false;
	}
	
	String getAlias();
}
