package org.openmrs.module.epts.etl.reconciliation.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationType;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class CentralAndRemoteDataReconciliationSearchParams extends SyncSearchParams<DatabaseObject> {
	
	private boolean selectAllRecords;
	
	private SyncOperationType type;
	
	public CentralAndRemoteDataReconciliationSearchParams(EtlItemConfiguration config, RecordLimits limits,
	    SyncOperationType type, Connection conn) {
		super(config, limits);
		
		this.type = type;
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = this.getSrcTableConf();
		
		if (this.type.isMissingRecordsDetector()) {
			searchClauses.addColumnToSelect(
			    "src_.record_origin_id, src_.record_uuid uuid, src_.record_uuid,  src_.record_origin_location_code");
			
			searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName() + " src_ ");
			
			if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauses(
				    "not exists (select * from person dest_ inner join patient on patient_id = person_id where dest_.uuid = src_.record_uuid)");
			} else {
				searchClauses.addToClauses(
				    "not exists (select * from " + tableInfo.getTableName() + " dest_ where dest_.uuid = src_.record_uuid)");
			}
			
			searchClauses.addToClauses("src_.consistent = 1");
		} else if (this.type.isOutdatedRecordsDetector()) {
			searchClauses.addColumnToSelect("dest_.*");
			
			searchClauses.addColumnToSelect("src_.record_origin_id, src_.record_uuid, src_.record_origin_location_code");
			
			if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauseFrom("person inner join patient dest_ on patient_id = person_id");
				searchClauses.addColumnToSelect("person.uuid");
				searchClauses.addToClauseFrom(
				    "inner join " + tableInfo.generateFullStageTableName() + " src_ on person.uuid = src_.record_uuid");
			} else {
				searchClauses.addToClauseFrom(tableInfo.getTableName() + " dest_");
				searchClauses.addToClauseFrom(
				    "inner join " + tableInfo.generateFullStageTableName() + " src_ on dest_.uuid = src_.record_uuid");
			}
			
			searchClauses.addToClauses("src_.consistent = 1");
		} else if (this.type.isPhantomRecordsDetector()) {
			searchClauses.addColumnToSelect("dest_.*");
			
			if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauseFrom("person inner join patient dest_ on patient_id = person_id");
				searchClauses.addToClauseFrom(
				    "left join " + tableInfo.generateFullStageTableName() + " src_ on person.uuid = src_.record_uuid");
				
				searchClauses.addColumnToSelect("person.uuid");
			} else {
				searchClauses.addToClauseFrom(tableInfo.getTableName() + " dest_");
				searchClauses.addToClauseFrom(
				    "left join " + tableInfo.generateFullStageTableName() + " src_ on dest_.uuid = src_.record_uuid");
			}
			
			searchClauses.addToClauses("id is null");
		} else {
			throw new ForbiddenOperationException("Operation " + this.type + " not supported!");
		}
		
		if (!this.selectAllRecords) {
			
			if (this.getLimits() != null) {
				
				if (type.isOutdatedRecordsDetector() || type.isPhantomRecordsDetector()) {
					searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				} else if (type.isMissingRecordsDetector()) {
					searchClauses.addToClauses("id between ? and ?");
				}
				
				searchClauses.addToParameters(this.getLimits().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getLimits().getCurrentLastRecordId());
			}
			
			if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
				searchClauses.addToClauses(this.getConfig().getSrcConf().getExtraConditionForExtract());
			}
		}
		
		return searchClauses;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<DatabaseObject> getRecordClass() {
		if (type.isMissingRecordsDetector()) {
			return DatabaseEntityPOJOGenerator
			        .tryToGetExistingCLass("org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject");
		} else if (type.isOutdatedRecordsDetector()) {
			return (Class<DatabaseObject>) getSrcTableConf().getSyncRecordClass(this.getConfig().getMainApp());
			
		} else if (type.isPhantomRecordsDetector()) {
			return (Class<DatabaseObject>) this.getSrcTableConf().getSyncRecordClass(this.getConfig().getMainApp());
		}
		
		throw new ForbiddenOperationException("Unsupported operation type '" + type + "'");
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		CentralAndRemoteDataReconciliationSearchParams auxSearchParams = new CentralAndRemoteDataReconciliationSearchParams(
		        this.getConfig(), this.getLimits(), this.type, conn);
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.getLimits();
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		
		return count;
	}
}
