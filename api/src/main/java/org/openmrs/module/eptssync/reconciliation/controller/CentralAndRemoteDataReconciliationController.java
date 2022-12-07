package org.openmrs.module.eptssync.reconciliation.controller;

import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.reconciliation.engine.CentralAndRemoteDataReconciliationEngine;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 *
 */
public class CentralAndRemoteDataReconciliationController extends OperationController {
		
	public CentralAndRemoteDataReconciliationController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public void onStart() {
			
		if (!existDataReconciliationInfoTable()) {
			generateDataReconciliationInfoTable();
		}
		
		super.onStart();
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
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
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		if (tableInfo.getTableName().equalsIgnoreCase("users")) return 0;
		
		OpenConnection conn = openConnection();
		
		int id = 0;
			
		try {
			if (isMissingRecordsDetector()) {
				SyncImportInfoVO record = SyncImportInfoDAO.getFirstMissingRecordInDestination(tableInfo, conn);
				
				id = record != null ? record.getId() : 0;
			}
			else
			if (isOutdateRecordsDetector()) {
				id = DatabaseObjectDAO.getFirstRecord(tableInfo, conn);
			}
			else
			if (isPhantomRecordsDetector()){
				DatabaseObject record = DatabaseObjectDAO.getFirstPhantomRecordInDestination(tableInfo, conn);
				
				id = record != null ? record.getObjectId() : 0;
			}
		
			return id;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		if (tableInfo.getTableName().equalsIgnoreCase("users")) return 0;
		
		OpenConnection conn = openConnection();
		
		int id = 0;
		
		try {
			if (isMissingRecordsDetector()) {
				SyncImportInfoVO record = SyncImportInfoDAO.getLastMissingRecordInDestination(tableInfo, conn);
				
				id = record != null ? record.getId() : 0;
			}
			else
			if (isOutdateRecordsDetector()) {
				id = DatabaseObjectDAO.getLastRecord(tableInfo, conn);
			}
			else
			if (isPhantomRecordsDetector()){
				DatabaseObject record = DatabaseObjectDAO.getLastPhantomRecordInDestination(tableInfo, conn);
				
				id = record != null ? record.getObjectId() : 0;
			}
		
			return id;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}

	@Override
	public OpenConnection openConnection() {
		OpenConnection conn = super.openConnection();
	
		if (getOperationConfig().isDoIntegrityCheckInTheEnd()) {
			try {
				DBUtilities.disableForegnKeyChecks(conn);
			} catch (DBException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return conn;
	}
	
	
	public boolean existDataReconciliationInfoTable() {
		OpenConnection conn = openConnection();
		
		String schema = getConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "data_conciliation_info";

		try {
			return DBUtilities.isResourceExist(schema, resourceType, tabName, conn);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminected();
			conn.finalizeConnection();
		}
	}
	
	private void generateDataReconciliationInfoTable() {
		OpenConnection conn = openConnection();
		
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
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();

			st.close();
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} 
		finally {
			conn.markAsSuccessifullyTerminected();
			conn.finalizeConnection();
		}	
	}
	
}
