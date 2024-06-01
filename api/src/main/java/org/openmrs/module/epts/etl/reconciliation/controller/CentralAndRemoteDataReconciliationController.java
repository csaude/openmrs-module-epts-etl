package org.openmrs.module.epts.etl.reconciliation.controller;

import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.reconciliation.engine.CentralAndRemoteDataReconciliationEngine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class CentralAndRemoteDataReconciliationController extends OperationController {
	
	public CentralAndRemoteDataReconciliationController(ProcessController processController,
	    EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public void onStart() {
		
		try {
			if (!existDataReconciliationInfoTable()) {
				generateDataReconciliationInfoTable();
			}
		}
		catch (DBException e) {
			throw e.parseToRuntimeException();
		}
		
		super.onStart();
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		return new CentralAndRemoteDataReconciliationEngine(monitor, limits);
	}
	
	public boolean isMissingRecordsDetector() {
		return this.getOperationType().isMissingRecordsDetector();
	}
	
	public boolean isOutdateRecordsDetector() {
		return this.getOperationType().isOutdatedRecordsDetector();
	}
	
	public boolean isPhantomRecordsDetector() {
		return this.getOperationType().isPhantomRecordsDetector();
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		if (config.getSrcConf().getTableName().equalsIgnoreCase("users"))
			return 0;
		
		OpenConnection conn = null;
		
		int id = 0;
		
		try {
			conn = openConnection();
			
			if (isMissingRecordsDetector()) {
				SyncImportInfoVO record = SyncImportInfoDAO.getFirstMissingRecordInDestination(config.getSrcConf(), conn);
				
				id = record != null ? record.getId() : 0;
			} else if (isOutdateRecordsDetector()) {
				id = DatabaseObjectDAO.getFirstRecord(config.getSrcConf(), conn);
			} else if (isPhantomRecordsDetector()) {
				EtlDatabaseObject record = DatabaseObjectDAO.getFirstPhantomRecordInDestination(config.getSrcConf(), conn);
				
				id = record != null ? record.getObjectId().getSimpleValueAsInt() : 0;
			}
			
			return id;
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		if (config.getSrcConf().getTableName().equalsIgnoreCase("users"))
			return 0;
		
		OpenConnection conn = null;
		
		int id = 0;
		
		try {
			conn = openConnection();
			
			if (isMissingRecordsDetector()) {
				SyncImportInfoVO record = SyncImportInfoDAO.getLastMissingRecordInDestination(config.getSrcConf(), conn);
				
				id = record != null ? record.getId() : 0;
			} else if (isOutdateRecordsDetector()) {
				id = DatabaseObjectDAO.getLastRecord(config.getSrcConf(), conn);
			} else if (isPhantomRecordsDetector()) {
				EtlDatabaseObject record = DatabaseObjectDAO.getLastPhantomRecordInDestination(config.getSrcConf(), conn);
				
				id = record != null ? record.getObjectId().getSimpleValueAsInt() : 0;
			}
			
			return id;
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	@Override
	public OpenConnection openConnection() throws DBException {
		OpenConnection conn = super.openConnection();
		
		if (getOperationConfig().isDoIntegrityCheckInTheEnd()) {
			try {
				DBUtilities.disableForegnKeyChecks(conn);
			}
			catch (DBException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return conn;
	}
	
	public boolean existDataReconciliationInfoTable() throws DBException {
		
		String schema = getConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "data_conciliation_info";
		
		OpenConnection conn = openConnection();
		
		try {
			
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	private void generateDataReconciliationInfoTable() {
		OpenConnection conn = null;
		
		String sql = "";
		
		sql += "CREATE TABLE " + getConfiguration().getSyncStageSchema() + ".data_conciliation_info (\n";
		sql += "id int(11) NOT NULL AUTO_INCREMENT,\n";
		sql += "record_uuid varchar(100) NOT NULL,\n";
		sql += "record_origin_location_code varchar(100) NOT NULL,\n";
		sql += "reason_type varchar(100) NOT NULL,\n";
		sql += "table_name VARCHAR(100) NOT NULL,\n";
		sql += "creation_date datetime DEFAULT CURRENT_TIMESTAMP,\n";
		sql += "PRIMARY KEY (id)\n";
		sql += ") ENGINE=InnoDB;\n";
		
		try {
			conn = openConnection();
			
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();
			
			st.close();
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
}
