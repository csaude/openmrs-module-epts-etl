package org.openmrs.module.epts.etl.merge.controller;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.merge.processor.DataBasesMergeFromSourceProcessor;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the process of data bases merge
 * 
 * @author jpboane
 */
public class DataBaseMergeFromSourceDBController extends EtlController {
	
	public DataBaseMergeFromSourceDBController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt, Engine<EtlDatabaseObject> engine) {
		
		throw new ForbiddenOperationException("Review this method");
		
		/*AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new DataBaseMergeFromSourceDBSearchParams(processor, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;*/
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		return new DataBasesMergeFromSourceProcessor(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		Integer id = Integer.valueOf(0);
		
		try {
			conn = openSrcConnection();
			
			EtlStageRecordVO record = SyncImportInfoDAO.getFirstMissingRecordInDestination(engine.getSrcConf(), conn);
			
			id = record != null ? record.getId() : 0;
			
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
		OpenConnection conn = null;
		
		Integer id = Integer.valueOf(0);
		
		try {
			conn = openSrcConnection();
			
			EtlStageRecordVO record = SyncImportInfoDAO.getLastMissingRecordInDestination(engine.getSrcConf(), conn);
			
			id = record != null ? record.getId() : 0;
			
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
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
