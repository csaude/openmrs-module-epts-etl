package org.openmrs.module.epts.etl.model.pojo.generic;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a data base which can be represented by a Pojo. The
 * {@link DatabaseObjectConfiguration} can be a database table or a query result
 */
public interface DatabaseObjectConfiguration extends EtlDataConfiguration {
	
	boolean isFullLoaded();
	
	void fullLoad() throws DBException;
	
	void fullLoad(Connection conn) throws DBException;
	
	@JsonIgnore
	default File getPOJOCopiledFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOCompiledFilesDirectory();
	}
	
	@JsonIgnore
	default File getPOJOSourceFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOSourceFilesDirectory();
	}
	
	default AppInfo getMainApp() {
		return getRelatedSyncConfiguration().getMainApp();
	}
	
	@JsonIgnore
	default String generateFullPackageName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return fullPackageName;
	}
	
	@JsonIgnore
	default String getOriginAppLocationCode() {
		return getRelatedSyncConfiguration().getOriginAppLocationCode();
	}
	
	@JsonIgnore
	default String getClasspackage(AppInfo application) {
		return application.getPojoPackageName();
	}
	
	@JsonIgnore
	default String generateFullClassName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return utilities.concatStringsWithSeparator(fullPackageName, generateClassName(), ".");
	}
	
	@JsonIgnore
	default File getClassPath() {
		return new File(this.getParentConf().getRelatedSyncConfiguration().getClassPath());
	}
	
	String generateClassName();
	
	EtlDataConfiguration getParentConf();
	
	String getObjectName();
	
	List<Field> getFields();
	
	UniqueKeyInfo getPrimaryKey();
	
	String getSharePkWith();
	
	@JsonIgnore
	default boolean hasPK() {
		return getPrimaryKey() != null;
	}
	
	boolean hasPK(Connection conn);
	
	boolean isMetadata();
	
	AppInfo getRelatedAppInfo();
	
	void setSyncRecordClass(Class<? extends EtlDatabaseObject> syncRecordClass);
	
	default EtlConfiguration getRelatedSyncConfiguration() {
		return this.getParentConf().getRelatedSyncConfiguration();
	}
	
	default boolean hasDateFields() {
		for (Field t : this.getFields()) {
			if (t.isDateField()) {
				return true;
			}
		}
		
		return false;
	}
	
	@JsonIgnore
	default Class<? extends EtlDatabaseObject> getSyncRecordClass() throws ForbiddenOperationException {
		return this.getSyncRecordClass(getRelatedAppInfo());
	}
	
	@JsonIgnore
	default Class<? extends EtlDatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		
		if (getSyncRecordClass() == null) {
			Class<? extends EtlDatabaseObject> syncRecordClass = GenericDatabaseObject.class;
			
			this.setSyncRecordClass(syncRecordClass);
		}
		return getSyncRecordClass();
	}
	
	boolean isDestinationInstallationType();
	
	void generateRecordClass(AppInfo app, boolean fullClass);
	
	List<ParentTable> getParentRefInfo();
	
	List<ChildTable> getChildRefInfo();
	
	DatabaseObjectLoaderHelper getLoadHealper();
	
	default boolean hasParentRefInfo() {
		return utilities.arrayHasElement(this.getParentRefInfo());
	}
	
	default boolean hasChildRefInfo() {
		return isMustLoadChildrenInfo() && utilities.arrayHasElement(this.getChildRefInfo());
	}
	
	boolean isMustLoadChildrenInfo();
	
	default boolean containsField(String fieldName) {
		for (Field f : this.getFields()) {
			if (f.getName().equals(fieldName)) {
				return true;
			}
		}
		
		return false;
	}
	
	default Field getField(String fieldName) {
		for (Field f : this.getFields()) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}
		
		return null;
	}
	
	String getAlias();
	
	default List<Field> cloneFields() {
		List<Field> clonedFields = new ArrayList<>();
		
		if (hasFields()) {
			for (Field field : this.getFields()) {
				clonedFields.add(field.createACopy());
			}
		}
		
		return clonedFields;
	}
	
	default boolean hasFields() {
		return utilities.arrayHasElement(this.getFields());
	}
	
	default boolean hasCompositeKey() {
		return this.getPrimaryKey() != null && this.getPrimaryKey().isCompositeKey();
	}
	
	/**
	 * Generates a full sql select from query.
	 * 
	 * @return the generated select sql query
	 */
	String generateSelectFromQuery();
	
}
