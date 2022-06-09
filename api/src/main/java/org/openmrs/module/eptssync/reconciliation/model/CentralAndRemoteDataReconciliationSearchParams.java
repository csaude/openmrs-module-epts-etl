package org.openmrs.module.eptssync.reconciliation.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.GenericOpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class CentralAndRemoteDataReconciliationSearchParams extends SyncSearchParams<OpenMRSObject>{
	private boolean selectAllRecords;
	private String appCode;
	private String type;
	
	public CentralAndRemoteDataReconciliationSearchParams(SyncTableConfiguration tableInfo, String appCode, RecordLimits limits, String type, Connection conn) {
		super(tableInfo, limits);
		
		this.appCode = appCode;
		
		setOrderByFields(tableInfo.getPrimaryKey());
		
		this.type = type;
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		if (this.type.equals(SyncOperationConfig.SYNC_OPERATION_MISSING_RECORDS_DETECTOR)) {
			searchClauses.addColumnToSelect("src_.record_uuid uuid");
			
			searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName() + " src_ ");
			
			if (getTableInfo().getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauses("not exists (select * from person dest_ inner join patient on patient_id = person_id where dest_.uuid = src_.record_uuid)");
			}
			else {
				searchClauses.addToClauses("not exists (select * from " + getTableInfo().getTableName() + " where dest_.uuid = src_.record_uuid)");
			}
		}
		else
		if (this.type.equals(SyncOperationConfig.SYNC_OPERATION_OUTDATED_RECORDS_DETECTOR)) {
			searchClauses.addColumnToSelect("dest_.uuid");
			
			searchClauses.addToClauseFrom(tableInfo.getTableName() + " dest_");
			
			if (getTableInfo().getTableName().equalsIgnoreCase("patient")) {
				searchClauses.removeFromClauseFrom(tableInfo.getTableName() + " dest_");
				
				searchClauses.addToClauseFrom("person dest_ inner patient on patient_id = person_id");
			}
				
			searchClauses.addToClauseFrom("inner join " + getTableInfo().generateFullStageTableName() + " on dest_.uuid = src_.record_uuid");
			
			searchClauses.addToClauses("src_.consistent = 1");
			searchClauses.addToClauses("(dest_.date_created < src_.record_date_created " + 
												"or (dest_.date_changed is null and src_.record_date_changed is not null) " + 
													"or (dest_.date_voided is null and src_.record_date_voided is not null) " +
														"or (dest_.date_changed < src_.record_date_changed)" +
															"or (dest_.date_voided < src_.record_date_voided))");
		}
		else
		if (this.type.equals(SyncOperationConfig.SYNC_OPERATION_PHANTOM_RECORDS_DETECTOR)) {
			searchClauses.addColumnToSelect("dest_.uuid");
			
			searchClauses.addToClauseFrom(tableInfo.getTableName() + " dest_");
			
			if (getTableInfo().getTableName().equalsIgnoreCase("patient")) {
				searchClauses.removeFromClauseFrom(tableInfo.getTableName() + " dest_");
				
				searchClauses.addToClauseFrom("person dest_ inner patient on patient_id = person_id");
			}
				
			searchClauses.addToClauses("not exists (select * from " + getTableInfo().generateFullStageTableName() + " where dest_.uuid = src_.record_uuid)");
		}
		else {
			throw new ForbiddenOperationException("Operation " + this.type + " not supported!");
		}
			
		if (!this.selectAllRecords) {
			
			if (limits != null) {
				
				if (utilities.isStringIn(type, SyncOperationConfig.SYNC_OPERATION_OUTDATED_RECORDS_DETECTOR, SyncOperationConfig.SYNC_OPERATION_PHANTOM_RECORDS_DETECTOR)) {
					searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				}
				else {
					searchClauses.addToClauses("id between ? and ?");
				}
				
				searchClauses.addToParameters(this.limits.getFirstRecordId());
				searchClauses.addToParameters(this.limits.getLastRecordId());
			}
			
			if (this.tableInfo.getExtraConditionForExport() != null) {
				searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
			}
		}
		
		return searchClauses;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		return (Class<OpenMRSObject>) (new GenericOpenMRSObject()).getClass();
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		CentralAndRemoteDataReconciliationSearchParams auxSearchParams = new CentralAndRemoteDataReconciliationSearchParams(this.tableInfo, this.appCode, this.limits, this.type, conn);
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
