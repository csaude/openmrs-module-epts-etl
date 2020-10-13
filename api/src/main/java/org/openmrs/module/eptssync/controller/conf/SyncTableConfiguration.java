package org.openmrs.module.eptssync.controller.conf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.base.BaseDAO;
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
	

	private boolean mustRecompileTable;

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
	private static Logger logger = Logger.getLogger(SyncTableConfiguration.class);
	
	public SyncTableConfiguration() {
	}

	public String getClasspackage() {
		return getRelatedSyncTableInfoSource().getClasspackage();
	}
	
	public boolean isDoIntegrityCheckInTheEnd(String operationType) {
		return getRelatedSyncTableInfoSource().isDoIntegrityCheckInTheEnd(operationType);
	}
	
	/*
	public int getQtyRecordsPerSelect(String operationType) {
		return qtyRecordsPerSelect != 0 ? qtyRecordsPerSelect : getRelatedSyncTableInfoSource().getDefaultQtyRecordsPerSelect(operationType);
	}

	public void setQtyRecordsPerSelect(int qtyRecordsPerSelect) {
		this.qtyRecordsPerSelect = qtyRecordsPerSelect;
	}

	public void setQtyRecordsPerEngine(int qtyRecordsPerEngine) {
		this.qtyRecordsPerEngine = qtyRecordsPerEngine;
	}
	*/
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
			generateRecordClass(conn);
		}
		
		return syncRecordClass;
	}

	public void setSyncRecordClass(Class<OpenMRSObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}

	public SyncConfiguration getRelatedSyncTableInfoSource() {
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
	
	public synchronized List<ParentRefInfo> getChildRefInfo(Connection conn) {
		if (this.childRefInfo == null) {
			
			logInfo("DISCOVERING CHILDREN FOR '" + this.tableName + "'");
			
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
					if (getRelatedSyncTableInfoSource().find(ref.getReferenceTableInfo()) == null) {
						ref.setMetadata(true);
					}
				
					
					this.childRefInfo.add(ref);
				}
			
				logInfo(this.childRefInfo.size() + " CHILDREN FOR '" + this.tableName + "' DISCOVERED");
				
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
	
	public synchronized List<ParentRefInfo> getParentRefInfo(Connection conn) {
		if (this.parentRefInfo == null) {
			
			logInfo("DISCOVERING PARENTS FOR '" + this.tableName + "'");
			
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
			
				logInfo(this.parentRefInfo.size() + " PARENTS FOR '" + this.tableName + "' DISCOVERED");
			} catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return parentRefInfo;
	}

	private static SyncTableConfiguration init(String tableName, SyncConfiguration sourceInfo) {
		SyncTableConfiguration tableInfo = new SyncTableConfiguration();
		tableInfo.setTableName(tableName);
		tableInfo.setRelatedSyncTableInfoSource(sourceInfo);
		tableInfo.setMustRecompileTable(sourceInfo.isMustRecompileTable());
		return tableInfo;
	}

	public Class<OpenMRSObject> getRecordClass() {
		if (this.syncRecordClass == null) {
			this.syncRecordClass = OpenMRSClassGenerator.tryToGetExistingCLass(this.generateFullClassName());
		
			if (this.syncRecordClass == null) throw new ForbiddenOperationException("No Sync Record Class found for: " + this.tableName);
		}
		
		return this.syncRecordClass; 
	}

	public String generateFullClassName() {
		return "org.openmrs.module.eptssync.model.openmrs." + getClasspackage() + "." + generateClassName();
	}
	
	public String getOriginAppLocationCode() {
		return getRelatedSyncTableInfoSource().getOriginAppLocationCode();
	}

	
	public void generateRecordClass(Connection conn) {
		try {
			this.syncRecordClass = OpenMRSClassGenerator.generate(this, conn);
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

	public void setMustRecompileTable(boolean mustRecompileTable) {
		this.mustRecompileTable = mustRecompileTable;
	}

	public boolean isMustRecompileTable() {
		return mustRecompileTable;
	}

	public boolean mustRecompileTableClass() {
		return isMustRecompileTable();
	}

	public boolean isMetadata() {
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}

	/**
	 * Try to generate on related table the aditional information needed for
	 * synchronization process
	 * 
	 * @throws SQLException
	 */
	public void tryToUpgradeDataBaseInfo(Connection conn) throws SQLException {
		logInfo("UPGRATING TABLE INFO [" + this.tableName + "]");
		
		String newColumnDefinition = "";
		
		if (mustCreateStageSchemaElements() && !existRelatedExportStageTable(conn)) {
			logInfo("GENERATING RELATED STAGE TABLE FOR [" + this.tableName + "]");
			
			createRelatedExportStageTable(conn);
			
			logInfo("RELATED STAGE TABLE FOR [" + this.tableName + "] GENERATED");
			
		}
		
		/*if (!isFirstExportDoneColumnExistsOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateFirstExportColumnGeneration());
		}*/
		
		if (!isConsistentColumnExistOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateConsistentColumnGeneration());
		}

		if (!isLastSyncDateColumnExistOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateLastSyncDateColumnCreation());
		}

		/*if (!isUuidColumnExistOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateUuidColumnCreation());
		}*/

		if (!isOriginRecordIdColumnExistOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateOriginRecordIdColumnGeneration());
		}

		if (!isDateChangedColumnExistOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateDateChangedColumnGeneration());
		}

		if (!isOriginAppLocationCodeColumnExistsOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateOriginAppLocationCodeColumnGeneration());
		}
		
		
		String uniqueOrigin = "";
		
		if (!isUniqueOriginConstraintsExists(conn)) {
			uniqueOrigin = "UNIQUE KEY " + generateUniqueOriginConstraintsName() + "(origin_record_id, origin_app_location_code)";
		}
		
		String batch = "";
		
		if (utilities.stringHasValue(newColumnDefinition)) {
			batch = "ALTER TABLE " + this.getTableName() + " ADD (" + newColumnDefinition + ")"; 
		}
		
		if (utilities.stringHasValue(uniqueOrigin)) {
			batch +=  (!utilities.stringHasValue(batch) ? "ALTER TABLE " + this.getTableName() + " ADD " + uniqueOrigin : ", ADD " + uniqueOrigin); 
		}
	
		if (utilities.stringHasValue(batch)) {
			logInfo("CONF COLUMNS FOR TABLE [" + this.tableName + "] CREATED");
			BaseDAO.executeBatch(conn, batch);
			logInfo("CREATING CONF COLUMNS FOR TABLE [" + this.tableName + "]");
		}
		
		/*
		if (!isIndexOnFirtExportDoneColumn(conn)) {
			createIndexOnFirstExportDoneCOlumn(conn);
		}*/
		
		if (!isExistRelatedTriggers(conn)) {
			logInfo("CREATING RELATED TRIGGERS FOR [" + this.tableName + "]");
			
			createLastUpdateDateMonitorTrigger(conn);
		
			logInfo("RELATED TRIGGERS FOR [" + this.tableName + "] CREATED");
		}
	}

	private boolean isUniqueOriginConstraintsExists(Connection conn) throws SQLException {
		String unqKey = generateUniqueOriginConstraintsName(); 
	
		return DBUtilities.isIndexExistsOnTable(conn.getCatalog(), getTableName(), unqKey, conn);
	}

	private String generateUniqueOriginConstraintsName() {
		return this.getTableName() + "origin_unq";
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

	private void createRelatedExportStageTable(Connection conn) {
		String sql = "";

		sql += "CREATE TABLE " + getSyncStageSchema() + "." + generateRelatedStageTableName() + "(\n";
		sql += "	id int(11) NOT NULL AUTO_INCREMENT,\n";
		sql += "	record_id int(11) NOT NULL,\n";
		sql += "	creation_date DATETIME DEFAULT CURRENT_TIMESTAMP,\n";
		sql += "	json VARCHAR(7500) NOT NULL,\n";
		sql += "	origin_app_location_code VARCHAR(100) NOT NULL,\n";
		sql += "	last_migration_try_date DATETIME DEFAULT NULL,\n";
		sql += "	last_migration_try_err varchar(250) DEFAULT NULL,\n";
		sql += "	migration_status int(1) DEFAULT 1,\n";
		sql += "	CONSTRAINT CHK_" + generateRelatedStageTableName() + "_MIG_STATUS CHECK (migration_status = -1 OR migration_status = 0 OR migration_status = 1),";
		sql += "	UNIQUE KEY " + generateRelatedStageTableName() + "UNQ_RECORD(record_id, origin_app_location_code),\n";
		sql += "	PRIMARY KEY (id)\n";
		sql += ")\n";
		sql += " ENGINE=InnoDB DEFAULT CHARSET=utf8";
		
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();

			st.close();
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} 
	}
	
	public boolean mustCreateStageSchemaElements() {
		return getRelatedSyncTableInfoSource().mustCreateStageSchemaElements();
	}

	public String generateRelatedStageTableName() {
		return this.getTableName() + "_stage";
	}

	public String getSyncStageSchema() {
		return getRelatedSyncTableInfoSource().getSyncStageSchema();
	}

	public String generateFullStageTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageTableName();
	}
	
	private boolean existRelatedExportStageTable(Connection conn) {
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

	private boolean isOriginAppLocationCodeColumnExistsOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "origin_app_location_code", conn);
	}

	private String generateOriginAppLocationCodeColumnGeneration() {
		return "origin_app_location_code VARCHAR(100) NULL";
	}

	/*
	private boolean isFirstExportDoneColumnExistsOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "first_export_done", conn);
	}

	private String generateFirstExportColumnGeneration() {
		return "first_export_done int(1) NULL";
	}
	*/
	
	private String generateDateChangedColumnGeneration() throws SQLException {
		return "date_changed datetime NULL";
	}

	private boolean isDateChangedColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "date_changed", conn);
	}

	private void createLastUpdateDateMonitorTrigger(Connection conn) throws SQLException {
		Statement st = conn.createStatement();

		st.addBatch(generateTriggerCode(this.generateLastUpdateDateInsertTriggerMonitor(), "INSERT"));
		st.addBatch(generateTriggerCode(this.generateLastUpdateDateUpdateTriggerMonitor(), "UPDATE"));

		st.executeBatch();

		st.close();

	}

	private String generateTriggerCode(String triggerName, String triggerEvent) {
		String sql = "";

		// sql += "DELIMITER $$\n";
		sql += "CREATE TRIGGER " + triggerName + " BEFORE " + triggerEvent + " ON " + this.tableName + "\n";
		sql += "FOR EACH ROW\n";
		sql += "	BEGIN\n";
		sql += "		SET NEW.date_changed = CURRENT_TIMESTAMP();\n";
		sql += "	END;\n";
		// sql += "$$\n";
		// sql += "DELIMITER ;";

		return sql;
	}

	private boolean isExistRelatedTriggers(Connection conn) throws SQLException {
		return DBUtilities.isResourceExist(conn.getCatalog(), DBUtilities.RESOURCE_TYPE_TRIGGER,
				generateLastUpdateDateInsertTriggerMonitor(), conn);
	}

	private String generateLastUpdateDateInsertTriggerMonitor() {
		return this.tableName + "_date_changed_insert_monitor";
	}

	private String generateLastUpdateDateUpdateTriggerMonitor() {
		return this.tableName + "_date_changed_update_monitor";
	}

	private String generateLastSyncDateColumnCreation() {
		return "last_sync_date datetime NULL";
	}

	private boolean isLastSyncDateColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "last_sync_date", conn);
	}

	private String generateOriginRecordIdColumnGeneration() {
		return "origin_record_id int(11) NULL";
	}

	private boolean isConsistentColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "consistent", conn);
	}

	private String generateConsistentColumnGeneration() {
		return "consistent int(1) DEFAULT 1";
	}

	/*private boolean isUuidColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "uuid", conn);
	}*/

	/*private String generateUuidColumnCreation() {
		return "uuid char(38) NULL";
	}*/
	
	private boolean isOriginRecordIdColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "origin_record_id", conn);
	}
	
	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}
	
	public boolean checkIfisIgnorableParentByClassAttName(String parentAttName, Connection conn) {
		for (ParentRefInfo  parent : this.getParentRefInfo(conn)) {
			if (parent.getReferenceColumnAsClassAttName().equals(parentAttName)) {
				return parent.isIgnorable();
			}
		}
		
		throw new ForbiddenOperationException("The att '" + parentAttName + "' doesn't represent any defined parent att");
	}
	
	
	public void logInfo(String msg) {
		utilities.logInfo(msg, logger);
	}
	
	public void logError(String msg) {
		utilities.logErr(msg, logger);
	}
	
	public void logDebug(String msg) {
		utilities.logDebug(msg, logger);
	}

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
