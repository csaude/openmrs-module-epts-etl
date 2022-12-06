package org.openmrs.module.eptssync.dbquickcopy.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.eptssync.dbquickcopy.model.DBQuickCopySearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBQuickCopyEngine extends Engine {
		
	public DBQuickCopyEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		OpenConnection srcConn = getRelatedOperationController().openSrcConnection();
		
		try {
			return  utilities.parseList(SearchParamsDAO.search(this.searchParams, srcConn), SyncRecord.class);
		}
		finally {
			srcConn.finalizeConnection();
		}

	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public DBQuickCopyController getRelatedOperationController() {
		return (DBQuickCopyController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		try {
			List<DatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, DatabaseObject.class);
			
			logInfo("LOADING  '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO DESTINATION DB");
			
			List<SyncImportInfoVO> records = SyncImportInfoVO.generateFromSyncRecord(syncRecordsAsOpenMRSObjects, getRelatedOperationController().getAppOriginLocationCode(), false);
			
			for (SyncImportInfoVO rec : records) {
				rec.setConsistent(1);
			}
				
			SyncImportInfoDAO.insertAllBatch(records, getSyncTableConfiguration(), conn);
			
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}	
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBQuickCopySearchParams(this.getSyncTableConfiguration(), limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}

}
