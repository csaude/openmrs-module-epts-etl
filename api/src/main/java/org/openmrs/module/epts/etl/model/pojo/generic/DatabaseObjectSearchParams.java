package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseObjectSearchParams extends SyncSearchParams<EtlDatabaseObject> {
	
	public DatabaseObjectSearchParams(EtlItemConfiguration config, RecordLimits limits) {
		super(config, limits);
	}
	
	public DatabaseObjectLoaderHelper getLoaderHealper() {
		return this.getConfig().getSrcConf().getLoadHealper();
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		AbstractTableConfiguration tbConfig = getSearchSourceType().isSource() ? getSrcTableConf()
		        : getDstLastTableConfiguration();
		
		
		searchClauses.addColumnToSelect(tbConfig.generateFullAliasedSelectColumns());
		searchClauses.addToClauseFrom(tbConfig.generateSelectFromClauseContent());
		
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
