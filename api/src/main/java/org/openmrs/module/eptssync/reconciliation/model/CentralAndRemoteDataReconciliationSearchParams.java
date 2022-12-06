package org.openmrs.module.eptssync.reconciliation.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncOperationType;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class CentralAndRemoteDataReconciliationSearchParams extends SyncSearchParams<DatabaseObject>{
	private boolean selectAllRecords;
	private SyncOperationType type;
	
	public CentralAndRemoteDataReconciliationSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, SyncOperationType type, Connection conn) {
		super(tableInfo, limits);
				
		this.type = type;
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		if (this.type.isMissingRecordsDetector()) {
			searchClauses.addColumnToSelect("src_.record_origin_id, src_.record_uuid uuid, src_.record_uuid,  src_.record_origin_location_code");
		
			searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName() + " src_ ");
			
			if (getTableInfo().getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauses("not exists (select * from person dest_ inner join patient on patient_id = person_id where dest_.uuid = src_.record_uuid)");
			}
			else {
				searchClauses.addToClauses("not exists (select * from " + getTableInfo().getTableName() + " dest_ where dest_.uuid = src_.record_uuid)");
			}
			
			searchClauses.addToClauses("src_.consistent = 1");
		}
		else
		if (this.type.isOutdatedRecordsDetector()) {
			searchClauses.addColumnToSelect("dest_.*");
			
			searchClauses.addColumnToSelect("src_.record_origin_id, src_.record_uuid, src_.record_origin_location_code");
			
			if (getTableInfo().getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauseFrom("person inner join patient dest_ on patient_id = person_id");
				searchClauses.addColumnToSelect("person.uuid");
				searchClauses.addToClauseFrom("inner join " + getTableInfo().generateFullStageTableName() + " src_ on person.uuid = src_.record_uuid");
			}
			else {
				searchClauses.addToClauseFrom(tableInfo.getTableName() + " dest_");
				searchClauses.addToClauseFrom("inner join " + getTableInfo().generateFullStageTableName() + " src_ on dest_.uuid = src_.record_uuid");
			}
			
			
			searchClauses.addToClauses("src_.consistent = 1");
		}
		else
		if (this.type.isPhantomRecordsDetector() ) {
			searchClauses.addColumnToSelect("dest_.*");
			
			if (getTableInfo().getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauseFrom("person inner join patient dest_ on patient_id = person_id");
				searchClauses.addToClauseFrom("left join " + getTableInfo().generateFullStageTableName() + " src_ on person.uuid = src_.record_uuid");
				
				searchClauses.addColumnToSelect("person.uuid");
			}
			else {
				searchClauses.addToClauseFrom(tableInfo.getTableName() + " dest_");
				searchClauses.addToClauseFrom("left join " + getTableInfo().generateFullStageTableName() + " src_ on dest_.uuid = src_.record_uuid");
			}
				
			
			searchClauses.addToClauses("id is null");
		}
		else {
			throw new ForbiddenOperationException("Operation " + this.type + " not supported!");
		}
			
		if (!this.selectAllRecords) {
			
			if (limits != null) {
				
				if (type.isOutdatedRecordsDetector() || type.isPhantomRecordsDetector()) {
					searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				}
				else 
				if (type.isMissingRecordsDetector()){
					searchClauses.addToClauses("id between ? and ?");
				}
				
				searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
				searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
			}
			
			if (this.tableInfo.getExtraConditionForExport() != null) {
				searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
			}
		}
		
		return searchClauses;
	}	
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
		 if (type.isMissingRecordsDetector() ) {
			 return DatabaseEntityPOJOGenerator.tryToGetExistingCLass("org.openmrs.module.eptssync.model.pojo.generic.GenericOpenMRSObject");
		 }
		 else
		 if (type.isOutdatedRecordsDetector()) {
			 return tableInfo.getSyncRecordClass(tableInfo.getMainApp());

		 }
		 else
		 if (type.isPhantomRecordsDetector()) {
			 return tableInfo.getSyncRecordClass(tableInfo.getMainApp());
		 }
		 
		 throw new ForbiddenOperationException("Unsupported operation type '" + type + "'");
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		CentralAndRemoteDataReconciliationSearchParams auxSearchParams = new CentralAndRemoteDataReconciliationSearchParams(this.tableInfo, this.limits, this.type, conn);
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		return count;
	}
}
