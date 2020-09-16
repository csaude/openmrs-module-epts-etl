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
import org.openmrs.module.eptssync.model.openmrs.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.OpenMRSClassGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableInfo {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	private String tableName;
	
	private List<String> parents;
	
	private List<ParentRefInfo> parentRefInfo;

	private boolean mustRecompileTable;

	private Class<OpenMRSObject> syncRecordClass;

	private SyncTableInfoSource relatedSyncTableInfoSource;

	private String primaryKey;
	private String sharePkWith;
	
	private String extraConditionForExport;
	
	private boolean metadata;
	
	private static Logger logger = Logger.getLogger(SyncTableInfo.class);
	
	public SyncTableInfo() {
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
	
	public String getPrimaryKey() {
		if (primaryKey == null) {
			OpenConnection conn = DBConnectionService.getInstance().openConnection();
			
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
	
	public void setParentRefInfo(List<ParentRefInfo> parentRefInfo) {
		this.parentRefInfo = parentRefInfo;
	}
	
	public synchronized List<ParentRefInfo> getParentRefInfo() {
		if (this.parentRefInfo == null) {
			
			logInfo("DISCOVERING PARENTS FOR '" + this.tableName + "'");
			
			OpenConnection conn = DBConnectionService.getInstance().openConnection();
			
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
			
				logInfo("PARENTS FOR '" + this.tableName + "' DISCOVERED");
				
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
	
	private OpenConnection openConnection() {
		return DBConnectionService.getInstance().openConnection();
	}

	public void generateRecordClass() {
		try {
			this.syncRecordClass = OpenMRSClassGenerator.generate(this);
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
	public void tryToUpgradeDataBaseInfo() throws SQLException {
		logInfo("UPGRATING TABLE INFO [" + this.tableName + "]");
		
		OpenConnection conn = DBConnectionService.getInstance().openConnection();

		if (!existRelatedExportStageTable()) {
			logInfo("GENERATING RELATED STAGE TABLE FOR [" + this.tableName + "]");
			
			createRelatedExportStageTable();
			
			logInfo("RELATED STAGE TABLE FOR [" + this.tableName + "] GENERATED");
			
		}

		if (!isLastSyncDateColumnExistOnTable(conn)) {
			logInfo("CREATING 'LAST_SYNC_DATE' COLUMN FOR [" + this.tableName + "]");
			
			createLastSyncDateOnTable(conn);
			
			logInfo("'LAST_SYNC_DATE' COLUMN FOR [" + this.tableName + "] CREATED");
		}

		if (!isUuidColumnExistOnTable(conn)) {
			logInfo("CREATING 'UUID' COLUMN FOR [" + this.tableName + "]");
			
			createUuidColumnOnTable(conn);
			
			logInfo("'UUID' COLUMN FOR [" + this.tableName + "] CREATED");
			
		}

		if (!isOriginRecordIdColumnExistOnTable(conn)) {
			logInfo("CREATING 'ORIGIN_RECORD_ID' COLUMN FOR [" + this.tableName + "]");
			
			createOriginRecordIdColumnOnTable(conn);
			
			logInfo("'ORIGIN_RECORD_ID' COLUMN FOR [" + this.tableName + "] CREATED");
		}

		if (!isDateChangedColumnExistOnTable(conn)) {
			logInfo("CREATING 'DATE_CHANGED' COLUMN FOR [" + this.tableName + "]");
			
			createDateChangedColumnOnTable(conn);
			
			logInfo("'DATE_CHANGED' COLUMN FOR [" + this.tableName + "] CREATED");
			
		}
	
		if (!isOriginAppLocationCodeColumnExistsOnTable(conn)) {
			logInfo("CREATING 'ORIGIN_APP_LOCATION' COLUMN FOR [" + this.tableName + "]");
			
			createOriginAppLocationCodeColumnOnTable(conn);
			
			logInfo("'ORIGIN_APP_LOCATION' COLUMN FOR [" + this.tableName + "] CREATED");
		}
		
		if (!isExistRelatedTriggers(conn)) {
			logInfo("CREATING RELATED TRIGGERS FOR [" + this.tableName + "]");
			
			createLastUpdateDateMonitorTrigger(conn);
		
			logInfo("RELATED TRIGGERS FOR [" + this.tableName + "] CREATED");
		}
	}

	
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
	}

	private boolean isOriginAppLocationCodeColumnExistsOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "origin_app_location_code", conn);
	}

	private void createOriginAppLocationCodeColumnOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD origin_app_location_code VARCHAR(100) NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
	}

	
	private void createDateChangedColumnOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD date_changed datetime NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
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

	private void createLastSyncDateOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD last_sync_date datetime NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
	}

	private boolean isLastSyncDateColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "last_sync_date", conn);
	}

	private void createOriginRecordIdColumnOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD origin_record_id int(11) NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
	}
	
	private boolean isUuidColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "uuid", conn);
	}

	private void createUuidColumnOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD uuid char(38) NULL";
		
		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
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
}
