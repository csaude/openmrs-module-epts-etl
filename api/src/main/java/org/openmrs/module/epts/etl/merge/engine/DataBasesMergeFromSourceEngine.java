package org.openmrs.module.epts.etl.merge.engine;

/**
 * The data bases merge performes the merge of db from several sources to the central DB. It cames after {@link DBQuickCopyEngine} process.
 * The data bases merge load the minimal information of records from the stage area and then load the full record info from the origin schema of winning record 
 * 
 */
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.merge.controller.DataBaseMergeFromSourceDBController;
import org.openmrs.module.epts.etl.merge.model.DataBaseMergeFromSourceDBSearchParams;
import org.openmrs.module.epts.etl.merge.model.MergingRecord;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBasesMergeFromSourceEngine extends Engine {
	
	public DataBasesMergeFromSourceEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public DataBaseMergeFromSourceDBController getRelatedOperationController() {
		return (DataBaseMergeFromSourceDBController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		logInfo("PERFORMING MERGE ON " + syncRecords.size() + "' " + getSrcTableName());
		
		int i = 1;
		
		for (SyncRecord record : syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			logDebug(startingStrLog + ": Merging Record: [" + record + "]");
			
			MergingRecord data = new MergingRecord((SyncImportInfoVO) record, getSrcTableConfiguration(),
			        getRelatedOperationController().getRemoteApp(), getRelatedOperationController().getMainApp());
			
			try {
				data.merge(conn);
			}
			catch (MissingParentException e) {
				logWarn(record + " - " + e.getMessage() + " The record will be skipped");
			}
			
			i++;
		}
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DataBaseMergeFromSourceDBSearchParams(
		        this.getEtlConfiguration(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
}
