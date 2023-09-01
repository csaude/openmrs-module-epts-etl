package org.openmrs.module.eptssync.detectgapes.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.detectgapes.controller.DetectGapesController;
import org.openmrs.module.eptssync.detectgapes.model.DetectGapesSearchParams;
import org.openmrs.module.eptssync.detectgapes.model.GapeDAO;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

/**
 * Detect gapes within records in a specific tables. For Eg. If in a table we have a min record as 1
 * and the max as 10 then there will be gapes if the table only contains 1,2,3,6,7,8,10. The gapes
 * are 4,5,9. The gapes are writen on an csv file
 * 
 * @author jpboane
 */
public class DetectGapesEngine extends Engine {
	
	/*
	 * The previous record
	 */
	private DatabaseObject prevRec;
	
	public DetectGapesEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		List<SyncRecord> records = new ArrayList<SyncRecord>();
		
		records = utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
		
		return records;
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public DetectGapesController getRelatedOperationController() {
		return (DetectGapesController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		logDebug("DETECTING GAPES ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		if (this.prevRec == null) {
			this.prevRec = (DatabaseObject) syncRecords.get(0);
		}
		
		for (SyncRecord record : syncRecords) {
			DatabaseObject rec = (DatabaseObject) record;
			
			int diff = rec.getObjectId() - prevRec.getObjectId();
			
			if (diff > 1) {
				logDebug("Found gape of " + diff + " between " + prevRec.getObjectId() + " and " + rec.getObjectId());
				
				for (int i = prevRec.getObjectId() + 1; i < rec.getObjectId(); i++) {
					GapeDAO.insert(getSyncTableConfiguration(), i, conn);
				}
			}
			
			prevRec = rec;
		}
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DetectGapesSearchParams(this.getSyncTableConfiguration(),
		        limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
	
}