package org.openmrs.module.epts.etl.model.pojo.generic;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
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
	
	TableConfiguration findFullConfiguredConfInAllRelatedTable(String fullTableName, List<Integer> alreadyCheckedObjects);
	
	@JsonIgnore
	default File getPOJOCopiledFilesDirectory() {
		return getRelatedEtlConf().getPOJOCompiledFilesDirectory();
	}
	
	@JsonIgnore
	default File getPOJOSourceFilesDirectory() {
		return getRelatedEtlConf().getPOJOSourceFilesDirectory();
	}
	
	default DBConnectionInfo getSrcConnInfo() {
		return getRelatedEtlConf().getSrcConnInfo();
	}
	
	@JsonIgnore
	default String generateFullPackageName(DBConnectionInfo connInfo) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(connInfo);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return fullPackageName;
	}
	
	@JsonIgnore
	default String getOriginAppLocationCode() {
		return getRelatedEtlConf().getOriginAppLocationCode();
	}
	
	@JsonIgnore
	default String getClasspackage(DBConnectionInfo connInfo) {
		return connInfo.getPojoPackageName();
	}
	
	@JsonIgnore
	default String generateFullClassName(DBConnectionInfo connInfo) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(connInfo);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return utilities.concatStringsWithSeparator(fullPackageName, generateClassName(), ".");
	}
	
	@JsonIgnore
	default File getClassPath() {
		return new File(this.getParentConf().getRelatedEtlConf().getClassPath());
	}
	
	String generateClassName();
	
	EtlDataConfiguration getParentConf();
	
	String getObjectName();
	
	<T extends Field> List<T> getFields();
	
	UniqueKeyInfo getPrimaryKey();
	
	String getSharePkWith();
	
	boolean hasPK();
	
	boolean hasPK(Connection conn) throws DBException;
	
	boolean isMetadata();
	
	DBConnectionInfo getRelatedConnInfo();
	
	void setSyncRecordClass(Class<? extends EtlDatabaseObject> syncRecordClass);
	
	default EtlConfiguration getRelatedEtlConf() {
		return this.getParentConf().getRelatedEtlConf();
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
		return this.getSyncRecordClass(getRelatedConnInfo());
	}
	
	@JsonIgnore
	default Class<? extends EtlDatabaseObject> getSyncRecordClass(DBConnectionInfo connInfo)
	        throws ForbiddenOperationException {
		
		if (getSyncRecordClass() == null) {
			Class<? extends EtlDatabaseObject> syncRecordClass = GenericDatabaseObject.class;
			
			this.setSyncRecordClass(syncRecordClass);
		}
		return getSyncRecordClass();
	}
	
	boolean isDestinationInstallationType();
	
	void generateRecordClass(DBConnectionInfo connInfo, boolean fullClass);
	
	List<ParentTable> getParentRefInfo();
	
	List<ChildTable> getChildRefInfo();
	
	DatabaseObjectLoaderHelper getLoadHealper();
	
	default List<String> getParentRefInfoAsString() {
		List<String> parents = new ArrayList<>();
		
		if (hasParentRefInfo()) {
			for (ParentTable p : this.getParentRefInfo()) {
				parents.add(p.getTableName());
			}
		}
		
		return parents;
	}
	
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
		if (this.hasFields()) {
			for (Field f : this.getFields()) {
				if (f.getName().equals(fieldName)) {
					return f;
				}
			}
		}
		
		return null;
	}
	
	String getAlias();
	
	default List<Field> cloneFields(EtlDatabaseObject originalObject) {
		List<Field> clonedFields = new ArrayList<>();
		
		if (hasFields()) {
			for (Field field : this.getFields()) {
				Field copy = field.createACopy();
				
				if (originalObject != null) {
					try {
						Object value = originalObject.getFieldValue(field.getName());
						copy.setValue(value);
					}
					catch (ForbiddenOperationException e) {}
				}
				
				clonedFields.add(copy);
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
	 * Generates a full dump select from query.
	 * 
	 * @return the generated select dump query
	 */
	String generateSelectFromQuery();
	
}
