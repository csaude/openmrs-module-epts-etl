package org.openmrs.module.epts.etl.conf;

import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractTableConfiguration extends AbstractEtlDataConfiguration implements Comparable<AbstractTableConfiguration>, TableConfiguration {
	
	private List<String> ignorableFields;
	
	private String tableName;
	
	private String tableAlias;
	
	private List<ParentTableImpl> parents;
	
	private List<? extends ParentTable> parentRefInfo;
	
	private List<ChildTable> childRefInfo;
	
	private Class<? extends EtlDatabaseObject> syncRecordClass;
	
	private AbstractEtlDataConfiguration parentConf;
	
	private PrimaryKey primaryKey;
	
	private String sharePkWith;
	
	private boolean metadata;
	
	protected boolean fullLoaded;
	
	private boolean removeForbidden;
	
	/**
	 * List the field to observe when sync by date (ex: date_created, date_update, etc)
	 */
	private List<String> observationDateFields;
	
	private List<UniqueKeyInfo> uniqueKeys;
	
	private List<Field> fields;
	
	/**
	 * When merge existing records, the incoming record will win if the listed fields have the
	 * specified values. Note that, for the outer list the join condition will be "OR" and for the
	 * inner list the join condition will be "AND"
	 */
	private List<List<Field>> winningRecordFieldsInfo;
	
	private boolean autoIncrementId;
	
	private boolean disabled;
	
	private boolean mustLoadChildrenInfo;
	
	private String extraConditionForExtract;
	
	private String insertSQLWithObjectId;
	
	private String insertSQLWithoutObjectId;
	
	private String updateSql;
	
	private DatabaseObjectLoaderHelper loadHealper;
	
	private boolean allRelatedTablesFullLoaded;
	
	private String schema;
	
	public AbstractTableConfiguration() {
		this.loadHealper = new DatabaseObjectLoaderHelper(this);
	}
	
	public AbstractTableConfiguration(String tableName) {
		this();
		
		this.tableName = tableName;
	}
	
	@Override
	public String getSchema() {
		return schema;
	}
	
	@Override
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	@Override
	public List<String> getIgnorableFields() {
		return ignorableFields;
	}
	
	@Override
	public void setIgnorableFields(List<String> ignorableFields) {
		this.ignorableFields = ignorableFields;
	}
	
	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}
	
	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public boolean isAllRelatedTablesFullLoaded() {
		return allRelatedTablesFullLoaded;
	}
	
	public void setAllRelatedTablesFullLoaded(boolean allRelatedTablesFullLoaded) {
		this.allRelatedTablesFullLoaded = allRelatedTablesFullLoaded;
	}
	
	public Class<? extends EtlDatabaseObject> getSyncRecordClass() {
		
		if (syncRecordClass == null) {
			this.syncRecordClass = GenericDatabaseObject.class;
		}
		
		return syncRecordClass;
	}
	
	public void setFullLoaded(boolean fullLoaded) {
		this.fullLoaded = fullLoaded;
	}
	
	public void setInsertSQLWithObjectId(String insertSQLWithObjectId) {
		this.insertSQLWithObjectId = insertSQLWithObjectId;
	}
	
	public void setInsertSQLWithoutObjectId(String insertSQLWithoutObjectId) {
		this.insertSQLWithoutObjectId = insertSQLWithoutObjectId;
	}
	
	@Override
	public void setUpdateSql(String updateSQL) {
		this.updateSql = updateSQL;
	}
	
	public String getTableAlias() {
		return tableAlias;
	}
	
	@Override
	public String getAlias() {
		return getTableAlias();
	}
	
	public void setTableAlias(String tableAlias) {
		if (hasAlias() && !tableAlias.equals(this.getTableAlias()))
			throw new ForbiddenOperationException("This table has already an alias and change is forbidden!");
		
		this.tableAlias = tableAlias;
	}
	
	@Override
	public DatabaseObjectLoaderHelper getLoadHealper() {
		return this.loadHealper;
	}
	
	public void setLoadHealper(DatabaseObjectLoaderHelper loadHealper) {
		this.loadHealper = loadHealper;
	}
	
	public String getInsertSQLWithObjectId() {
		return insertSQLWithObjectId;
	}
	
	public String getInsertSQLWithoutObjectId() {
		return insertSQLWithoutObjectId;
	}
	
	public String getUpdateSql() {
		return updateSql;
	}
	
	public String getExtraConditionForExtract() {
		return extraConditionForExtract;
	}
	
	public void setExtraConditionForExtract(String extraConditionForExtract) {
		this.extraConditionForExtract = extraConditionForExtract;
	}
	
	public boolean isMustLoadChildrenInfo() {
		return mustLoadChildrenInfo;
	}
	
	public void setMustLoadChildrenInfo(boolean mustLoadChildrenInfo) {
		this.mustLoadChildrenInfo = mustLoadChildrenInfo;
	}
	
	public boolean isAutoIncrementId() {
		return autoIncrementId;
	}
	
	public void setAutoIncrementId(boolean autoIncrementId) {
		this.autoIncrementId = autoIncrementId;
	}
	
	public List<List<Field>> getWinningRecordFieldsInfo() {
		return winningRecordFieldsInfo;
	}
	
	public void setWinningRecordFieldsInfo(List<List<Field>> winningRecordFieldsInfo) {
		this.winningRecordFieldsInfo = winningRecordFieldsInfo;
	}
	
	public boolean hasWinningRecordsInfo() {
		return this.winningRecordFieldsInfo != null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ParentTable> getParentRefInfo() {
		return (List<ParentTable>) parentRefInfo;
	}
	
	public void setParentRefInfo(List<? extends ParentTable> parentRefInfo) {
		this.parentRefInfo = parentRefInfo;
	}
	
	public void setChildRefInfo(List<ChildTable> childRefInfo) {
		this.childRefInfo = childRefInfo;
	}
	
	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	
	public List<UniqueKeyInfo> getUniqueKeys() {
		return uniqueKeys;
	}
	
	public void setUniqueKeys(List<UniqueKeyInfo> uniqueKeys) {
		this.uniqueKeys = uniqueKeys;
	}
	
	public List<String> getObservationDateFields() {
		return observationDateFields;
	}
	
	public void setObservationDateFields(List<String> observationDateFields) {
		this.observationDateFields = observationDateFields;
	}
	
	public boolean isRemoveForbidden() {
		return removeForbidden;
	}
	
	public void setRemoveForbidden(boolean removeForbidden) {
		this.removeForbidden = removeForbidden;
	}
	
	@Override
	public List<ChildTable> getChildRefInfo() {
		if (!this.mustLoadChildrenInfo) {
			throw new ForbiddenOperationException(
			        "The table configuration is set to not load Children. Please change configuration if you what to access Children ifo.");
		}
		
		return this.childRefInfo;
	}
	
	public List<ParentTable> getParents() {
		return utilities.parseList(parents, ParentTable.class);
	}
	
	@SuppressWarnings("unchecked")
	public void setParents(List<? extends ParentTable> parents) {
		this.parents = (List<ParentTableImpl>) parents;
	}
	
	public String getSharePkWith() {
		return sharePkWith;
	}
	
	public void setSharePkWith(String sharePkWith) {
		this.sharePkWith = sharePkWith;
	}
	
	@Override
	public AbstractEtlDataConfiguration getParentConf() {
		return parentConf;
	}
	
	public void setParentConf(EtlDataConfiguration parentConf) {
		this.parentConf = (AbstractEtlDataConfiguration) parentConf;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void setSyncRecordClass(Class<? extends EtlDatabaseObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}
	
	public boolean isMetadata() {
		return metadata;
	}
	
	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}
	
	@JsonIgnore
	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String toString = "Table [" + getFullTableDescription();
		
		toString += hasPK() ? ", pk: " + this.getPrimaryKey() : "";
		
		toString += "]";
	
		return toString;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractTableConfiguration))
			return false;
		
		return this.getTableName().equalsIgnoreCase(((AbstractTableConfiguration) obj).getTableName());
	}
	
	@Override
	public int compareTo(AbstractTableConfiguration o) {
		if (this.equals(o))
			return 0;
		
		return this.tableName.compareTo(o.getTableName());
	}
	
}
