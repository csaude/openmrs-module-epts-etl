package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.OpenMRSClassGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableConfiguration {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	private String tableName;
	
	private List<String> parents;
	
	private List<ParentRefInfo> parentRefInfo;
	private List<ParentRefInfo> childRefInfo;
	
	private Class<OpenMRSObject> syncRecordClass;

	private SyncConfiguration relatedSyncTableInfoSource;

	private String primaryKey;
	private String primaryKeyType;
	private String sharePkWith;
	
	private String extraConditionForExport;
	
	private boolean metadata;
	
	/*private int qtyRecordsPerEngine;
	private int qtyRecordsPerSelect;*/
	private boolean fullLoaded;
	
	public SyncTableConfiguration() {
	}
	
	public String getClasspackage() {
		return getRelatedSynconfiguration().getClasspackage();
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
	
	public List<String> getParents() {
		return parents;
	}
	
	public void setParents(List<String> parents) {
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
	public Class<OpenMRSObject> getSyncRecordClass(Connection conn) {
		if (syncRecordClass == null) {
			generateRecordClass(false, conn);
		}
		
		return syncRecordClass;
	}

	public void setSyncRecordClass(Class<OpenMRSObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
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
	
	public String getPrimaryKeyAsClassAtt(Connection conn) {
		return convertTableAttNameToClassAttName(getPrimaryKey(conn));
	}

	public String getPrimaryKey(Connection conn) {
		if (primaryKey == null) {
			
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
		}
		
		return primaryKey;
	}
	
	public String getPrimaryKeyType() {
		return primaryKeyType;
	}
	
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.getPrimaryKeyType());
	}
	
	public boolean hasPK(Connection conn) {
		return getPrimaryKey(conn) != null;
	}
	
	public void setChildRefInfo(List<ParentRefInfo> childRefInfo) {
		this.parentRefInfo = childRefInfo;
	}
	
	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}
	
	
	public synchronized List<ParentRefInfo> getChildRefInfo(Connection conn) {
		if (this.childRefInfo == null) {
			
			try {
				this.childRefInfo = new ArrayList<ParentRefInfo>();  
				
				ResultSet foreignKeyRS = conn.getMetaData().getExportedKeys(null, null, tableName);
				
				while(foreignKeyRS.next()) {
					ParentRefInfo ref = new ParentRefInfo();
					
					ref.setReferenceColumnName(foreignKeyRS.getString("FKCOLUMN_NAME"));
					ref.setReferenceTableInfo(SyncTableConfiguration.init(foreignKeyRS.getString("FKTABLE_NAME"), this.relatedSyncTableInfoSource));
					
					ref.setReferencedColumnName(foreignKeyRS.getString("PKCOLUMN_NAME"));
					ref.setReferencedTableInfo(this);
					
					ref.setIgnorable(DBUtilities.isTableColumnAllowNull(ref.getReferenceTableInfo().getTableName(), ref.getReferenceColumnName(), conn));
					
					//Mark as metadata if there is no table info congigured
					if (getRelatedSynconfiguration().find(ref.getReferenceTableInfo()) == null) {
						ref.setMetadata(true);
					}
					
					this.childRefInfo.add(ref);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return childRefInfo;
	}

	public void setParentRefInfo(List<ParentRefInfo> parentRefInfo) {
		this.parentRefInfo = parentRefInfo;
	}
	
	public boolean checkIfisIgnorableParentByClassAttName(String parentAttName, Connection conn) {
		for (ParentRefInfo  parent : this.getParentRefInfo(conn)) {
			if (parent.getReferenceColumnAsClassAttName().equals(parentAttName)) {
				return parent.isIgnorable();
			}
		}
		
		throw new ForbiddenOperationException("The att '" + parentAttName + "' doesn't represent any defined parent att");
	}
	
	
	public synchronized List<ParentRefInfo> getParentRefInfo(Connection conn) {
		if (this.parentRefInfo == null) {
			
			try {
				this.parentRefInfo = new ArrayList<ParentRefInfo>();  
				
				ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(null, null, tableName);
				
				while(foreignKeyRS.next()) {
					ParentRefInfo ref = new ParentRefInfo();
					
					ref.setReferenceColumnName(foreignKeyRS.getString("FKCOLUMN_NAME"));
					ref.setReferencedColumnName(foreignKeyRS.getString("PKCOLUMN_NAME"));
					ref.setReferenceTableInfo(this);
					
					ref.setReferencedTableInfo(SyncTableConfiguration.init(foreignKeyRS.getString("PKTABLE_NAME"), this.relatedSyncTableInfoSource));
					
					ref.setIgnorable(DBUtilities.isTableColumnAllowNull(this.tableName, ref.getReferenceColumnName(), conn));
					
					ref.setRefColumnType(AttDefinedElements.convertMySQLTypeTOJavaType(DBUtilities.determineColunType(this.getTableName(), ref.getReferenceColumnName(), conn)));
					
					//Mark as metadata if is not specificaly mapped as parent in conf file
					if (this.parents != null && !this.parents.contains(foreignKeyRS.getString("PKTABLE_NAME"))) {
						ref.setMetadata(true);
					}
					
					if (this.sharePkWith != null && this.sharePkWith.equalsIgnoreCase(ref.getReferenceColumnName())) {
						ref.setSharedPk(true);
					}
					
					this.parentRefInfo.add(ref);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return parentRefInfo;
	}
			
	private static SyncTableConfiguration init(String tableName, SyncConfiguration sourceInfo) {
		SyncTableConfiguration tableInfo = sourceInfo.findPulledTableConfiguration(tableName);
		
		if (tableInfo == null) {
			tableInfo = new SyncTableConfiguration();
			tableInfo.setTableName(tableName);
			tableInfo.setRelatedSyncTableInfoSource(sourceInfo);
			
			sourceInfo.addToTableConfigurationPull(tableInfo);
		}
		
		return tableInfo;
	}

	public Class<OpenMRSObject> getRecordClass() {
		String root = getRelatedSynconfiguration().getPojoProjectLocation().getAbsolutePath();

		File destinationFileLocation = new File(root + "/bin/");

		this.syncRecordClass = OpenMRSClassGenerator.tryToGetExistingCLass(destinationFileLocation, this.generateFullClassName());
	
		if (this.syncRecordClass == null) throw new ForbiddenOperationException("No Sync Record Class found for: " + this.tableName);
		
		return this.syncRecordClass; 
	}

	public String generateFullClassName() {
		return "org.openmrs.module.eptssync.model.openmrs." + getClasspackage() + "." + generateClassName();
	}
	
	public String getOriginAppLocationCode() {
		return getRelatedSynconfiguration().getOriginAppLocationCode();
	}

	
	public void generateRecordClass(boolean fullClass, Connection conn) {
		try {
			if (fullClass) {
				this.syncRecordClass = OpenMRSClassGenerator.generate(this, conn);
			}
			else {
				this.syncRecordClass = OpenMRSClassGenerator.generateSkeleton(this, conn);
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
			this.syncRecordClass = OpenMRSClassGenerator.generateSkeleton(this, conn);
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
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}

	/*
	private boolean isIndexOnFirtExportDoneColumn(Connection conn) throws SQLException {
		return DBUtilities.isResourceExist(conn.getCatalog(), DBUtilities.RESOURCE_TYPE_INDEX,  generateNameOfIndexOnExportDoneColumn(), conn);
	}
	
	private String generateNameOfIndexOnExportDoneColumn() {
		return this.tableName + "__index";
	}
	 */
	
	/*
	private void createIndexOnFirstExportDoneCOlumn(Connection conn) throws SQLException {
		String sql = "CREATE INDEX " + generateNameOfIndexOnExportDoneColumn() + " ON " + this.tableName + "(first_export_done);";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();
		st.close();
	}*/


	public boolean mustCreateStageSchemaElements() {
		return getRelatedSynconfiguration().mustCreateStageSchemaElements();
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


	/*
	private boolean isFirstExportDoneColumnExistsOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "first_export_done", conn);
	}

	private String generateFirstExportColumnGeneration() {
		return "first_export_done int(1) NULL";
	}
	*/
	

	/*
	public int getQtyRecordsPerEngine(String operationType) {
		return this.qtyRecordsPerEngine != 0 ? this.qtyRecordsPerEngine : getRelatedSyncTableInfoSource().getDefaultQtyRecordsPerEngine(operationType);
	}*/
	

	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	public synchronized void fullLoad(Connection conn) {
		getParentRefInfo(conn);
		getChildRefInfo(conn);

		this.fullLoaded = true;
	}

	public ParentRefInfo getSharedKeyRefInfo(Connection conn) {
		for (ParentRefInfo refInfo : getParentRefInfo(conn)) {
			if (refInfo.isSharedPk()) return refInfo;
		}
			
		return null;
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
}
