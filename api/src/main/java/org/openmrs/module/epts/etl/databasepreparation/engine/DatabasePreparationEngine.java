package org.openmrs.module.epts.etl.databasepreparation.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.openmrs.module.epts.etl.databasepreparation.model.DatabasePreparationRecord;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class DatabasePreparationEngine extends TaskProcessor<DatabasePreparationRecord> {
	
	private boolean updateDone;
	
	public DatabasePreparationEngine(Engine<DatabasePreparationRecord> monitor, IntervalExtremeRecord limits) {
		super(monitor, limits);
	}
	
	public boolean isUpdateDone() {
		return updateDone;
	}
	
	private String getTableName() {
		return getEtlConfiguration().getSrcConf().getTableName();
	}
	
	@Override
	public EtlOperationResultHeader<DatabasePreparationRecord> performeSync(List<DatabasePreparationRecord> records,
	        Connection srcConn, Connection dstConn) throws DBException {
		try {
			updateTableInfo(srcConn);
			
			this.updateDone = true;
			
			return new EtlOperationResultHeader<>(getLimits());
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
	}
	
	private void updateTableInfo(Connection conn) throws SQLException {
		logDebug("UPGRATING TABLE INFO [" + this.getTableName() + "]");
		
		if (!getEtlConfiguration().getSrcConf().existRelatedExportStageTable(conn)) {
			logDebug("GENERATING RELATED STAGE TABLE FOR [" + this.getTableName() + "]");
			
			createRelatedSyncStageAreaTable(conn);
			
			logDebug("RELATED STAGE TABLE FOR [" + this.getTableName() + "] GENERATED");
		}
		
		if (!getEtlConfiguration().getSrcConf().existRelatedExportStageUniqueKeysTable(conn)) {
			logDebug("GENERATING RELATED STAGE UNIQUE KEYS TABLE FOR [" + this.getTableName() + "]");
			
			createRelatedSyncStageAreaUniqueKeysTable(conn);
			
			logDebug("RELATED STAGE UNIQUE KEYS TABLE FOR [" + this.getTableName() + "] GENERATED");
		}
		
		logDebug("THE PREPARATION OF TABLE '" + getTableName() + "' IS FINISHED!");
	}
	
	@SuppressWarnings("unused")
	private String generateDateChangedColumnGeneration() throws SQLException {
		return "date_changed datetime NULL";
	}
	
	@SuppressWarnings("unused")
	private boolean isDateChangedColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "date_changed", conn);
	}
	
	protected void createLastUpdateDateMonitorTrigger(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		
		st.addBatch(generateTriggerCode(this.generateLastUpdateDateInsertTriggerMonitor(), "INSERT"));
		st.addBatch(generateTriggerCode(this.generateLastUpdateDateUpdateTriggerMonitor(), "UPDATE"));
		
		st.executeBatch();
		
		st.close();
	}
	
	private String generateTriggerCode(String triggerName, String triggerEvent) {
		String sql = "";
		
		sql += "CREATE TRIGGER " + triggerName + " BEFORE " + triggerEvent + " ON " + this.getTableName() + "\n";
		sql += "FOR EACH ROW\n";
		sql += "	BEGIN\n";
		sql += "	UPDATE " + getEtlConfiguration().getSrcConf().generateFullStageTableName()
		        + " SET last_update_date = CURRENT_TIMESTAMP();\n";
		sql += "	END;\n";
		
		return sql;
	}
	
	protected boolean isExistRelatedTriggers(Connection conn) throws SQLException {
		return DBUtilities.isResourceExist(conn.getCatalog(), getTableName(), DBUtilities.RESOURCE_TYPE_TRIGGER,
		    generateLastUpdateDateInsertTriggerMonitor(), conn);
	}
	
	private String generateLastUpdateDateInsertTriggerMonitor() {
		return this.getTableName() + "_date_changed_insert_monitor";
	}
	
	private String generateLastUpdateDateUpdateTriggerMonitor() {
		return this.getTableName() + "_date_changed_update_monitor";
	}
	
	@SuppressWarnings("unused")
	private String generateLastSyncDateColumnCreation() {
		return "last_sync_date datetime NULL";
	}
	
	@SuppressWarnings("unused")
	private boolean isLastSyncDateColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "last_sync_date", conn);
	}
	
	@SuppressWarnings("unused")
	private String generateOriginRecordIdColumnGeneration() {
		return "record_origin_id int(11) NULL";
	}
	
	@SuppressWarnings("unused")
	private boolean isConsistentColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "consistent", conn);
	}
	
	@SuppressWarnings("unused")
	private String generateConsistentColumnGeneration() {
		return "consistent int(1) DEFAULT -1";
	}
	
	@SuppressWarnings("unused")
	private boolean isUuidColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "uuid", conn);
	}
	
	@SuppressWarnings("unused")
	private String generateUuidColumnCreation() {
		return "uuid char(38) NULL";
	}
	
	@SuppressWarnings("unused")
	private boolean isOriginRecordIdColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "record_origin_id", conn);
	}
	
	@SuppressWarnings("unused")
	private boolean isUniqueOriginConstraintsExists(Connection conn) throws SQLException {
		String unqKey = generateUniqueOriginConstraintsName();
		
		return DBUtilities.isIndexExistsOnTable(conn.getCatalog(), getTableName(), unqKey, conn);
	}
	
	private String generateUniqueOriginConstraintsName() {
		return this.getTableName() + "origin_unq";
	}
	
	@SuppressWarnings("unused")
	private boolean isOriginAppLocationCodeColumnExistsOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "origin_app_location_code", conn);
	}
	
	@SuppressWarnings("unused")
	private String generateOriginAppLocationCodeColumnGeneration() {
		return "origin_app_location_code VARCHAR(100) NULL";
	}
	
	private void createRelatedSyncStageAreaUniqueKeysTable(Connection conn) throws DBException {
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String parentTableName = getEtlConfiguration().getSrcConf().generateFullStageTableName();
		String tableName = getEtlConfiguration().getSrcConf().generateRelatedStageUniqueKeysTableName();
		
		sql += "CREATE TABLE " + getEtlConfiguration().getSrcConf().generateFullStageUniqueKeysTableName() + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("record_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("key_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("column_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("key_value", 100, "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_record_key".toLowerCase(),
		    "record_id, key_name, column_name", conn) + endLineMarker;
		sql += DBUtilities.generateTableForeignKeyDefinition(tableName + "_parent_record", "record_id", parentTableName,
		    "id", conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();
			
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private void createRelatedSyncStageAreaTable(Connection conn) throws DBException {
		String tableName = getEtlConfiguration().getSrcConf().generateRelatedStageTableName();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		sql += "CREATE TABLE " + getEtlConfiguration().getSrcConf().generateFullStageTableName() + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("record_origin_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, notNullConstraint, conn)
		        + endLineMarker;
		sql += DBUtilities.generateTableTextField("json", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("last_sync_date", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("last_sync_try_err", 250, "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("last_update_date", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableNumericField("consistent", 1, "NULL", -1, conn) + endLineMarker;
		sql += DBUtilities.generateTableNumericField("migration_status", 1, "NULL", 1, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		
		sql += DBUtilities.generateTableDateTimeField("record_date_created", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("record_date_changed", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("record_date_voided", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("destination_id", "NULL", conn) + endLineMarker;
		
		String checkCondition = "migration_status = -1 OR migration_status = 0 OR migration_status = 1";
		String keyName = "CHK_" + getEtlConfiguration().getSrcConf().generateRelatedStageTableName() + "_MIG_STATUS";
		
		sql += DBUtilities.generateTableCheckConstraintDefinition(keyName, checkCondition, conn) + endLineMarker;
		
		String uniqueKeyName = tableName + "_UNQ_RECORD_ID".toLowerCase();
		
		if (getEtlConfiguration().getSrcConf().isDestinationInstallationType()
		        || getEtlConfiguration().getSrcConf().isDBQuickLoad()) {
			
			sql += DBUtilities.generateTableUniqueKeyDefinition(uniqueKeyName,
			    "record_origin_id, record_origin_location_code", conn) + endLineMarker;
			
		} else {
			sql += DBUtilities.generateTableUniqueKeyDefinition(uniqueKeyName, "record_origin_id", conn) + endLineMarker;
		}
		
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn);
		sql += ")";
		
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();
			
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
}
