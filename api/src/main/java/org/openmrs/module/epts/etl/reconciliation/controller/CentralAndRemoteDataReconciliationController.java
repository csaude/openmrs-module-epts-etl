package org.openmrs.module.epts.etl.reconciliation.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.reconciliation.engine.CentralAndRemoteDataReconciliationEngine;
import org.openmrs.module.epts.etl.reconciliation.model.CentralAndRemoteDataReconciliationSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class CentralAndRemoteDataReconciliationController extends OperationController<EtlDatabaseObject> {
	
	public CentralAndRemoteDataReconciliationController(ProcessController processController,
	    EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt,
	        Engine<EtlDatabaseObject> engine) {
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new CentralAndRemoteDataReconciliationSearchParams(
		        engine, intervalsMgt, this.getOperationType());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;
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
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits,  boolean runningInConcurrency) {
		return new CentralAndRemoteDataReconciliationEngine(monitor, limits, runningInConcurrency);
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
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		if (engine.getSrcConf().getTableName().equalsIgnoreCase("users"))
			return 0;
		
		OpenConnection conn = null;
		
		int id = 0;
		
		try {
			conn = openSrcConnection();
			
			if (isMissingRecordsDetector()) {
				SyncImportInfoVO record = SyncImportInfoDAO.getFirstMissingRecordInDestination(engine.getSrcConf(), conn);
				
				id = record != null ? record.getId() : 0;
			} else if (isOutdateRecordsDetector()) {
				id = DatabaseObjectDAO.getFirstRecord(engine.getSrcConf(), conn);
			} else if (isPhantomRecordsDetector()) {
				EtlDatabaseObject record = DatabaseObjectDAO.getFirstPhantomRecordInDestination(engine.getSrcConf(), conn);
				
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
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		if (engine.getSrcConf().getTableName().equalsIgnoreCase("users"))
			return 0;
		
		OpenConnection conn = null;
		
		int id = 0;
		
		try {
			conn = openSrcConnection();
			
			if (isMissingRecordsDetector()) {
				SyncImportInfoVO record = SyncImportInfoDAO.getLastMissingRecordInDestination(engine.getSrcConf(), conn);
				
				id = record != null ? record.getId() : 0;
			} else if (isOutdateRecordsDetector()) {
				id = DatabaseObjectDAO.getLastRecord(engine.getSrcConf(), conn);
			} else if (isPhantomRecordsDetector()) {
				EtlDatabaseObject record = DatabaseObjectDAO.getLastPhantomRecordInDestination(engine.getSrcConf(), conn);
				
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
	public OpenConnection openSrcConnection() throws DBException {
		OpenConnection conn = super.openSrcConnection();
		
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
		
		String schema = getEtlConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "data_conciliation_info";
		
		OpenConnection conn = openSrcConnection();
		
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
		
		sql += "CREATE TABLE " + getEtlConfiguration().getSyncStageSchema() + ".data_conciliation_info (\n";
		sql += "id int(11) NOT NULL AUTO_INCREMENT,\n";
		sql += "record_uuid varchar(100) NOT NULL,\n";
		sql += "record_origin_location_code varchar(100) NOT NULL,\n";
		sql += "reason_type varchar(100) NOT NULL,\n";
		sql += "table_name VARCHAR(100) NOT NULL,\n";
		sql += "creation_date datetime DEFAULT CURRENT_TIMESTAMP,\n";
		sql += "PRIMARY KEY (id)\n";
		sql += ") ENGINE=InnoDB;\n";
		
		try {
			conn = openSrcConnection();
			
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

	@Override
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
}
