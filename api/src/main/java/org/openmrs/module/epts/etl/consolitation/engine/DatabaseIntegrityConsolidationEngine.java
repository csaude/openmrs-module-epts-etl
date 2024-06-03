package org.openmrs.module.epts.etl.consolitation.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.epts.etl.consolitation.model.DatabaseIntegrityConsolidationSearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseIntegrityConsolidationEngine extends Engine {
	
	public DatabaseIntegrityConsolidationEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	public DatabaseIntegrityConsolidationController getRelatedOperationController() {
		return (DatabaseIntegrityConsolidationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException{
		if (true) throw new ForbiddenOperationException("Rever este metodo!");
		
		/*List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		
		this.getMonitor().logInfo("CONSOLIDATING INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			obj.setRelatedSyncInfo(SyncImportInfoVO.retrieveFromSyncRecord(getSyncTableConfiguration(), obj, getRelatedOperationController().getAppOriginLocationCode(), conn));
			
			obj.consolidateData(getSyncTableConfiguration(), conn);
		}
		
		this.getMonitor().logInfo("INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " CONSOLIDATED!");*/
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadRecordIntervalsManager limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new DatabaseIntegrityConsolidationSearchParams(this.getEtlConfiguration(), limits,  conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;
	}
}
