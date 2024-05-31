package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.problems_solver.engine.GenericEngine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ProblemsSolverSearchParams extends AbstractEtlSearchParams<EtlObject> {
	
	private int savedCount;
	
	public ProblemsSolverSearchParams(EtlItemConfiguration config, RecordLimits limits,
	    GenericEngine relatedEngine) {
		super(config, limits, relatedEngine);
	}
	
	@Override
	public List<EtlObject> searchNextRecords(Connection conn) throws DBException {
		EtlObject rec = new EtlObject() {
			
			@Override
			public void setExcluded(boolean excluded) {
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
			}
			
			@Override
			public boolean isExcluded() {
				return false;
			}
			
			@Override
			public String generateTableName() {
				return null;
			}
		};
		
		if (!GenericEngine.done) {
			return utilities.parseToList(rec);
		} else {
			return null;
		}
	}
	

	
	@Override
	public SearchClauses<EtlObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<EtlObject> searchClauses = new SearchClauses<>(this);
		
		String tableName = "tmp_user";
		
		searchClauses.addToClauseFrom(tableName);
		
		searchClauses.addColumnToSelect(tableName + ".*");
		
		if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
			searchClauses.addToClauses(this.getConfig().getSrcConf().getExtraConditionForExtract());
		}
		
		searchClauses.addToClauses("processed = 0");
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		if (this.savedCount > 0)
			return this.savedCount;
		
		RecordLimits bkpLimits = this.getLimits();
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		
		this.savedCount = count;
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<EtlObject> getRecordClass() {
		try {
			return (Class<EtlObject>) GenericDatabaseObject.class.getClassLoader()
			        .loadClass("org.openmrs.module.epts.etl.problems_solver.model.TmpUserVO");
		}
		catch (ClassNotFoundException e) {
			throw new ForbiddenOperationException(e);
		}
	}

	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AbstractEtlSearchParams<EtlObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
