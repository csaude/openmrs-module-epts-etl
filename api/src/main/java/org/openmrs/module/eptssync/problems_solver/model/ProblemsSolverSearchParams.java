package org.openmrs.module.eptssync.problems_solver.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class ProblemsSolverSearchParams extends OpenMRSObjectSearchParams{
	private int savedCount;

	public ProblemsSolverSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits) {
		super(tableInfo, limits);
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
			SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
			
			String tableName = "tmp_user";
			
			searchClauses.addToClauseFrom(tableName);
			
			searchClauses.addColumnToSelect("distinct(" + tableName + ".user_uuid) uuid");
			
			searchClauses.addToGroupingFields("user_uuid");
			searchClauses.addToHavingClauses("count(*) > 1");
			
			if (this.tableInfo.getExtraConditionForExport() != null) {
				searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
			}
			
			if (utilities.stringHasValue(getExtraCondition())) {
				searchClauses.addToClauses(getExtraCondition());
			}
			
			return searchClauses;
	}
		
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		if (this.savedCount > 0) return this.savedCount; 
			
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		this.savedCount = count;
		
		return count;	
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
	
	/*@SuppressWarnings("unchecked")
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		try {
			return (Class<OpenMRSObject>) GenericOpenMRSObject.class.getClassLoader().loadClass("org.openmrs.module.eptssync.model.pojo.generic.GenericOpenMRSObject");
		}
		catch (ClassNotFoundException e) {
			throw new ForbiddenOperationException(e);
		}
	}*/
}
