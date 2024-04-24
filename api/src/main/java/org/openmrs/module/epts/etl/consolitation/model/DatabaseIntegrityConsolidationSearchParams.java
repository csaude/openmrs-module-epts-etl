package org.openmrs.module.epts.etl.consolitation.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseIntegrityConsolidationSearchParams extends SyncSearchParams<DatabaseObject> {
	
	private boolean selectAllRecords;
	
	public DatabaseIntegrityConsolidationSearchParams(EtlItemConfiguration config, RecordLimits limits, Connection conn) {
		super(config, limits);
		
		setOrderByFields(config.getSrcConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		searchClauses.addColumnToSelect(getSrcTableConf().getTableName() + ".*");
		searchClauses.addToClauseFrom(getSrcTableConf().getTableName());
		
		if (getSrcTableConf().isFromOpenMRSModel()
		        && getSrcTableConf().getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addToClauseFrom("INNER JOIN person ON person.person_id = patient.patient_id");
		}
		
		searchClauses.addToClauseFrom(
		    "INNER JOIN " + getSrcTableConf().generateFullStageTableName() + " ON record_uuid = uuid");
		
		if (!this.selectAllRecords) {
			searchClauses.addToClauses("consistent = -1");
			searchClauses.addToClauses("last_sync_date is null or last_sync_date < ?");
			searchClauses.addToParameters(this.getSyncStartDate());
			
			tryToAddLimits(searchClauses);
			
			tryToAddExtraConditionForExport(searchClauses);
		}
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		DatabaseIntegrityConsolidationSearchParams auxSearchParams = new DatabaseIntegrityConsolidationSearchParams(
		        this.getConfig(), this.getLimits(), conn);
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.getLimits();
		
		this.setLimits(null);
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		
		return count;
	}
}
