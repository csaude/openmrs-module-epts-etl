package org.openmrs.module.epts.etl.synchronization.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.synchronization.engine.DataBaseMergeFromJSONEngine;
import org.openmrs.module.epts.etl.synchronization.model.DataBaseMergeFromJSONSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the synchronization processs between tables
 * 
 * @author jpboane
 */
public class DatabaseMergeFromJSONController extends SiteOperationController<SyncImportInfoVO> {
	
	public DatabaseMergeFromJSONController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@Override
	public TaskProcessor<SyncImportInfoVO> initRelatedEngine(Engine<SyncImportInfoVO> monitor,
	        IntervalExtremeRecord limits) {
		
		return new DataBaseMergeFromJSONEngine(monitor, limits);
	}
	
	@Override
		public AbstractEtlSearchParams<SyncImportInfoVO> initMainSearchParams(ThreadRecordIntervalsManager<SyncImportInfoVO> intervalsMgt,
		        Engine<SyncImportInfoVO> engine) {
		/*AbstractEtlSearchParams<? extends EtlObject> searchParams = new DataBaseMergeFromJSONSearchParams(this.getSyncTableConfiguration(), limits, this.getRelatedOperationController().getAppOriginLocationCode());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;*/
		
		return null;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams((Engine<SyncImportInfoVO>) engine, null);
			searchParams.setSyncStartDate(this.progressInfo.getStartTime());
			
			SyncImportInfoVO obj = SyncImportInfoDAO.getFirstRecord(searchParams, conn);
			
			if (obj != null)
				return obj.getId();
			
			return 0;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams((Engine<SyncImportInfoVO>) engine, null);
			searchParams.setSyncStartDate(this.progressInfo.getStartTime());
			
			SyncImportInfoVO obj = SyncImportInfoDAO.getLastRecord(searchParams, conn);
			
			if (obj != null)
				return obj.getId();
			
			return 0;
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
		return hasNestedController() ? false : true;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}

	@Override
	public void afterEtl(List<SyncImportInfoVO> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
}
