package org.openmrs.module.epts.etl.resolveconflictsinstagearea.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.controller.ResolveConflictsInStageAreaController;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ResolveConflictsInStageAreaEngine extends TaskProcessor<EtlStageRecordVO> {
	
	public ResolveConflictsInStageAreaEngine(Engine<EtlStageRecordVO> monitor, IntervalExtremeRecord limits,  boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public ResolveConflictsInStageAreaController getRelatedOperationController() {
		return (ResolveConflictsInStageAreaController) super.getRelatedOperationController();
	}
	
	
	@Override
	public void performeEtl(List<EtlStageRecordVO> records, Connection srcConn,
	        Connection dstConn) throws DBException {

		utilities.throwReviewMethodException();

		/*List<EtlStageRecordVO> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, EtlStageRecordVO.class);
		
		this.getMonitor().logInfo("PERFORMING CONFLICTS RESOLUTION ACTION '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		for (EtlStageRecordVO obj : syncRecordsAsOpenMRSObjects) {
			try {
				List<EtlStageRecordVO> recordsInConflict = SyncImportInfoDAO.getAllByUuid(getSyncTableConfiguration(), obj.getRecordUuid(), conn);
				
				EtlStageRecordVO mostRecent = EtlStageRecordVO.chooseMostRecent(recordsInConflict);
				
				recordsInConflict.remove(mostRecent);
				
				String loosers = "";
				
				for (EtlStageRecordVO recInConflict : recordsInConflict) {
					
					loosers += (!loosers.isEmpty() ? "," : "") + "{" + generateRecInfo(recInConflict) + "}";
					
					recInConflict.markAsInconsistent(getSyncTableConfiguration(), conn);
				}
				
				logDebug("Done Processing dstRecord: " + obj.getRecordUuid() + "! Win: {" + generateRecInfo(mostRecent) + "} loosers: [" + loosers + "]");
			} catch (Exception e) {
				e.printStackTrace();
				
				logError("Any error occurred processing dstRecord [uuid: " + obj.getRecordUuid() + ", id: " + obj.getId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		this.getMonitor().logInfo("CONFLICTS RESOLVED FOR RECORDS '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + "!");
		*/
	}

}
