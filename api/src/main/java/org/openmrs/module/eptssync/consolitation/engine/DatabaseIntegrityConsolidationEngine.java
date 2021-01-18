package org.openmrs.module.eptssync.consolitation.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.consolitation.model.DatabaseIntegrityConsolidationSearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DatabaseIntegrityConsolidationEngine extends Engine {
	
	public DatabaseIntegrityConsolidationEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public DatabaseIntegrityConsolidationController getRelatedOperationController() {
		return (DatabaseIntegrityConsolidationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		
		this.getMonitor().logInfo("CONSOLIDATING INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			obj.consolidateData(getSyncTableConfiguration(), conn);
		}
		
		this.getMonitor().logInfo("INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " CONSOLIDATED!");
	}

	/*
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		
		this.getMonitor().logInfo("CONSOLIDATING INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		try {
			for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
				consolidateData(obj, getSyncTableConfiguration(), conn);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			
			logInfo("ERROR PERFORMINF..." + getSyncTableConfiguration());
			e.printStackTrace();
			
			TimeCountDown.sleep(2000);
		}
			
		this.getMonitor().logInfo("INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " CONSOLIDATED!");
	}
	
	
	public void consolidateMetadata(OpenMRSObject obj, Connection conn) throws DBException {
		OpenMRSObject recordOnDB = OpenMRSObjectDAO.thinGetByUuid(obj.getClass(), obj.getUuid(), conn);
		
		if (recordOnDB == null) {
			//Check if ID is free 
			OpenMRSObject recOnDBById = OpenMRSObjectDAO.getById(obj.getClass(), obj.getObjectId(), conn);
			
			if (recOnDBById == null) {
				OpenMRSObjectDAO.insert(obj, conn);
			}
			else {
				throw new MetadataInconsistentException(recOnDBById);
			}
		}
		else {
			if (recordOnDB.getObjectId() != obj.getObjectId()) {
				throw new MetadataInconsistentException(recordOnDB);
			}
		}
	}
	
	public void removeDueInconsistency(OpenMRSObject obj, SyncTableConfiguration tableConf, Map<ParentRefInfo, Integer> missingParents, Connection conn) throws DBException{
		SyncImportInfoVO syncInfo = obj.retrieveRelatedSyncInfo(tableConf, conn);
		
		syncInfo.markAsFailedToMigrate(tableConf, obj.generateMissingInfo(missingParents), conn);
		
		obj.remove(conn);
		
		BaseDAO.commit(conn);
		
		try {
			for (ParentRefInfo refInfo: tableConf.getChildRefInfo(conn)) {
				
				if (!refInfo.isMetadata()) {
					List<OpenMRSObject> children =  OpenMRSObjectDAO.getByOriginParentId(refInfo.determineRelatedReferenceClass(conn), refInfo.getReferenceColumnName(), obj.getOriginRecordId(), obj.getOriginAppLocationCode(), conn);
					
					for (OpenMRSObject child : children) {
						consolidateData(child, refInfo.getReferenceTableInfo(), conn);
					}
				}
			}
		} catch (Exception e) {
			logInfo("ERROR PERFORMING REMOTION ON "+obj.generateTableName());
			TimeCountDown.sleep(2000);
		}
	}
	
	public void consolidateData(OpenMRSObject obj, SyncTableConfiguration tableInfo,  Connection conn) throws DBException{
		Map<ParentRefInfo, Integer> missingParents = obj.loadMissingParents(tableInfo, conn);
		
		boolean missingNotIgnorableParent = false;
		
		for (Entry<ParentRefInfo, Integer> missingParent : missingParents.entrySet()) {
			if (!missingParent.getKey().isIgnorable()) {
				missingNotIgnorableParent = true;
				break;
			}
		}
		
		if (missingNotIgnorableParent) {
			obj.removeDueInconsistency(getSyncTableConfiguration(), missingParents, conn);
		}
		else {
			obj.loadDestParentInfo(conn);
			
			obj.save(conn);
			
			SyncImportInfoVO syncInfo = obj.retrieveRelatedSyncInfo(tableInfo, conn);
			
			if (obj.hasIgnoredParent()) {
				syncInfo.markAsPartialMigrated(tableInfo, obj.generateMissingInfo(missingParents), conn);
			}
			else syncInfo.delete(tableInfo, conn);
			
			obj.markAsConsistent(conn);
			
			BaseDAO.commit(conn);
		}
	}
	*/
	
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DatabaseIntegrityConsolidationSearchParams(this.getSyncTableConfiguration(), limits, getRelatedOperationController().getAppOriginLocationCode(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
	
		return searchParams;
	}
}
