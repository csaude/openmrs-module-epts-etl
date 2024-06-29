package org.openmrs.module.epts.etl.changedrecordsdetector.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.openmrs.module.epts.etl.changedrecordsdetector.engine.ChangedRecordsDetectorEngine;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.ChangedRecordsDetectorSearchParams;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.DetectedRecordInfoDAO;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class ChangedRecordsDetectorController extends OperationController<EtlDatabaseObject> {
	
	public ChangedRecordsDetectorController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
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
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		return new ChangedRecordsDetectorEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			if (operationConfig.isChangedRecordsDetector()) {
				return DetectedRecordInfoDAO.getFirstChangedRecord(engine.getSrcConf(), getEtlConfiguration().getStartDate(),
				    conn);
			} else if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getFirstNewRecord(engine.getSrcConf(), getEtlConfiguration().getStartDate(),
				    conn);
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
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			if (operationConfig.isChangedRecordsDetector()) {
				return DetectedRecordInfoDAO.getLastChangedRecord(engine.getSrcConf(), getEtlConfiguration().getStartDate(),
				    conn);
			} else if (operationConfig.isNewRecordsDetector()) {
				return DetectedRecordInfoDAO.getLastNewRecord(engine.getSrcConf(), getEtlConfiguration().getStartDate(),
				    conn);
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
	
	public boolean existDetectedRecordInfoTable() throws DBException {
		OpenConnection conn = openSrcConnection();
		
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
			
			conn = openSrcConnection();
			
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
	
	@Override
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsManager, Engine<EtlDatabaseObject> engine) {
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new ChangedRecordsDetectorSearchParams(engine,
		        intervalsManager, this.getOperationType());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
