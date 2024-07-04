package org.openmrs.module.epts.etl.synchronization.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.synchronization.engine.DataBaseMergeFromJSONEngine;
import org.openmrs.module.epts.etl.synchronization.model.DataBaseMergeFromJSONSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the synchronization processs between tables
 * 
 * @author jpboane
 */
public class DatabaseMergeFromJSONController extends SiteOperationController<EtlStageRecordVO> {
	
	public DatabaseMergeFromJSONController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@Override
	public TaskProcessor<EtlStageRecordVO> initRelatedTaskProcessor(Engine<EtlStageRecordVO> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		
		return new DataBaseMergeFromJSONEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlStageRecordVO> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlStageRecordVO> intervalsMgt, Engine<EtlStageRecordVO> engine) {
		/*AbstractEtlSearchParams<? extends EtlObject> searchParams = new DataBaseMergeFromJSONSearchParams(this.getSyncTableConfiguration(), limits, this.getRelatedOperationController().getAppOriginLocationCode());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;*/
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams(
			        (Engine<EtlStageRecordVO>) engine, null);
			searchParams.setSyncStartDate(this.progressInfo.getStartTime());
			
			EtlStageRecordVO obj = SyncImportInfoDAO.getFirstRecord(searchParams, conn);
			
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
			
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams(
			        (Engine<EtlStageRecordVO>) engine, null);
			searchParams.setSyncStartDate(this.progressInfo.getStartTime());
			
			EtlStageRecordVO obj = SyncImportInfoDAO.getLastRecord(searchParams, conn);
			
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
	public void afterEtl(List<EtlStageRecordVO> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
}
