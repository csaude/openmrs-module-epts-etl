package org.openmrs.module.epts.etl.synchronization.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.synchronization.controller.DatabaseMergeFromJSONController;
import org.openmrs.module.epts.etl.synchronization.model.DataBaseMergeFromJSONSearchParams;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBaseMergeFromJSONEngine extends Engine {
	
	public DataBaseMergeFromJSONEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		super(monitor, limits);
	}

	@Override
	protected void restart() {
		this.getSearchParams().setSyncStartDate(DateAndTimeUtilities.getCurrentDate());
	}
	
	@Override
	public DatabaseMergeFromJSONController getRelatedOperationController() {
		return (DatabaseMergeFromJSONController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		throw new ForbiddenOperationException("Rever este metodo!");
		
		/*getRelatedOperationController().logInfo("SYNCHRONIZING '"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());
		
		if (getSyncTableConfiguration().isDoIntegrityCheckInTheEnd(getRelatedOperationController().getOperationType()) && !getSyncTableConfiguration().useSharedPKKey()) {
			List<OpenMRSObject> objects = SyncImportInfoVO.convertAllToOpenMRSObject(getSyncTableConfiguration(), utilities.parseList(syncRecords, SyncImportInfoVO.class), conn);
			
			OpenMRSObjectDAO.insertAll(objects, getSyncTableConfiguration(), getRelatedOperationController().getAppOriginLocationCode(), conn);
			
			SyncImportInfoDAO.markAsToBeCompletedInFuture(getSyncTableConfiguration(), utilities.parseList(syncRecords, SyncImportInfoVO.class), conn);
		}
		else{
			for (EtlObject record : syncRecords) {
				((SyncImportInfoVO)record).sync(this.getSyncTableConfiguration(), conn);
			}
		}
		
		getRelatedOperationController().logInfo("SYNCHRONIZED'"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());*/
	}
	
	@Override
	public DataBaseMergeFromJSONSearchParams getSearchParams() {
		return (DataBaseMergeFromJSONSearchParams) super.getSearchParams();
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadLimitsManager limits, Connection conn) {
		/*AbstractEtlSearchParams<? extends EtlObject> searchParams = new DataBaseMergeFromJSONSearchParams(this.getSyncTableConfiguration(), limits, this.getRelatedOperationController().getAppOriginLocationCode());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;*/
		
		return null;
	}
	
}
