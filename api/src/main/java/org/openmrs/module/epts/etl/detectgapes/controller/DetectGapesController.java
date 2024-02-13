
package org.openmrs.module.epts.etl.detectgapes.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.detectgapes.engine.DetectGapesEngine;
import org.openmrs.module.epts.etl.detectgapes.model.DetectGapesSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the detect gapes process.
 * 
 * @author jpboane
 * @see DetectGapesEngine
 */
public class DetectGapesController extends OperationController {
	
	public DetectGapesController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
		
		tryToCreateTableGape();
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DetectGapesEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return getExtremeRecord(tableInfo, "min", conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return getExtremeRecord(tableInfo, "max", conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private long getExtremeRecord(SyncTableConfiguration tableInfo, String function, Connection conn) throws DBException {
		DetectGapesSearchParams searchParams = new DetectGapesSearchParams(tableInfo, null, this);
		searchParams.setSyncStartDate(getConfiguration().getStartDate());
		
		SearchClauses<DatabaseObject> searchClauses = searchParams.generateSearchClauses(conn);
		
		int bkpQtyRecsPerSelect = searchClauses.getSearchParameters().getQtdRecordPerSelected();
		
		searchClauses.setColumnsToSelect(function + "(" + tableInfo.getPrimaryKey() + ") as value");
		
		String sql = searchClauses.generateSQL(conn);
		
		SimpleValue simpleValue = BaseDAO.find(SimpleValue.class, sql, searchClauses.getParameters(), conn);
		
		searchClauses.getSearchParameters().setQtdRecordPerSelected(bkpQtyRecsPerSelect);
		
		if (simpleValue != null && CommonUtilities.getInstance().stringHasValue(simpleValue.getValue())) {
			return simpleValue.intValue();
		}
		
		return 0;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	private void tryToCreateTableGape() {
		
		OpenConnection conn = openConnection();
		
		try {
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
