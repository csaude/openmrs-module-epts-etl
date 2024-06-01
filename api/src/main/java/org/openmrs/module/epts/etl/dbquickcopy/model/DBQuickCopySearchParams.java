package org.openmrs.module.epts.etl.dbquickcopy.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbquickcopy.engine.DBQuickCopyEngine;
import org.openmrs.module.epts.etl.dbquickload.model.LoadedRecordsSearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DBQuickCopySearchParams extends EtlDatabaseObjectSearchParams{
	
	public DBQuickCopySearchParams(EtlItemConfiguration config, ThreadLimitsManager limits,
	    DBQuickCopyEngine relatedEngine) {
		super(config, limits, relatedEngine);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		searchClauses.addToClauseFrom(tableInfo.generateSelectFromClauseContent());
		
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
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		LoadedRecordsSearchParams syncSearchParams = new LoadedRecordsSearchParams(getConfig(), null,
		        getRelatedController().getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(conn);
		
		int allRecords = countAllRecords(conn);
		
		return allRecords - processed;
	}


	@Override
	protected AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
}
