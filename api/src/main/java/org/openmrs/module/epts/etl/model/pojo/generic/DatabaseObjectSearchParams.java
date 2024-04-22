package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseObjectSearchParams extends SyncSearchParams<DatabaseObject> {
	
	private DatabaseObjectLoaderHelper loaderHealper;
	
	public DatabaseObjectSearchParams(EtlConfiguration config, RecordLimits limits) {
		super(config, limits);
		
		this.loaderHealper = new DatabaseObjectLoaderHelper(config.getSrcConf());
	}
	
	public DatabaseObjectLoaderHelper getLoaderHealper() {
		return loaderHealper;
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		AbstractTableConfiguration tbConfig = getSearchSourceType().isSource() ? getSrcTableConf()
		        : getDstLastTableConfiguration();
		
		if (tbConfig.isFromOpenMRSModel() && tbConfig.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addToClauseFrom("patient inner join person src_ on person_id = patient_id");
			searchClauses.addColumnToSelect("patient.*, src_.uuid");
		} else {
			searchClauses.addToClauseFrom(tbConfig.generateFullTableName(conn) + " src_");
			
			searchClauses.addColumnToSelect("src_.*");
		}
		
		tryToAddLimits(searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses);
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.getLimits();
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		
		return count;
	}
	
	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		throw new ForbiddenOperationException("Implement this method your self");
	}
}
