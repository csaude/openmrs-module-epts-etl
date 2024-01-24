package org.openmrs.module.epts.etl.dbcopy.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.MappedTableInfo;
import org.openmrs.module.epts.etl.dbcopy.controller.DBCopyController;
import org.openmrs.module.epts.etl.dbcopy.model.DBCopySearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DBCopyEngine extends Engine {
	
	public DBCopyEngine(EngineMonitor monitor, RecordLimits limits) {
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
	public DBCopyController getRelatedOperationController() {
		return (DBCopyController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		logInfo("PERFORMING BATCH COPY ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		Map<String, List<DatabaseObject>> mergingRecs = new HashMap<>();
		
		try {
			
			for (SyncRecord record : syncRecords) {
				DatabaseObject rec = (DatabaseObject) record;
				
				for (MappedTableInfo mappingInfo : getSyncTableConfiguration().getDestinationTableMappingInfo()) {
					
					DatabaseObject destObject = null;
					
					destObject = mappingInfo.generateMappedObject(rec, this.getDstApp(), conn);
					
					if (mergingRecs.get(mappingInfo.getTableName()) == null) {
						mergingRecs.put(mappingInfo.getTableName(), new ArrayList<>(syncRecords.size()));
					}
					
					mergingRecs.get(mappingInfo.getTableName()).add(destObject);
				}
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				syncRecords.removeAll(recordsToIgnoreOnStatistics);
			}
			
			for (String key : mergingRecs.keySet()) {
				DatabaseObjectDAO.insertAllDataWithoutId(mergingRecs.get(key), dstConn);
			}
			
			logInfo("COPY DONE ON " + syncRecords.size() + " " + getSyncTableConfiguration().getTableName() + "!");
			
			dstConn.markAsSuccessifullyTerminated();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	@Override
	public void requestStop() {
	}
	
	public AppInfo getDstApp() {
		return this.getRelatedOperationController().getDstAppInfo();
	}
	
	public AppInfo getSrcApp() {
		return this.getRelatedOperationController().getSrcAppInfo();
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBCopySearchParams(this.getSyncTableConfiguration(),
		        limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSyncConfiguration().getObservationDate());
		
		return searchParams;
	}
	
}
