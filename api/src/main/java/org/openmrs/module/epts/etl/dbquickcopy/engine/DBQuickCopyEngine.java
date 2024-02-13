package org.openmrs.module.epts.etl.dbquickcopy.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.epts.etl.dbquickcopy.model.DBQuickCopySearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DBQuickCopyEngine extends Engine {
	
	public DBQuickCopyEngine(EngineMonitor monitor, RecordLimits limits) {
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
	public DBQuickCopyController getRelatedOperationController() {
		return (DBQuickCopyController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		try {
			List<DatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, DatabaseObject.class);
			
			logInfo("LOADING  '" + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName()
			        + " TO DESTINATION DB");
			
			List<SyncImportInfoVO> records = SyncImportInfoVO.generateFromSyncRecord(syncRecordsAsOpenMRSObjects,
			    getRelatedOperationController().getAppOriginLocationCode(), false);
			
			for (SyncImportInfoVO rec : records) {
				rec.setConsistent(1);
			}
			
			SyncImportInfoDAO.insertAllBatch(records, getSyncTableConfiguration(), conn);
			
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBQuickCopySearchParams(this.getSyncTableConfiguration(),
		        limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
