package org.openmrs.module.epts.etl.dbquickcopy.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.epts.etl.dbquickload.model.LoadedRecordsSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DBQuickCopySearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	private DBQuickCopyController relatedController;
	
	public DBQuickCopySearchParams(EtlItemConfiguration config, RecordLimits limits,
	    DBQuickCopyController relatedController) {
		super(config, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		searchClauses.addToClauseFrom(tableInfo.generateSelectFromClauseContentOnSpecificSchema(conn));
		
		searchClauses.addColumnToSelect(tableInfo.generateFullAliasedSelectColumns());
		
		tryToAddLimits(searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses);
		
		return searchClauses;
	}
	
	@Override
	public Class<EtlDatabaseObject> getRecordClass() {
		return DatabaseEntityPOJOGenerator
		        .tryToGetExistingCLass("org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject");
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = super.getLimits();
		
		super.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		super.setLimits(bkpLimits);
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		LoadedRecordsSearchParams syncSearchParams = new LoadedRecordsSearchParams(getConfig(), null,
		        relatedController.getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(conn);
		
		int allRecords = countAllRecords(conn);
		
		return allRecords - processed;
	}
}
