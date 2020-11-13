package org.openmrs.module.eptssync.databasepreparation.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.databasepreparation.controller.DatabasePreparationController;
import org.openmrs.module.eptssync.databasepreparation.model.DatabasePreparationRecord;
import org.openmrs.module.eptssync.databasepreparation.model.DatabasePreparationSearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;

/**
 * The engine responsible for transport synchronization files from origin to
 * destination site
 * <p>
 * This is temporariy transportation method which suppose that the origin and
 * destination are in the same matchine, so the transport process consist on
 * moving files from export directory to import directory
 * <p>
 * In the future a propery transportation method should be implemented.
 * 
 * @author jpboane
 */
public class DatabasePreparationEngine extends Engine {
	
	private boolean updateDone;
	
	public DatabasePreparationEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	public boolean isUpdateDone() {
		return updateDone;
	}
	
	@Override
	protected void restart() {
	}
	
	private String getTableName() {
		return getSyncTableConfiguration().getTableName();
	}
	
	@Override
	public void performeSync(List<SyncRecord> migrationRecords, Connection conn) throws DBException {
		try {
			updateTableInfo(conn);
			
			this.updateDone = true;
		} catch (SQLException e) {
			e.printStackTrace();
		
			throw new DBException(e);
		}
	}
	
	private void updateTableInfo(Connection conn) throws SQLException {
		logInfo("UPGRATING TABLE INFO [" + this.getTableName() + "]");
		
		String newColumnDefinition = "";
		
		if (!getSyncTableConfiguration().existRelatedExportStageTable(conn)) {
			logInfo("GENERATING RELATED STAGE TABLE FOR [" + this.getTableName() + "]");
			
			createRelatedExportStageTable(conn);
			
			logInfo("RELATED STAGE TABLE FOR [" + this.getTableName() + "] GENERATED");
		}
		
		if (!isConsistentColumnExistOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateConsistentColumnGeneration());
		}

		if (!isLastSyncDateColumnExistOnTable(conn)) {
			newColumnDefinition += utilities.stringHasValue(newColumnDefinition) ? "," : "";
			newColumnDefinition = utilities.concatStrings(newColumnDefinition, generateLastSyncDateColumnCreation());
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
			logInfo("CONF COLUMNS FOR TABLE [" + this.getTableName() + "] CREATED");
			BaseDAO.executeBatch(conn, batch);
			logInfo("CREATING CONF COLUMNS FOR TABLE [" + this.getTableName() + "]");
		}
		
		if (!isExistRelatedTriggers(conn)) {
			logInfo("CREATING RELATED TRIGGERS FOR [" + this.getTableName() + "]");
			
			createLastUpdateDateMonitorTrigger(conn);
		
			logInfo("RELATED TRIGGERS FOR [" + this.getTableName() + "] CREATED");
		}
		
		logInfo("THE PREPARATION OF TABLE '" + getTableName() + "' IS FINISHED!");
	}
	
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
		sql += "CREATE TRIGGER " + triggerName + " BEFORE " + triggerEvent + " ON " + this.getTableName() + "\n";
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
		return this.getTableName() + "_date_changed_insert_monitor";
	}
	
	private String generateLastUpdateDateUpdateTriggerMonitor() {
		return this.getTableName() + "_date_changed_update_monitor";
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
		return "consistent int(1) DEFAULT -1";
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

	private boolean isUniqueOriginConstraintsExists(Connection conn) throws SQLException {
		String unqKey = generateUniqueOriginConstraintsName(); 
	
		return DBUtilities.isIndexExistsOnTable(conn.getCatalog(), getTableName(), unqKey, conn);
	}

	private String generateUniqueOriginConstraintsName() {
		return this.getTableName() + "origin_unq";
	}

	private boolean isOriginAppLocationCodeColumnExistsOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getSyncTableConfiguration().getTableName(), "origin_app_location_code", conn);
	}
	
	private String generateOriginAppLocationCodeColumnGeneration() {
		return "origin_app_location_code VARCHAR(100) NULL";
	}

	@Override
	protected List<SyncRecord> searchNextRecords(Connection conn) {
		if (updateDone) return null;
		
		List<SyncRecord> records = new ArrayList<SyncRecord>();
		
		records.add(new DatabasePreparationRecord(getSyncTableConfiguration()));
		
		return records;
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		return new DatabasePreparationSearchParams(this, limits, conn);
	}

	private void createRelatedExportStageTable(Connection conn) {
		String sql = "";

		sql += "CREATE TABLE " + getSyncTableConfiguration().getSyncStageSchema() + "." + getSyncTableConfiguration().generateRelatedStageTableName() + "(\n";
		sql += "	id int(11) NOT NULL AUTO_INCREMENT,\n";
		sql += "	record_id int(11) NOT NULL,\n";
		sql += "	creation_date DATETIME DEFAULT CURRENT_TIMESTAMP,\n";
		sql += "	json VARCHAR(7500) NOT NULL,\n";
		sql += "	origin_app_location_code VARCHAR(100) NOT NULL,\n";
		sql += "	last_migration_try_date DATETIME DEFAULT NULL,\n";
		sql += "	last_migration_try_err varchar(250) DEFAULT NULL,\n";
		sql += "	migration_status int(1) DEFAULT 1,\n";
		sql += "	CONSTRAINT CHK_" + getSyncTableConfiguration().generateRelatedStageTableName() + "_MIG_STATUS CHECK (migration_status = -1 OR migration_status = 0 OR migration_status = 1),";
		sql += "	UNIQUE KEY " + getSyncTableConfiguration().generateRelatedStageTableName() + "UNQ_RECORD(record_id, origin_app_location_code),\n";
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
	
	@Override
	public DatabasePreparationController getRelatedOperationController() {
		return (DatabasePreparationController) super.getRelatedOperationController();
	}

	@Override
	public void requestStop() {
	}
}
