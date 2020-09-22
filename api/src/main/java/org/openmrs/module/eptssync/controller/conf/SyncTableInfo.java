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
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.OpenMRSClassGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableInfo {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	private String tableName;
	
	private List<String> parents;
	
	private List<ParentRefInfo> parentRefInfo;
	private List<ParentRefInfo> childRefInfo;
	

	private boolean mustRecompileTable;

	private Class<OpenMRSObject> syncRecordClass;

	private SyncTableInfoSource relatedSyncTableInfoSource;

	private String primaryKey;
	private String sharePkWith;
	
	private String extraConditionForExport;
	
	private boolean metadata;
	private int qtyProcessingEngine;
	private int qtyRecordsPerSelect;
	
	private static Logger logger = Logger.getLogger(SyncTableInfo.class);
	
	public SyncTableInfo() {
	}

	public boolean isDoIntegrityCheckInTheEnd() {
		return getRelatedSyncTableInfoSource().isDoIntegrityCheckInTheEnd();
	}
	
	public int getQtyRecordsPerSelect() {
		return qtyRecordsPerSelect != 0 ? qtyRecordsPerSelect : getRelatedSyncTableInfoSource().getDefaultQtyRecordsPerSelect();
	}

	public void setQtyRecordsPerSelect(int qtyRecordsPerSelect) {
		this.qtyRecordsPerSelect = qtyRecordsPerSelect;
	}

	public void setQtyProcessingEngine(int qtyProcessingEngine) {
		this.qtyProcessingEngine = qtyProcessingEngine;
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
	public Class<OpenMRSObject> getSyncRecordClass() {
		return syncRecordClass;
	}

	public void setSyncRecordClass(Class<OpenMRSObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}

	public SyncTableInfoSource getRelatedSyncTableInfoSource() {
		return relatedSyncTableInfoSource;
	}

	public void setRelatedSyncTableInfoSource(SyncTableInfoSource relatedSyncTableInfoSource) {
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
		return relatedSyncTableInfoSource.openConnection();
	}
	
	public String getPrimaryKey() {
		if (primaryKey == null) {
			OpenConnection conn =  openConnection();
			
			try {
				ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, tableName);
	
				rs.next();
				
				this.primaryKey = rs.getString("COLUMN_NAME");
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
	
	public void setChildRefInfo(List<ParentRefInfo> childRefInfo) {
		this.parentRefInfo = childRefInfo;
	}
	
	public synchronized List<ParentRefInfo> getChildRefInfo() {
		if (this.childRefInfo == null) {
			
			logInfo("DISCOVERING CHILDREN FOR '" + this.tableName + "'");
			
			OpenConnection conn =  openConnection();
					/*PKTABLE_CAT String => primary key table catalog (may be null)
					PKTABLE_SCHEM String => primary key table schema (may be null)
					PKTABLE_NAME String => primary key table name
					PKCOLUMN_NAME String => primary key column name
					FKTABLE_CAT String => foreign key table catalog (may be null) being exported (may be null)
					FKTABLE_SCHEM String => foreign key table schema (may be null) being exported (may be null)
					FKTABLE_NAME String => foreign key table name being exported
					FKCOLUMN_NAME String => foreign key column name being exported*/
			try {
				this.childRefInfo = new ArrayList<ParentRefInfo>();  
				
				ResultSet foreignKeyRS = conn.getMetaData().getExportedKeys(null, null, tableName);
				
				//System.out.println("-----------------------------------------------------");
				
				while(foreignKeyRS.next()) {
					ParentRefInfo ref = new ParentRefInfo();
					
					ref.setReferenceColumnName(foreignKeyRS.getString("FKCOLUMN_NAME"));
					ref.setReferencedColumnName(foreignKeyRS.getString("PKCOLUMN_NAME"));
					ref.setTableName(foreignKeyRS.getString("FKTABLE_NAME"));
					ref.setTableInfo(this);
					ref.setIgnorable(DBUtilities.isTableColumnAllowNull(ref.getTableName(), ref.getReferenceColumnName(), conn));
					
					/*System.out.print("PKTABLE_NAME\t");
					System.out.print("PKCOLUMN_NAME\t");
					System.out.print("FKTABLE_NAME\t");
					System.out.print("FKCOLUMN_NAME\t");
					System.out.println("NULLABLE\t");
					
					System.out.print(foreignKeyRS.getString("PKTABLE_NAME")+"\t\t");
					System.out.print(foreignKeyRS.getString("PKCOLUMN_NAME")+"\t\t");
					System.out.print(foreignKeyRS.getString("FKTABLE_NAME")+"\t\t");
					System.out.print(foreignKeyRS.getString("FKCOLUMN_NAME")+ "\t\t");
					System.out.println(ref.isIgnorable());*/
					
					this.childRefInfo.add(ref);
				}
			
				logInfo(this.childRefInfo.size() + " CHILDREN FOR '" + this.tableName + "' DISCOVERED");
				
			} catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		return parentRefInfo;
	}

	public void setParentRefInfo(List<ParentRefInfo> parentRefInfo) {
		this.parentRefInfo = parentRefInfo;
	}
	
	public synchronized List<ParentRefInfo> getParentRefInfo() {
		if (this.parentRefInfo == null) {
			
			logInfo("DISCOVERING PARENTS FOR '" + this.tableName + "'");
			
			OpenConnection conn =  openConnection();
			
			try {
				this.parentRefInfo = new ArrayList<ParentRefInfo>();  
				
				ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(null, null, tableName);
				
				while(foreignKeyRS.next()) {
					ParentRefInfo ref = new ParentRefInfo();
					
					ref.setReferenceColumnName(foreignKeyRS.getString("FKCOLUMN_NAME"));
					ref.setReferencedColumnName(foreignKeyRS.getString("PKCOLUMN_NAME"));
					ref.setTableName(foreignKeyRS.getString("PKTABLE_NAME"));
					ref.setTableInfo(this);
					ref.setIgnorable(DBUtilities.isTableColumnAllowNull(this.tableName, ref.getReferenceColumnName(), conn));
					
					//Mark as metadata if is not specificaly mapped as parent in conf file
					if (!this.parents.contains(foreignKeyRS.getString("PKTABLE_NAME"))) {
						ref.setMetadata(true);
					}
					
					if (this.sharePkWith != null && this.sharePkWith.equalsIgnoreCase(ref.getTableName())) {
						ref.setSharedPk(true);
					}
					
					this.parentRefInfo.add(ref);
				}
			
				logInfo(this.parentRefInfo.size() + " PARENTS FOR '" + this.tableName + "' DISCOVERED");
			} catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		return parentRefInfo;
	}

	public Class<OpenMRSObject> getRecordClass() {
		return this.syncRecordClass;
	}

	public String getOriginAppLocationCode() {
		return getRelatedSyncTableInfoSource().getOriginAppLocationCode();
	}

	public void generateRecordClass() {
		OpenConnection conn = openConnection();
		
		try {
			
			this.syncRecordClass = OpenMRSClassGenerator.generate(this, conn);
			conn.markAsSuccessifullyTerminected();
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
		finally {
			conn.finalizeConnection();
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
	public void tryToUpgradeDataBaseInfo() throws SQLException {
		logInfo("UPGRATING TABLE INFO [" + this.tableName + "]");
		
		OpenConnection conn = openConnection();

		try {
			String newColumnDefinition = "";
			
			if (!existRelatedExportStageTable()) {
				logInfo("GENERATING RELATED STAGE TABLE FOR [" + this.tableName + "]");
				
				createRelatedExportStageTable();
				
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

			if (!isUuidColumnExistOnTable(conn)) {
				newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
				newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateUuidColumnCreation());
			}

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
			
			if (utilities.stringHasValue(newColumnDefinition)) {
				logInfo("CREATING CONF COLUMNS FOR TABLE [" + this.tableName + "]");
				
				newColumnDefinition = "ALTER TABLE " + this.getTableName() + " ADD (" + newColumnDefinition + ")"; 
				
				Statement st = conn.createStatement();
				st.addBatch(newColumnDefinition);
				st.executeBatch();
						
				st.close();
				
				logInfo("CONF COLUMNS FOR TABLE [" + this.tableName + "] CREATED");
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
			
			conn.markAsSuccessifullyTerminected();
		} 
		finally {
			conn.finalizeConnection();
		}
	}

	/*
	private boolean isIndexOnFirtExportDoneColumn(Connection conn) throws SQLException {
		return DBUtilities.isResourceExist(conn.getCatalog(), DBUtilities.RESOURCE_TYPE_INDEX,  generateNameOfIndexOnExportDoneColumn(), conn);
	}
	
	private String generateNameOfIndexOnExportDoneColumn() {
		return this.tableName + "_first_export_done_index";
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

	private void createRelatedExportStageTable() {
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
		sql += "	PRIMARY KEY (id)\n";
		sql += ")\n";
		sql += " ENGINE=InnoDB DEFAULT CHARSET=utf8";
		
		OpenConnection conn = openConnection();

		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();

			st.close();

			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} finally {
			conn.finalizeConnection();
		}
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
	
	private boolean existRelatedExportStageTable() {
		OpenConnection conn = openConnection();

		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = generateRelatedStageTableName();

		try {
			return DBUtilities.isResourceExist(schema, resourceType, tabName, conn);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
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

	private void createLastUpdateDateMonitorTrigger(OpenConnection conn) throws SQLException {
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

	private boolean isExistRelatedTriggers(OpenConnection conn) throws SQLException {
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

	private boolean isUuidColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "uuid", conn);
	}

	private String generateUuidColumnCreation() {
		return "uuid char(38) NULL";
	}
	
	private boolean isOriginRecordIdColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "origin_record_id", conn);
	}
	
	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}
	
	public boolean checkIfisIgnorableParentByClassAttName(String parentAttName) {
		for (ParentRefInfo  parent : this.getParentRefInfo()) {
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

	public int getQtyProcessingEngine() {
		return this.qtyProcessingEngine != 0 ? this.qtyProcessingEngine : getRelatedSyncTableInfoSource().getDefaultQtyProcessingEngine();
	}

	public void fullLoad() {
		getParentRefInfo();
		getChildRefInfo();
	}

	public ParentRefInfo getSharedKeyRefInfo() {
		for (ParentRefInfo refInfo : getParentRefInfo()) {
			if (refInfo.isSharedPk()) return refInfo;
		}
			
		return null;
	}
}
