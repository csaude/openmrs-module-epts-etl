package org.openmrs.module.epts.etl.synchronization.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.synchronization.controller.DatabaseMergeFromJSONController;
import org.openmrs.module.epts.etl.synchronization.model.DataBaseMergeFromJSONSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBaseMergeFromJSONProcessor extends TaskProcessor<EtlStageRecordVO> {
	
	public DataBaseMergeFromJSONProcessor(Engine<EtlStageRecordVO> monitor, IntervalExtremeRecord limits,  boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DatabaseMergeFromJSONController getRelatedOperationController() {
		return (DatabaseMergeFromJSONController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlStageRecordVO> records, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		throw new ForbiddenOperationException("Rever este metodo!");
		
		/*getRelatedOperationController().logInfo("SYNCHRONIZING '"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());
		
		if (getSyncTableConfiguration().isDoIntegrityCheckInTheEnd(getRelatedOperationController().getOperationType()) && !getSyncTableConfiguration().useSharedPKKey()) {
			List<OpenMRSObject> objects = EtlStageRecordVO.convertAllToOpenMRSObject(getSyncTableConfiguration(), utilities.parseList(syncRecords, EtlStageRecordVO.class), conn);
			
			OpenMRSObjectDAO.insertAll(objects, getSyncTableConfiguration(), getRelatedOperationController().getAppOriginLocationCode(), conn);
			
			SyncImportInfoDAO.markAsToBeCompletedInFuture(getSyncTableConfiguration(), utilities.parseList(syncRecords, EtlStageRecordVO.class), conn);
		}
		else{
			for (EtlObject dstRecord : syncRecords) {
				((EtlStageRecordVO)dstRecord).sync(this.getSyncTableConfiguration(), conn);
			}
		}
		
		getRelatedOperationController().logInfo("SYNCHRONIZED'"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());*/
	}
	
	@Override
	public DataBaseMergeFromJSONSearchParams getSearchParams() {
		return (DataBaseMergeFromJSONSearchParams) super.getSearchParams();
	}
	
	
	@Override
	public TaskProcessor<EtlStageRecordVO> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
}
