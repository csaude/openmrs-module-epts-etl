package org.openmrs.module.epts.etl.merge.processor;

/**
 * The data bases merge performes the merge of db from several sources to the central DB. It cames after {@link DBQuickCopyEngine} process.
 * The data bases merge load the minimal information of records from the stage area and then load the full dstRecord info from the origin schema of winning dstRecord 
 * 
 */
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.merge.controller.DataBaseMergeFromSourceDBController;
import org.openmrs.module.epts.etl.merge.model.MergingRecord;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBasesMergeFromSourceProcessor extends TaskProcessor<EtlDatabaseObject> {
	
	public DataBasesMergeFromSourceProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DataBaseMergeFromSourceDBController getRelatedOperationController() {
		return (DataBaseMergeFromSourceDBController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn) throws DBException {
		logInfo("PERFORMING MERGE ON " + etlObjects.size() + "' " + getMainSrcTableName());
		
		int i = 1;
		
		for (EtlObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logDebug(startingStrLog + ": Merging Record: [" + record + "]");
			
			MergingRecord data = new MergingRecord((EtlStageRecordVO) record, getSrcConf(),
			        getRelatedOperationController().getSrcConnInfo(), getRelatedOperationController().getDstConnInfo());
			
			try {
				data.merge(srcConn);
			}
			catch (MissingParentException e) {
				logWarn(record + " - " + e.getMessage() + " The dstRecord will be skipped");
			}
			
			i++;
		}
		getTaskResultInfo().addAllToRecordsWithNoError(EtlOperationItemResult.parseFromEtlDatabaseObject(etlObjects));
		
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
}
