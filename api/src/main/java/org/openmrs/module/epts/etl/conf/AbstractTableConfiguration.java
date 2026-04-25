package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.PreparedQuery;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.AutoIncrementHandlingType;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.EtlConfException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractTableConfiguration extends AbstractEtlDataConfiguration implements Comparable<AbstractTableConfiguration>, TableConfiguration {
	
	private List<String> ignorableFields;
	
	private String tableName;
	
	private String tableAlias;
	
	private List<ParentTable> parents;
	
	private List<? extends ParentTable> parentRefInfo;
	
	private List<ChildTable> childRefInfo;
	
	private Class<? extends EtlDatabaseObject> syncRecordClass;
	
	private EtlDataConfiguration parentConf;
	
	private PrimaryKey primaryKey;
	
	private String sharePkWith;
	
	private Boolean metadata;
	
	protected Boolean fullLoaded;
	
	private Boolean removeForbidden;
	
	/**
	 * List the field to observe when sync by date (ex: date_created, date_update, etc)
	 */
	private List<String> observationDateFields;
	
	private List<UniqueKeyInfo> uniqueKeys;
	
	/**
	 * If present, the value from this field will be mapped as a primary key for all tables under
	 * this configuration that don't have a primary key but have a field with name matching this
	 * field.
	 */
	private String manualMapPrimaryKeyOnField;
	
	private List<Field> fields;
	
	private PreparedQuery defaultPreparedQuery;
	
	/**
	 * When merge existing records, the incoming dstRecord will win if the listed fields have the
	 * specified values. Note that, for the outer list the join condition will be "OR" and for the
	 * inner list the join condition will be "AND"
	 */
	private List<List<Field>> winningRecordFieldsInfo;
	
	private Boolean autoIncrementId;
	
	private Boolean disabled;
	
	private Boolean mustLoadChildrenInfo;
	
	private String extraConditionForExtract;
	
	private String insertSQLWithObjectId;
	
	private String insertSQLWithoutObjectId;
	
	private String updateSql;
	
	protected DatabaseObjectLoaderHelper loadHealper;
	
	private Boolean allRelatedTablesFullLoaded;
	
	private String schema;
	
	private Boolean usingManualDefinedAlias;
	
	private String insertSQLQuestionMarksWithObjectId;
	
	private String insertSQLQuestionMarksWithoutObjectId;
	
	private Boolean includePrimaryKeyOnInsert;
	
	private Boolean uniqueKeyInfoLoaded;
	
	private Boolean primaryKeyInfoLoaded;
	
	private Boolean fieldsLoaded;
	
	private Boolean tableNameInfoLoaded;
	
	private Boolean parentsLoaded;
	
	private ConflictResolutionType onConflict;
	
	private Boolean useMysqlInsertIgnore;
	
	private Boolean ignoreMissingParameters;
	
	private AutoIncrementHandlingType autoIncrementHandlingType;
	
	private Integer primaryKeyInitialIncrementValue;
	
	private List<String> dynamicElements;
	
	public AbstractTableConfiguration() {
		this.loadHealper = new DatabaseObjectLoaderHelper(this);
	}
	
	public AbstractTableConfiguration(String tableName) {
		this();
		
		this.tableName = tableName;
	}
	
	@Override
	public List<String> getDynamicElements() {
		return dynamicElements;
	}
	
	public void setDynamicElements(List<String> dynamicElements) {
		this.dynamicElements = dynamicElements;
	}
	
	@Override
	public void tryToLoadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists {
		
		TableConfiguration.super.tryToLoadSchemaInfo(schemaInfoSrc, conn);
		
		if (this.isTableNameInfoLoaded())
			return;
		
		if (this.getSchema() == null) {
			this.setSchema(DBUtilities.determineSchemaName(conn));
		}
		
		Boolean exists = DBUtilities.isTableExists(this.getSchema(), this.getTableName(), conn);
		
		if (!exists)
			throw new DatabaseResourceDoesNotExists(this.generateFullTableName(conn));
		
		this.setTableNameInfoLoaded(true);
	}
	
	@Override
	public void fullLoad(Connection conn) throws DBException {
		this.tryToLoadDumpScriptContentToField("extraConditionForExtract", this.retrieveNearestTemplate(), conn);
		
		TableConfiguration.super.fullLoad(conn);
	}
	
	@Override
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		
		if (hasExtraConditionForExtract()) {
			if (!SQLUtilities.isValidSelectSqlQuery("select * from where " + this.getExtraConditionForExtract(), null)) {
				throw new EtlConfException("Invalid extraConditionForExtract  \n" + this.getExtraConditionForExtract());
			}
		}
		
		if (this.loadHealper == null) {
			this.loadHealper = new DatabaseObjectLoaderHelper(this);
		}
		if (this.onConflict == null) {
			this.onConflict = ConflictResolutionType.MAKE_YOUR_DECISION;
		}
	}
	
	@Override
	public AutoIncrementHandlingType getAutoIncrementHandlingType() {
		return autoIncrementHandlingType;
	}
	
	@Override
	public Integer getPrimaryKeyInitialIncrementValue() {
		return primaryKeyInitialIncrementValue;
	}
	
	@Override
	public void setPrimaryKeyInitialIncrementValue(Integer primaryKeyInitialIncrementValue) {
		this.primaryKeyInitialIncrementValue = primaryKeyInitialIncrementValue;
	}
	
	@Override
	public void setAutoIncrementHandlingType(AutoIncrementHandlingType autoIncrementHandlingType) {
		this.autoIncrementHandlingType = autoIncrementHandlingType;
	}
	
	public Boolean isIgnoreMissingParameters() {
		return isTrue(ignoreMissingParameters);
	}
	
	public Boolean ignoreMissingParameters() {
		return isTrue(ignoreMissingParameters);
	}
	
	public void setIgnoreMissingParameters(Boolean ignoreMissingParameters) {
		this.ignoreMissingParameters = ignoreMissingParameters;
	}
	
	public String getManualMapPrimaryKeyOnField() {
		return manualMapPrimaryKeyOnField;
	}
	
	public void setManualMapPrimaryKeyOnField(String manualMapPrimaryKeyOnField) {
		this.manualMapPrimaryKeyOnField = manualMapPrimaryKeyOnField;
	}
	
	public Boolean isUseMysqlInsertIgnore() {
		return isTrue(useMysqlInsertIgnore);
	}
	
	@Override
	public Boolean useMysqlInsertIgnore() {
		return isUseMysqlInsertIgnore();
	}
	
	public void setUseMysqlInsertIgnore(Boolean useMysqlInsertIgnore) {
		this.useMysqlInsertIgnore = useMysqlInsertIgnore;
	}
	
	@Override
	public Boolean isParentsLoaded() {
		return isTrue(parentsLoaded);
	}
	
	@Override
	public void setParentsLoaded(Boolean parentsLoaded) {
		this.parentsLoaded = parentsLoaded;
	}
	
	@Override
	public Boolean isFieldsLoaded() {
		return isTrue(fieldsLoaded);
	}
	
	@Override
	public void setFieldsLoaded(Boolean fieldsLoaded) {
		this.fieldsLoaded = fieldsLoaded;
	}
	
	@Override
	public Boolean isTableNameInfoLoaded() {
		return isTrue(tableNameInfoLoaded);
	}
	
	@Override
	public void setTableNameInfoLoaded(Boolean tableNameInfoLoaded) {
		this.tableNameInfoLoaded = tableNameInfoLoaded;
	}
	
	@Override
	public Boolean isPrimaryKeyInfoLoaded() {
		return isTrue(primaryKeyInfoLoaded);
	}
	
	@Override
	public void setPrimaryKeyInfoLoaded(Boolean primaryKeyInfoLoaded) {
		this.primaryKeyInfoLoaded = primaryKeyInfoLoaded;
	}
	
	@Override
	public Boolean isUniqueKeyInfoLoaded() {
		return isTrue(uniqueKeyInfoLoaded);
	}
	
	@Override
	public void setUniqueKeyInfoLoaded(Boolean uniqueKeyInfoLoaded) {
		this.uniqueKeyInfoLoaded = uniqueKeyInfoLoaded;
	}
	
	@Override
	public Boolean includePrimaryKeyOnInsert() {
		return isTrue(includePrimaryKeyOnInsert);
	}
	
	@Override
	public void setIncludePrimaryKeyOnInsert(Boolean includePrimaryKeyOnInsert) {
		this.includePrimaryKeyOnInsert = includePrimaryKeyOnInsert;
	}
	
	public Boolean isIncludePrimaryKeyOnInsert() {
		return isTrue(includePrimaryKeyOnInsert);
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithObjectId() {
		return insertSQLQuestionMarksWithObjectId;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithObjectId(String insertSQLQuestionMarksWithObjectId) {
		this.insertSQLQuestionMarksWithObjectId = insertSQLQuestionMarksWithObjectId;
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithoutObjectId() {
		return insertSQLQuestionMarksWithoutObjectId;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithoutObjectId(String insertSQLQuestionMarksWithoutObjectId) {
		this.insertSQLQuestionMarksWithoutObjectId = insertSQLQuestionMarksWithoutObjectId;
	}
	
	public void setUsingManualDefinedAlias(Boolean usingManualDefinedAlias) {
		this.usingManualDefinedAlias = usingManualDefinedAlias;
	}
	
	@Override
	public Boolean isUsingManualDefinedAlias() {
		return isTrue(this.usingManualDefinedAlias);
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
	
	@Override
	public void loadManualConfiguredPk(Connection conn) throws ForbiddenOperationException, DBException {
		if (this.primaryKey != null) {
			if (!isPrimaryKeyInfoLoaded()) {
				this.primaryKey.setManualConfigured(true);
				try {
					this.primaryKey.setTabConf(this);
				}
				catch (NullPointerException e) {
					throw e;
				}
				
				this.setPrimaryKeyInfoLoaded(true);
				this.primaryKey.setKeyName("pk");
				
				if (!isFieldsLoaded()) {
					loadFields(conn);
				}
				
				for (Field key : this.primaryKey.getFields()) {
					Field field = getField(key.getName());
					
					if (field != null) {
						key.setDataType(field.getDataType());
					} else {
						throw new ForbiddenOperationException("The field '" + key.getName()
						        + "' defined as part of primaryKey cannot found on table " + getFullTableName() + "'");
					}
				}
			}
		} else {
			throw new ForbiddenOperationException("The primaryKey is null!");
		}
	}
	
	private void tryToManualLoadConfiguredPk(Connection conn) throws DBException {
		try {
			loadManualConfiguredPk(conn);
		}
		catch (ForbiddenOperationException e) {}
	}
	
	@Override
	public PrimaryKey getPrimaryKey() {
		if (isPrimaryKeyInfoLoaded()) {
			return primaryKey;
		}
		
		OpenConnection conn = null;
		
		try {
			conn = getRelatedConnInfo().openConnection();
			
			tryToManualLoadConfiguredPk(conn);
			
			loadPrimaryKeyInfo(conn);
			
			return this.primaryKey;
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	public ConflictResolutionType getOnConflict() {
		return onConflict;
	}
	
	public void setOnConflict(ConflictResolutionType onConflict) {
		this.onConflict = onConflict;
	}
	
	@Override
	public ConflictResolutionType onConflict() {
		return getOnConflict();
	}
	
	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
		
		if (hasPK()) {
			this.getPrimaryKey().setTabConf(this);
		}
	}
	
	public Boolean isAllRelatedTablesFullLoaded() {
		return isTrue(allRelatedTablesFullLoaded);
	}
	
	public void setAllRelatedTablesFullLoaded(Boolean allRelatedTablesFullLoaded) {
		this.allRelatedTablesFullLoaded = allRelatedTablesFullLoaded;
	}
	
	public Class<? extends EtlDatabaseObject> getSyncRecordClass() {
		
		if (syncRecordClass == null) {
			this.syncRecordClass = GenericDatabaseObject.class;
		}
		
		return syncRecordClass;
	}
	
	public void setFullLoaded(Boolean fullLoaded) {
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
		if (hasAlias() && !hasDynamicAlias() && !tableAlias.equals(this.getTableAlias())) {
			throw new ForbiddenOperationException("This table has already an alias and change is forbidden!");
		}
		
		this.tableAlias = tableAlias;
	}
	
	@Override
	public DatabaseObjectLoaderHelper getLoadHealper() {
		return this.loadHealper;
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
	
	@Override
	public String getExtraConditionForExtract() {
		return extraConditionForExtract;
	}
	
	@Override
	public void setExtraConditionForExtract(String extraConditionForExtract) {
		this.extraConditionForExtract = extraConditionForExtract;
	}
	
	public Boolean isMustLoadChildrenInfo() {
		return isTrue(mustLoadChildrenInfo);
	}
	
	public void setMustLoadChildrenInfo(Boolean mustLoadChildrenInfo) {
		this.mustLoadChildrenInfo = mustLoadChildrenInfo;
	}
	
	public Boolean isAutoIncrementId() {
		return isTrue(autoIncrementId);
	}
	
	public void setAutoIncrementId(Boolean autoIncrementId) {
		this.autoIncrementId = autoIncrementId;
	}
	
	public List<List<Field>> getWinningRecordFieldsInfo() {
		return winningRecordFieldsInfo;
	}
	
	public void setWinningRecordFieldsInfo(List<List<Field>> winningRecordFieldsInfo) {
		this.winningRecordFieldsInfo = winningRecordFieldsInfo;
	}
	
	public Boolean hasWinningRecordsInfo() {
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
	
	public Boolean isRemoveForbidden() {
		return removeForbidden;
	}
	
	public void setRemoveForbidden(Boolean removeForbidden) {
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
		return parents;
	}
	
	public void setParents(List<ParentTable> parents) {
		this.parents = parents;
	}
	
	public String getSharePkWith() {
		return sharePkWith;
	}
	
	public void setSharePkWith(String sharePkWith) {
		this.sharePkWith = sharePkWith;
	}
	
	@Override
	public EtlDataConfiguration getParentConf() {
		return parentConf;
	}
	
	public void setParentConf(EtlDataConfiguration parentConf) {
		this.parentConf = (EtlDataConfiguration) parentConf;
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
	
	@Override
	public Boolean isMetadata() {
		return isTrue(metadata);
	}
	
	public void setMetadata(Boolean metadata) {
		this.metadata = metadata;
	}
	
	@JsonIgnore
	public Boolean isFullLoaded() {
		return isTrue(fullLoaded);
	}
	
	public Boolean isDisabled() {
		return isTrue(disabled);
	}
	
	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}
	
	@Override
	public Boolean hasPK() {
		return this.primaryKey != null;
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String toString = "Table [" + getFullTableDescription();
		
		toString += hasPK() ? ", pk: " + this.primaryKey : "";
		
		toString += "]";
		
		return toString;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractTableConfiguration))
			return false;
		
		return getFullTableName().equalsIgnoreCase(((AbstractTableConfiguration) obj).getFullTableName());
	}
	
	@Override
	public int compareTo(AbstractTableConfiguration o) {
		if (this.equals(o))
			return 0;
		
		return this.tableName.compareTo(o.getTableName());
	}
	
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
		this.setIgnorableFields(utilities.tryToReplacePlaceholdersAll(getIgnorableFields(), schemaInfoSrc));
		setTableAlias(utilities.tryToReplacePlaceholders(getTableAlias(), schemaInfoSrc));
		
		if (hasParents()) {
			for (ParentTable p : this.getParents()) {
				p.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
		
		if (hasPK()) {
			this.getPrimaryKey().tryToReplacePlaceholders(schemaInfoSrc);
		}
		
		setSharePkWith(utilities.tryToReplacePlaceholders(this.getSharePkWith(), schemaInfoSrc));
		
		this.setObservationDateFields(utilities.tryToReplacePlaceholders(this.getObservationDateFields(), schemaInfoSrc));
		
		if (hasUniqueKeys()) {
			UniqueKeyInfo.tryToReplacePlaceholders(this.getUniqueKeys(), schemaInfoSrc);
		}
		
		setExtraConditionForExtract(utilities.tryToReplacePlaceholders(getExtraConditionForExtract(), schemaInfoSrc));
		
		tryToReplacePlaceholdersOnOwnElements(schemaInfoSrc);
	}
	
	public PreparedQuery getDefaultPreparedQuery() {
		return defaultPreparedQuery;
	}
	
	public void setDefaultPreparedQuery(PreparedQuery defaultPreparedQuery) {
		this.defaultPreparedQuery = defaultPreparedQuery;
	}
	
	@Override
	public void tryToLoadFromTemplate() {
		super.tryToLoadFromTemplate();
		
		this.loadHealper.setTableConf(this);
	}
	
	public abstract void tryToReplacePlaceholdersOnOwnElements(EtlDatabaseObject schemaInfoSrc);
	
}
