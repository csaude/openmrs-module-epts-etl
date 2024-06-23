package org.openmrs.module.epts.etl.detectgapes.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.detectgapes.controller.DetectGapesController;
import org.openmrs.module.epts.etl.detectgapes.model.GapeDAO;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
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
	
	public DetectGapesEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DetectGapesController getRelatedOperationController() {
		return (DetectGapesController) super.getRelatedOperationController();
	}
	
	@Override
	public EtlOperationResultHeader<EtlDatabaseObject> performeSync(List<EtlDatabaseObject> etlObjects, Connection srcConn,
	        Connection dstConn) throws DBException {
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
					GapeDAO.insert(getSrcConf(), i, srcConn);
				}
			}
			
			prevRec = rec;
		}
		
		return new EtlOperationResultHeader<>(etlObjects);
	}
	
}
