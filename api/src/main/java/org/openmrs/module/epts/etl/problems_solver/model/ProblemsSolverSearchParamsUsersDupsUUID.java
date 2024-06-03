package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.problems_solver.engine.GenericEngine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ProblemsSolverSearchParamsUsersDupsUUID extends EtlDatabaseObjectSearchParams {
	
	public ProblemsSolverSearchParamsUsersDupsUUID(EtlItemConfiguration config, ThreadRecordIntervalsManager limits, GenericEngine relatedEngine) {
		super(config, limits, relatedEngine);
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addToClauseFrom("tmp_user");
		searchClauses.addColumnToSelect("tmp_user.user_uuid");
		searchClauses.addToGroupingFields("user_uuid");
		
		tryToAddExtraConditionForExport(searchClauses);
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		String sql = "select count(*) from tmp_user where 1 = 1";
		
		return DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn).intValue();
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		String sql = "select count(*) from tmp_user where processed = 0";
		
		return DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn).intValue();
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<EtlDatabaseObject> getRecordClass() {
		try {
			return (Class<EtlDatabaseObject>) GenericDatabaseObject.class.getClassLoader()
			        .loadClass("org.openmrs.module.epts.etl.problems_solver.model.TmpUserVO");
		}
		catch (ClassNotFoundException e) {
			throw new ForbiddenOperationException(e);
		}
	}
}
