package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseObjectSearchParams extends SyncSearchParams<DatabaseObject>{		
		
	public DatabaseObjectSearchParams(SyncTableConfiguration tableConfiguration, RecordLimits limits){		
		super(tableConfiguration, limits);
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
			
		if (tableInfo.isFromOpenMRSModel() &&  tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addToClauseFrom("patient inner join person src_ on person_id = patient_id");
			searchClauses.addColumnToSelect("patient.*, src_.uuid");
		}
		else {
			searchClauses.addToClauseFrom(tableInfo.generateFullTableName(conn) + " src_");
			
			searchClauses.addColumnToSelect("src_.*");
		}
			
		if (limits != null) {
			searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
			searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
			searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
		}
		
		if (this.tableInfo.getExtraConditionForExport() != null) {
			searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
		}
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
	}

	@Override
	public Class<DatabaseObject> getRecordClass() {
		return this.getTableInfo().getSyncRecordClass(tableInfo.getMainApp());
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		
		return count;	
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		throw new ForbiddenOperationException("Implement this method your self");
	}
}
