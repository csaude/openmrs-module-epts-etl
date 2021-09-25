package org.openmrs.module.eptssync.changedrecordsdetector.controller;

import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.eptssync.changedrecordsdetector.engine.ChangedRecordsDetectorEngine;
import org.openmrs.module.eptssync.changedrecordsdetector.model.DetectedRecordInfoDAO;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 *
 */
public class ChangedRecordsDetectorController extends OperationController {
	public ChangedRecordsDetectorController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
		
		this.controllerId = processController.getControllerId() + "_" + getOperationType();	
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (!existDetectedRecordInfoTable()) {
			generateDetectedRecordInfoTable();
		}
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new ChangedRecordsDetectorEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			
			if (operationConfig.isChangedRecordsDetector()) {
				return DetectedRecordInfoDAO.getFirstChangedRecord(tableInfo, getConfiguration().getApplicationCode(), getConfiguration().getObservationDate(), conn);
			}
			else 
			if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getFirstNewRecord(tableInfo, getConfiguration().getApplicationCode(), getConfiguration().getObservationDate(), conn);
			}
			else throw new ForbiddenOperationException("The operation '" + getOperationType() + "' is not supported in this controller!");
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
		OpenConnection conn = openConnection();
		
		try {
			if (operationConfig.isChangedRecordsDetector()) {
				return DetectedRecordInfoDAO.getLastChangedRecord(tableInfo, getConfiguration().getApplicationCode(), getConfiguration().getObservationDate(), conn);
			}
			else 
			if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getLastNewRecord(tableInfo, getConfiguration().getApplicationCode(), getConfiguration().getObservationDate(), conn);
			}
			else throw new ForbiddenOperationException("The operation '" + getOperationType() + "' is not supported in this controller!");
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
	public String getOperationType() {
		return  this.operationConfig.getOperationType();
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
	
	public boolean existDetectedRecordInfoTable() {
		OpenConnection conn = openConnection();

		try {
			String schema = conn.getCatalog();
			String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
			String tabName = "detected_record_info";
			
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
	
	private void generateDetectedRecordInfoTable() {
		OpenConnection conn = openConnection();
		
		String sql = "";
		
		sql += "CREATE TABLE detected_record_info (\n";
		sql += "id int(11) NOT NULL AUTO_INCREMENT,\n";
		sql += "table_name varchar(100) NOT NULL,\n";
		sql += "record_id int(11) NOT NULL,\n";
		sql += "record_uuid varchar(38) NOT NULL,\n";
		sql += "operation_date datetime NOT NULL,\n";
		sql += "operation_type VARCHAR(1) NOT NULL,\n";
		sql += "creation_date datetime DEFAULT CURRENT_TIMESTAMP,\n";
		sql += "app_code VARCHAR(100) NOT NULL,\n";
		sql += "record_origin_location_code VARCHAR(100) NOT NULL,\n";
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
