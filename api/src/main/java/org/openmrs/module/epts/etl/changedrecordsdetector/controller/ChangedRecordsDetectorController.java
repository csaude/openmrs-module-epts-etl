package org.openmrs.module.epts.etl.changedrecordsdetector.controller;

import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.epts.etl.changedrecordsdetector.engine.ChangedRecordsDetectorEngine;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.DetectedRecordInfoDAO;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class ChangedRecordsDetectorController extends OperationController {
	
	private AppInfo actionPerformeApp;
	
	public ChangedRecordsDetectorController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
		
		//We assume that there is only one application listed in appConf
		this.actionPerformeApp = getConfiguration().exposeAllAppsNotMain().get(0);
	}
	
	public AppInfo getActionPerformeApp() {
		return actionPerformeApp;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		try {
			if (!existDetectedRecordInfoTable()) {
				generateDetectedRecordInfoTable();
			}
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new ChangedRecordsDetectorEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			if (operationConfig.isChangedRecordsDetector()) {
				return DetectedRecordInfoDAO.getFirstChangedRecord(config.getSrcConf(),
				    this.getActionPerformeApp().getApplicationCode(), getConfiguration().getStartDate(), conn);
			} else if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getFirstNewRecord(config.getSrcConf(),
				    this.getActionPerformeApp().getApplicationCode(), getConfiguration().getStartDate(), conn);
			} else
				throw new ForbiddenOperationException(
				        "The operation '" + getOperationType() + "' is not supported in this controller!");
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
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			if (operationConfig.isChangedRecordsDetector()) {
				return DetectedRecordInfoDAO.getLastChangedRecord(config.getSrcConf(),
				    this.getActionPerformeApp().getApplicationCode(), getConfiguration().getStartDate(), conn);
			} else if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getLastNewRecord(config.getSrcConf(),
				    this.getActionPerformeApp().getApplicationCode(), getConfiguration().getStartDate(), conn);
			} else
				throw new ForbiddenOperationException(
				        "The operation '" + getOperationType() + "' is not supported in this controller!");
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
	
	public OpenConnection openConnection() throws DBException {
		OpenConnection conn = getDefaultApp().openConnection();
		
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
	
	public boolean existDetectedRecordInfoTable() throws DBException {
		OpenConnection conn = openConnection();
		
		try {
			String schema = conn.getCatalog();
			String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
			String tabName = "detected_record_info";
			
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
	
	private void generateDetectedRecordInfoTable() {
		OpenConnection conn = null;
		
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
		sql += "PRIMARY KEY (id),\n";
		sql += " UNIQUE KEY detected_record_info_unq (table_name,app_code,record_origin_location_code,record_id,operation_type)\n";
		sql += ") ENGINE=InnoDB;\n";
		
		try {
			
			conn = openConnection();
			
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.addBatch("CREATE INDEX d_rec_info_app_idx ON detected_record_info (app_code);");
			st.addBatch("CREATE INDEX d_rec_info_origin_idx ON detected_record_info (record_origin_location_code);");
			st.addBatch(
			    "CREATE INDEX d_rec_info_table_app_origin_idx ON detected_record_info (table_name, app_code, record_origin_location_code);");
			st.addBatch("CREATE INDEX d_rec_info_table_idx ON detected_record_info (table_name);");
			
			st.executeBatch();
			
			st.close();
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null) {
				conn.markAsSuccessifullyTerminated();
				conn.finalizeConnection();
			}
		}
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
	
}
