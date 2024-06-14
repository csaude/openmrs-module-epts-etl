package org.openmrs.module.epts.etl.dbquickcopy.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.epts.etl.dbquickcopy.model.DBQuickCopySearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DBQuickCopyEngine extends EtlEngine {
	
	public DBQuickCopyEngine(Engine monitor, ThreadRecordIntervalsManager limits) {
		super(monitor, limits);
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
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		try {
			List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(etlObjects, EtlDatabaseObject.class);
			
			logInfo(
			    "LOADING  '" + etlObjects.size() + "' " + getSrcConf().getTableName() + " TO DESTINATION DB");
			
			List<SyncImportInfoVO> records = SyncImportInfoVO.generateFromSyncRecord(syncRecordsAsOpenMRSObjects,
			    getRelatedOperationController().getAppOriginLocationCode(), false);
			
			for (SyncImportInfoVO rec : records) {
				rec.setConsistent(1);
			}
			
			SyncImportInfoDAO.insertAllBatch(records, getSrcConf(), conn);
			
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadRecordIntervalsManager limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new DBQuickCopySearchParams(this.getEtlConfiguration(), limits,
		        this);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
