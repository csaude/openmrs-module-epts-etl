package org.openmrs.module.eptssync.changedrecordsdetector.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.eptssync.changedrecordsdetector.engine.ChangedRecordsDetectorEngine;
import org.openmrs.module.eptssync.changedrecordsdetector.model.DetectedRecordInfoDAO;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 *
 */
public class ChangedRecordsDetectorController extends OperationController {
	private AppInfo actionPerformeApp;

	
	public ChangedRecordsDetectorController(ProcessController processController, SyncOperationConfig operationConfig) {
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
				return DetectedRecordInfoDAO.getFirstChangedRecord(tableInfo, this.getActionPerformeApp().getApplicationCode(), getConfiguration().getObservationDate(), conn);
			}
			else 
			if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getFirstNewRecord(tableInfo, this.getActionPerformeApp().getApplicationCode(), getConfiguration().getObservationDate(), conn);
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
				return DetectedRecordInfoDAO.getLastChangedRecord(tableInfo, this.getActionPerformeApp().getApplicationCode(), getConfiguration().getObservationDate(), conn);
			}
			else 
			if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getLastNewRecord(tableInfo, this.getActionPerformeApp().getApplicationCode(), getConfiguration().getObservationDate(), conn);
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

	public OpenConnection openConnection() {
		OpenConnection conn = getDefaultApp().openConnection();
	
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
		sql += "PRIMARY KEY (id),\n";
		sql += " UNIQUE KEY detected_record_info_unq (table_name,app_code,record_origin_location_code,record_id,operation_type)\n";
		sql += ") ENGINE=InnoDB;\n";
		
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.addBatch("CREATE INDEX d_rec_info_app_idx ON detected_record_info (app_code);");
			st.addBatch("CREATE INDEX d_rec_info_origin_idx ON detected_record_info (record_origin_location_code);");
			st.addBatch("CREATE INDEX d_rec_info_table_app_origin_idx ON detected_record_info (table_name, app_code, record_origin_location_code);");
			st.addBatch("CREATE INDEX d_rec_info_table_idx ON detected_record_info (table_name);");
			
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

	public synchronized File generateJSONTempFile(SyncJSONInfo jsonInfo, SyncTableConfiguration tableInfo, int startRecord, int lastRecord) throws IOException {
		String fileName = "";
		
		fileName += tableInfo.getRelatedSynconfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getRelatedSynconfiguration().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getTableName();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getTableName();
		
		fileName += "_" + utilities().garantirXCaracterOnNumber(startRecord, 10);
		fileName += "_" + utilities().garantirXCaracterOnNumber(lastRecord, 10);
	
		if(new File(fileName).exists() ) {
			logInfo("The file '" + fileName + "' is already exists!!! Removing it...");
			new File(fileName).delete();
		}
		
		if(new File(fileName+".json").exists() ) {
			logInfo("The file '" + fileName  + ".json' is already exists!!! Removing it...");
			new File(fileName+".json").delete();
		}
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		File file = new File(fileName);
		file.createNewFile();
		
		return file;
	}
}
