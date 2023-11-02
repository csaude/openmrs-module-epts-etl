package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.tablemapping.MappedTableInfo;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.Field;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.model.pojo.mozart.MedicationVO;
import org.openmrs.module.eptssync.model.pojo.mozart.old.DsdVO;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableConfiguration extends BaseConfiguration implements Comparable<SyncTableConfiguration> {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String tableName;
	
	private List<RefInfo> parents;
	
	private List<RefInfo> childred;
	
	private List<RefInfo> conditionalParents;
	
	private Class<DatabaseObject> syncRecordClass;
	
	private SyncConfiguration relatedSyncConfiguration;
	
	private String primaryKey;
	
	private String primaryKeyType;
	
	private String sharePkWith;
	
	private String extraConditionForExport;
	
	private boolean metadata;
	
	protected boolean fullLoaded;
	
	private boolean removeForbidden;
	
	private boolean disabled;
	
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
	
	private List<MappedTableInfo> destinationTableMappingInfo;
	
	private boolean manualIdGeneration;
	
	public SyncTableConfiguration() {
	}
	
	public List<MappedTableInfo> getDestinationTableMappingInfo() {
		return destinationTableMappingInfo;
	}
	
	public void setDestinationTableMappingInfo(List<MappedTableInfo> destinationTableMappingInfo) {
		this.destinationTableMappingInfo = destinationTableMappingInfo;
	}
	
	public void clone(SyncTableConfiguration toCloneFrom) {
		this.tableName = toCloneFrom.tableName;
		this.parents = toCloneFrom.parents;
		this.childred = toCloneFrom.childred;
		this.conditionalParents = toCloneFrom.conditionalParents;
		this.syncRecordClass = toCloneFrom.syncRecordClass;
		this.relatedSyncConfiguration = toCloneFrom.relatedSyncConfiguration;
		this.primaryKey = toCloneFrom.primaryKey;
		this.primaryKeyType = toCloneFrom.primaryKeyType;
		this.sharePkWith = toCloneFrom.sharePkWith;
		this.extraConditionForExport = toCloneFrom.extraConditionForExport;
		this.metadata = toCloneFrom.metadata;
		this.fullLoaded = toCloneFrom.fullLoaded;
		this.removeForbidden = toCloneFrom.removeForbidden;
		this.disabled = toCloneFrom.disabled;
		this.observationDateFields = toCloneFrom.observationDateFields;
		this.uniqueKeys = toCloneFrom.uniqueKeys;
		this.fields = toCloneFrom.fields;
		this.winningRecordFieldsInfo = toCloneFrom.winningRecordFieldsInfo;
		this.destinationTableMappingInfo = toCloneFrom.destinationTableMappingInfo;
	}
	
	public boolean isManualIdGeneration() {
		return manualIdGeneration;
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
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@JsonIgnore
	public boolean isFromOpenMRSModel() {
		return this.getRelatedSyncConfiguration().isOpenMRSModel();
	}
	
	public boolean isRemoveForbidden() {
		return removeForbidden;
	}
	
	public void setRemoveForbidden(boolean removeForbidden) {
		this.removeForbidden = removeForbidden;
	}
	
	public List<RefInfo> getConditionalParents() {
		return conditionalParents;
	}
	
	public void setConditionalParents(List<RefInfo> conditionalParents) {
		this.conditionalParents = conditionalParents;
	}
	
	@JsonIgnore
	public List<RefInfo> getChildred() {
		return childred;
	}
	
	public void setChildred(List<RefInfo> childred) {
		this.childred = childred;
	}
	
	public AppInfo getMainApp() {
		return getRelatedSyncConfiguration().getMainApp();
	}
	
	@JsonIgnore
	public String getClasspackage(AppInfo application) {
		return application.getPojoPackageName();
	}
	
	public boolean isDoIntegrityCheckInTheEnd(SyncOperationType operationType) {
		return getRelatedSyncConfiguration().isDoIntegrityCheckInTheEnd(operationType);
	}
	
	@JsonIgnore
	public String getId() {
		return this.getRelatedSyncConfiguration().getDesignation() + "_" + this.tableName;
	}
	
	@JsonIgnore
	public boolean isUuidColumnNotExists() {
		return this.isFromOpenMRSModel() && this.tableName.equals("patient") ? true : false;
		
		//return uuidColumnNotExists;
	}
	
	@JsonIgnore
	public String getParentsAsString() {
		String sourceFoldersAsString = "";
		
		if (utilities.arrayHasElement(this.getParents())) {
			for (int i = 0; i < this.getParents().size() - 1; i++) {
				sourceFoldersAsString += this.getParents().get(i).getTableName() + ",";
			}
			
			sourceFoldersAsString += this.getParents().get(this.getParents().size() - 1).getTableName();
		}
		
		return sourceFoldersAsString;
	}
	
	public List<RefInfo> getParents() {
		return parents;
	}
	
	public void setParents(List<RefInfo> parents) {
		this.parents = parents;
	}
	
	public String getExtraConditionForExport() {
		return extraConditionForExport;
	}
	
	public void setExtraConditionForExport(String extraConditionForExport) {
		this.extraConditionForExport = extraConditionForExport;
	}
	
	public String getSharePkWith() {
		return sharePkWith;
	}
	
	public void setSharePkWith(String sharePkWith) {
		this.sharePkWith = sharePkWith;
	}
	
	@JsonIgnore
	public SyncConfiguration getRelatedSyncConfiguration() {
		return relatedSyncConfiguration;
	}
	
	public void setRelatedSyncConfiguration(SyncConfiguration relatedSyncConfiguration) {
		this.relatedSyncConfiguration = relatedSyncConfiguration;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@JsonIgnore
	public boolean useSharedPKKey() {
		return utilities.stringHasValue(this.sharePkWith);
	}
	
	@JsonIgnore
	public String getPrimaryKeyAsClassAtt() {
		return convertTableAttNameToClassAttName(getPrimaryKey());
	}
	
	@JsonIgnore
	public String getPrimaryKey() {
		OpenConnection conn = relatedSyncConfiguration.getMainApp().openConnection();
		
		try {
			return getPrimaryKey(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@JsonIgnore
	public String getPrimaryKey(Connection conn) {
		if (primaryKey == null) {
			try {
				ResultSet rs = conn.getMetaData().getPrimaryKeys(conn.getCatalog(), conn.getSchema(), tableName);
				
				if (rs.next()) {
					this.primaryKey = rs.getString("COLUMN_NAME");
					
					this.primaryKeyType = DBUtilities.determineColunType(tableName, this.primaryKey, conn);
					
					this.primaryKeyType = AttDefinedElements.convertDatabaseTypeTOJavaType(this.primaryKeyType);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			
		}
		
		return primaryKey;
	}
	
	@JsonIgnore
	public void loadUniqueKeys() {
		OpenConnection conn = getRelatedSyncConfiguration().getMainApp().openConnection();
		
		try {
			loadUniqueKeys(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@JsonIgnore
	public void loadUniqueKeys(Connection conn) {
		if (this.uniqueKeys == null) {
			loadUniqueKeys(this, conn);
		}
	}
	
	@JsonIgnore
	private void loadUniqueKeys(SyncTableConfiguration tableConfiguration, Connection conn) {
		if (tableConfiguration.uniqueKeys == null) {
			try {
				this.uniqueKeys = UniqueKeyInfo.loadUniqueKeysInfo(this, conn);
				
				if (useSharedPKKey()) {
					SyncTableConfiguration parentConf = getSharedKeyRefInfo().getRefTableConfiguration();
					
					parentConf.loadUniqueKeys(conn);
					
					if (utilities.arrayHasElement(parentConf.getUniqueKeys())) {
					
						for (UniqueKeyInfo uk : parentConf.getUniqueKeys()) {
							if (!utilities.existOnArray(this.uniqueKeys, uk)) {
								this.uniqueKeys.add(uk);
							}
						}
					}
				}
				
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
		}
	}
	
	@JsonIgnore
	public String getPrimaryKeyType() {
		if (primaryKeyType == null)
			getPrimaryKey();
		
		return primaryKeyType;
	}
	
	public String getPrimaryKeyType(Connection conn) {
		if (primaryKeyType == null)
			getPrimaryKey(conn);
		
		return primaryKeyType;
	}
	
	@JsonIgnore
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.getPrimaryKeyType());
	}
	
	@JsonIgnore
	public boolean isNumericColumnType(Connection conn) {
		return AttDefinedElements.isNumeric(this.getPrimaryKeyType(conn));
	}
	
	@JsonIgnore
	public boolean hasPK() {
		return getPrimaryKey() != null;
	}
	
	@JsonIgnore
	public boolean hasPK(Connection conn) {
		return getPrimaryKey(conn) != null;
	}
	
	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}
	
	public boolean checkIfisIgnorableParentByClassAttName(String parentAttName, Connection conn) {
		for (RefInfo parent : this.getParents()) {
			if (parent.getRefColumnAsClassAttName().equals(parentAttName)) {
				return parent.isIgnorable();
			}
		}
		
		throw new ForbiddenOperationException("The att '" + parentAttName + "' doesn't represent any defined parent att");
	}
	
	private int countChildren(Connection conn) throws SQLException {
		ResultSet foreignKeyRS = conn.getMetaData().getExportedKeys(conn.getCatalog(), conn.getSchema(), tableName);
		
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
	
	protected synchronized void loadChildren(Connection conn) throws SQLException {
		logDebug("LOADING CHILDREN FOR TABLE '" + getTableName() + "'");
		
		this.childred = new ArrayList<RefInfo>();
		
		int count = countChildren(conn);
		
		if (count == 0) {
			logDebug("NO CHILDREN FOUND FOR TABLE '" + getTableName() + "'");
		} else {
			ResultSet foreignKeyRS = null;
			
			try {
				logDebug("DISCOVERED '" + count + "' CHILDREN FOR TABLE '" + getTableName() + "'");
				
				foreignKeyRS = conn.getMetaData().getExportedKeys(conn.getCatalog(), conn.getSchema(), tableName);
				
				while (foreignKeyRS.next()) {
					logDebug("CONFIGURING CHILD [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '"
					        + getTableName() + "'");
					
					RefInfo ref = new RefInfo();
					
					ref.setRefType(RefInfo.CHILD_REF_TYPE);
					ref.setRefColumnName(foreignKeyRS.getString("FKCOLUMN_NAME"));
					ref.setRefTableConfiguration(
					    SyncTableConfiguration.init(foreignKeyRS.getString("FKTABLE_NAME"), this.relatedSyncConfiguration));
					ref.setRefColumnType(AttDefinedElements.convertDatabaseTypeTOJavaType(DBUtilities.determineColunType(
					    ref.getRefTableConfiguration().getTableName(), ref.getRefColumnName(), conn)));
					ref.setRelatedSyncTableConfiguration(this);
					ref.setIgnorable(DBUtilities.isTableColumnAllowNull(ref.getRefTableConfiguration().getTableName(),
					    ref.getRefColumnName(), conn));
					
					//Mark as metadata if there is no table info configured
					if (getRelatedSyncConfiguration().find(ref.getRefTableConfiguration()) == null) {
						ref.getRefTableConfiguration().setMetadata(true);
					}
					
					this.childred.add(ref);
					
					logDebug("CHILDREN [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '" + getTableName()
					        + "' CONFIGURED");
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
	
	public void logInfo(String msg) {
		getRelatedSyncConfiguration().logInfo(msg);
	}
	
	public void logDebug(String msg) {
		getRelatedSyncConfiguration().logDebug(msg);
	}
	
	public void logWarn(String msg) {
		getRelatedSyncConfiguration().logWarn(msg);
	}
	
	public void logErr(String msg) {
		getRelatedSyncConfiguration().logErr(msg);
	}
	
	private int countParents(Connection conn) throws SQLException {
		ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(conn.getCatalog(), conn.getSchema(), tableName);
		
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
	
	protected synchronized void loadParents(Connection conn) throws SQLException {
		logDebug("LOADING PARENTS FOR TABLE '" + getTableName() + "'");
		
		ResultSet foreignKeyRS = null;
		
		int count = countParents(conn);
		
		if (count == 0) {
			logDebug("NO PARENT FOUND FOR TABLE '" + getTableName() + "'");
		} else
			try {
				
				List<RefInfo> auxRefInfo = new ArrayList<RefInfo>();
				logDebug("DISCOVERED '" + count + "' PARENTS FOR TABLE '" + getTableName() + "'");
				
				foreignKeyRS = conn.getMetaData().getImportedKeys(conn.getCatalog(), conn.getSchema(), tableName);
				
				while (foreignKeyRS.next()) {
					
					logDebug("CONFIGURING PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '"
					        + getTableName() + "'");
					
					String refColumName = foreignKeyRS.getString("FKCOLUMN_NAME");
					
					SyncTableConfiguration refTableConfiguration = SyncTableConfiguration
					        .init(foreignKeyRS.getString("PKTABLE_NAME"), this.relatedSyncConfiguration);
					
					RefInfo ref = generateRefInfo(refColumName, null, null, RefInfo.PARENT_REF_TYPE, refTableConfiguration,
					    conn);
					
					if (utilities.existOnArray(auxRefInfo, ref)) {
						logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName()
						        + "' WAS ALREDY CONFIGURED! SKIPPING...");
						continue;
					}
					
					RefInfo configuredParent = findParent(ref);
					
					if (configuredParent != null) {
						ref.setDefaultValueDueInconsistency(configuredParent.getDefaultValueDueInconsistency());
						ref.setSetNullDueInconsistency(configuredParent.isSetNullDueInconsistency());
					}
					
					logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName()
					        + "' CONFIGURED");
					
					auxRefInfo.add(ref);
				}
				
				//Check if there is a configured parent but not defined on the db schema
				
				if (utilities.arrayHasElement(this.parents)) {
					for (RefInfo configuredParent : this.parents) {
						if (configuredParent.getRefColumnName() == null)
							continue;
						
						RefInfo autoGeneratedParent = utilities.findOnList(auxRefInfo, configuredParent);
						
						if (autoGeneratedParent == null) {
							configuredParent.setRefTableConfiguration(
							    SyncTableConfiguration.init(configuredParent.getTableName(), this.relatedSyncConfiguration));
							configuredParent.setRelatedSyncTableConfiguration(this);
							
							auxRefInfo.add(configuredParent);
						}
					}
				}
				
				this.parents = auxRefInfo;
				
				logDebug("LOADED PARENTS FOR TABLE '" + getTableName() + "'");
			}
			finally {
				if (foreignKeyRS != null) {
					foreignKeyRS.close();
				}
			}
		
	}
	
	protected void loadConditionalParents(Connection conn) throws DBException {
		if (!utilities.arrayHasElement(this.conditionalParents))
			return;
		
		for (int i = 0; i < this.conditionalParents.size(); i++) {
			RefInfo refInfo = this.conditionalParents.get(i);
			
			this.conditionalParents.set(i,
			    generateRefInfo(refInfo.getRefColumnName(), refInfo.getConditionField(), refInfo.getConditionValue(),
			        RefInfo.PARENT_REF_TYPE, init(refInfo.getTableName(), this.getRelatedSyncConfiguration()), conn));
		}
	}
	
	private RefInfo generateRefInfo(String refColumName, String conditionField, Integer conditionValue, String refType,
	        SyncTableConfiguration refTableConfiguration, Connection conn) throws DBException {
		String refColumnType = AttDefinedElements
		        .convertDatabaseTypeTOJavaType(DBUtilities.determineColunType(this.getTableName(), refColumName, conn));
		boolean ignorable = DBUtilities.isTableColumnAllowNull(this.tableName, refColumName, conn);
		
		RefInfo ref = new RefInfo();
		
		ref.setRefType(refType);
		ref.setRefColumnName(refColumName);
		ref.setRefTableConfiguration(refTableConfiguration);
		ref.setIgnorable(ignorable);
		ref.setRefColumnType(refColumnType);
		ref.setRelatedSyncTableConfiguration(this);
		ref.setConditionField(conditionField);
		ref.setConditionValue(conditionValue);
		
		//Mark as metadata if is not specificaly mapped as parent in conf file
		if (!ref.getRefTableConfiguration().isConfigured()) {
			ref.getRefTableConfiguration().setMetadata(true);
		}
		
		return ref;
	}
	
	public static SyncTableConfiguration init(String tableName, SyncConfiguration sourceInfo) {
		SyncTableConfiguration tableInfo = sourceInfo.findPulledTableConfiguration(tableName);
		
		if (tableInfo == null) {
			tableInfo = new SyncTableConfiguration();
			tableInfo.setTableName(tableName);
			tableInfo.setRelatedSyncConfiguration(sourceInfo);
			
			sourceInfo.addToTableConfigurationPull(tableInfo);
		}
		
		return tableInfo;
	}
	
	@JsonIgnore
	public Class<DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		if (syncRecordClass == null)
			this.syncRecordClass = DatabaseEntityPOJOGenerator.tryToGetExistingCLass(generateFullClassName(application),
			    getRelatedSyncConfiguration());
		
		if (syncRecordClass == null) {
			OpenConnection conn = application.openConnection();
			
			try {
				generateRecordClass(application, true);
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		if (syncRecordClass == null)
			throw new ForbiddenOperationException("The related pojo of table " + getTableName() + " was not found!!!!");
		
		return syncRecordClass;
	}
	
	private static String[] REMOVABLE_METADATA = {};
	
	/**
	 * By default an metadata cannot be removed, but there are situations where is needed to remove
	 * a metadata
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isRemovableMetadata() {
		return utilities.existOnArray(utilities.parseArrayToList(SyncTableConfiguration.REMOVABLE_METADATA), this.tableName);
	}
	
	@JsonIgnore
	public boolean existsSyncRecordClass(AppInfo application) {
		try {
			return getSyncRecordClass(application) != null;
		}
		catch (ForbiddenOperationException e) {
			
			return false;
		}
	}
	
	public void setSyncRecordClass(Class<DatabaseObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}
	
	@JsonIgnore
	public String generateFullClassName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.eptssync.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return utilities.concatStringsWithSeparator(fullPackageName, generateClassName(), ".");
	}
	
	@JsonIgnore
	public String generateFullPackageName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.eptssync.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return fullPackageName;
	}
	
	@JsonIgnore
	public String getOriginAppLocationCode() {
		return getRelatedSyncConfiguration().getOriginAppLocationCode();
	}
	
	public void generateRecordClass(AppInfo application, boolean fullClass) {
		try {
			if (fullClass) {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generate(this, application);
			} else {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, application);
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
	
	public void generateSkeletonRecordClass(AppInfo application) {
		try {
			this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, application);
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
	public String generateClassName() {
		return generateClassName(this.tableName);
	}
	
	private String generateClassName(String tableName) {
		String[] nameParts = tableName.split("_");
		
		String className = utilities.capitalize(nameParts[0]);
		
		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}
		
		return className + "VO";
	}
	
	public boolean isMetadata() {
		return metadata;
	}
	
	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
		
		//if (utilities.isStringIn(this.getTableName(), "obs") && metadata) throw new ForbiddenOperationException("Obs cannot be metadata");
	}
	
	@JsonIgnore
	public String generateRelatedStageTableName() {
		return this.getTableName() + "_stage";
	}
	
	@JsonIgnore
	public String generateRelatedStageUniqueKeysTableName() {
		return generateRelatedStageTableName() + "_unique_keys";
	}
	
	@JsonIgnore
	public String getSyncStageSchema() {
		return getRelatedSyncConfiguration().getSyncStageSchema();
	}
	
	@JsonIgnore
	public String generateFullStageTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageTableName();
	}
	
	public String generateFullTableName(Connection conn) throws DBException {
		return DBUtilities.tryToPutSchemaOnDatabaseObject(getTableName(), conn);
	}
	
	@JsonIgnore
	public String generateFullStageUniqueKeysTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageUniqueKeysTableName();
	}
	
	public boolean existRelatedExportStageTable(Connection conn) {
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
	
	public boolean existRelatedExportStageUniqueKeysTable(Connection conn) {
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
	
	@JsonIgnore
	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	@JsonIgnore
	public boolean isConfigured() {
		for (SyncTableConfiguration tabConf : getRelatedSyncConfiguration().getTablesConfigurations()) {
			if (tabConf.equals(this))
				return true;
		}
		
		return false;
	}
	
	public synchronized void fullLoad(Connection conn) {
		try {
			boolean exists = DBUtilities.isTableExists(conn.getSchema(), getTableName(), conn);
			
			if (!exists)
				throw new ForbiddenOperationException("The table '" + getTableName() + "' does not exist!!!");
			
			getPrimaryKey(conn);
			
			loadParents(conn);
			loadChildren(conn);
			
			loadConditionalParents(conn);
			
			loadUniqueKeys(conn);
			
			
			setFields(DBUtilities.getTableFields(getTableName(), DBUtilities.determineSchemaName(conn), conn));
		
			
			
			this.fullLoaded = true;
			
			if (destinationTableMappingInfo == null) {
				destinationTableMappingInfo = MappedTableInfo.generateFromSyncTableConfiguration(this);
				
			} else {
				
				for (MappedTableInfo map : this.destinationTableMappingInfo) {
					map.setRelatedSyncConfiguration(relatedSyncConfiguration);
					map.setRelatedTableConfiguration(this);
					if (!utilities.arrayHasElement(map.getFieldsMapping())) {
						map.generateMappingFields(this);
					}
					
					map.loadAdditionalFieldsInfo();
				}
			}
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
	}
	
	public synchronized void fullLoad() throws DBException {
		OpenConnection mainConn = getRelatedSyncConfiguration().getMainApp().openConnection();
		
		OpenConnection mappedConn = null;
		
		try {
			fullLoad(mainConn);
			
			List<AppInfo> otherApps = getRelatedSyncConfiguration().exposeAllAppsNotMain();
			
			if (utilities.arrayHasElement(otherApps)) {
				mappedConn = otherApps.get(0).openConnection();
				
				for (MappedTableInfo map : this.destinationTableMappingInfo) {
					
					if (DBUtilities.isTableExists(mappedConn.getSchema(), map.getTableName(), mappedConn)) {
						map.fullLoad(mappedConn);
						
						this.manualIdGeneration = !DBUtilities.checkIfTableUseAutoIcrement(map.getTableName(), mappedConn);
					}
					
				}
			} else {
				this.manualIdGeneration = !DBUtilities.checkIfTableUseAutoIcrement(this.getTableName(), mainConn);
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			mainConn.finalizeConnection();
			
			if (mappedConn != null) {
				mappedConn.finalizeConnection();
			}
		}
	}
	
	public RefInfo getSharedKeyRefInfo() {
		if (sharePkWith == null) {
			return null;
		} else if (utilities.arrayHasElement(this.getParents())) {
			
			for (RefInfo refInfo : getParents()) {
				if (refInfo.getRefTableConfiguration().getTableName().equalsIgnoreCase(this.sharePkWith)) {
					return refInfo;
				}
			}
		}
		
		throw new ForbiddenOperationException("The related table of shared pk " + sharePkWith + " of table "
		        + this.getTableName() + " is not listed inparents!");
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		return "Table [name:" + this.tableName + ", pk: " + this.primaryKey + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof SyncTableConfiguration))
			return false;
		
		return this.getTableName().equalsIgnoreCase(((SyncTableConfiguration) obj).getTableName());
	}
	
	@JsonIgnore
	public File getPOJOCopiledFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOCompiledFilesDirectory();
	}
	
	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOSourceFilesDirectory();
	}
	
	public RefInfo findParent(RefInfo parent) {
		if (!utilities.arrayHasElement(this.parents))
			return null;
		
		for (RefInfo info : this.parents) {
			if (info.getTableName().equals(parent.getTableName()))
				return info;
		}
		
		return null;
	}
	
	@Override
	public int compareTo(SyncTableConfiguration o) {
		if (this.equals(o))
			return 0;
		
		return this.tableName.compareTo(o.getTableName());
	}
	
	@JsonIgnore
	public File getClassPath() {
		return new File(relatedSyncConfiguration.getClassPath());
	}
	
	@JsonIgnore
	public boolean isDestinationInstallationType() {
		return getRelatedSyncConfiguration().isDataBaseMergeFromJSONProcess();
	}
	
	@JsonIgnore
	public boolean isDataReconciliationProcess() {
		return getRelatedSyncConfiguration().isDataReconciliationProcess();
	}
	
	@JsonIgnore
	public boolean isDBQuickLoad() {
		return getRelatedSyncConfiguration().isDBQuickLoadProcess();
	}
	
	@JsonIgnore
	public boolean isDBQuickCopy() {
		return getRelatedSyncConfiguration().isDbCopy();
	}
	
	@JsonIgnore
	public boolean isDbCopy() {
		return getRelatedSyncConfiguration().isDBQuickCopyProcess();
	}
	
	@JsonIgnore
	public boolean isDataBasesMergeFromSourceDBProcess() {
		return getRelatedSyncConfiguration().isDataBaseMergeFromSourceDBProcess();
	}
	
	@JsonIgnore
	public boolean hasNoDateVoidedField() {
		return utilities.isStringIn(getTableName(), "note");
	}
	
	@JsonIgnore
	public boolean hasNotDateChangedField() {
		return utilities.isStringIn(getTableName(), "obs");
	}
	
	/**
	 * Generates SQL join condition between two tables (some table) one from source another from
	 * destination database based on the {@link #uniqueKeys}
	 * 
	 * @param sourceTableAlias alias name for source table
	 * @param destinationTableAlias alias name for destination table
	 * @return the generated join condition based on {@link #uniqueKeys}
	 */
	@JsonIgnore
	public String generateUniqueKeysJoinCondition(String sourceTableAlias, String destinationTableAlias) {
		String joinCondition = "";
		
		for (int i = 0; i < this.getUniqueKeys().size(); i++) {
			
			String uniqueKeyJoinField = generateUniqueKeyJoinField(this.getUniqueKeys().get(i));
			
			if (i > 0)
				joinCondition += " OR ";
			
			joinCondition += "(" + uniqueKeyJoinField + ")";
		}
		
		if (!utilities.stringHasValue(joinCondition) && this.isMetadata()) {
			joinCondition = "dest_." + getPrimaryKey() + " = src_." + getPrimaryKey();
		}
		
		return joinCondition;
	}
	
	private String generateUniqueKeyJoinField(UniqueKeyInfo uniqueKey_) {
		List<Field> uniqueKeyFields = uniqueKey_.getFields();
		
		String joinFields = "";
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				joinFields += " AND ";
			
			joinFields += "dest_." + uniqueKeyFields.get(i).getName() + " = src_." + uniqueKeyFields.get(i).getName();
		}
		
		return joinFields;
	}
	
	/**
	 * Generates SQL condition using the {@link #uniqueKeys} fulfilled with related values from
	 * especific object
	 * 
	 * @param dbObject the object from where the condition values will be retrieved from
	 * @return a SQL condition
	 */
	@JsonIgnore
	public String generateUniqueKeysParametrizedCondition(DatabaseObject dbObject) {
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
	public String generateUniqueKeysParametrizedCondition() {
		String joinCondition = "";
		
		for (int i = 0; i < this.getUniqueKeys().size(); i++) {
			
			String uniqueKeyJoinField = generateUniqueKeyConditionsFields(this.getUniqueKeys().get(i));
			
			if (i > 0)
				joinCondition += " OR ";
			
			joinCondition += "(" + uniqueKeyJoinField + ")";
		}
		
		return joinCondition;
	}
	
	private String generateUniqueKeyConditionsFields(UniqueKeyInfo uniqueKey) {
		String joinFields = "";
		
		List<Field> uniqueKeyFields = uniqueKey.getFields();
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				joinFields += " AND ";
			
			joinFields += uniqueKeyFields.get(i) + " = ? ";
		}
		
		return joinFields;
	}
	
	private String generateUniqueKeyConditionsFields(UniqueKeyInfo uniqueKey, DatabaseObject dbObject) {
		String conditionFields = "";
		
		List<Field> uniqueKeyFields = uniqueKey.getFields();
		
		uniqueKey.loadValuesToFields(dbObject);
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			Field field = uniqueKeyFields.get(i);
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getName(), field.getValue());
		}
		
		return conditionFields;
	}
	
	public boolean hasUniqueKeys() {
		return utilities.arrayHasElement(this.getUniqueKeys());
	}
	
	public static void main(String[] args) throws IOException, DBException {
		System.out.println("AAAAAAAAAAAAAAAA");
		
		SyncConfiguration syncConfig = SyncConfiguration.loadFromFile(new File(
		        "D:\\PRG\\JEE\\Workspace\\CSaude\\eptssync\\mozart\\test\\db_quick_merge_mozart_without_pojo_generation.json"));
		
		OpenConnection connection = syncConfig.getMainApp().openConnection();
		
		try {
			MedicationVO m = DatabaseObjectDAO.getById(MedicationVO.class, 33248, connection);
			
			System.out.println(m.getDosage());
			
			//String s = new String(m.getDosage(), "UTF-8");
			
			//System.out.println(s.toString());
			
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public boolean useManualIdGeneration(Connection conn) throws DBException {
		return DBUtilities.checkIfTableUseAutoIcrement(this.tableName, conn);
	}
	
	public static void main_(String[] args) throws DBException, IOException {
		SyncConfiguration syncConfig = SyncConfiguration
		        .loadFromFile(new File("D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\mozart\\detect_problematic_dbs.json"));
		
		SyncTableConfiguration config = syncConfig.find(SyncTableConfiguration.init("patient_state", syncConfig));
		config.fullLoad();
		
		Object[] params = { "946a0f13-dfa8-4eeb-b63a-d17b16ebc495" };
		
		OpenConnection conn = syncConfig.getMainApp().openConnection();
		
		DsdVO dbObject = DatabaseObjectDAO.find(DsdVO.class, "select * from dsd where dsd_uuid = ? ", params, conn);
		
		String condition = config.generateUniqueKeysParametrizedCondition(dbObject);
		
		System.out.println(condition);
		
		conn.finalizeConnection();
	}
}
