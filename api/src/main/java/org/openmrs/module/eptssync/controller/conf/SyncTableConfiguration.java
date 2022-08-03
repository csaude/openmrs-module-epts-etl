package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.OpenMRSPOJOGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableConfiguration implements Comparable<SyncTableConfiguration>{
	static CommonUtilities utilities = CommonUtilities.getInstance();

	private String tableName;
	
	private List<RefInfo> parents;
	private List<RefInfo> childred;
	
	private Class<OpenMRSObject> syncRecordClass;

	private SyncConfiguration relatedSyncTableInfoSource;

	private String primaryKey;
	private String primaryKeyType;
	private String sharePkWith;
	
	private String extraConditionForExport;
	
	private boolean metadata;
	
	private boolean fullLoaded;
	private boolean removeForbidden;
	
	private boolean disabled;
	
	public SyncTableConfiguration() {
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public boolean isRemoveForbidden() {
		return removeForbidden;
	}
	
	public void setRemoveForbidden(boolean removeForbidden) {
		this.removeForbidden = removeForbidden;
	}
	
	@JsonIgnore
	public List<RefInfo> getChildred() {
		return childred;
	}
	
	public void setChildred(List<RefInfo> childred) {
		this.childred = childred;
	}
	
	public AppInfo getMainApp() {
		return getRelatedSynconfiguration().getMainApp();
	}
	
	@JsonIgnore
	public String getClasspackage(AppInfo application) {
		return application.getPojoPackageName();
	}
	
	public boolean isDoIntegrityCheckInTheEnd(SyncOperationType operationType) {
		return getRelatedSynconfiguration().isDoIntegrityCheckInTheEnd(operationType);
	}
	
	@JsonIgnore
	public String getId() {
		return this.getRelatedSynconfiguration().getDesignation() + "_" + this.tableName;
	}
	
	@JsonIgnore
	public boolean isUuidColumnNotExists() {
		return this.tableName.equals("patient") ? true : false;
		
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
	public SyncConfiguration getRelatedSynconfiguration() {
		return relatedSyncTableInfoSource;
	}

	public void setRelatedSyncTableInfoSource(SyncConfiguration relatedSyncTableInfoSource) {
		this.relatedSyncTableInfoSource = relatedSyncTableInfoSource;
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
		if (primaryKey == null) {
			
			OpenConnection conn = relatedSyncTableInfoSource.getMainApp().openConnection();
			
			try {
				ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, tableName);
				
				if (rs.next()) {
					this.primaryKey = rs.getString("COLUMN_NAME");
				
					this.primaryKeyType = DBUtilities.determineColunType(tableName, this.primaryKey, conn);
				
					this.primaryKeyType = AttDefinedElements.convertMySQLTypeTOJavaType(this.primaryKeyType);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		return primaryKey;
	}
	
	@JsonIgnore
	public String getPrimaryKeyType() {
		if (primaryKeyType == null) getPrimaryKey();
		
		return primaryKeyType;
	}
	
	@JsonIgnore
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.getPrimaryKeyType());
	}
	
	@JsonIgnore
	public boolean hasPK() {
		return getPrimaryKey() != null;
	}
	
	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}
	
	public boolean checkIfisIgnorableParentByClassAttName(String parentAttName, Connection conn) {
		for (RefInfo  parent : this.getParents()) {
			if (parent.getRefColumnAsClassAttName().equals(parentAttName)) {
				return parent.isIgnorable();
			}
		}
		
		throw new ForbiddenOperationException("The att '" + parentAttName + "' doesn't represent any defined parent att");
	}
	
	private synchronized void loadChildren(Connection conn) throws SQLException {
		logInfo("LOADING CHILDREN FOR TABLE '" + getTableName() + "'");
		
		this.childred = new ArrayList<RefInfo>();  
		
		ResultSet foreignKeyRS = conn.getMetaData().getExportedKeys(null, null, tableName);
		
		foreignKeyRS.last();
		
		logDebug("DISCOVERED '" + foreignKeyRS.getRow() + "' CHILDREN FOR TABLE '" + getTableName() + "'");
		
		foreignKeyRS.beforeFirst();
	
		while(foreignKeyRS.next()) {
			logDebug("CONFIGURING CHILD [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "'");
			
			RefInfo ref = new RefInfo();
			
			ref.setRefType(RefInfo.CHILD_REF_TYPE);
			ref.setRefColumnName(foreignKeyRS.getString("FKCOLUMN_NAME"));
			ref.setRefTableConfiguration(SyncTableConfiguration.init(foreignKeyRS.getString("FKTABLE_NAME"), this.relatedSyncTableInfoSource));
			ref.setRefColumnType(AttDefinedElements.convertMySQLTypeTOJavaType(DBUtilities.determineColunType(ref.getRefTableConfiguration().getTableName(), ref.getRefColumnName(), conn)));
			ref.setRelatedSyncTableConfiguration(this);
			ref.setIgnorable(DBUtilities.isTableColumnAllowNull(ref.getRefTableConfiguration().getTableName(), ref.getRefColumnName(), conn));
			
			//Mark as metadata if there is no table info configured
			if (getRelatedSynconfiguration().find(ref.getRefTableConfiguration()) == null) {
				ref.getRefTableConfiguration().setMetadata(true);
			}
			
			this.childred.add(ref);
			
			logDebug("CHILDREN [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "' CONFIGURED");
		}
		
		logInfo("LOADED CHILDREN FOR TABLE '" + getTableName() + "'");
	}
	
	public void logInfo(String msg) {
		getRelatedSynconfiguration().logInfo(msg);
	}
	
	public void logDebug(String msg) {
		getRelatedSynconfiguration().logDebug(msg);
	}
	
	public void logWarn(String msg) {
		getRelatedSynconfiguration().logWarn(msg);
	}
		
	public void logErr(String msg) {
		getRelatedSynconfiguration().logErr(msg);
	}
	
	private synchronized void loadParents(Connection conn) throws SQLException {
		logInfo("LOADING PARENTS FOR TABLE '" + getTableName() + "'");
		
		List<RefInfo> auxRefInfo = new ArrayList<RefInfo>();  
		
		ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(null, null, tableName);
		
		foreignKeyRS.last();
		
		logDebug("DISCOVERED '" + foreignKeyRS.getRow() + "' PARENTS FOR TABLE '" + getTableName() + "'");
		
		foreignKeyRS.beforeFirst();
		
		while(foreignKeyRS.next()) {
			logDebug("CONFIGURING PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "'");
			
			String refColumName = foreignKeyRS.getString("FKCOLUMN_NAME");
			
			SyncTableConfiguration refTableConfiguration = SyncTableConfiguration.init(foreignKeyRS.getString("PKTABLE_NAME"), this.relatedSyncTableInfoSource);
			
			RefInfo ref = generateRefInfo(refColumName, RefInfo.PARENT_REF_TYPE, refTableConfiguration, conn);
			
			if (utilities.existOnArray(auxRefInfo, ref)) {
				logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "' WAS ALREDY CONFIGURED! SKIPPING...");
				continue;	
			}
			
			RefInfo configuredParent = findParent(ref);
			
			if (configuredParent != null) {
				ref.setDefaultValueDueInconsistency(configuredParent.getDefaultValueDueInconsistency());
				ref.setSetNullDueInconsistency(configuredParent.isSetNullDueInconsistency());
			}
			
			logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "' CONFIGURED");
			
			auxRefInfo.add(ref);
		}
		
		//Check if there is a configured parent but not defined on the db schema
		
		if (utilities.arrayHasElement(this.parents)){
			for (RefInfo configuredParent : this.parents) {
				if (configuredParent.getRefColumnName() == null) continue;
				
				 RefInfo autoGeneratedParent = utilities.findOnList(auxRefInfo, configuredParent);
				 
				 if (autoGeneratedParent == null) {
					 configuredParent.setRefTableConfiguration(SyncTableConfiguration.init(configuredParent.getTableName(), this.relatedSyncTableInfoSource));
					 configuredParent.setRelatedSyncTableConfiguration(this);		
						
					 auxRefInfo.add(configuredParent);
				 }
			}
		}
		
		this.parents = auxRefInfo;
		
		logInfo("LOADED PARENTS FOR TABLE '" + getTableName() + "'");
		
	}
			

	private RefInfo generateRefInfo(String refColumName, String refType, SyncTableConfiguration refTableConfiguration, Connection conn) throws DBException {
		String refColumnType = AttDefinedElements.convertMySQLTypeTOJavaType(DBUtilities.determineColunType(this.getTableName(), refColumName, conn));
		boolean ignorable = DBUtilities.isTableColumnAllowNull(this.tableName, refColumName, conn);
		
		RefInfo ref = new RefInfo();
		
		ref.setRefType(refType);
		ref.setRefColumnName(refColumName);
		ref.setRefTableConfiguration(refTableConfiguration);
		ref.setIgnorable(ignorable);
		ref.setRefColumnType(refColumnType);
		ref.setRelatedSyncTableConfiguration(this);		
		
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
			tableInfo.setRelatedSyncTableInfoSource(sourceInfo);
			
			sourceInfo.addToTableConfigurationPull(tableInfo);
		}
		
		return tableInfo;
	}
	
	@JsonIgnore
	public Class<OpenMRSObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException{
		if (syncRecordClass == null) this.syncRecordClass = OpenMRSPOJOGenerator.tryToGetExistingCLass(generateFullClassName(application), getRelatedSynconfiguration());
		
		if (syncRecordClass == null) throw new ForbiddenOperationException("The related pojo of table " + getTableName() + " was not found!!!!");
		
		return syncRecordClass;
	}
	
	private static String[] REMOVABLE_METADATA = {};

	/**
	 * By default an metadata cannot be removed, but there are situations where is needed to remove a metadata
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
		} catch (ForbiddenOperationException e) {
			
			return false;
		}
	}

	public void setSyncRecordClass(Class<OpenMRSObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}

	@JsonIgnore
	public String generateFullClassName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.eptssync.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return  utilities.concatStringsWithSeparator(fullPackageName,  generateClassName(),  ".");
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
		return getRelatedSynconfiguration().getOriginAppLocationCode();
	}
	
	public void generateRecordClass(AppInfo application, boolean fullClass,  Connection conn) {
		try {
			if (fullClass) {
				this.syncRecordClass = OpenMRSPOJOGenerator.generate(this, application, conn);
			}
			else {
				this.syncRecordClass = OpenMRSPOJOGenerator.generateSkeleton(this, application, conn);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	public void generateSkeletonRecordClass(AppInfo application, Connection conn) {
		try {
			this.syncRecordClass = OpenMRSPOJOGenerator.generateSkeleton(this, application, conn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (SQLException e) {
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
	public String getSyncStageSchema() {
		return getRelatedSynconfiguration().getSyncStageSchema();
	}

	@JsonIgnore
	public String generateFullStageTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageTableName();
	}
	
	public boolean existRelatedExportStageTable(Connection conn) {
		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = generateRelatedStageTableName();

		try {
			return DBUtilities.isResourceExist(schema, resourceType, tabName, conn);
		} catch (SQLException e) {
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
		for (SyncTableConfiguration tabConf : getRelatedSynconfiguration().getTablesConfigurations()) {
			if (tabConf.equals(this)) return true;
		}
		
		return false;
	}
	
	private synchronized void fullLoad(Connection conn) {
		try {
			
			getPrimaryKey();
			
			loadParents(conn);
			loadChildren(conn);
			this.fullLoaded = true;
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
	}
	
	public synchronized void fullLoad() {
		OpenConnection conn = getRelatedSynconfiguration().getMainApp().openConnection();
		
		try {
			fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	public RefInfo getSharedKeyRefInfo(Connection conn) {
		if (sharePkWith == null) {
			return null;
		}
		else
		for (RefInfo refInfo : getParents()) {
			if (refInfo.getRefTableConfiguration().getTableName().equalsIgnoreCase(this.sharePkWith)) {
				return refInfo;
			}
		}
			
		throw new ForbiddenOperationException("The related table of shared pk " + sharePkWith + " of table " + this.getTableName() + " is not listed inparents!");
	}

	@Override
	@JsonIgnore
	public String toString() {
		return "Table [name:" + this.tableName + ", pk: " + this.primaryKey +"]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof SyncTableConfiguration)) return false;
		
		return this.getTableName().equalsIgnoreCase(((SyncTableConfiguration)obj).getTableName());
	}

	@JsonIgnore
	public File getPOJOCopiledFilesDirectory() {
		return getRelatedSynconfiguration().getPOJOCompiledFilesDirectory();
	}

	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		return getRelatedSynconfiguration().getPOJOSourceFilesDirectory();
	}
	
	public RefInfo findParent(RefInfo parent) {
		if (!utilities.arrayHasElement(this.parents)) return null;
		
		for (RefInfo info : this.parents) {
			if (info.getTableName().equals(parent.getTableName())) return info;
		}
		
		return null;
	}

	@Override
	public int compareTo(SyncTableConfiguration o) {
		if (this.equals(o)) return 0;
		
		return this.tableName.compareTo(o.getTableName());
	}

	@JsonIgnore
	public File getClassPath() {
		return new File(relatedSyncTableInfoSource.getClassPath());
	}
	
	@JsonIgnore
	public boolean isDestinationInstallationType() {
		return getRelatedSynconfiguration().isDataBaseMergeFromJSONProcess();
	}
	
	@JsonIgnore
	public boolean isDataReconciliationProcess() {
		return getRelatedSynconfiguration().isDataReconciliationProcess();
	}
	
	@JsonIgnore
	public boolean isDBQuickLoad() {
		return getRelatedSynconfiguration().isDBQuickLoadProcess();
	}
	
	@JsonIgnore
	public boolean isDBQuickCopy() {
		return getRelatedSynconfiguration().isDBQuickCopyProcess();
	}
	
	@JsonIgnore
	public boolean isDataBasesMergeFromSourceDBProcess() {
		return getRelatedSynconfiguration().isDataBaseMergeFromSourceDBProcess();
	}
	
	public boolean hasNoDateVoidedField() {
		return utilities.isStringIn(getTableName(), "note");
	}
	
	public boolean hasNotDateChangedField() {
		return utilities.isStringIn(getTableName(), "obs");
	}
}
