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
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableConfiguration {
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
	
	public SyncTableConfiguration() {
	}
	
	public boolean isRemoveForbidden() {
		return removeForbidden;
	}
	
	public void setRemoveForbidden(boolean removeForbidden) {
		this.removeForbidden = removeForbidden;
	}
	
	public List<RefInfo> getChildred() {
		return childred;
	}
	
	public void setChildred(List<RefInfo> childred) {
		this.childred = childred;
	}
	
	public String getClasspackage() {
		return getRelatedSynconfiguration().getPojoPackage();
	}
	
	public boolean isDoIntegrityCheckInTheEnd(String operationType) {
		return getRelatedSynconfiguration().isDoIntegrityCheckInTheEnd(operationType);
	}
	
	public String getId() {
		return this.getRelatedSynconfiguration().getDesignation() + "_" + this.tableName;
	}
	
	public boolean isFirstExport() {
		return this.relatedSyncTableInfoSource.isFirstExport();
	}

	
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
	
	public boolean useSharedPKKey() {
		return utilities.stringHasValue(this.sharePkWith);
	}
	
	public String getPrimaryKeyAsClassAtt() {
		return convertTableAttNameToClassAttName(getPrimaryKey());
	}
	
	public OpenConnection openConnection() {
		return relatedSyncTableInfoSource.openConnetion();
	}
	
	public String getPrimaryKey() {
		if (primaryKey == null) {
			
			OpenConnection conn = openConnection();
			
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
	
	public String getPrimaryKeyType() {
		if (primaryKeyType == null) getPrimaryKey();
		
		return primaryKeyType;
	}
	
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.getPrimaryKeyType());
	}
	
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
		
		logInfo("DISCOVERED '" + foreignKeyRS.getRow() + "' CHILDREN FOR TABLE '" + getTableName() + "'");
		
		foreignKeyRS.beforeFirst();
	
		while(foreignKeyRS.next()) {
			logInfo("CONFIGURING CHILD [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "'");
			
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
			
			logInfo("CHILDREN [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "' CONFIGURED");
		}
	}
	
	public void logInfo(String msg) {
		getRelatedSynconfiguration().logInfo(msg);
	}
	
	private synchronized void loadParents(Connection conn) throws SQLException {
		logInfo("LOADING PARENTS FOR TABLE '" + getTableName() + "'");
		
		List<RefInfo> auxRefInfo = new ArrayList<RefInfo>();  
		
		ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(null, null, tableName);
		
		foreignKeyRS.last();
		
		logInfo("DISCOVERED '" + foreignKeyRS.getRow() + "' PARENTS FOR TABLE '" + getTableName() + "'");
		
		foreignKeyRS.beforeFirst();
		
		while(foreignKeyRS.next()) {
			logInfo("CONFIGURING PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "'");
			
			RefInfo ref = new RefInfo();
			ref.setRefType(RefInfo.PARENT_REF_TYPE);
			
			ref.setRefColumnName(foreignKeyRS.getString("FKCOLUMN_NAME"));
			ref.setRefTableConfiguration(SyncTableConfiguration.init(foreignKeyRS.getString("PKTABLE_NAME"), this.relatedSyncTableInfoSource));
			ref.setIgnorable(DBUtilities.isTableColumnAllowNull(this.tableName, ref.getRefColumnName(), conn));
			ref.setRefColumnType(AttDefinedElements.convertMySQLTypeTOJavaType(DBUtilities.determineColunType(this.getTableName(), ref.getRefColumnName(), conn)));
			ref.setRelatedSyncTableConfiguration(this);
			
			RefInfo configuredParent = findParent(ref);
			
			if (configuredParent != null) {
				ref.setDefaultValueDueInconsistency(configuredParent.getDefaultValueDueInconsistency());
			}
			
			//Mark as metadata if is not specificaly mapped as parent in conf file
			if (!ref.getRefTableConfiguration().isConfigured()) {
				ref.getRefTableConfiguration().setMetadata(true);
			}
			
			logInfo("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName() + "' CONFIGURED");
			
			auxRefInfo.add(ref);
		}
		
		this.parents = auxRefInfo;
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
	public Class<OpenMRSObject> getSyncRecordClass() throws ForbiddenOperationException{
		if (syncRecordClass == null) this.syncRecordClass = OpenMRSPOJOGenerator.tryToGetExistingCLass(getRelatedSynconfiguration().getPOJOCompiledFilesDirectory(), generateFullClassName());
		
		if (syncRecordClass == null) throw new ForbiddenOperationException("The related pojo of table " + getTableName() + " was not found!!!!");
		
		return syncRecordClass;
	}
	
	public boolean existsSyncRecordClass() {
		try {
			return getSyncRecordClass() != null;
		} catch (ForbiddenOperationException e) {
			
			return false;
		}
	}

	public void setSyncRecordClass(Class<OpenMRSObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}

	public String generateFullClassName() {
		return "org.openmrs.module.eptssync.model.pojo." + getClasspackage() + "." + generateClassName();
	}
	
	public String getOriginAppLocationCode() {
		return getRelatedSynconfiguration().getOriginAppLocationCode();
	}

	
	public void generateRecordClass(boolean fullClass, Connection conn) {
		try {
			if (fullClass) {
				this.syncRecordClass = OpenMRSPOJOGenerator.generate(this, conn);
			}
			else {
				this.syncRecordClass = OpenMRSPOJOGenerator.generateSkeleton(this, conn);
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

	
	public void generateSkeletonRecordClass(Connection conn) {
		try {
			this.syncRecordClass = OpenMRSPOJOGenerator.generateSkeleton(this, conn);
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
		if (utilities.isStringIn(this.getTableName(), "obs") && metadata) throw new ForbiddenOperationException("Obs cannot be metadata");

		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
		
		if (utilities.isStringIn(this.getTableName(), "obs") && metadata) throw new ForbiddenOperationException("Obs cannot be metadata");
	}
	
	public String generateRelatedStageTableName() {
		return this.getTableName() + "_stage";
	}

	public String getSyncStageSchema() {
		return getRelatedSynconfiguration().getSyncStageSchema();
	}

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

	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	public boolean isConfigured() {
		for (SyncTableConfiguration tabConf : getRelatedSynconfiguration().getTablesConfigurations()) {
			if (tabConf.equals(this)) return true;
		}
		
		return false;
	}
	
	public synchronized void fullLoad() {
		OpenConnection conn = openConnection();
		
		try {
			getPrimaryKey();
			
			loadParents(conn);
			loadChildren(conn);
			this.fullLoaded = true;
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
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
	public String toString() {
		return "Table [name:" + this.tableName + ", pk: " + this.primaryKey +"]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof SyncTableConfiguration)) return false;
		
		return this.getTableName().equalsIgnoreCase(((SyncTableConfiguration)obj).getTableName());
	}

	public File getPOJOCopiledFilesDirectory() {
		return getRelatedSynconfiguration().getPOJOCompiledFilesDirectory();
	}

	public File getPOJOSourceFilesDirectory() {
		return getRelatedSynconfiguration().getPOJOSourceFilesDirectory();
	}
	
	public RefInfo findParent(RefInfo parent) {
		return utilities.findOnList(this.parents, parent);
	}
}
