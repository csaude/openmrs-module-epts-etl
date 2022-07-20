package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class OpenMRSObjectSearchParams extends SyncSearchParams<OpenMRSObject>{		
		
	public OpenMRSObjectSearchParams(SyncTableConfiguration tableConfiguration, RecordLimits limits){		
		super(tableConfiguration, limits);
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		
		if (tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addToClauseFrom("patient inner join person src_ on person_id = patient_id");
			searchClauses.addColumnToSelect("patient.*, src_.uuid");
		}
		else {
			searchClauses.addToClauseFrom(tableInfo.getTableName() + " src_");
			
			searchClauses.addColumnToSelect("src_.*");
		}
			
		if (limits != null) {
			searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
			searchClauses.addToParameters(this.limits.getFirstRecordId());
			searchClauses.addToParameters(this.limits.getLastRecordId());
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
	public Class<OpenMRSObject> getRecordClass() {
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
