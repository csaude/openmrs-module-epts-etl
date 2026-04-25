package org.openmrs.module.epts.etl.conf.interfaces;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openmrs.module.epts.etl.conf.AbstractRelatedTable;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlTemplateInfo;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.PrimaryKey;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.RefType;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.types.AutoIncrementHandlingType;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.DuplicateMappingException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingJoiningElementsException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface TableConfiguration extends DatabaseObjectConfiguration, EtlDataSource {
	
	public static final String[] REMOVABLE_METADATA = {};
	
	public static final Object lock = new Object();
	
	void setTableName(String tableName);
	
	AutoIncrementHandlingType getAutoIncrementHandlingType();
	
	void setAutoIncrementHandlingType(AutoIncrementHandlingType autoIncrementHandlingType);
	
	String getTableAlias();
	
	String getTableName();
	
	String getAlias();
	
	/**
	 * @return the initial increment to be done on the table ID for the first record when the ID is
	 *         manually generated
	 */
	Integer getPrimaryKeyInitialIncrementValue();
	
	void setPrimaryKeyInitialIncrementValue(Integer primaryKeyInitialIncrementValue);
	
	void setTableAlias(String tableAlias);
	
	//void setLoadHealper(DatabaseObjectLoaderHelper loadHealper);
	
	String getInsertSQLWithObjectId();
	
	String getInsertSQLWithoutObjectId();
	
	String getUpdateSql();
	
	String getExtraConditionForExtract();
	
	void setInsertSQLWithoutObjectId(String string);
	
	void setExtraConditionForExtract(String extraConditionForExtract);
	
	Boolean isMustLoadChildrenInfo();
	
	void setMustLoadChildrenInfo(Boolean mustLoadChildrenInfo);
	
	Boolean isAutoIncrementId();
	
	void setAutoIncrementId(Boolean autoIncrementId);
	
	void loadManualConfiguredPk(Connection conn) throws ForbiddenOperationException, DBException;
	
	List<List<Field>> getWinningRecordFieldsInfo();
	
	void setWinningRecordFieldsInfo(List<List<Field>> winningRecordFieldsInfo);
	
	List<String> getIgnorableFields();
	
	void setIgnorableFields(List<String> ignorable);
	
	Boolean includePrimaryKeyOnInsert();
	
	void setIncludePrimaryKeyOnInsert(Boolean includePrimaryKeyOnInsert);
	
	Boolean isUniqueKeyInfoLoaded();
	
	void setUniqueKeyInfoLoaded(Boolean uniqueKeyInfoLoaded);
	
	Boolean isPrimaryKeyInfoLoaded();
	
	void setPrimaryKeyInfoLoaded(Boolean primaryKeyInfoLoaded);
	
	Boolean isFieldsLoaded();
	
	void setFieldsLoaded(Boolean fieldsLoaded);
	
	Boolean isTableNameInfoLoaded();
	
	void setTableNameInfoLoaded(Boolean tableNameInfoLoaded);
	
	Boolean isParentsLoaded();
	
	void setParentsLoaded(Boolean parentsLoaded);
	
	ConflictResolutionType onConflict();
	
	ConflictResolutionType getOnConflict();
	
	void setOnConflict(ConflictResolutionType onConflict);
	
	void setParentRefInfo(List<? extends ParentTable> parentRefInfo);
	
	void setChildRefInfo(List<ChildTable> childRefInfo);
	
	List<UniqueKeyInfo> getUniqueKeys();
	
	void setUniqueKeys(List<UniqueKeyInfo> uniqueKeys);
	
	List<String> getObservationDateFields();
	
	void setObservationDateFields(List<String> observationDateFields);
	
	Boolean isRemoveForbidden();
	
	Boolean ignoreMissingParameters();
	
	public void setRemoveForbidden(Boolean removeForbidden);
	
	List<ParentTable> getParents();
	
	Boolean isUsingManualDefinedAlias();
	
	void setUsingManualDefinedAlias(Boolean usingManualDefinedAlias);
	
	void setParents(List<ParentTable> parents);
	
	void setSharePkWith(String sharePkWith);
	
	void setParentConf(EtlDataConfiguration parentConf);
	
	void setPrimaryKey(PrimaryKey primaryKey);
	
	void setMetadata(Boolean metadata);
	
	public Boolean isDisabled();
	
	public void setDisabled(Boolean disabled);
	
	void setFields(List<Field> tableFields);
	
	void setFullLoaded(Boolean fullLoaded);
	
	void setInsertSQLQuestionMarksWithObjectId(String insertQuestionMarks);
	
	String getInsertSQLQuestionMarksWithObjectId();
	
	void setInsertSQLQuestionMarksWithoutObjectId(String insertQuestionMarks);
	
	String getInsertSQLQuestionMarksWithoutObjectId();
	
	void setInsertSQLWithObjectId(String sql);
	
	void setSchema(String schema);
	
	Boolean isAllRelatedTablesFullLoaded();
	
	void setAllRelatedTablesFullLoaded(Boolean b);
	
	/**
	 * If present, the value from this method will be mapped as a primary key for this table if the
	 * table does not have a primary key but have a field with name matching this field.
	 */
	String getManualMapPrimaryKeyOnField();
	
	void setManualMapPrimaryKeyOnField(String manualMapPrimaryKeyOnField);
	
	Boolean useMysqlInsertIgnore();
	
	@Override
	PrimaryKey getPrimaryKey();
	
	@Override
	default Boolean hasPK(Connection conn) throws DBException {
		
		if (!this.isPrimaryKeyInfoLoaded()) {
			this.loadPrimaryKeyInfo(conn);
		}
		
		return this.getPrimaryKey() != null;
	}
	
	default Boolean hasTableName() {
		return this.getTableName() != null && !this.getTableName().isEmpty();
	}
	
	default void createTable(Connection conn) throws DBException {
		
		synchronized (lock) {
			if (!DBUtilities.isTableExists(this.getSyncStageSchema(), this.getTableName(), conn)) {
				
				if (!this.hasFields()) {
					throw new ForbiddenOperationException("There is no field for creation table!!");
				}
				
				if (!this.hasTableName()) {
					throw new ForbiddenOperationException("The table has no table name!!");
				}
				
				String notNullConstraint = "NOT NULL";
				String nullConstraint = "NULL";
				String endLineMarker = ",\n";
				
				String sql = "CREATE TABLE" + getFullTableName() + "(";
				
				for (Field f : this.getFields()) {
					String constraint = f.allowNull() ? nullConstraint : notNullConstraint;
					
					if (f.isClob()) {
						sql += DBUtilities.generateTableClobField(f.getName(), constraint, conn) + endLineMarker;
					} else if (f.isTextField()) {
						sql += DBUtilities.generateTableTextField(f.getName(), constraint, conn) + endLineMarker;
					} else if (f.isString()) {
						sql += DBUtilities.generateTableVarcharField(f.getName(), f.getPrecision().getLength(), constraint,
						    conn) + endLineMarker;
					} else if (f.isLongField()) {
						sql += DBUtilities.generateTableBigIntField(f.getName(), constraint, conn) + endLineMarker;
					} else if (f.isIntegerField() || f.isSmallIntType()) {
						sql += DBUtilities.generateTableIntegerField(f.getName(), f.getPrecision().getLength(), constraint,
						    conn) + endLineMarker;
					} else if (f.isDateField()) {
						sql += DBUtilities.generateTableDateTimeField(f.getName(), constraint, conn) + endLineMarker;
					} else if (f.isDecimalField()) {
						sql += DBUtilities.generateTableDecimalField(f.getName(), f.getPrecision().getLength(),
						    f.getPrecision().getDecimalDigits(), constraint, conn) + endLineMarker;
						
					} else {
						sql += DBUtilities.generateTableVarcharField(f.getName(), f.getPrecision().getLength(), constraint,
						    conn) + endLineMarker;
					}
				}
				
				//Remove the last #endLineMarker 
				sql = utilities.removeLastChar(sql);
				
				if (this.hasPK()) {
					sql += endLineMarker;
					
					sql += DBUtilities.generateTablePrimaryKeyDefinition(
					    getPrimaryKey().parseFieldNamesToCommaSeparatedString(), this.getTableName() + "_pk", conn);
				}
				
				if (this.hasUniqueKeys()) {
					sql += endLineMarker;
					
					for (UniqueKeyInfo uk : this.getUniqueKeys()) {
						sql += DBUtilities.generateTableUniqueKeyDefinition(uk.getKeyName(),
						    uk.parseFieldNamesToCommaSeparatedString(), conn) + endLineMarker;
					}
				}
				
				//Remove the last #endLineMarker 
				sql = utilities.removeLastChar(sql);
				
				sql += ")";
				
				BaseDAO.executeBatch(conn, sql);
			}
		}
	}
	
	default Boolean useManualGeneratedObjectId() {
		return !this.isAutoIncrementId() && this.useSimpleNumericPk();
	}
	
	default Boolean hasAlias() {
		return utilities.stringHasValue(this.getTableAlias());
	}
	
	default Boolean hasWinningRecordsInfo() {
		return this.getWinningRecordFieldsInfo() != null;
	}
	
	@JsonIgnore
	default String getId() {
		return this.getRelatedEtlConf().getDesignation() + "_" + this.getTableName();
	}
	
	default Boolean hasExtraConditionForExtract() {
		return utilities.stringHasValue(this.getExtraConditionForExtract());
	}
	
	@JsonIgnore
	default String getParentsAsString() {
		String sourceFoldersAsString = "";
		
		if (utilities.listHasElement(this.getParents())) {
			for (int i = 0; i < this.getParents().size() - 1; i++) {
				sourceFoldersAsString += this.getParents().get(i).getTableName() + ",";
			}
			
			sourceFoldersAsString += this.getParents().get(this.getParents().size() - 1).getTableName();
		}
		
		return sourceFoldersAsString;
	}
	
	default String getObjectName() {
		return getTableName();
	}
	
	/**
	 * Clones gives list of UniqueKeys to this tableConfiguration
	 * 
	 * @param uniqueKeys to clone to this table
	 */
	default void cloneUnikeKeys(List<UniqueKeyInfo> uniqueKeys) {
		
		if (utilities.listHasElement(uniqueKeys)) {
			setUniqueKeys(new ArrayList<>(uniqueKeys.size()));
			
			for (UniqueKeyInfo uk : UniqueKeyInfo.cloneAll_(uniqueKeys)) {
				uk.setTabConf(this);
				
				this.getUniqueKeys().add(uk);
			}
			
		} else {
			this.setUniqueKeys(null);
		}
	}
	
	default void clone(TableConfiguration toCloneFrom, EtlDataConfiguration parent, EtlDatabaseObject schemaInfoSrc,
	        Connection conn) throws DBException {
		this.setTableName(toCloneFrom.getTableName());
		
		if (!this.hasAlias() && toCloneFrom.hasDynamicAlias()) {
			this.setTableAlias(toCloneFrom.getAlias());
			this.setUsingManualDefinedAlias(true);
		}
		
		this.tryToLoadSchemaInfo(schemaInfoSrc, conn);
		
		if (!this.hasSchema()) {
			this.setSchema(toCloneFrom.getSchema());
		}
		
		this.setParents(toCloneFrom.getParents());
		this.setMustLoadChildrenInfo(toCloneFrom.isMustLoadChildrenInfo());
		this.setOnConflict(toCloneFrom.onConflict());
		this.setFullLoaded(toCloneFrom.isFullLoaded());
		this.setUniqueKeyInfoLoaded(toCloneFrom.isUniqueKeyInfoLoaded());
		this.setPrimaryKeyInfoLoaded(toCloneFrom.isPrimaryKeyInfoLoaded());
		this.setFieldsLoaded(toCloneFrom.isFieldsLoaded());
		this.setTableNameInfoLoaded(toCloneFrom.isTableNameInfoLoaded());
		this.setParentsLoaded(toCloneFrom.isParentsLoaded());
		this.setPrimaryKeyInitialIncrementValue(toCloneFrom.getPrimaryKeyInitialIncrementValue());
		
		if (isMustLoadChildrenInfo()) {
			this.setChildRefInfo(toCloneFrom.getChildRefInfo());
		}
		
		this.setParentRefInfo(toCloneFrom.getParentRefInfo());
		this.setSyncRecordClass(toCloneFrom.getSyncRecordClass());
		this.setParentConf(parent != null ? parent : toCloneFrom.getParentConf());
		this.setFields(toCloneFrom.getFields());
		this.setIgnorableFields(toCloneFrom.getIgnorableFields());
		
		if (toCloneFrom.hasPK()) {
			this.setPrimaryKey(toCloneFrom.getPrimaryKey());
			this.getPrimaryKey().setTabConf(this);
		}
		
		this.setWinningRecordFieldsInfo(toCloneFrom.getWinningRecordFieldsInfo());
		
		this.setSharePkWith(toCloneFrom.getSharePkWith());
		this.setMetadata(toCloneFrom.isMetadata());
		this.setRemoveForbidden(toCloneFrom.isRemoveForbidden());
		this.setObservationDateFields(toCloneFrom.getObservationDateFields());
		
		this.cloneUnikeKeys(toCloneFrom.getUniqueKeys());
		
		this.setFullLoaded(toCloneFrom.isFullLoaded());
		
		this.setInsertSQLWithObjectId(toCloneFrom.getInsertSQLWithObjectId());
		this.setInsertSQLWithoutObjectId(toCloneFrom.getInsertSQLWithoutObjectId());
		this.setUpdateSql(toCloneFrom.getUpdateSql());
		
		if (toCloneFrom.getRelatedEtlConf() != null) {
			this.tryToGenerateTableAlias(toCloneFrom.getRelatedEtlConf());
		}
		
		if (toCloneFrom.hasExtraConditionForExtract() && !toCloneFrom.isUsingManualDefinedAlias()) {
			//First try to replace the alias
			this.setExtraConditionForExtract(toCloneFrom.getExtraConditionForExtract()
			        .replaceAll(toCloneFrom.getTableAlias() + "\\.", getTableAlias() + "\\."));
			//Second try to replace tableName
			
			this.setExtraConditionForExtract(
			    this.getExtraConditionForExtract().replaceAll(toCloneFrom.getTableName() + "\\.", getTableAlias() + "\\."));
			
		} else {
			setExtraConditionForExtract(null);
		}
		
		if (isFullLoaded()) {
			loadOwnElements(schemaInfoSrc, conn);
		}
		
	}
	
	default Boolean hasDynamicAlias() {
		return this.hasAlias() && this.getAlias().contains("@");
	}
	
	default Boolean useDynamicTableName() {
		return this.getTableName().contains("@");
	}
	
	@JsonIgnore
	default Boolean useSharedPKKey() {
		return utilities.stringHasValue(this.getSharePkWith());
	}
	
	default void loadPrimaryKeyInfo(Connection conn) throws DBException {
		PrimaryKey primaryKey = null;
		
		if (!this.isPrimaryKeyInfoLoaded()) {
			
			if (this.hasPK()) {
				this.loadManualConfiguredPk(conn);
			} else {
				
				this.tryToLoadSchemaInfo(null, conn);
				
				this.loadFields(conn);
				
				try {
					
					ResultSet rs = conn.getMetaData().getPrimaryKeys(this.getCatalog(conn), this.getSchema(),
					    this.getTableName());
					
					while (rs.next()) {
						primaryKey = new PrimaryKey(this);
						primaryKey.setKeyName("pk");
						
						Key pk = new Key();
						pk.setName(rs.getString("COLUMN_NAME"));
						
						pk.setDataType(getField(pk.getName()).getDataType());
						
						primaryKey.addKey(pk);
					}
					
					this.setPrimaryKey(primaryKey);
				}
				catch (SQLException e) {
					throw new DBException(e);
				}
				
				if (primaryKey == null && this.hasManualMapPrimaryKeyOnField()) {
					if (this.containsField(this.getManualMapPrimaryKeyOnField())) {
						primaryKey = new PrimaryKey();
						
						primaryKey.addKey(Key.fastCreateKey(this.getManualMapPrimaryKeyOnField()));
						
						this.setPrimaryKey(primaryKey);
						
						this.loadManualConfiguredPk(conn);
					}
				}
			}
			
			this.setPrimaryKeyInfoLoaded(true);
		}
	}
	
	default Boolean hasManualMapPrimaryKeyOnField() {
		return this.getManualMapPrimaryKeyOnField() != null;
	}
	
	@SuppressWarnings("deprecation")
	default EtlDatabaseObject generateAndSaveDefaultObject(Connection conn) throws DBException {
		
		synchronized (this) {
			
			try {
				EtlDatabaseObject defaultObject = this.getDefaultObject(conn);
				
				if (defaultObject != null) {
					return defaultObject;
				} else {
					defaultObject = this.getSyncRecordClass().newInstance();
					defaultObject.setRelatedConfiguration(this);
					
					defaultObject.loadWithDefaultValues(conn);
					
					try {
						defaultObject.save(this, conn);
					}
					catch (DBException e) {
						if (!e.isDuplicatePrimaryOrUniqueKeyException()) {
							throw e;
						}
					}
					
					defaultObject = this.getDefaultObject(conn);
					
					EtlConfigurationTableConf defaultGeneratedObjectKeyTabConf = this.getRelatedEtlConf()
					        .getDefaultGeneratedObjectKeyTabConf();
					
					if (!defaultGeneratedObjectKeyTabConf.isFullLoaded()) {
						
						defaultGeneratedObjectKeyTabConf.setTableName(this.getRelatedEtlConf().getSyncStageSchema() + "."
						        + defaultGeneratedObjectKeyTabConf.getTableName());
						
						defaultGeneratedObjectKeyTabConf.setTableAlias(defaultGeneratedObjectKeyTabConf.getTableName());
						defaultGeneratedObjectKeyTabConf.fullLoad(conn);
					}
					
					for (Key key : defaultObject.getObjectId().getFields()) {
						EtlDatabaseObject keyInfo = defaultGeneratedObjectKeyTabConf.getSyncRecordClass().newInstance();
						
						keyInfo.setRelatedConfiguration(defaultGeneratedObjectKeyTabConf);
						
						keyInfo.setFieldValue("table_name", defaultObject.getRelatedConfiguration().getObjectName());
						keyInfo.setFieldValue("column_name", key.getName());
						keyInfo.setFieldValue("key_value", key.getValue());
						
						keyInfo.save(defaultGeneratedObjectKeyTabConf, conn);
					}
					
					return defaultObject;
				}
			}
			catch (InstantiationException | IllegalAccessException | ForbiddenOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	default void deleteAllSkippedRecord(Connection srcConn) throws DBException {
		EtlConfigurationTableConf skippedRecordTabConf = this.getRelatedEtlConf().getSkippedRecordTabConf();
		
		DatabaseObjectDAO.removeAll(skippedRecordTabConf, "table_name = '" + getTableName() + "'", srcConn);
	}
	
	default void saveSkippedRecord(EtlDatabaseObject skippedrecord, Connection srcConn) throws DBException {
		EtlConfigurationTableConf skippedRecordTabConf = this.getRelatedEtlConf().getSkippedRecordTabConf();
		
		if (!skippedRecordTabConf.isFullLoaded()) {
			skippedRecordTabConf.fullLoad(srcConn);
		}
		
		try {
			@SuppressWarnings("deprecation")
			EtlDatabaseObject keyInfo = skippedRecordTabConf.getSyncRecordClass().newInstance();
			
			keyInfo.setRelatedConfiguration(skippedRecordTabConf);
			
			keyInfo.setFieldValue("table_name", skippedrecord.getObjectName());
			keyInfo.setFieldValue("object_id", skippedrecord.getObjectId().getSimpleValue());
			
			keyInfo.save(skippedRecordTabConf, ConflictResolutionType.KEEP_EXISTING, srcConn);
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	default String generateSkippedRecordInclusionClause() {
		
		if (!getPrimaryKey().isSimpleKey())
			throw new ForbiddenOperationException("Only simple pk is supported!");
		
		EtlConfigurationTableConf recWithDefaultParents = this.getRelatedEtlConf().getRecordWithDefaultParentsInfoTabConf();
		
		String sql = "";
		sql += this.getTableAlias() + "." + this.getPrimaryKey().retrieveSimpleKeyColumnName();
		sql += " in  (	select src_rec_id \n";
		sql += "		from " + recWithDefaultParents.getFullTableName() + "\n";
		sql += " 		where src_table_name = '" + this.getTableName() + "')";
		
		return sql;
	}
	
	default EtlDatabaseObject getDefaultObject(Connection conn) throws DBException, ForbiddenOperationException {
		return DatabaseObjectDAO.getDefaultRecord(this, conn);
	}
	
	@JsonIgnore
	default void loadUniqueKeys() {
		if (isUniqueKeyInfoLoaded())
			return;
		
		OpenConnection conn = null;
		
		try {
			conn = this.getRelatedEtlConf().getSrcConnInfo().openConnection();
			
			this.loadUniqueKeys(conn);
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@JsonIgnore
	default void loadUniqueKeys(Connection conn) {
		if (this.isUniqueKeyInfoLoaded())
			return;
		
		if (this.getUniqueKeys() == null) {
			this.loadUniqueKeys(this, conn);
		} else {
			for (UniqueKeyInfo uk : this.getUniqueKeys()) {
				uk.setTabConf(this);
			}
		}
		
		this.setUniqueKeyInfoLoaded(true);
	}
	
	@JsonIgnore
	default void loadUniqueKeys(TableConfiguration tableConfiguration, Connection conn) {
		if (tableConfiguration.getUniqueKeys() == null) {
			try {
				this.setUniqueKeys(UniqueKeyInfo.loadUniqueKeysInfo(this, conn));
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
		}
	}
	
	default Boolean checkIfisIgnorableParentByClassAttName(String parentAttName, Connection conn) {
		for (ParentTable parent : this.getParentRefInfo()) {
			RefMapping map = parent.getRefMappingByChildClassAttName(parentAttName);
			
			return map.isIgnorable();
		}
		
		throw new ForbiddenOperationException("The att '" + parentAttName + "' doesn't represent any defined parent att");
	}
	
	default int countChildren(Connection conn) throws SQLException {
		String tableName = SQLUtilities.extractTableNameFromFullTableName(this.getTableName());
		
		ResultSet foreignKeyRS = conn.getMetaData().getExportedKeys(this.getCatalog(conn), this.getSchema(), tableName);
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				foreignKeyRS.last();
			} else {
				while (foreignKeyRS.next()) {}
			}
			
			return foreignKeyRS.getRow();
		}
		finally {
			foreignKeyRS.close();
		}
		
	}
	
	default void logInfo(String msg) {
		this.getRelatedEtlConf().logInfo(msg);
	}
	
	default void logDebug(String msg) {
		this.getRelatedEtlConf().logDebug(msg);
	}
	
	default void logTrace(String msg) {
		this.getRelatedEtlConf().logTrace(msg);
	}
	
	default void logWarn(String msg) {
		this.getRelatedEtlConf().logWarn(msg);
	}
	
	default void logErr(String msg) {
		this.getRelatedEtlConf().logErr(msg);
	}
	
	default int countParents(Connection conn) throws SQLException {
		ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(this.getCatalog(conn), this.getSchema(),
		    this.getTableName());
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				foreignKeyRS.last();
			} else {
				while (foreignKeyRS.next()) {}
			}
			
			return foreignKeyRS.getRow();
		}
		finally {
			foreignKeyRS.close();
		}
	}
	
	default void loadParents(Connection conn) throws DBException {
		if (!getRelatedEtlConf().doNotResolveRelationship()) {
			
			synchronized (this) {
				try {
					if (this.isParentsLoaded())
						return;
					
					logDebug("LOADING PARENTS FOR TABLE '" + this.getTableName() + "'");
					
					ResultSet foreignKeyRS = null;
					
					int count = this.countParents(conn);
					
					//First load all necessary info on configured parents
					
					if (utilities.listHasElement(this.getParents())) {
						for (ParentTable p : this.getParents()) {
							
							p.setChildTableConf(this);
							
							if (p.getRefMapping() != null) {
								
								for (RefMapping map : p.getRefMapping()) {
									map.setParentTabConf((ParentTableImpl) p);
									
									Field field = utilities.findOnArray(this.getFields(),
									    new Field(map.getChildFieldName()));
									map.getChildField().setDataType(field.getDataType());
								}
							}
						}
					}
					
					if (count == 0) {
						this.logDebug("NO PARENT FOUND FOR TABLE '" + getTableName() + "'");
					} else
						try {
							this.logDebug("DISCOVERED '" + count + "' PARENTS FOR TABLE '" + this.getTableName() + "'");
							
							foreignKeyRS = conn.getMetaData().getImportedKeys(this.getCatalog(conn), this.getSchema(),
							    this.getTableName());
							
							while (foreignKeyRS.next()) {
								
								this.logDebug("CONFIGURING PARENT [" + foreignKeyRS.getString("PKTABLE_NAME")
								        + "] FOR TABLE '" + this.getTableName() + "'");
								
								String refCode = foreignKeyRS.getString("FK_NAME");
								
								String childFieldName = foreignKeyRS.getString("FKCOLUMN_NAME");
								if (!utilities.containsAll(this.getIgnorableFields(), childFieldName)) {
									String parentFieldName = foreignKeyRS.getString("PKCOLUMN_NAME");
									String parentTableName = foreignKeyRS.getString("PKTABLE_NAME");
									
									ParentTableImpl parentTabConf = ParentTableImpl.init(parentTableName, refCode);
									
									if (childFieldName.equals(this.getPrimaryKey().asSimpleKey().getName())) {
										this.setSharePkWith(parentTableName);
										
										if (this instanceof DstConf) {
											DstConf dstConf = (DstConf) this;
											
											if (dstConf.hasParentDstConf()) {
												//Try to retrieve parent from the parent etlItemConf
												
												if (dstConf.getParentDstConf().getTableName()
												        .equals(this.getSharePkWith())) {
													parentTabConf.setIgnorableFields(
													    dstConf.getParentDstConf().getIgnorableFields());
												}
											}
										}
									}
									
									parentTabConf.setParentConf(this.getParentConf());
									parentTabConf.setChildTableConf(this);
									parentTabConf.setRelatedEtlConfig(getRelatedEtlConf());
									parentTabConf.setSchema(foreignKeyRS.getString("PKTABLE_SCHEM"));
									
									if (!parentTabConf.hasSchema()) {
										parentTabConf.setSchema(foreignKeyRS.getString("PKTABLE_CAT"));
									}
									
									addParentMappingInfo(refCode, childFieldName, parentTabConf, parentFieldName, conn);
									
									logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '"
									        + getTableName() + "' CONFIGURED");
								}
								
								//Copy additional configured Info
								if (this.hasParentRefInfo() && this.hasParents()) {
									
									for (ParentTable autoLoadedRefInfo : this.getParentRefInfo()) {
										
										for (ParentTable manualConfiguredRefInfo : this.getParents()) {
											
											ParentTable mixedConfiguredRef = manualConfiguredRefInfo;
											
											if (autoLoadedRefInfo.getTableName()
											        .equals(manualConfiguredRefInfo.getTableName())) {
												
												if (!manualConfiguredRefInfo.hasMapping()) {
													if (autoLoadedRefInfo.isCompositeMapping()) {
														throw new ForbiddenOperationException(
														        "You must manual configure the ref info for parent "
														                + manualConfiguredRefInfo.getTableName()
														                + " on table " + this.getTableName()
														                + ". Optionaly you can remove the manual parent specification");
													}
													
													//create default refInfo to force the copy of shared ref info
													
													mixedConfiguredRef = ParentTableImpl
													        .init(manualConfiguredRefInfo.getTableName(), "");
													
													mixedConfiguredRef.setConditionalFields(
													    manualConfiguredRefInfo.getConditionalFields());
													
													mixedConfiguredRef.setChildTableConf(this);
													
													mixedConfiguredRef.setRefMapping(autoLoadedRefInfo.cloneAllMapping());
													
													mixedConfiguredRef.getSimpleRefMapping().setDefaultValueDueInconsistency(
													    manualConfiguredRefInfo.getDefaultValueDueInconsistency());
													mixedConfiguredRef.getSimpleRefMapping().setSetNullDueInconsistency(
													    manualConfiguredRefInfo.isSetNullDueInconsistency());
													
													mixedConfiguredRef.setDefaultValueDueInconsistency(
													    manualConfiguredRefInfo.getDefaultValueDueInconsistency());
													mixedConfiguredRef.setSetNullDueInconsistency(
													    manualConfiguredRefInfo.isSetNullDueInconsistency());
													mixedConfiguredRef.setIgnorableFields(
													    manualConfiguredRefInfo.getIgnorableFields());
												}
												
												if (autoLoadedRefInfo.equals(mixedConfiguredRef)) {
													autoLoadedRefInfo
													        .setConditionalFields(mixedConfiguredRef.getConditionalFields());
													
													autoLoadedRefInfo.setDefaultValueDueInconsistency(
													    manualConfiguredRefInfo.getDefaultValueDueInconsistency());
													autoLoadedRefInfo.setSetNullDueInconsistency(
													    manualConfiguredRefInfo.isSetNullDueInconsistency());
													
													autoLoadedRefInfo.setIgnorableFields(
													    manualConfiguredRefInfo.getIgnorableFields());
													
													for (RefMapping map : autoLoadedRefInfo.getRefMapping()) {
														RefMapping configuredMap = mixedConfiguredRef.findRefMapping(
														    map.getChildFieldName(), map.getParentFieldName());
														
														if (configuredMap == null) {
															throw new ForbiddenOperationException(
															        "The mapping [" + map.getChildFieldName() + " : "
															                + map.getParentFieldName()
															                + "] was not found on configured mapping!");
														}
														
														map.setIgnorable(map.isIgnorable() ? configuredMap.isIgnorable()
														        : map.isIgnorable());
														map.setDefaultValueDueInconsistency(
														    configuredMap.getDefaultValueDueInconsistency());
														map.setSetNullDueInconsistency(
														    configuredMap.isSetNullDueInconsistency());
													}
												}
											}
										}
									}
								}
							}
							//Check if there is a configured parent but not defined on the db schema
							
							if (utilities.listHasElement(this.getParents())) {
								
								for (ParentTable configuredParent : this.getParents()) {
									if (configuredParent.hasMapping()) {
										if (!this.getParentRefInfo().contains(configuredParent)) {
											configuredParent.setManualyConfigured(true);
											
											this.getParentRefInfo().add(configuredParent);
										}
									}
								}
							}
							
							if (hasParentRefInfo()) {
								//Find and exclude duplicated ref
								
								List<ParentTable> cleanList = new ArrayList<>();
								
								for (ParentTable ref : this.getParentRefInfo()) {
									if (!cleanList.contains(ref)) {
										cleanList.add(ref);
									}
								}
								
								this.setParentRefInfo(cleanList);
							}
							
							this.logDebug("LOADED PARENTS FOR TABLE '" + this.getTableName() + "'");
						}
						finally
						
						{
							if (foreignKeyRS != null) {
								foreignKeyRS.close();
							}
						}
					
					this.setParentsLoaded(true);
				}
				catch (SQLException e) {
					throw new DBException(e);
				}
			}
		}
	}
	
	default Boolean hasParents() {
		return utilities.listHasElement(this.getParents());
	}
	
	default void loadChildren(Connection conn) throws SQLException {
		
		synchronized (this) {
			
			if (!this.isMustLoadChildrenInfo())
				return;
			
			logDebug("LOADING CHILDREN FOR TABLE '" + this.getTableName() + "'");
			
			List<ChildTable> childRefInfo = new ArrayList<ChildTable>();
			
			setChildRefInfo(childRefInfo);
			
			int count = countChildren(conn);
			
			if (count == 0) {
				logDebug("NO CHILDREN FOUND FOR TABLE '" + this.getTableName() + "'");
			} else {
				ResultSet foreignKeyRS = null;
				
				try {
					logDebug("DISCOVERED '" + count + "' CHILDREN FOR TABLE '" + this.getTableName() + "'");
					
					foreignKeyRS = conn.getMetaData().getExportedKeys(this.getCatalog(conn), this.getSchema(),
					    this.getTableName());
					
					int i = 0;
					
					while (foreignKeyRS.next()) {
						logDebug("CONFIGURING CHILD " + ++i + " [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '"
						        + this.getTableName() + "'");
						
						String refCode = foreignKeyRS.getString("FK_NAME");
						
						String childTableName = foreignKeyRS.getString("FKTABLE_NAME");
						String childFieldName = foreignKeyRS.getString("FKCOLUMN_NAME");
						
						String parentFieldName = foreignKeyRS.getString("PKCOLUMN_NAME");
						
						ChildTable childTabConf = ChildTable.init(childTableName, refCode);
						
						childTabConf.setParentTableConf(this);
						childTabConf.setParentConf(this.getParentConf());
						childTabConf.setRelatedEtlConfig(getRelatedEtlConf());
						childTabConf.setSchema(foreignKeyRS.getString("FKTABLE_SCHEM"));
						
						if (!childTabConf.hasSchema()) {
							childTabConf.setSchema(foreignKeyRS.getString("PKTABLE_CAT"));
						}
						
						this.addChildMappingInfo(refCode, childTabConf, childFieldName, parentFieldName, conn);
						
						logDebug("CHILDREN " + i + " [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '"
						        + getTableName() + "' CONFIGURED");
					}
					
					this.logDebug("LOADED CHILDREN FOR TABLE '" + this.getTableName() + "'");
				}
				finally {
					if (foreignKeyRS != null) {
						foreignKeyRS.close();
					}
				}
			}
		}
		
	}
	
	default void addChildMappingInfo(String refCode, ChildTable childTabConf, String childFieldName, String parentFieldName,
	        Connection conn) throws DBException {
		
		TableConfiguration parentTabConf = this;
		
		initRefInfo(RefType.EXPORTED, refCode, childTabConf, childFieldName, parentTabConf, parentFieldName, conn);
	}
	
	default void addParentMappingInfo(String refCode, String childFieldName, ParentTableImpl parentTabConf,
	        String parentFieldName, Connection conn) throws DBException {
		
		TableConfiguration childTabConf = this;
		
		this.initRefInfo(RefType.IMPORTED, refCode, childTabConf, childFieldName, parentTabConf, parentFieldName, conn);
	}
	
	default void initRefInfo(RefType refType, String refCode, TableConfiguration childTabConf, String childFieldname,
	        TableConfiguration parentTabConf, String parentFieldName, Connection conn) throws DBException {
		
		String fieldName = null;
		
		if (refType.isImported()) {
			fieldName = childFieldname;
		} else {
			fieldName = parentFieldName;
		}
		
		Field field = utilities.findOnArray(this.getFields(), new Field(fieldName));
		
		if (field == null) {
			throw new ForbiddenOperationException(
			        "The field '" + fieldName + "' was not found on '" + this.getTableName() + "' fields!!!");
		}
		
		Boolean ignorable = DBUtilities.isTableColumnAllowNull(this.getTableName(), this.getSchema(), fieldName, conn);
		
		RefMapping map = RefMapping.fastCreate(childFieldname, parentFieldName);
		
		map.getChildField().setDataType(field.getDataType());
		map.getParentField().setDataType(field.getDataType());
		map.setIgnorable(ignorable);
		
		if (this.getParentRefInfo() == null) {
			this.setParentRefInfo(new ArrayList<>());
		}
		
		if (isMustLoadChildrenInfo() && this.getChildRefInfo() == null) {
			this.setChildRefInfo(new ArrayList<>());
		}
		
		RelatedTable ref = null;
		RelatedTable existingRef = null;
		
		if (refType.isImported()) {
			existingRef = utilities.findOnList(this.getParentRefInfo(), refCode);
			
			if (existingRef == null) {
				ref = (AbstractRelatedTable) parentTabConf;
				ref.setMetadata(!ref.isConfigured());
				this.getParentRefInfo().add((ParentTable) ref);
			}
			
		} else {
			existingRef = utilities.findOnList(this.getChildRefInfo(), refCode);
			
			if (existingRef == null) {
				ref = (AbstractRelatedTable) childTabConf;
				
				ref.setRelatedTabConf(parentTabConf);
				ref.setMetadata(!ref.isConfigured());
				
				this.getChildRefInfo().add((ChildTable) ref);
			}
		}
		
		try {
			if (ref != null) {
				ref.addMapping(map);
			}
		}
		catch (DuplicateMappingException e) {}
	}
	
	/**
	 * By default an metadata cannot be removed, but there are situations where is needed to remove
	 * a metadata
	 * 
	 * @return
	 */
	@JsonIgnore
	default Boolean isRemovableMetadata() {
		return utilities.existOnArray(utilities.parseArrayToList(TableConfiguration.REMOVABLE_METADATA),
		    this.getTableName());
	}
	
	@JsonIgnore
	default Boolean existsSyncRecordClass(DBConnectionInfo connInfo) {
		try {
			return this.getSyncRecordClass(connInfo) != null;
		}
		catch (ForbiddenOperationException e) {
			
			return false;
		}
	}
	
	default void generateRecordClass(DBConnectionInfo connInfo, Boolean fullClass) {
		try {
			if (fullClass) {
				this.setSyncRecordClass(DatabaseEntityPOJOGenerator.generate(this, connInfo));
			} else {
				this.setSyncRecordClass(DatabaseEntityPOJOGenerator.generateSkeleton(this, connInfo));
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	default void generateSkeletonRecordClass(DBConnectionInfo application) {
		try {
			this.setSyncRecordClass(DatabaseEntityPOJOGenerator.generateSkeleton(this, application));
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@JsonIgnore
	@Override
	default String generateClassName() {
		return this.generateClassName(this.getTableName());
	}
	
	default String generateClassName(String tableName) {
		String[] nameParts = this.getTableName().split("\\.");
		
		//Ignore the schema
		if (nameParts.length > 1) {
			tableName = nameParts[1];
		}
		
		nameParts = getTableName().split("_");
		
		String className = utilities.capitalize(nameParts[0]);
		
		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}
		
		return className + "VO";
	}
	
	@JsonIgnore
	default String generateRelatedSrcStageTableName() {
		return this.getTableName() + "_src_stage";
	}
	
	@JsonIgnore
	default String generateRelatedDstStageTableName() {
		return this.getTableName() + "_dst_stage";
	}
	
	@JsonIgnore
	default String generateRelatedStageSrcUniqueKeysTableName() {
		return this.generateRelatedSrcStageTableName() + "_src_unique_keys";
	}
	
	@JsonIgnore
	default String generateRelatedStageDstUniqueKeysTableName() {
		return this.generateRelatedSrcStageTableName() + "_dst_unique_keys";
	}
	
	@JsonIgnore
	default String getSyncStageSchema() {
		return this.getRelatedEtlConf().getSyncStageSchema();
	}
	
	@JsonIgnore
	default String generateFullSrcStageTableName() {
		return this.getSyncStageSchema() + "." + this.generateRelatedSrcStageTableName();
	}
	
	@JsonIgnore
	default String generateFullDstStageTableName() {
		return this.getSyncStageSchema() + "." + this.generateRelatedDstStageTableName();
	}
	
	default String generateFullTableName(Connection conn) throws DBException {
		return this.getSchema() + "." + this.getTableName();
	}
	
	default String generateFullTableNameWithAlias(Connection conn) throws DBException, ForbiddenOperationException {
		if (!this.hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.getTableName());
		}
		
		return this.generateFullTableName(conn) + " " + this.getTableAlias();
	}
	
	default String generateFullTableNameWithAlias() {
		if (!this.hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.getTableName());
		}
		if (!this.hasSchema()) {
			throw new ForbiddenOperationException("No schema is defined for table" + this.getTableName());
		}
		
		return this.getSchema() + "." + this.generateTableNameWithAlias();
	}
	
	default String generateTableNameWithAlias() {
		if (!this.hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.getTableName());
		}
		
		return this.getTableName() + " " + this.getTableAlias();
	}
	
	@JsonIgnore
	default String generateFullStageSrcUniqueKeysTableName() {
		return this.getSyncStageSchema() + "." + this.generateRelatedStageSrcUniqueKeysTableName();
	}
	
	@JsonIgnore
	default String generateFullStageDstUniqueKeysTableName() {
		return this.getSyncStageSchema() + "." + this.generateRelatedStageDstUniqueKeysTableName();
	}
	
	@JsonIgnore
	default Boolean existRelatedExportStageTable(Connection conn) {
		return existStageTable(this.generateRelatedSrcStageTableName(), conn);
	}
	
	default Boolean existRelatedDstStageTable(Connection conn) {
		return existStageTable(this.generateRelatedDstStageTableName(), conn);
	}
	
	default Boolean existRelatedStageSrcUniqueKeysTable(Connection conn) {
		return existStageTable(this.generateRelatedStageSrcUniqueKeysTableName(), conn);
	}
	
	default Boolean existRelatedStageDstUniqueKeysTable(Connection conn) {
		return existStageTable(this.generateRelatedStageDstUniqueKeysTableName(), conn);
	}
	
	default Boolean existStageTable(String stageTable, Connection conn) {
		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		
		try {
			
			return DBUtilities.isResourceExist(schema, null, resourceType, stageTable, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@JsonIgnore
	default Boolean isConfigured() {
		for (TableConfiguration tabConf : this.getRelatedEtlConf().getConfiguredTables()) {
			if (tabConf.getTableName().equals(this.getTableName()))
				return true;
		}
		
		return false;
	}
	
	@Override
	default void fullLoad(Connection conn) throws DBException {
		this.tryToGenerateTableAlias(this.getRelatedEtlConf());
		
		synchronized (this) {
			
			try {
				
				if (this.isFullLoaded()) {
					return;
				}
				
				tryToLoadSchemaInfo(null, conn);
				
				this.loadFields(conn);
				
				this.loadPrimaryKeyInfo(conn);
				
				this.loadParents(conn);
				this.loadChildren(conn);
				
				this.loadUniqueKeys(conn);
				
				this.loadAttDefinition(conn);
				
				if (this.getParentConf() instanceof EtlItemConfiguration) {
					EtlItemConfiguration parent = (EtlItemConfiguration) this.getParentConf();
					
					if (this.getAutoIncrementHandlingType() == null) {
						this.setAutoIncrementHandlingType(parent.getAutoIncrementHandlingType());
					}
					
					if (this.getPrimaryKeyInitialIncrementValue() == null) {
						this.setPrimaryKeyInitialIncrementValue(parent.getPrimaryKeyInitialIncrementValue());
					}
				}
				
				if (overrideAutoIncrement()) {
					this.setAutoIncrementId(false);
					this.setIncludePrimaryKeyOnInsert(true);
				} else {
					this.setAutoIncrementId(useAutoIncrementId(conn));
				}
				
				if (!this.includePrimaryKeyOnInsert()) {
					
					//Force the inclusion of primaryKey if the table is not autoincrement or if it uses shared pj
					if ((!this.isAutoIncrementId() || this.useSharedPKKey()
					        || this.getRelatedEtlConf().isDoNotTransformsPrimaryKeys())
					        && !(this instanceof EtlConfigurationTableConf)) {
						this.setIncludePrimaryKeyOnInsert(true);
					}
				}
				
				if (hasExtraConditionForExtract()) {
					setExtraConditionForExtract(
					    SQLUtilities.qualifyUnqualifiedSqlFields(getExtraConditionForExtract(), getTableName()));
				}
				
				if (this.hasExtraConditionForExtract()) {
					this.setExtraConditionForExtract(
					    this.getExtraConditionForExtract().replaceAll(getTableName() + "\\.", this.getTableAlias() + "\\."));
				}
				
				this.loadOwnElements(null, conn);
				
				this.setFullLoaded(true);
				
				getRelatedEtlConf().addToFullLoadedTables(this);
				
			}
			catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
	}
	
	default Boolean overrideAutoIncrement() {
		return this.getAutoIncrementHandlingType() != null && this.getAutoIncrementHandlingType().isIgnoreSchemaDefinition();
	}
	
	default void addParent(ParentTable p) {
		if (!hasParents()) {
			this.setParents(new ArrayList<>());
		}
		
		if (!this.getParents().contains(p)) {
			this.getParents().add(p);
		}
	}
	
	default EtlItemConfiguration retrieveRelatedItemConf() {
		EtlItemConfiguration item = null;
		
		EtlDataConfiguration parent = this.getParentConf();
		
		while (parent != null && !(parent instanceof EtlItemConfiguration)) {
			parent = parent.getParentConf();
		}
		
		return item;
	}
	
	default String getParamValue(EtlDatabaseObject schemaInfoSrc, String paramName) {
		
		Object paramValue = null;
		
		if (schemaInfoSrc != null) {
			try {
				paramValue = schemaInfoSrc.getFieldValue(paramName);
			}
			catch (ForbiddenOperationException e) {}
		}
		
		if (paramValue == null && this.getRelatedEtlConf() != null) {
			paramValue = this.getRelatedEtlConf().getParamValue(paramName);
		}
		
		return paramValue != null ? paramValue.toString() : null;
	}
	
	/**
	 * @throws ForbiddenOperationException
	 */
	@Override
	default void tryToLoadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists {
		
		if (this.isTableNameInfoLoaded())
			return;
		
		if (hasDynamicAlias() && schemaInfoSrc != null) {
			this.setTableAlias(SQLUtilities.tryToReplaceParamsInQuery(this.getAlias(), schemaInfoSrc));
		}
		
		String[] tableNameParts = getTableName().split("\\.");
		
		if (tableNameParts.length == 1) {} else if (tableNameParts.length == 2) {
			this.setTableName(tableNameParts[1]);
			this.setSchema(tableNameParts[0]);
		} else {
			throw new ForbiddenOperationException("The table name " + this.getTableName() + " is malformed!");
		}
		
		if (this.hasSchema() && this.getSchema().startsWith("@")) {
			String normalizedSchema = SQLUtilities.normalizeQuery(this.getSchema());
			
			String param = utilities.removeFirsChar(normalizedSchema);
			
			Object paramValue = this.getParamValue(schemaInfoSrc, param);
			
			if (paramValue == null && !this.ignoreMissingParameters()) {
				throw new ForbiddenOperationException("You should configure the parameter '" + param + "'");
			}
			
			if (paramValue != null) {
				this.setSchema(paramValue.toString());
			}
		}
		
		if (this.getTableName().startsWith("@")) {
			String normalizedTableName = SQLUtilities.normalizeQuery(this.getTableName());
			
			String param = utilities.removeFirsChar(normalizedTableName);
			
			Object paramValue = this.getParamValue(schemaInfoSrc, param);
			
			if (paramValue == null && !this.ignoreMissingParameters()) {
				throw new ForbiddenOperationException("You should configure the parameter '" + param + "'");
			}
			
			if (paramValue != null) {
				this.setTableName(paramValue.toString());
			}
		}
		
	}
	
	/**
	 * @param conn
	 * @throws DBException
	 */
	default void loadFields(Connection conn) throws DBException {
		this.logDebug("Loading field for table " + getFullTableDescription());
		
		if (this.isFieldsLoaded())
			return;
		
		List<Field> flds = null;
		
		try {
			flds = DBUtilities.getTableFields(this.getTableName(), this.getSchema(), conn);
		}
		catch (Exception e) {
			throw e;
		}
		
		this.setFields(new ArrayList<>());
		
		for (Field f : flds) {
			if (!this.isIgnorableField(f)) {
				this.getFields().add(f);
			}
		}
		
		logTrace("Fields for table " + this.getFullTableDescription() + " Loaded!");
	}
	
	default void loadAttDefinition(Connection conn) {
		int qtyAttrs = this.getFields().size();
		
		for (int i = 0; i < qtyAttrs - 1; i++) {
			Field field = this.getFields().get(i);
			
			field.setAttDefinedElements(AttDefinedElements.define(field.getName(), field.getDataType(), false, this));
		}
		
		Field field = this.getFields().get(qtyAttrs - 1);
		
		field.setAttDefinedElements(AttDefinedElements.define(field.getName(), field.getDataType(), true, this));
		
		this.generateSQLElemenets();
	}
	
	@Override
	default void fullLoad() throws DBException {
		synchronized (this) {
			OpenConnection mainConn = this.getRelatedEtlConf().getSrcConnInfo().openConnection();
			
			OpenConnection dstConn = null;
			
			try {
				this.fullLoad(mainConn);
			}
			finally {
				mainConn.finalizeConnection();
				
				if (dstConn != null) {
					dstConn.finalizeConnection();
				}
			}
		}
	}
	
	default ParentTable getSharedTableConf(Connection conn) {
		if (this.getSharedKeyRefInfo(conn) != null) {
			return this.getSharedKeyRefInfo(conn);
		}
		
		return null;
	}
	
	default ParentTable getSharedKeyRefInfo(Connection conn) {
		if (this.getSharePkWith() == null) {
			return null;
		} else if (this.hasParentRefInfo()) {
			
			for (ParentTable parent : this.getParentRefInfo()) {
				if (parent.getTableName().equalsIgnoreCase(this.getSharePkWith())) {
					
					if (!parent.isFullLoaded()) {
						try {
							
							if (conn == null) {
								conn = this.getRelatedEtlConf().openSrcConn();
							}
							
							parent.fullLoad(conn);
						}
						catch (DBException e) {
							throw new EtlExceptionImpl(e);
						}
					}
					
					PrimaryKey pk = (PrimaryKey) parent.getPrimaryKey();
					
					PrimaryKey refInfoKey = new PrimaryKey(parent);
					refInfoKey.setFields(parent.extractParentFieldsFromRefMapping());
					
					if (pk.hasSameFields(refInfoKey)) {
						return parent;
					}
				}
			}
		}
		
		throw new ForbiddenOperationException("The related table of shared pk " + getSharePkWith() + " of table "
		        + this.getTableName() + " is not listed in parents!");
	}
	
	default TableConfiguration findParentOnChildRefInfo(String parentTableName) {
		for (ChildTable ref : this.getChildRefInfo()) {
			if (parentTableName.equals(ref.getParentTableConf().getTableName())) {
				return ref.getParentTableConf();
			}
		}
		
		return null;
	}
	
	default EtlDatabaseObject find(String condition, Object[] params, Connection conn)
	        throws DBException, ForbiddenOperationException {
		String sql = generateSelectFromQuery();
		sql += "WHERE " + condition;
		
		return DatabaseObjectDAO.find(getLoadHealper(), this.getSyncRecordClass(), sql, params, conn);
	}
	
	default ParentTable findParentRefInfoByField(String fieldName) {
		for (ParentTable ref : this.getParentRefInfo()) {
			try {
				if (ref.getChildColumnOnSimpleMapping().equals(fieldName)) {
					return ref;
				}
			}
			catch (ForbiddenOperationException e) {}
		}
		
		return null;
	}
	
	default String getFullTableName() {
		return this.hasSchema() ? this.getSchema() + "." + this.getTableName() : this.getTableName();
	}
	
	default String generateFullTableNameOnSchema(String schema) {
		return schema + "." + this.getTableName();
	}
	
	default String getFullTableDescription() {
		return this.getFullTableName() + (this.hasAlias() ? " as " + this.getTableAlias() : "");
	}
	
	default List<ParentTable> findAllRefToParent(String parentTableName) {
		if (!hasParentRefInfo()) {
			return null;
		}
		
		List<ParentTable> references = new ArrayList<>();
		
		for (ParentTable parent : this.getParentRefInfo()) {
			if (parentTableName.equals(parent.getTableName())) {
				references.add(parent);
			}
		}
		
		return references;
	}
	
	default List<ParentTable> findAllRefToParent(String parentTableName, String schema) {
		if (!hasParentRefInfo()) {
			return null;
		}
		
		List<ParentTable> references = new ArrayList<>();
		
		for (ParentTable parent : this.getParentRefInfo()) {
			if (parentTableName.equals(parent.getTableName()) && schema.equals(parent.getSchema())) {
				references.add(parent);
			}
		}
		
		return references;
	}
	
	@JsonIgnore
	default Boolean isDestinationInstallationType() {
		return getRelatedEtlConf().isDataBaseMergeFromJSONProcess();
	}
	
	@JsonIgnore
	default Boolean isDataReconciliationProcess() {
		return this.getRelatedEtlConf().isDataReconciliationProcess();
	}
	
	@JsonIgnore
	default Boolean isDBQuickLoad() {
		return this.getRelatedEtlConf().isDBQuickLoadProcess();
	}
	
	@JsonIgnore
	default Boolean isDataBasesMergeFromSourceDBProcess() {
		return this.getRelatedEtlConf().isDataBaseMergeFromSourceDBProcess();
	}
	
	@JsonIgnore
	default Boolean hasNoDateVoidedField() {
		return utilities.isStringIn(getTableName(), "note");
	}
	
	@JsonIgnore
	default Boolean hasNotDateChangedField() {
		return utilities.isStringIn(getTableName(), "obs");
	}
	
	/**
	 * Generates SQL condition using the {@link #uniqueKeys} fulfilled with related values from
	 * especific object
	 * 
	 * @param dbObject the object from where the condition values will be retrieved from
	 * @return a SQL condition
	 */
	@JsonIgnore
	default String generateUniqueKeysParametrizedCondition(EtlDatabaseObject dbObject) {
		if (this.getUniqueKeys() == null)
			return null;
		
		String joinCondition = "";
		
		for (int i = 0; i < this.getUniqueKeys().size(); i++) {
			
			String uniqueKeyJoinField = this.generateUniqueKeyConditionsFields(this.getUniqueKeys().get(i), dbObject);
			
			if (i > 0)
				joinCondition += " OR ";
			
			joinCondition += "(" + uniqueKeyJoinField + ")";
		}
		
		return joinCondition;
	}
	
	/**
	 * Generates SQL parametrized condition for {@link #uniqueKeys}
	 * 
	 * @return a parametrized SQL condition
	 */
	@JsonIgnore
	default String generateUniqueKeysParametrizedCondition() {
		String joinCondition = "";
		
		for (int i = 0; i < this.getUniqueKeys().size(); i++) {
			
			String uniqueKeyJoinField = this.generateUniqueKeyConditionsFields(this.getUniqueKeys().get(i));
			
			if (i > 0)
				joinCondition += " OR ";
			
			joinCondition += "(" + uniqueKeyJoinField + ")";
		}
		
		return joinCondition;
	}
	
	default String generateUniqueKeyConditionsFields(UniqueKeyInfo uniqueKey) {
		String joinFields = "";
		
		List<Key> uniqueKeyFields = uniqueKey.getFields();
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				joinFields += " AND ";
			
			joinFields += uniqueKeyFields.get(i) + " = ? ";
		}
		
		return joinFields;
	}
	
	default String generateUniqueKeyConditionsFields(UniqueKeyInfo uniqueKey, EtlDatabaseObject dbObject) {
		String conditionFields = "";
		
		List<Key> uniqueKeyFields = uniqueKey.getFields();
		
		uniqueKey.loadValuesToFields(dbObject);
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			Field field = uniqueKeyFields.get(i);
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getName(), field.getValue());
		}
		
		return conditionFields;
	}
	
	default Boolean hasUniqueKeys() {
		return utilities.listHasElement(this.getUniqueKeys());
	}
	
	default Boolean useSimpleNumericPk() {
		return this.getPrimaryKey() != null && ((PrimaryKey) this.getPrimaryKey()).isSimpleNumericKey();
	}
	
	default Boolean useSimplePk() {
		return this.getPrimaryKey() != null && ((PrimaryKey) this.getPrimaryKey()).isSimpleNumericKey();
	}
	
	default Boolean useAutoIncrementId(Connection conn) throws DBException {
		
		if (this.getPrimaryKey() == null || this.getPrimaryKey().isCompositeKey()) {
			return false;
		}
		
		return DBUtilities.checkIfTableUseAutoIcrement(this.getSchema(), this.getTableName(), conn);
	}
	
	default List<ParentTable> getConditionalParents() {
		List<ParentTable> conditionalParents = null;
		
		if (this.hasParents()) {
			
			conditionalParents = new ArrayList<>();
			
			for (ParentTable p : this.getParents()) {
				if (p.hasConditionalFields()) {
					conditionalParents.add(p);
				}
			}
		}
		
		return conditionalParents;
	}
	
	default void generateSQLElemenets() {
		this.generateInsertSQLWithObjectId();
		this.generateInsertSQLWithoutObjectId();
		this.setUpdateSql(this.generateUpdateSQL());
	}
	
	void setUpdateSql(java.lang.String generateUpdateSQL);
	
	default String generateFullFilledUpdateSql(EtlDatabaseObject obj) {
		if (this.getPrimaryKey() == null) {
			
			if (this.getUniqueKeys() == null) {
				throw new ForbiddenOperationException("Impossible to generate update params, there is no primary key");
			}
		}
		
		String updateSQL = "UPDATE " + this.getObjectName() + " SET ";
		
		for (Field field : this.getFields()) {
			AttDefinedElements attElements = field.getAttDefinedElements();
			
			updateSQL = utilities.concatStrings(updateSQL, attElements.getSqlUpdateDefinition(obj));
		}
		
		if (this.getPrimaryKey() != null) {
			obj.loadObjectIdData(this);
			
			updateSQL += " WHERE " + obj.getObjectId().parseToFilledStringConditionWithoutAlias();
		} else {
			List<UniqueKeyInfo> cloned = UniqueKeyInfo.cloneAllAndLoadValues(getUniqueKeys(), obj);
			
			updateSQL += " WHERE " + UniqueKeyInfo.parseToFilledStringConditionToAllWithoutAlias(cloned);
		}
		
		return updateSQL;
	}
	
	default Object[] generateInsertParamsWithObjectId(EtlDatabaseObject obj) {
		int qtyAttrs = this.getFields().size();
		
		Object[] params = new Object[qtyAttrs];
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Field field = this.getFields().get(i);
			
			params[i] = obj.getFieldValue(field.getName());
		}
		
		return params;
	}
	
	default Object[] generateInsertParamsWithoutObjectId(EtlDatabaseObject obj) {
		int qtyAttrs = this.getFields().size() - this.getPrimaryKey().getFields().size();
		
		Object[] params = new Object[qtyAttrs];
		
		AttDefinedElements attElements;
		
		int i = 0;
		
		for (Field field : this.getFields()) {
			attElements = field.getAttDefinedElements();
			
			if (!attElements.isPartOfObjectId()) {
				params[i++] = obj.getFieldValue(field.getName());
			}
		}
		
		return params;
	}
	
	default void generateInsertSQLWithoutObjectId() {
		String insertSQLFieldsWithoutObjectId = "";
		String insertSQLQuestionMarksWithoutObjectId = "";
		
		for (Field field : this.getFields()) {
			AttDefinedElements attElements = field.getAttDefinedElements();
			
			if (!attElements.isPartOfObjectId()) {
				insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId,
				    attElements.getSqlInsertFirstPartDefinition());
				
				insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId,
				    attElements.getSqlInsertLastEndPartDefinition());
			}
		}
		
		this.setInsertSQLWithoutObjectId("INSERT INTO " + this.getFullTableName() + "(" + insertSQLFieldsWithoutObjectId
		        + ") VALUES( " + insertSQLQuestionMarksWithoutObjectId + ");");
		
		this.setInsertSQLQuestionMarksWithoutObjectId(insertSQLQuestionMarksWithoutObjectId);
	}
	
	default void generateInsertSQLWithObjectId() {
		String insertSQLFieldsWithObjectId = "";
		String insertSQLQuestionMarksWithObjectId = "";
		
		for (Field field : this.getFields()) {
			AttDefinedElements attElements = field.getAttDefinedElements();
			
			insertSQLFieldsWithObjectId = utilities.concatStrings(insertSQLFieldsWithObjectId,
			    attElements.getSqlInsertFirstPartDefinition());
			
			insertSQLQuestionMarksWithObjectId = utilities.concatStrings(insertSQLQuestionMarksWithObjectId,
			    attElements.getSqlInsertLastEndPartDefinition());
		}
		
		this.setInsertSQLWithObjectId("INSERT INTO " + this.getFullTableName() + "(" + insertSQLFieldsWithObjectId
		        + ") VALUES( " + insertSQLQuestionMarksWithObjectId + ");");
		
		this.setInsertSQLQuestionMarksWithObjectId(insertSQLQuestionMarksWithObjectId);
	}
	
	@JsonIgnore
	default String generateUpdateSQL() {
		if (this.getPrimaryKey() == null) {
			
			if (this.getUniqueKeys() == null) {
				throw new ForbiddenOperationException("Impossible to generate update params, there is no primary key");
			}
		}
		
		String updateSQL = "UPDATE " + this.getFullTableName() + " SET ";
		
		for (Field field : this.getFields()) {
			AttDefinedElements attElements = field.getAttDefinedElements();
			
			updateSQL = utilities.concatStrings(updateSQL, attElements.getSqlUpdateDefinition());
			
		}
		
		if (this.getPrimaryKey() != null) {
			updateSQL += " WHERE " + this.getPrimaryKey().parseToParametrizedStringConditionWithoutAlias();
		} else {
			updateSQL += " WHERE " + UniqueKeyInfo.parseToParametrizedStringConditionToAllWithoutAlias(this.getUniqueKeys());
		}
		
		return updateSQL;
	}
	
	@JsonIgnore
	default Object[] generateUpdateParams(EtlDatabaseObject obj) {
		if (this.getPrimaryKey() == null && (this.getUniqueKeys() == null || this.getUniqueKeys().isEmpty()))
			throw new ForbiddenOperationException("Impossible to generate update params, there is unique key defied");
		
		int qtyAttrs = this.getFields().size();
		
		List<Key> keys = null;
		
		if (this.getPrimaryKey() != null) {
			keys = this.getPrimaryKey().getFields();
		} else {
			keys = new ArrayList<>();
			
			for (UniqueKeyInfo key : this.getUniqueKeys()) {
				keys.addAll(key.getFields());
			}
		}
		
		Object[] params = new Object[qtyAttrs + keys.size()];
		
		int i = 0;
		
		for (i = 0; i < this.getFields().size(); i++) {
			Field field = this.getFields().get(i);
			
			params[i] = obj.getFieldValue(field.getName());
		}
		
		for (Key key : keys) {
			params[i++] = obj.getFieldValue(key.getName());
		}
		
		return params;
		
	}
	
	default String generateInsertValuesWithoutObjectId(EtlDatabaseObject obj) {
		String insertValuesWithObjectIdDefinition = "";
		
		AttDefinedElements attElements;
		
		for (Field field : this.getFields()) {
			attElements = field.getAttDefinedElements();
			
			if (!attElements.isPartOfObjectId()) {
				insertValuesWithObjectIdDefinition = utilities.concatStrings(insertValuesWithObjectIdDefinition,
				    attElements.defineSqlInsertValue(obj));
			}
		}
		
		return insertValuesWithObjectIdDefinition;
	}
	
	default String generateInsertValuesWithObjectId(EtlDatabaseObject obj) {
		String insertValuesWithObjectIdDefinition = "";
		
		AttDefinedElements attElements;
		
		for (Field field : this.getFields()) {
			attElements = field.getAttDefinedElements();
			
			insertValuesWithObjectIdDefinition = utilities.concatStrings(insertValuesWithObjectIdDefinition,
			    attElements.defineSqlInsertValue(obj));
		}
		
		return insertValuesWithObjectIdDefinition;
	}
	
	public abstract Boolean isGeneric();
	
	/**
	 * Generate a select columns content using the alias {@link #tableAlias}
	 * 
	 * @return
	 * @throws ForbiddenOperationException if the table does not have alias
	 */
	@JsonIgnore
	default String generateFullAliasedSelectColumns() throws ForbiddenOperationException {
		
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.getTableName());
		}
		
		String fullSelectColumns = "";
		
		if (this.getFields() == null) {
			throw new ForbiddenOperationException("Null Fields not allowed");
		}
		
		for (Field f : this.getFields()) {
			fullSelectColumns = utilities.concatStringsWithSeparator(fullSelectColumns, f.generateAliasedSelectColumn(this),
			    ",\n");
		}
		
		if (this.useSharedPKKey()) {
			fullSelectColumns += "," + this.getSharedTableConf(null).generateFullAliasedSelectColumns();
		}
		
		return fullSelectColumns;
	}
	
	/**
	 * Generates the content for SELECT FROM clause content.
	 * 
	 * @return
	 */
	default String generateSelectFromClauseContent() {
		String fromClause = this.generateFullTableNameWithAlias();
		
		if (useSharedPKKey()) {
			fromClause += "\n INNER JOIN " + this.getSharedTableConf(null).generateFullTableNameWithAlias() + " ON "
			        + this.getSharedKeyRefInfo(null).generateJoinCondition();
		}
		
		return fromClause;
	}
	
	default Boolean hasSchema() {
		return utilities.stringHasValue(getSchema());
	}
	
	default List<FieldsMapping> tryToLoadJoinFields(TableConfiguration relatedTabConf, Connection conn) {
		
		List<FieldsMapping> joinFields = new ArrayList<>();
		
		//Assuming that this datasource is parent
		List<ParentTable> pInfo = relatedTabConf.findAllRefToParent(this.getTableName());
		
		if (utilities.listHasElement(pInfo)) {
			
			if (utilities.listHasExactlyOneElement(pInfo)) {
				
				ParentTable ref = pInfo.get(0);
				
				for (RefMapping map : ref.getRefMapping()) {
					joinFields
					        .add(new FieldsMapping(map.getParentField().getName(), "", map.getChildField().getName(), conn));
				}
			} else {
				throw new ForbiddenOperationException(
				        "The mapping joinFields cannot be auto generated! Multiple references were found between "
				                + this.getTableName() + " And " + relatedTabConf.getTableName()
				                + ". Please define joinFields manually ");
				
			}
		} else {
			
			//Assuning that the this data src is child
			pInfo = this.findAllRefToParent(relatedTabConf.getTableName());
			
			if (utilities.listHasElement(pInfo)) {
				if (utilities.listHasExactlyOneElement(pInfo)) {
					
					ParentTable ref = pInfo.get(0);
					
					for (RefMapping map : ref.getRefMapping()) {
						joinFields.add(
						    new FieldsMapping(map.getChildField().getName(), "", map.getParentField().getName(), conn));
					}
				} else {
					throw new ForbiddenOperationException(
					        "The mapping cannot be auto generated! Multiple references were found between "
					                + this.getTableName() + " And " + relatedTabConf.getTableName());
					
				}
			}
		}
		
		if (!utilities.listHasElement(joinFields)) {
			throw new MissingJoiningElementsException(this, relatedTabConf);
		}
		
		return joinFields;
	}
	
	default String generateConditionsFields(EtlDatabaseObject parentObject, List<FieldsMapping> joinFields,
	        String joinExtraCondition) {
		String conditionFields = "";
		
		if (utilities.listHasElement(joinFields)) {
			
			for (int i = 0; i < joinFields.size(); i++) {
				if (i > 0)
					conditionFields += " AND ";
				
				FieldsMapping field = joinFields.get(i);
				
				if (!field.hasSrcField()) {
					field.setSrcField(field.getDstField());
				}
				
				//By default the joining value is marked as parameter 
				Object value = "@" + field.getDstField();
				
				if (parentObject != null) {
					try {
						value = parentObject.getFieldValue(field.getDstField());
					}
					catch (ForbiddenOperationException e) {
						value = parentObject.getFieldValue(field.getDstFieldAsClassField());
					}
				}
				
				conditionFields += AttDefinedElements
				        .defineSqlAtribuitionString(tryToConvertFieldToAlias(field.getSrcField()), value);
			}
		}
		
		if (utilities.stringHasValue(joinExtraCondition)) {
			conditionFields += " AND (" + joinExtraCondition + ")";
		}
		
		return utilities.stringHasValue(conditionFields) ? conditionFields : "1=1";
	}
	
	default String tryToConvertFieldToAlias(String fieldName) {
		if (this.containsField(fieldName)) {
			return getTableAlias() + "." + fieldName;
		}
		
		return fieldName;
	}
	
	default String generateJoinCondition(TableConfiguration joiningTable, List<FieldsMapping> joinFields,
	        String joinExtraCondition) {
		String conditionFields = "";
		
		for (int i = 0; i < joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = joinFields.get(i);
			
			conditionFields += this.tryToConvertFieldToAlias(field.getSrcField()) + " = "
			        + joiningTable.tryToConvertFieldToAlias(field.getDstField());
		}
		
		if (utilities.stringHasValue(joinExtraCondition)) {
			conditionFields += " AND (" + joinExtraCondition + ")";
		}
		
		return conditionFields;
	}
	
	default TableConfiguration findFullConfiguredConfInAllRelatedTable(String fullTableName,
	        List<Integer> alreadyCheckedObjects) {
		
		if (alreadyCheckedObjects == null) {
			throw new ForbiddenOperationException("The 'alreadyCheckedObjects' list should be not null!!!");
		}
		
		Integer identity = System.identityHashCode(this);
		
		if (alreadyCheckedObjects.contains(identity)) {
			logTrace("Skipping verification of table '" + fullTableName + "' on table " + this.getFullTableName() + "("
			        + System.identityHashCode(this) + ")");
			
			return null;
		}
		
		if (this.getFullTableName().equals(fullTableName) && this.isFullLoaded()) {
			return this;
		}
		
		logTrace("Finding Configured table '" + fullTableName + "' on Table '" + this.getFullTableName() + "("
		        + System.identityHashCode(this) + ") with Parents: [" + this.getParentRefInfoAsString() + "]");
		
		alreadyCheckedObjects.add(identity);
		
		if (this.hasParentRefInfo()) {
			for (ParentTable p : this.getParentRefInfo()) {
				TableConfiguration fullLoadedTable = p.findFullConfiguredConfInAllRelatedTable(fullTableName,
				    alreadyCheckedObjects);
				
				if (fullLoadedTable != null) {
					return fullLoadedTable;
				}
			}
		}
		
		if (this.hasChildRefInfo()) {
			for (ChildTable p : this.getChildRefInfo()) {
				TableConfiguration fullLoadedTable = p.findFullConfiguredConfInAllRelatedTable(fullTableName,
				    alreadyCheckedObjects);
				
				if (fullLoadedTable != null) {
					return fullLoadedTable;
				}
			}
		}
		
		return null;
	}
	
	String getSchema();
	
	default String getCatalog(Connection conn) throws DBException {
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				return getSchema();
			} else {
				return conn.getCatalog();
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	default void fullLoadAllRelatedTables(TableAliasesGenerator aliasGenerator, TableConfiguration related, Connection conn)
	        throws DBException {
		if (this.isAllRelatedTablesFullLoaded()) {
			return;
		}
		
		if (this.hasParentRefInfo()) {
			for (ParentTable ref : this.getParentRefInfo()) {
				
				TableConfiguration existingConf = null;
				
				if (related != null) {
					existingConf = related.findFullConfiguredConfInAllRelatedTable(ref.getFullTableName(),
					    new ArrayList<>());
				}
				
				if (existingConf == null) {
					existingConf = this.findFullConfiguredConfInAllRelatedTable(ref.getFullTableName(), new ArrayList<>());
				}
				
				ref.tryToGenerateTableAlias(aliasGenerator);
				
				if (existingConf != null) {
					ref.clone(existingConf, this, null, conn);
					
				} else {
					ref.fullLoad(conn);
				}
			}
			
			this.setAllRelatedTablesFullLoaded(true);
		}
	}
	
	default Boolean containsField(String fieldName) {
		if (!this.hasFields())
			return false;
		
		for (Field f : this.getFields()) {
			if (f.getName().equals(fieldName) || f.generateAliasedColumn(this).equals(fieldName)) {
				return true;
			}
		}
		
		return false;
	}
	
	default Boolean isIgnorableField(Field field) {
		if (!this.hasIgnorableField())
			return false;
		
		for (String ignorable : this.getIgnorableFields()) {
			if (field.getName().equals(ignorable)) {
				return true;
			}
		}
		
		return false;
	}
	
	default Boolean hasIgnorableField() {
		return utilities.listHasElement(this.getIgnorableFields());
	}
	
	/**
	 * Usually called after a call to {@link #fullLoad()}, allow the loading of own elements. Eg.
	 * the join fields,etc
	 */
	void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException;
	
	/**
	 * Generates a full dump select from query.
	 * 
	 * @return the generated select dump query
	 */
	default String generateSelectFromQuery() {
		String sql = " SELECT " + this.generateFullAliasedSelectColumns() + "\n";
		sql += " FROM " + this.generateSelectFromClauseContent() + "\n";
		
		return sql;
	}
	
	default Boolean checkIfFieldIsForeignKey(Field field) {
		
		if (!this.hasParentRefInfo())
			return false;
		
		for (ParentTable p : this.getParentRefInfo()) {
			if (p.checkIfContainsRefMappingByChildName(field.getName())) {
				return true;
			}
		}
		
		return false;
	}
	
	default ParentTable getFieldIsRelatedParent(Field field) {
		
		if (!this.hasParentRefInfo())
			return null;
		
		for (ParentTable p : this.getParentRefInfo()) {
			if (p.checkIfContainsRefMappingByChildName(field.getName())) {
				return p;
			}
		}
		
		return null;
	}
	
	default ParentTable findParentRefInfoByParentTable(String parentTable) {
		List<ParentTable> parents = findAllRefToParent(parentTable);
		
		if (utilities.listHasElement(parents)) {
			if (utilities.listHasExactlyOneElement(parents)) {
				return parents.get(0);
			}
			
			throw new EtlExceptionImpl(
			        "Multiple references to parent '" + parentTable + "' found within table '" + this.getTableName() + "'");
		}
		
		return null;
	}
	
	default void tryToGenerateTableAlias(TableAliasesGenerator aliasGenerator) {
		synchronized (this) {
			if (this.hasAlias())
				return;
			
			if (!this.useDynamicTableName()) {
				aliasGenerator.generateAliasForTable(this);
			}
		}
	}
	
	default EtlDatabaseObject createRecordInstance() {
		try {
			@SuppressWarnings("deprecation")
			EtlDatabaseObject rec = this.getSyncRecordClass().newInstance();
			
			rec.setRelatedConfiguration(this);
			
			return rec;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	default Boolean hasObservationDateFields() {
		return utilities.listHasElement(this.getObservationDateFields());
	}
	
	default void addUniqueKey(UniqueKeyInfo uk) {
		if (!this.hasUniqueKeys()) {
			this.setUniqueKeys(new ArrayList<>());
		}
		
		if (!uk.isContained(this.getUniqueKeys())) {
			uk.setTabConf(this);
			
			this.getUniqueKeys().add(uk);
		} else {
			throw new ForbiddenOperationException(
			        "The uk you are trying to add is already in the uk list for this table " + this.getTableName());
		}
		
	}
	
	default Boolean containsAllFields(List<Field> fields) {
		if (!this.hasFields() || fields == null)
			return false;
		
		for (Field f : fields) {
			if (!this.containsField(f.getName()))
				return false;
		}
		
		return true;
	}
	
	default List<ParentTable> tryToCloneAllParentsForOtherTable(TableConfiguration tableToCloneTo, Connection conn)
	        throws DBException {
		
		if (!hasParents())
			return null;
		
		List<ParentTable> parents = new ArrayList<>();
		
		for (ParentTable parentToCloneFrom : this.getParents()) {
			if (DBUtilities.isTableExists(tableToCloneTo.getSchema(), parentToCloneFrom.getTableName(), conn)) {
				ParentTable clonedParent = new ParentTableImpl();
				
				clonedParent.setTableName(parentToCloneFrom.getTableName());
				clonedParent.setSchema(tableToCloneTo.getSchema());
				clonedParent.setRelatedEtlConfig(getRelatedEtlConf());
				clonedParent.loadFields(conn);
				clonedParent.setChildTableConf(tableToCloneTo);
				clonedParent.setConditionalFields(parentToCloneFrom.getConditionalFields());
				clonedParent.setDefaultValueDueInconsistency(parentToCloneFrom.getDefaultValueDueInconsistency());
				clonedParent.setSetNullDueInconsistency(parentToCloneFrom.isSetNullDueInconsistency());
				
				if (clonedParent.containsAllFields(parentToCloneFrom.parseMappingToParentFields())) {
					for (RefMapping map : parentToCloneFrom.getRefMapping()) {
						RefMapping clonedMap = map.clone();
						clonedMap.setParentTabConf((ParentTableImpl) clonedParent);
						
						clonedParent.addRefMapping(clonedMap);
					}
				}
				
				parents.add(clonedParent);
			}
		}
		
		return parents;
	}
	
	/**
	 * Checks if this table configuration has its own unique keys rather that the keys from the
	 * shared key parent
	 * 
	 * @return true if this table configuration has its own unique keys rather that the keys from
	 *         the shared key parent or false in contrary
	 */
	default Boolean hasItsOwnKeys() {
		
		if (!this.hasUniqueKeys())
			return false;
		
		if (!this.useSharedPKKey())
			return true;
		
		for (UniqueKeyInfo keyInfo : this.getUniqueKeys()) {
			if (containsAllFields(utilities.parseList(keyInfo.getFields(), Field.class))) {
				return true;
			}
		}
		
		return false;
	}
	
	static String generateInsertDump(List<EtlDatabaseObject> objects) throws DBException {
		if (utilities.listHasNoElement(objects))
			return null;
		
		String sql = objects.get(0).getInsertSQLWithObjectId().split("VALUES")[0];
		
		sql += " VALUES";
		
		sql = sql.toLowerCase();
		
		String values = "";
		
		for (int i = 0; i < objects.size(); i++) {
			
			values += "(" + objects.get(i).generateInsertValuesWithObjectId() + "),";
		}
		
		return utilities.removeLastChar(values);
	}
	
	default void generateStagingTables(Connection conn) throws DBException {
		
		synchronized (this.getTableName()) {
			
			conn = this.getRelatedEtlConf().openSrcConn();
			
			this.logDebug("UPGRATING TABLE INFO [" + this.getTableName() + "]");
			
			if (!this.existRelatedExportStageTable(conn)) {
				this.logDebug("GENERATING RELATED STAGE TABLE FOR [" + this.getTableName() + "]");
				
				this.createRelatedSrcStageAreaTable(conn);
				
				this.logDebug("RELATED STAGE TABLE FOR [" + this.getTableName() + "] GENERATED");
			}
			
			if (!this.existRelatedDstStageTable(conn)) {
				this.logDebug("GENERATING RELATED DST STAGE TABLE FOR [" + this.getTableName() + "]");
				
				this.createRelatedDstSyncStage(conn);
				
				this.logDebug("RELATED DST STAGE TABLE FOR [" + this.getTableName() + "] GENERATED");
			}
			
			if (!this.existRelatedStageSrcUniqueKeysTable(conn)) {
				this.logDebug("GENERATING RELATED STAGE ORIGIN UNIQUE KEYS TABLE FOR [" + this.getTableName() + "]");
				
				this.createRelatedStageAreaSrcUniqueKeysTable(conn);
				
				this.logDebug("RELATED STAGE SRC UNIQUE KEYS TABLE FOR [" + this.getTableName() + "] GENERATED");
			}
			
			if (!this.existRelatedStageDstUniqueKeysTable(conn)) {
				this.logDebug("GENERATING RELATED STAGE DST UNIQUE KEYS TABLE FOR [" + this.getTableName() + "]");
				
				this.createRelatedSyncStageAreaDstUniqueKeysTable(conn);
				
				this.logDebug("RELATED STAGE DST UNIQUE KEYS TABLE FOR [" + this.getTableName() + "] GENERATED");
			}
			
			this.logDebug("THE PREPARATION OF TABLE '" + getTableName() + "' IS FINISHED!");
			
		}
		
	}
	
	default EtlConfigurationTableConf generateRelatedSrcStageTableConf(Connection conn) throws DBException {
		if (!existRelatedExportStageTable(conn)) {
			createRelatedSrcStageAreaTable(conn);
		}
		
		return this.generateRelatedStageTabConf(this.generateRelatedSrcStageTableName(), this.getSyncStageSchema(), conn);
	}
	
	default EtlConfigurationTableConf generateRelatedDstStageTableConf(Connection conn) throws DBException {
		if (!existRelatedDstStageTable(conn)) {
			createRelatedDstSyncStage(conn);
		}
		
		return this.generateRelatedStageTabConf(this.generateRelatedDstStageTableName(), this.getSyncStageSchema(), conn);
	}
	
	default EtlConfigurationTableConf generateRelatedStageDstUniqueKeysTableConf(Connection conn) throws DBException {
		if (!existRelatedStageDstUniqueKeysTable(conn)) {
			createRelatedSyncStageAreaDstUniqueKeysTable(conn);
		}
		
		return this.generateRelatedStageTabConf(this.generateRelatedStageDstUniqueKeysTableName(), this.getSyncStageSchema(),
		    conn);
	}
	
	default EtlConfigurationTableConf generateRelatedStageSrcUniqueKeysTableConf(Connection conn) throws DBException {
		if (!existRelatedStageSrcUniqueKeysTable(conn)) {
			createRelatedStageAreaSrcUniqueKeysTable(conn);
		}
		
		return this.generateRelatedStageTabConf(this.generateRelatedStageSrcUniqueKeysTableName(), this.getSyncStageSchema(),
		    conn);
	}
	
	default EtlConfigurationTableConf generateRelatedStageTabConf(String tableName, String schema, Connection conn)
	        throws DBException {
		
		synchronized (tableName) {
			TableConfiguration tabConf = this.getRelatedEtlConf().findOnFullLoadedTables(tableName, schema);
			
			if (tabConf == null) {
				tabConf = new EtlConfigurationTableConf(tableName, this.getRelatedEtlConf());
			}
			
			if (!tabConf.isFullLoaded()) {
				tabConf.fullLoad(conn);
			}
			
			return (EtlConfigurationTableConf) tabConf;
		}
		
	}
	
	default void createRelatedDstSyncStage(Connection conn) throws DBException {
		
		synchronized (lock) {
			if (!DBUtilities.isTableExists(this.getSyncStageSchema(), this.generateRelatedDstStageTableName(), conn)) {
				
				String sql = "";
				String notNullConstraint = "NOT NULL";
				String nullConstraint = "NULL";
				String endLineMarker = ",\n";
				
				String tableName = this.generateRelatedDstStageTableName();
				
				String fullTableName = this.getSyncStageSchema() + "." + tableName;
				
				sql += "CREATE TABLE " + fullTableName + "(\n";
				sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
				sql += DBUtilities.generateTableBigIntField("stage_record_id", notNullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("dst_table_name", 100, notNullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("dst_compacted_object_uk", 190, nullConstraint, conn)
				        + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("src_stage_table_name", 100, notNullConstraint, conn)
				        + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("etl_confing_id", 190, nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("conflict_resolution_type", 30, notNullConstraint, conn)
				        + endLineMarker;
				
				sql += DBUtilities.generateTableDateTimeField("last_sync_date", nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("last_sync_try_err", 500, nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableNumericField("consistent", 1, nullConstraint, -1, conn) + endLineMarker;
				sql += DBUtilities.generateTableNumericField("migration_status", 1, nullConstraint, 1, conn) + endLineMarker;
				
				sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
				
				String checkCondition = "migration_status IN (-1,0,1,2)";
				String keyName = "CHK_" + tableName + "_MIG_STATUS".toUpperCase();
				
				sql += DBUtilities.generateTableCheckConstraintDefinition(keyName, checkCondition, conn) + endLineMarker;
				
				sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unique_key_dst".toLowerCase(),
				    "dst_compacted_object_uk", conn) + endLineMarker;
				
				sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_dst".toLowerCase(),
				    "stage_record_id, src_stage_table_name, etl_confing_id, dst_table_name", conn) + endLineMarker;
				
				sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
				sql += ")";
				
				String indexName1 = tableName + "_idx";
				String indexFields1 = "stage_record_id, src_stage_table_name";
				
				String idxDefinition1 = DBUtilities.generateIndexDefinition(fullTableName, indexName1, indexFields1, conn);
				
				String indexName2 = tableName + "_src_rec_idx";
				String indexFields2 = "stage_record_id, dst_table_name";
				
				String idxDefinition2 = DBUtilities.generateIndexDefinition(fullTableName, indexName2, indexFields2, conn);
				
				try {
					BaseDAO.executeBatch(conn, sql, idxDefinition1, idxDefinition2);
				}
				catch (DBException e) {
					if (!e.getLocalizedMessage().contains("Duplicate key name")) {
						throw e;
					}
				}
			}
			
		}
	}
	
	default void createRelatedStageAreaSrcUniqueKeysTable(Connection conn) throws DBException {
		this.createRelatedSyncStageAreaUniqueKeysTable(this.generateRelatedStageSrcUniqueKeysTableName(),
		    this.generateFullSrcStageTableName(), conn);
	}
	
	default void createRelatedSyncStageAreaDstUniqueKeysTable(Connection conn) throws DBException {
		this.createRelatedSyncStageAreaUniqueKeysTable(this.generateRelatedStageDstUniqueKeysTableName(),
		    this.generateFullDstStageTableName(), conn);
	}
	
	default void createRelatedSyncStageAreaUniqueKeysTable(String tableName, String parentTableName, Connection conn)
	        throws DBException {
		
		synchronized (lock) {
			if (!DBUtilities.isTableExists(this.getSyncStageSchema(), tableName, conn)) {
				
				String sql = "";
				String notNullConstraint = "NOT NULL";
				String nullConstraint = "NULL";
				String endLineMarker = ",\n";
				
				String uuid = utilities.removeCharactersOnString(UUID.randomUUID().toString(), "-");
				
				String fullTableName = this.getSyncStageSchema() + "." + tableName;
				
				sql += "CREATE TABLE " + fullTableName + "(\n";
				sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
				sql += DBUtilities.generateTableBigIntField("stage_record_id", notNullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("key_name", 100, notNullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("column_name", 100, notNullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("key_value", 100, nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
				sql += DBUtilities.generateTableUniqueKeyDefinition(("unq_record_key_" + uuid).toLowerCase(),
				    "stage_record_id, key_name, column_name", conn) + endLineMarker;
				sql += DBUtilities.generateTableForeignKeyDefinition(("parent_record_" + uuid).toLowerCase(),
				    "stage_record_id", parentTableName, "id", conn) + endLineMarker;
				sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
				sql += ")";
				
				String indexName = ("idx_" + uuid).toLowerCase();
				String indexFields = "key_name, column_name, key_value";
				
				String idxDefinition = DBUtilities.generateIndexDefinition(fullTableName, indexName, indexFields, conn);
				
				try {
					BaseDAO.executeBatch(conn, sql, idxDefinition);
				}
				catch (DBException e) {
					if (!e.getLocalizedMessage().contains("Duplicate key name")) {
						throw e;
					}
				}
			}
		}
	}
	
	default void createRelatedSrcStageAreaTable(Connection conn) throws DBException {
		
		synchronized (lock) {
			if (!DBUtilities.isTableExists(this.getSyncStageSchema(), this.generateRelatedSrcStageTableName(), conn)) {
				
				String tableName = this.generateRelatedSrcStageTableName();
				
				String sql = "";
				String notNullConstraint = "NOT NULL";
				String nullConstraint = "NULL";
				String endLineMarker = ",\n";
				
				sql += "CREATE TABLE " + this.generateFullSrcStageTableName() + "(\n";
				sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, notNullConstraint, conn)
				        + endLineMarker;
				
				sql += DBUtilities.generateTableVarcharField("compacted_object_uk", 190, notNullConstraint, conn)
				        + endLineMarker;
				
				sql += DBUtilities.generateTableTextField("json", nullConstraint, conn) + endLineMarker;
				
				sql += DBUtilities.generateTableDateTimeField("last_sync_date", nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableVarcharField("last_sync_try_err", 250, nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableDateTimeField("last_update_date", nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableNumericField("consistent", 1, nullConstraint, -1, conn) + endLineMarker;
				sql += DBUtilities.generateTableNumericField("migration_status", 1, nullConstraint, 1, conn) + endLineMarker;
				sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
				
				sql += DBUtilities.generateTableDateTimeField("record_date_created", nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableDateTimeField("record_date_changed", nullConstraint, conn) + endLineMarker;
				sql += DBUtilities.generateTableDateTimeField("record_date_voided", nullConstraint, conn) + endLineMarker;
				
				String checkCondition = "migration_status in (-1,0,1,2)";
				String keyName = ("CHK_" + this.generateRelatedSrcStageTableName() + "_MIG_STATUS").toLowerCase();
				
				sql += DBUtilities.generateTableCheckConstraintDefinition(keyName, checkCondition, conn) + endLineMarker;
				
				sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_record_key".toLowerCase(),
				    "compacted_object_uk, record_origin_location_code", conn) + endLineMarker;
				
				sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn);
				sql += ")";
				
				String indexName = tableName + "_location_idx";
				String indexFields = "record_origin_location_code";
				
				String idxDefinition = DBUtilities.generateIndexDefinition(this.generateFullSrcStageTableName(), indexName,
				    indexFields, conn);
				
				BaseDAO.executeBatch(conn, sql, idxDefinition);
			}
		}
	}
	
	default String generateLastUpdateDateInsertTriggerMonitor() {
		return this.getTableName() + "_date_changed_insert_monitor";
	}
	
	default String generateLastUpdateDateUpdateTriggerMonitor() {
		return this.getTableName() + "_date_changed_update_monitor";
	}
	
	default void createLastUpdateDateMonitorTrigger(Connection conn) throws SQLException {
		String insert = this.generateTriggerCode(this.generateLastUpdateDateInsertTriggerMonitor(), "INSERT");
		String update = this.generateTriggerCode(this.generateLastUpdateDateUpdateTriggerMonitor(), "UPDATE");
		
		BaseDAO.executeBatch(conn, insert, update);
	}
	
	default String generateTriggerCode(String triggerName, String triggerEvent) {
		String sql = "";
		
		sql += "CREATE TRIGGER " + triggerName + " BEFORE " + triggerEvent + " ON " + this.getTableName() + "\n";
		sql += "FOR EACH ROW\n";
		sql += "	BEGIN\n";
		sql += "	UPDATE " + this.generateFullSrcStageTableName() + " SET last_update_date = CURRENT_TIMESTAMP();\n";
		sql += "	END;\n";
		
		return sql;
	}
	
	default Boolean isExistRelatedTriggers(Connection conn) throws SQLException {
		return DBUtilities.isResourceExist(conn.getCatalog(), getTableName(), DBUtilities.RESOURCE_TYPE_TRIGGER,
		    generateLastUpdateDateInsertTriggerMonitor(), conn);
	}
	
	default long getExtremeRecord(Engine<? extends EtlDatabaseObject> engine, SqlFunctionType function, Connection conn)
	        throws DBException {
		
		SrcConf srcConf = null;
		
		if (this instanceof SrcConf) {
			srcConf = (SrcConf) this;
		} else {
			srcConf = this.cloneToSrcConf(engine.getEtlItemConfiguration(), conn);
		}
		
		EtlDatabaseObjectSearchParams searchParams = new EtlDatabaseObjectSearchParams(srcConf, null);
		
		return searchParams.retrieveExtremeRecord(function, conn);
		
	}
	
	default SrcConf cloneToSrcConf(EtlItemConfiguration relatedItemConf, Connection conn) throws DBException {
		SrcConf srcConf = new SrcConf();
		
		srcConf.copyFromOther(this, null, relatedItemConf, conn);
		
		return srcConf;
	}
	
	default long getMinRecordId(Engine<? extends EtlDatabaseObject> engine, Connection conn) throws DBException {
		return this.getExtremeRecord(engine, SqlFunctionType.MIN, conn);
	}
	
	default long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine, Connection conn) throws DBException {
		return this.getExtremeRecord(engine, SqlFunctionType.MAX, conn);
	}
	
	default EtlTemplateInfo retrieveNearestTemplate() {
		return this.getTemplate();
	}
	
}
