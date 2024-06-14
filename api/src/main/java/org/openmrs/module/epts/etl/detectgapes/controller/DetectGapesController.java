
package org.openmrs.module.epts.etl.detectgapes.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.detectgapes.engine.DetectGapesEngine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the detect gapes process.
 * 
 * @author jpboane
 * @see DetectGapesEngine
 */
public class DetectGapesController extends EtlController {
	
	public DetectGapesController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
		
		tryToCreateTableGape();
	}
	
	@Override
	public TaskProcessor initRelatedEngine(Engine monitor, ThreadRecordIntervalsManager limits) {
		return new DetectGapesEngine(monitor, limits);
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	private void tryToCreateTableGape() {
		
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			String syncStageSchema = operationConfig.getRelatedSyncConfig().getSyncStageSchema();
			
			if (!DBUtilities.isTableExists(syncStageSchema, "sync_table_gape", conn)) {
				createGapesTable(conn);
			}
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
		
	}
	
	private void createGapesTable(Connection conn) throws DBException {
		String tableName = operationConfig.getRelatedSyncConfig().getSyncStageSchema() + ".sync_table_gape";
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		sql += "CREATE TABLE " + tableName + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("record_id", notNullConstraint, conn) + endLineMarker;
		
		sql += DBUtilities.generateTableUniqueKeyDefinition("gape_unq_record", "table_name,record_id ", conn)
		        + endLineMarker;
		
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", "gape_pk", conn);
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
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
