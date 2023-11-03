package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ProblemsSolverSearchParamsUsersDupsUUID extends DatabaseObjectSearchParams{
	private int savedCount;

	public ProblemsSolverSearchParamsUsersDupsUUID(SyncTableConfiguration tableInfo, RecordLimits limits) {
		super(tableInfo, limits);
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
			SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
			
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
			return (Class<OpenMRSObject>) GenericOpenMRSObject.class.getClassLoader().loadClass("org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject");
		}
		catch (ClassNotFoundException e) {
			throw new ForbiddenOperationException(e);
		}
	}*/
}
