package org.openmrs.module.epts.etl.detectgapes.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.detectgapes.controller.DetectGapesController;
import org.openmrs.module.epts.etl.detectgapes.model.DetectGapesSearchParams;
import org.openmrs.module.epts.etl.detectgapes.model.GapeDAO;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Detect gapes within records in a specific tables. For Eg. If in a table we have a min record as 1
 * and the max as 10 then there will be gapes if the table only contains 1,2,3,6,7,8,10. The gapes
 * are 4,5,9. The gapes are writen on an csv file
 * 
 * @author jpboane
 */
public class DetectGapesEngine extends EtlEngine {
	
	/*
	 * The previous record
	 */
	private EtlDatabaseObject prevRec;
	
	public DetectGapesEngine(Engine monitor, ThreadRecordIntervalsManager limits) {
		super(monitor, limits);
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
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		logDebug("DETECTING GAPES ON " + etlObjects.size() + "' " + getMainSrcTableName());
		
		if (this.prevRec == null) {
			this.prevRec = (EtlDatabaseObject) etlObjects.get(0);
		}
		
		for (EtlObject record : etlObjects) {
			EtlDatabaseObject rec = (EtlDatabaseObject) record;
			
			int diff = rec.getObjectId().getSimpleValueAsInt() - prevRec.getObjectId().getSimpleValueAsInt();
			
			if (diff > 1) {
				logDebug("Found gape of " + diff + " between " + prevRec.getObjectId() + " and " + rec.getObjectId());
				
				for (int i = prevRec.getObjectId().getSimpleValueAsInt() + 1; i < rec.getObjectId()
				        .getSimpleValueAsInt(); i++) {
					GapeDAO.insert(getSrcConf(), i, conn);
				}
			}
			
			prevRec = rec;
		}
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadRecordIntervalsManager limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new DetectGapesSearchParams(this.getEtlConfiguration(),
		        limits, this);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
