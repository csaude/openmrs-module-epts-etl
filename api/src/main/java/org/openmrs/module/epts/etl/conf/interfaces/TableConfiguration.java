package org.openmrs.module.epts.etl.conf.interfaces;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractRelatedTable;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.PrimaryKey;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.RefType;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.DuplicateMappingException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface TableConfiguration extends DatabaseObjectConfiguration {
	
	static final String LOCK_STRING = "LOCK_STRING";
	
	void setTableName(String tableName);
	
	String getTableAlias();
	
	String getTableName();
	
	String getAlias();
	
	void setTableAlias(String tableAlias);
	
	void setLoadHealper(DatabaseObjectLoaderHelper loadHealper);
	
	String getInsertSQLWithObjectId();
	
	String getInsertSQLWithoutObjectId();
	
	String getUpdateSql();
	
	String getExtraConditionForExtract();
	
	void setInsertSQLWithoutObjectId(String string);
	
	void setExtraConditionForExtract(String extraConditionForExtract);
	
	boolean isMustLoadChildrenInfo();
	
	void setMustLoadChildrenInfo(boolean mustLoadChildrenInfo);
	
	boolean isAutoIncrementId();
	
	void setAutoIncrementId(boolean autoIncrementId);
	
	List<List<Field>> getWinningRecordFieldsInfo();
	
	void setWinningRecordFieldsInfo(List<List<Field>> winningRecordFieldsInfo);
	
	List<String> getIgnorableFields();
	
	void setIgnorableFields(List<String> ignorable);
	
	boolean includePrimaryKeyOnInsert();
	
	void setIncludePrimaryKeyOnInsert(boolean includePrimaryKeyOnInsert);
	
	boolean isUniqueKeyInfoLoaded();
	
	void setUniqueKeyInfoLoaded(boolean uniqueKeyInfoLoaded);
	
	boolean isPrimaryKeyInfoLoaded();
	
	void setPrimaryKeyInfoLoaded(boolean primaryKeyInfoLoaded);
	
	boolean isFieldsLoaded();
	
	void setFieldsLoaded(boolean fieldsLoaded);
	
	boolean isTableNameInfoLoaded();
	
	void setTableNameInfoLoaded(boolean tableNameInfoLoaded);
	
	@Override
	default boolean hasPK(Connection conn) throws DBException {
		
		if (!isPrimaryKeyInfoLoaded()) {
			loadPrimaryKeyInfo(conn);
		}
		
		return getPrimaryKey() != null;
	}
	
	default boolean hasAlias() {
		return utilities.stringHasValue(this.getTableAlias());
	}
	
	default boolean hasWinningRecordsInfo() {
		return this.getWinningRecordFieldsInfo() != null;
	}
	
	void setParentRefInfo(List<? extends ParentTable> parentRefInfo);
	
	void setChildRefInfo(List<ChildTable> childRefInfo);
	
	List<UniqueKeyInfo> getUniqueKeys();
	
	void setUniqueKeys(List<UniqueKeyInfo> uniqueKeys);
	
	List<String> getObservationDateFields();
	
	void setObservationDateFields(List<String> observationDateFields);
	
	boolean isRemoveForbidden();
	
	public void setRemoveForbidden(boolean removeForbidden);
	
	default boolean isDoIntegrityCheckInTheEnd(EtlOperationType operationType) {
		return getRelatedSyncConfiguration().isDoIntegrityCheckInTheEnd(operationType);
	}
	
	@JsonIgnore
	default String getId() {
		return this.getRelatedSyncConfiguration().getDesignation() + "_" + this.getTableName();
	}
	
	default boolean hasExtraConditionForExtract() {
		return utilities.stringHasValue(getExtraConditionForExtract());
	}
	
	@JsonIgnore
	default String getParentsAsString() {
		String sourceFoldersAsString = "";
		
		if (utilities.arrayHasElement(this.getParents())) {
			for (int i = 0; i < this.getParents().size() - 1; i++) {
				sourceFoldersAsString += this.getParents().get(i).getTableName() + ",";
			}
			
			sourceFoldersAsString += this.getParents().get(this.getParents().size() - 1).getTableName();
		}
		
		return sourceFoldersAsString;
	}
	
	List<ParentTable> getParents();
	
	boolean isUsingManualDefinedAlias();
	
	void setUsingManualDefinedAlias(boolean usingManualDefinedAlias);
	
	void setParents(List<ParentTable> parents);
	
	void setSharePkWith(String sharePkWith);
	
	default String getObjectName() {
		return getTableName();
	}
	
	/**
	 * Clones gives list of UniqueKeys to this tableConfiguration
	 * 
	 * @param uniqueKeys to clone to this table
	 */
	default void cloneUnikeKeys(List<UniqueKeyInfo> uniqueKeys) {
		
		if (utilities.arrayHasElement(uniqueKeys)) {
			setUniqueKeys(new ArrayList<>(uniqueKeys.size()));
			
			for (UniqueKeyInfo uk : UniqueKeyInfo.cloneAll_(uniqueKeys)) {
				uk.setTabConf(this);
				
				getUniqueKeys().add(uk);
			}
			
		} else {
			setUniqueKeys(null);
		}
	}
	
	default void clone(TableConfiguration toCloneFrom, Connection conn) throws DBException {
		this.setTableName(toCloneFrom.getTableName());
		this.setParents(toCloneFrom.getParents());
		this.setMustLoadChildrenInfo(toCloneFrom.isMustLoadChildrenInfo());
		
		if (isMustLoadChildrenInfo()) {
			this.setChildRefInfo(toCloneFrom.getChildRefInfo());
		}
		
		this.setParentRefInfo(toCloneFrom.getParentRefInfo());
		this.setSyncRecordClass(toCloneFrom.getSyncRecordClass());
		this.setParentConf(toCloneFrom.getParentConf());
		
		this.setPrimaryKey(toCloneFrom.getPrimaryKey());
		
		if (this.hasPrimaryKey()) {
			this.getPrimaryKey().setTabConf(this);
		}
		
		this.setSharePkWith(toCloneFrom.getSharePkWith());
		this.setMetadata(toCloneFrom.isMetadata());
		this.setFullLoaded(toCloneFrom.isFullLoaded());
		this.setRemoveForbidden(toCloneFrom.isRemoveForbidden());
		this.setObservationDateFields(toCloneFrom.getObservationDateFields());
		
		this.cloneUnikeKeys(toCloneFrom.getUniqueKeys());
		
		this.setFields(toCloneFrom.getFields());
		this.setWinningRecordFieldsInfo(toCloneFrom.getWinningRecordFieldsInfo());
		this.setFullLoaded(toCloneFrom.isFullLoaded());
		
		this.setInsertSQLWithObjectId(toCloneFrom.getInsertSQLWithObjectId());
		this.setInsertSQLWithoutObjectId(toCloneFrom.getInsertSQLWithoutObjectId());
		this.setUpdateSql(toCloneFrom.getUpdateSql());
		this.setRelatedSyncConfiguration(toCloneFrom.getRelatedSyncConfiguration());
		this.setSchema(toCloneFrom.getSchema());
		
		this.tryToGenerateTableAlias(toCloneFrom.getRelatedSyncConfiguration());
		
		if (toCloneFrom.hasExtraConditionForExtract()) {
			//First try to replace the alias
			this.setExtraConditionForExtract(toCloneFrom.getExtraConditionForExtract()
			        .replaceAll(toCloneFrom.getTableAlias() + "\\.", getTableAlias() + "\\."));
			//Secodn try to replace tableName
			
			this.setExtraConditionForExtract(
			    this.getExtraConditionForExtract().replaceAll(toCloneFrom.getTableName() + "\\.", getTableAlias() + "\\."));
			
		} else {
			setExtraConditionForExtract(null);
		}
		
		loadOwnElements(conn);
	}
	
	default boolean hasPrimaryKey() {
		return this.getPrimaryKey() != null;
	}
	
	void setParentConf(EtlDataConfiguration parentConf);
	
	@JsonIgnore
	default boolean useSharedPKKey() {
		return utilities.stringHasValue(this.getSharePkWith());
	}
	
	void setPrimaryKey(PrimaryKey primaryKey);
	
	@Override
	PrimaryKey getPrimaryKey();
	
	default void loadPrimaryKeyInfo(Connection conn) throws DBException {
		PrimaryKey primaryKey = null;
		
		if (!isPrimaryKeyInfoLoaded()) {
			
			loadSchemaInfo(conn);
			loadFields(conn);
			
			try {
				
				ResultSet rs = conn.getMetaData().getPrimaryKeys(getCatalog(conn), getSchema(), getTableName());
				
				while (rs.next()) {
					primaryKey = new PrimaryKey(this);
					
					Key pk = new Key();
					pk.setName(rs.getString("COLUMN_NAME"));
					
					pk.setType(getField(pk.getName()).getType());
					
					primaryKey.addKey(pk);
				}
			}
			catch (SQLException e) {
				throw new DBException(e);
			}
			
			setPrimaryKey(primaryKey);
			setPrimaryKeyInfoLoaded(true);
		}
	}
	
	default EtlDatabaseObject generateAndSaveDefaultObject(Connection conn) throws DBException {
		
		synchronized (this) {
			
			try {
				EtlDatabaseObject defaultObject = getDefaultObject(conn);
				
				if (defaultObject != null) {
					return defaultObject;
				} else {
					defaultObject = getSyncRecordClass().newInstance();
					defaultObject.setRelatedConfiguration(this);
					
					defaultObject.loadWithDefaultValues(conn);
					
					defaultObject.save(this, conn);
					
					defaultObject = getDefaultObject(conn);
					
					EtlConfigurationTableConf defaultGeneratedObjectKeyTabConf = getRelatedSyncConfiguration()
					        .getDefaultGeneratedObjectKeyTabConf();
					
					if (!defaultGeneratedObjectKeyTabConf.isFullLoaded()) {
						
						defaultGeneratedObjectKeyTabConf.setTableName(getRelatedSyncConfiguration().getSyncStageSchema()
						        + "." + defaultGeneratedObjectKeyTabConf.getTableName());
						
						defaultGeneratedObjectKeyTabConf.setTableAlias(defaultGeneratedObjectKeyTabConf.getTableName());
						defaultGeneratedObjectKeyTabConf.fullLoad(conn);
					}
					
					for (Key key : defaultObject.getObjectId().getFields()) {
						EtlDatabaseObject keyInfo = defaultGeneratedObjectKeyTabConf.getSyncRecordClass().newInstance();
						
						keyInfo.setFieldValue("table_name", defaultObject.getObjectName());
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
	
	default EtlDatabaseObject getDefaultObject(Connection conn) throws DBException, ForbiddenOperationException {
		return DatabaseObjectDAO.getDefaultRecord(this, conn);
	}
	
	@JsonIgnore
	default void loadUniqueKeys() {
		if (isUniqueKeyInfoLoaded())
			return;
		
		OpenConnection conn = null;
		
		try {
			conn = getRelatedSyncConfiguration().getMainApp().openConnection();
			
			loadUniqueKeys(conn);
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
		if (isUniqueKeyInfoLoaded())
			return;
		
		if (this.getUniqueKeys() == null) {
			loadUniqueKeys(this, conn);
		} else {
			for (UniqueKeyInfo uk : this.getUniqueKeys()) {
				uk.setTabConf(this);
			}
		}
		
		setUniqueKeyInfoLoaded(true);
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
	
	default boolean checkIfisIgnorableParentByClassAttName(String parentAttName, Connection conn) {
		for (ParentTable parent : this.getParentRefInfo()) {
			RefMapping map = parent.getRefMappingByChildClassAttName(parentAttName);
			
			return map.isIgnorable();
		}
		
		throw new ForbiddenOperationException("The att '" + parentAttName + "' doesn't represent any defined parent att");
	}
	
	default int countChildren(Connection conn) throws SQLException {
		String tableName = DBUtilities.extractTableNameFromFullTableName(this.getTableName());
		
		ResultSet foreignKeyRS = conn.getMetaData().getExportedKeys(getCatalog(conn), getSchema(), tableName);
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				foreignKeyRS.last();
			} else {
				while (foreignKeyRS.next()) {}
				;
			}
			
			return foreignKeyRS.getRow();
		}
		finally {
			foreignKeyRS.close();
		}
		
	}
	
	default void logInfo(String msg) {
		getRelatedSyncConfiguration().logInfo(msg);
	}
	
	default void logDebug(String msg) {
		getRelatedSyncConfiguration().logDebug(msg);
	}
	
	default void logWarn(String msg) {
		getRelatedSyncConfiguration().logWarn(msg);
	}
	
	default void logErr(String msg) {
		getRelatedSyncConfiguration().logErr(msg);
	}
	
	default int countParents(Connection conn) throws SQLException {
		ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(getCatalog(conn), getSchema(), getTableName());
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				foreignKeyRS.last();
			} else {
				while (foreignKeyRS.next()) {}
				;
			}
			
			return foreignKeyRS.getRow();
		}
		finally {
			foreignKeyRS.close();
		}
		
	}
	
	default void loadParents(Connection conn) throws SQLException {
		synchronized (this) {
			logDebug("LOADING PARENTS FOR TABLE '" + getTableName() + "'");
			
			ResultSet foreignKeyRS = null;
			
			int count = countParents(conn);
			
			//First load all necessary info on configured parents
			
			if (utilities.arrayHasElement(this.getParents())) {
				for (ParentTable p : this.getParents()) {
					
					p.setChildTableConf(this);
					
					if (p.getRefMapping() != null) {
						
						for (RefMapping map : p.getRefMapping()) {
							map.setParentTabConf((ParentTableImpl) p);
							
							Field field = utilities.findOnArray(this.getFields(), new Field(map.getChildFieldName()));
							map.getChildField().setType(field.getType());
						}
					}
				}
			}
			
			if (count == 0) {
				logDebug("NO PARENT FOUND FOR TABLE '" + getTableName() + "'");
			} else
				try {
					logDebug("DISCOVERED '" + count + "' PARENTS FOR TABLE '" + getTableName() + "'");
					
					foreignKeyRS = conn.getMetaData().getImportedKeys(getCatalog(conn), getSchema(), getTableName());
					
					while (foreignKeyRS.next()) {
						
						logDebug("CONFIGURING PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '"
						        + getTableName() + "'");
						
						String refCode = foreignKeyRS.getString("FK_NAME");
						
						String childFieldName = foreignKeyRS.getString("FKCOLUMN_NAME");
						
						String parentFieldName = foreignKeyRS.getString("PKCOLUMN_NAME");
						String parentTableName = foreignKeyRS.getString("PKTABLE_NAME");
						
						ParentTableImpl parentTabConf = ParentTableImpl.init(parentTableName, refCode);
						
						parentTabConf.setParentConf(this.getParentConf());
						parentTabConf.setChildTableConf(this);
						parentTabConf.setRelatedSyncConfiguration(getRelatedSyncConfiguration());
						
						parentTabConf.setSchema(foreignKeyRS.getString("PKTABLE_SCHEM"));
						
						if (!parentTabConf.hasSchema()) {
							parentTabConf.setSchema(foreignKeyRS.getString("PKTABLE_CAT"));
						}
						
						addParentMappingInfo(refCode, childFieldName, parentTabConf, parentFieldName, conn);
						
						logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName()
						        + "' CONFIGURED");
					}
					
					//Copy additional configured Info
					if (this.hasParentRefInfo() && this.hasParents()) {
						
						for (ParentTable autoLoadedRefInfo : this.getParentRefInfo()) {
							
							for (ParentTable manualConfiguredRefInfo : this.getParents()) {
								
								ParentTable mixedConfiguredRef = manualConfiguredRefInfo;
								
								if (autoLoadedRefInfo.getTableName().equals(manualConfiguredRefInfo.getTableName())) {
									
									if (!manualConfiguredRefInfo.hasMapping()) {
										if (autoLoadedRefInfo.isCompositeMapping()) {
											throw new ForbiddenOperationException(
											        "You must manual configure the ref info for parent "
											                + manualConfiguredRefInfo.getTableName() + " on table "
											                + this.getTableName()
											                + ". Optionaly you can remove the manual parent specification");
										}
										
										//create default refInfo to force the copy of shared ref info
										
										mixedConfiguredRef = ParentTableImpl.init(manualConfiguredRefInfo.getTableName(),
										    "");
										
										mixedConfiguredRef
										        .setConditionalFields(manualConfiguredRefInfo.getConditionalFields());
										
										mixedConfiguredRef.setChildTableConf(this);
										
										mixedConfiguredRef.setRefMapping(autoLoadedRefInfo.cloneAllMapping());
										
										mixedConfiguredRef.getSimpleRefMapping().setDefaultValueDueInconsistency(
										    manualConfiguredRefInfo.getDefaultValueDueInconsistency());
										mixedConfiguredRef.getSimpleRefMapping().setSetNullDueInconsistency(
										    manualConfiguredRefInfo.isSetNullDueInconsistency());
									}
									
									if (autoLoadedRefInfo.equals(mixedConfiguredRef)) {
										autoLoadedRefInfo.setConditionalFields(mixedConfiguredRef.getConditionalFields());
										
										for (RefMapping map : autoLoadedRefInfo.getRefMapping()) {
											RefMapping configuredMap = mixedConfiguredRef
											        .findRefMapping(map.getChildFieldName(), map.getParentFieldName());
											
											if (configuredMap == null) {
												throw new ForbiddenOperationException("The mapping ["
												        + map.getChildFieldName() + " : " + map.getParentFieldName()
												        + "] was not found on configured mapping!");
											}
											
											map.setIgnorable(
											    map.isIgnorable() ? configuredMap.isIgnorable() : map.isIgnorable());
											map.setDefaultValueDueInconsistency(
											    configuredMap.getDefaultValueDueInconsistency());
											map.setSetNullDueInconsistency(configuredMap.isSetNullDueInconsistency());
										}
									}
								}
							}
						}
					}
					//Check if there is a configured parent but not defined on the db schema
					
					if (utilities.arrayHasElement(this.getParents())) {
						
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
					
					logDebug("LOADED PARENTS FOR TABLE '" + getTableName() + "'");
				}
				finally
				
				{
					if (foreignKeyRS != null) {
						foreignKeyRS.close();
					}
				}
		}
	}
	
	default boolean hasParents() {
		return utilities.arrayHasElement(this.getParents());
	}
	
	default void loadChildren(Connection conn) throws SQLException {
		
		synchronized (this) {
			
			if (!this.isMustLoadChildrenInfo())
				return;
			
			logDebug("LOADING CHILDREN FOR TABLE '" + getTableName() + "'");
			
			List<ChildTable> childRefInfo = new ArrayList<ChildTable>();
			
			setChildRefInfo(childRefInfo);
			
			int count = countChildren(conn);
			
			if (count == 0) {
				logDebug("NO CHILDREN FOUND FOR TABLE '" + getTableName() + "'");
			} else {
				ResultSet foreignKeyRS = null;
				
				try {
					logDebug("DISCOVERED '" + count + "' CHILDREN FOR TABLE '" + getTableName() + "'");
					
					foreignKeyRS = conn.getMetaData().getExportedKeys(getCatalog(conn), getSchema(), getTableName());
					
					int i = 0;
					
					while (foreignKeyRS.next()) {
						logDebug("CONFIGURING CHILD " + ++i + " [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '"
						        + getTableName() + "'");
						
						String refCode = foreignKeyRS.getString("FK_NAME");
						
						String childTableName = foreignKeyRS.getString("FKTABLE_NAME");
						String childFieldName = foreignKeyRS.getString("FKCOLUMN_NAME");
						
						String parentFieldName = foreignKeyRS.getString("PKCOLUMN_NAME");
						
						ChildTable childTabConf = ChildTable.init(childTableName, refCode);
						
						childTabConf.setParentTableConf(this);
						childTabConf.setParentConf(this.getParentConf());
						childTabConf.setRelatedSyncConfiguration(getRelatedSyncConfiguration());
						childTabConf.setSchema(foreignKeyRS.getString("FKTABLE_SCHEM"));
						
						if (!childTabConf.hasSchema()) {
							childTabConf.setSchema(foreignKeyRS.getString("PKTABLE_CAT"));
						}
						
						addChildMappingInfo(refCode, childTabConf, childFieldName, parentFieldName, conn);
						
						logDebug("CHILDREN " + i + " [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '"
						        + getTableName() + "' CONFIGURED");
					}
					
					logDebug("LOADED CHILDREN FOR TABLE '" + getTableName() + "'");
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
		
		initRefInfo(RefType.IMPORTED, refCode, childTabConf, childFieldName, parentTabConf, parentFieldName, conn);
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
		
		boolean ignorable = DBUtilities.isTableColumnAllowNull(this.getTableName(), fieldName, conn);
		
		RefMapping map = RefMapping.fastCreate(childFieldname, parentFieldName);
		
		map.getChildField().setType(field.getType());
		map.getParentField().setType(field.getType());
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
			ref.addMapping(map);
		}
		catch (DuplicateMappingException e) {}
		
	}
	
	public static final String[] REMOVABLE_METADATA = {};
	
	/**
	 * By default an metadata cannot be removed, but there are situations where is needed to remove
	 * a metadata
	 * 
	 * @return
	 */
	@JsonIgnore
	default boolean isRemovableMetadata() {
		return utilities.existOnArray(utilities.parseArrayToList(TableConfiguration.REMOVABLE_METADATA),
		    this.getTableName());
	}
	
	@JsonIgnore
	default boolean existsSyncRecordClass(AppInfo application) {
		try {
			return getSyncRecordClass(application) != null;
		}
		catch (ForbiddenOperationException e) {
			
			return false;
		}
	}
	
	default void generateRecordClass(AppInfo application, boolean fullClass) {
		try {
			if (fullClass) {
				this.setSyncRecordClass(DatabaseEntityPOJOGenerator.generate(this, application));
			} else {
				this.setSyncRecordClass(DatabaseEntityPOJOGenerator.generateSkeleton(this, application));
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
	
	default void generateSkeletonRecordClass(AppInfo application) {
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
		return generateClassName(this.getTableName());
	}
	
	default String generateClassName(String tableName) {
		String[] nameParts = getTableName().split("\\.");
		
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
	
	void setMetadata(boolean metadata);
	
	@JsonIgnore
	default String generateRelatedStageTableName() {
		return this.getTableName() + "_stage";
	}
	
	@JsonIgnore
	default String generateRelatedStageUniqueKeysTableName() {
		return generateRelatedStageTableName() + "_unique_keys";
	}
	
	@JsonIgnore
	default String getSyncStageSchema() {
		return getRelatedSyncConfiguration().getSyncStageSchema();
	}
	
	@JsonIgnore
	default String generateFullStageTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageTableName();
	}
	
	default String generateFullTableName(Connection conn) throws DBException {
		return DBUtilities.tryToPutSchemaOnDatabaseObject(getTableName(), conn);
	}
	
	default String generateFullTableNameWithAlias(Connection conn) throws DBException, ForbiddenOperationException {
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.getTableName());
		}
		
		return generateFullTableName(conn) + " " + this.getTableAlias();
	}
	
	default String generateFullTableNameWithAlias() {
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.getTableName());
		}
		if (!hasSchema()) {
			throw new ForbiddenOperationException("No schema is defined for table" + this.getTableName());
		}
		
		return getSchema() + "." + generateTableNameWithAlias();
	}
	
	default String generateTableNameWithAlias() {
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.getTableName());
		}
		
		return this.getTableName() + " " + this.getTableAlias();
	}
	
	@JsonIgnore
	default String generateFullStageUniqueKeysTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageUniqueKeysTableName();
	}
	
	default boolean existRelatedExportStageTable(Connection conn) {
		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = generateRelatedStageTableName();
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	default boolean existRelatedExportStageUniqueKeysTable(Connection conn) {
		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = generateRelatedStageUniqueKeysTableName();
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public boolean isDisabled();
	
	public void setDisabled(boolean disabled);
	
	@JsonIgnore
	default boolean isConfigured() {
		for (TableConfiguration tabConf : getRelatedSyncConfiguration().getConfiguredTables()) {
			if (tabConf.equals(this))
				return true;
		}
		
		return false;
	}
	
	@Override
	default void fullLoad(Connection conn) throws DBException {
		tryToGenerateTableAlias(getRelatedSyncConfiguration());
		
		synchronized (this) {
			
			try {
				
				if (this.isFullLoaded()) {
					return;
				}
				
				loadSchemaInfo(conn);
				
				loadFields(conn);
				
				loadPrimaryKeyInfo(conn);
				
				loadParents(conn);
				loadChildren(conn);
				
				tryToDiscoverySharedKeyInfo(conn);
				
				loadUniqueKeys(conn);
				
				loadAttDefinition(conn);
				
				this.setAutoIncrementId(useAutoIncrementId(conn));
				
				if (!includePrimaryKeyOnInsert()) {
					
					//Force the inclusion of primaryKey if the table is not autoincrement
					if (!isAutoIncrementId()) {
						setIncludePrimaryKeyOnInsert(true);
					}
				}
				
				if (hasExtraConditionForExtract() && !isUsingManualDefinedAlias()) {
					this.setExtraConditionForExtract(
					    this.getExtraConditionForExtract().replaceAll(getTableName() + "\\.", getTableAlias() + "\\."));
				}
				
				loadOwnElements(conn);
				
				this.setFullLoaded(true);
				
				getRelatedSyncConfiguration().addToFullLoadedTables(this);
				
			}
			catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
	}
	
	default void addParent(ParentTable p) {
		if (!hasParents()) {
			setParents(new ArrayList<>());
		}
		
		if (!getParents().contains(p)) {
			getParents().add(p);
		}
	}
	
	/**
	 * @param conn
	 * @throws DBException
	 * @throws ForbiddenOperationException
	 */
	default void loadSchemaInfo(Connection conn) throws DBException, ForbiddenOperationException {
		
		if (isTableNameInfoLoaded())
			return;
		
		String[] tableNameParts = getTableName().split("\\.");
		
		if (tableNameParts.length == 1) {
			if (getSchema() == null) {
				setSchema(DBUtilities.determineSchemaName(conn));
			}
		} else if (tableNameParts.length == 2) {
			setTableName(tableNameParts[1]);
			setSchema(tableNameParts[0]);
		} else {
			throw new ForbiddenOperationException("The table name " + getTableName() + " is malformed!");
		}
		
		boolean exists = DBUtilities.isTableExists(getSchema(), getTableName(), conn);
		
		if (!exists)
			throw new ForbiddenOperationException("The table '" + generateFullTableName(conn) + "' does not exist!!!");
		
		setTableNameInfoLoaded(true);
	}
	
	/**
	 * @param conn
	 * @throws DBException
	 */
	default void loadFields(Connection conn) throws DBException {
		if (isFieldsLoaded())
			return;
		
		List<Field> flds = DBUtilities.getTableFields(getTableName(), getSchema(), conn);
		
		this.setFields(new ArrayList<>());
		
		for (Field f : flds) {
			if (!isIgnorableField(f)) {
				this.getFields().add(f);
			}
		}
	}
	
	void setFields(List<Field> tableFields);
	
	void setFullLoaded(boolean fullLoaded);
	
	default void loadAttDefinition(Connection conn) {
		int qtyAttrs = this.getFields().size();
		
		for (int i = 0; i < qtyAttrs - 1; i++) {
			Field field = this.getFields().get(i);
			
			field.setAttDefinedElements(AttDefinedElements.define(field.getName(), field.getType(), false, this));
		}
		
		Field field = this.getFields().get(qtyAttrs - 1);
		
		field.setAttDefinedElements(AttDefinedElements.define(field.getName(), field.getType(), true, this));
		
		generateSQLElemenets();
	}
	
	default void tryToDiscoverySharedKeyInfo(Connection conn) throws DBException {
		//Discovery shared pk
		
		if (this.hasParentRefInfo()) {
			for (ParentTable ref : this.getParentRefInfo()) {
				
				ref.loadPrimaryKeyInfo(conn);
				
				PrimaryKey parentRefInfoAskey = new PrimaryKey(ref);
				parentRefInfoAskey.setFields(ref.extractParentFieldsFromRefMapping());
				
				PrimaryKey childRefInfoAskey = new PrimaryKey(ref.getChildTableConf());
				childRefInfoAskey.setFields(ref.extractChildFieldsFromRefMapping());
				
				if (ref.getPrimaryKey() != null && ref.getPrimaryKey().equals(parentRefInfoAskey)
				        && ref.getChildTableConf().getPrimaryKey().equals(childRefInfoAskey)) {
					this.setSharePkWith(ref.getTableName());
					
					break;
				}
			}
		}
		
	}
	
	@Override
	default void fullLoad() throws DBException {
		synchronized (this) {
			OpenConnection mainConn = getRelatedSyncConfiguration().getMainApp().openConnection();
			
			OpenConnection dstConn = null;
			
			try {
				fullLoad(mainConn);
			}
			finally {
				mainConn.finalizeConnection();
				
				if (dstConn != null) {
					dstConn.finalizeConnection();
				}
			}
		}
	}
	
	default ParentTable getSharedTableConf() {
		if (getSharedKeyRefInfo() != null) {
			return getSharedKeyRefInfo();
		}
		
		return null;
	}
	
	default ParentTable getSharedKeyRefInfo() {
		if (getSharePkWith() == null) {
			return null;
		} else if (hasParentRefInfo()) {
			
			for (ParentTable parent : this.getParentRefInfo()) {
				if (parent.getTableName().equalsIgnoreCase(this.getSharePkWith())) {
					
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
	
	default String getFullTableName() {
		return hasSchema() ? getSchema() + "." + getTableName() : getTableName();
	}
	
	default String generateFullTableNameOnSchema(String schema) {
		return schema + "." + getTableName();
	}
	
	default String getFullTableDescription() {
		return getFullTableName() + (hasAlias() ? " as " + getTableAlias() : "");
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
	default boolean isDestinationInstallationType() {
		return getRelatedSyncConfiguration().isDataBaseMergeFromJSONProcess();
	}
	
	@JsonIgnore
	default boolean isDataReconciliationProcess() {
		return getRelatedSyncConfiguration().isDataReconciliationProcess();
	}
	
	@JsonIgnore
	default boolean isDBQuickLoad() {
		return getRelatedSyncConfiguration().isDBQuickLoadProcess();
	}
	
	@JsonIgnore
	default boolean isDbCopy() {
		return getRelatedSyncConfiguration().isDBQuickCopyProcess();
	}
	
	@JsonIgnore
	default boolean isDataBasesMergeFromSourceDBProcess() {
		return getRelatedSyncConfiguration().isDataBaseMergeFromSourceDBProcess();
	}
	
	@JsonIgnore
	default boolean hasNoDateVoidedField() {
		return utilities.isStringIn(getTableName(), "note");
	}
	
	@JsonIgnore
	default boolean hasNotDateChangedField() {
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
			
			String uniqueKeyJoinField = generateUniqueKeyConditionsFields(this.getUniqueKeys().get(i), dbObject);
			
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
			
			String uniqueKeyJoinField = generateUniqueKeyConditionsFields(this.getUniqueKeys().get(i));
			
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
	
	default boolean hasUniqueKeys() {
		return utilities.arrayHasElement(this.getUniqueKeys());
	}
	
	default boolean useSimpleNumericPk() {
		return this.getPrimaryKey() != null && ((PrimaryKey) this.getPrimaryKey()).isSimpleNumericKey();
	}
	
	default boolean useAutoIncrementId(Connection conn) throws DBException {
		
		if (this.getPrimaryKey() == null || this.getPrimaryKey().isCompositeKey()) {
			return false;
		}
		
		return DBUtilities.checkIfTableUseAutoIcrement(this.getSchema(), this.getTableName(), conn);
	}
	
	default List<ParentTable> getConditionalParents() {
		List<ParentTable> conditionalParents = null;
		
		if (hasParents()) {
			
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
		generateInsertSQLWithObjectId();
		generateInsertSQLWithoutObjectId();
		setUpdateSql(generateUpdateSQL());
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
		
		setInsertSQLQuestionMarksWithoutObjectId(insertSQLQuestionMarksWithoutObjectId);
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
	
	void setInsertSQLQuestionMarksWithObjectId(String insertQuestionMarks);
	
	String getInsertSQLQuestionMarksWithObjectId();
	
	void setInsertSQLQuestionMarksWithoutObjectId(String insertQuestionMarks);
	
	String getInsertSQLQuestionMarksWithoutObjectId();
	
	void setInsertSQLWithObjectId(String sql);
	
	@JsonIgnore
	default String generateUpdateSQL() {
		if (this.getPrimaryKey() == null) {
			
			if (this.getUniqueKeys() == null) {
				throw new ForbiddenOperationException("Impossible to generate update params, there is no primary key");
			}
		}
		
		String updateSQL = "UPDATE " + this.getObjectName() + " SET ";
		
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
	
	public abstract boolean isGeneric();
	
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
		
		for (Field f : this.getFields()) {
			fullSelectColumns = utilities.concatStringsWithSeparator(fullSelectColumns, f.generateAliasedSelectColumn(this),
			    ",\n");
		}
		
		if (this.useSharedPKKey()) {
			fullSelectColumns += "," + this.getSharedTableConf().generateFullAliasedSelectColumns();
		}
		
		return fullSelectColumns;
	}
	
	/**
	 * Generates the content for SELECT FROM clause content.
	 * 
	 * @return
	 */
	default String generateSelectFromClauseContent() {
		String fromClause = generateFullTableNameWithAlias();
		
		if (useSharedPKKey()) {
			fromClause += "\n INNER JOIN " + this.getSharedTableConf().generateFullTableNameWithAlias() + " ON "
			        + this.getSharedKeyRefInfo().generateJoinCondition();
		}
		
		return fromClause;
	}
	
	default boolean hasSchema() {
		return utilities.stringHasValue(getSchema());
	}
	
	default List<FieldsMapping> tryToLoadJoinFields(TableConfiguration relatedTabConf) {
		
		List<FieldsMapping> joinFields = new ArrayList<>();
		
		//Assuming that this datasource is parent
		List<ParentTable> pInfo = relatedTabConf.findAllRefToParent(this.getTableName());
		
		if (utilities.arrayHasElement(pInfo)) {
			
			if (utilities.arrayHasExactlyOneElement(pInfo)) {
				
				ParentTable ref = pInfo.get(0);
				
				for (RefMapping map : ref.getRefMapping()) {
					joinFields.add(new FieldsMapping(map.getParentField().getName(), "", map.getChildField().getName()));
				}
			} else {
				throw new ForbiddenOperationException(
				        "The mapping cannot be auto generated! Multiple references were found between " + this.getTableName()
				                + " And " + relatedTabConf.getTableName());
				
			}
		} else {
			
			//Assuning that the this data src is child
			pInfo = this.findAllRefToParent(relatedTabConf.getTableName());
			
			if (utilities.arrayHasElement(pInfo)) {
				if (utilities.arrayHasExactlyOneElement(pInfo)) {
					
					ParentTable ref = pInfo.get(0);
					
					for (RefMapping map : ref.getRefMapping()) {
						joinFields.add(new FieldsMapping(map.getChildField().getName(), "", map.getParentField().getName()));
					}
				} else {
					throw new ForbiddenOperationException(
					        "The mapping cannot be auto generated! Multiple references were found between "
					                + this.getTableName() + " And " + relatedTabConf.getTableName());
					
				}
			}
		}
		
		if (!utilities.arrayHasElement(joinFields)) {
			throw new ForbiddenOperationException(
			        "No join fields were difined between " + this.getTableName() + " And " + relatedTabConf.getTableName());
		}
		
		return joinFields;
	}
	
	default String generateConditionsFields(EtlDatabaseObject dbObject, List<FieldsMapping> joinFields,
	        String joinExtraCondition) {
		String conditionFields = "";
		
		for (int i = 0; i < joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = joinFields.get(i);
			
			Object value;
			
			try {
				value = dbObject.getFieldValue(field.getSrcField());
			}
			catch (ForbiddenOperationException e) {
				value = dbObject.getFieldValue(field.getSrcFieldAsClassField());
			}
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getDstField(), value);
		}
		
		if (utilities.stringHasValue(joinExtraCondition)) {
			conditionFields += " AND (" + joinExtraCondition + ")";
		}
		
		return conditionFields;
	}
	
	default String generateJoinCondition(TableConfiguration joiningTable, List<FieldsMapping> joinFields,
	        String joinExtraCondition) {
		String conditionFields = "";
		
		for (int i = 0; i < joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = joinFields.get(i);
			
			conditionFields += getTableAlias() + "." + field.getSrcField() + " = " + joiningTable.getTableAlias() + "."
			        + field.getDstField();
		}
		
		if (utilities.stringHasValue(joinExtraCondition)) {
			conditionFields += " AND (" + joinExtraCondition + ")";
		}
		
		return conditionFields;
	}
	
	default TableConfiguration findFullConfiguredConfInAllRelatedTable(String fullTableName) {
		if (this.getFullTableName().equals(fullTableName) && this.isFullLoaded()) {
			return this;
		}
		
		if (this.hasParentRefInfo()) {
			for (ParentTable p : this.getParentRefInfo()) {
				TableConfiguration fullLoaded = p.findFullConfiguredConfInAllRelatedTable(fullTableName);
				
				if (fullLoaded != null) {
					return fullLoaded;
				}
			}
		}
		
		if (this.hasChildRefInfo()) {
			for (ChildTable p : this.getChildRefInfo()) {
				TableConfiguration fullLoaded = p.findFullConfiguredConfInAllRelatedTable(fullTableName);
				
				if (fullLoaded != null) {
					return fullLoaded;
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
	
	void setSchema(String schema);
	
	boolean isAllRelatedTablesFullLoaded();
	
	void setAllRelatedTablesFullLoaded(boolean b);
	
	default void fullLoadAllRelatedTables(TableAliasesGenerator aliasGenerator, TableConfiguration related, Connection conn)
	        throws DBException {
		if (isAllRelatedTablesFullLoaded()) {
			return;
		}
		
		if (this.hasParentRefInfo()) {
			for (ParentTable ref : this.getParentRefInfo()) {
				
				TableConfiguration existingConf = null;
				
				if (related != null) {
					existingConf = related.findFullConfiguredConfInAllRelatedTable(ref.getFullTableName());
				}
				
				if (existingConf == null) {
					existingConf = this.findFullConfiguredConfInAllRelatedTable(ref.getFullTableName());
				}
				
				ref.tryToGenerateTableAlias(aliasGenerator);
				
				if (existingConf != null) {
					ref.clone(existingConf, conn);
					
				} else {
					ref.fullLoad(conn);
				}
				
				//ref.fullLoadAllRelatedTables(aliasGenerator, this, conn);
			}
			
			this.setAllRelatedTablesFullLoaded(true);
		}
	}
	
	default boolean containsField(String fieldName) {
		for (Field f : this.getFields()) {
			if (f.getName().equals(fieldName)) {
				return true;
			}
		}
		
		return false;
	}
	
	default boolean isIgnorableField(Field field) {
		if (!hasIgnorableField())
			return false;
		
		for (String ignorable : this.getIgnorableFields()) {
			if (field.getName().equals(ignorable)) {
				return true;
			}
		}
		
		return false;
	}
	
	default boolean hasIgnorableField() {
		return utilities.arrayHasElement(getIgnorableFields());
	}
	
	/**
	 * Usually called after a call to {@link #fullLoad()}, allow the loading of own elements. Eg.
	 * the join fields,etc
	 */
	void loadOwnElements(Connection conn) throws DBException;
	
	/**
	 * Generates a full sql select from query.
	 * 
	 * @return the generated select sql query
	 */
	default String generateSelectFromQuery() {
		String sql = " SELECT " + generateFullAliasedSelectColumns() + "\n";
		sql += " FROM " + generateSelectFromClauseContent() + "\n";
		
		return sql;
	}
	
	default boolean checkIfFieldIsForeignKey(Field field) {
		
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
	
	default void tryToGenerateTableAlias(TableAliasesGenerator aliasGenerator) {
		
		synchronized (this) {
			if (hasAlias())
				return;
			
			aliasGenerator.generateAliasForTable(this);
		}
	}
	
	default EtlDatabaseObject createRecordInstance() {
		try {
			return getSyncRecordClass().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	default boolean hasObservationDateFields() {
		return utilities.arrayHasElement(getObservationDateFields());
	}
	
	default boolean containsAllFields(List<Field> fields) {
		if (!hasFields())
			return false;
		
		for (Field f : fields) {
			if (!containsField(f.getName()))
				return false;
		}
		
		return true;
	}
}
