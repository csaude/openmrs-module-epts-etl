package org.openmrs.module.epts.etl.dbquickcopy.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.epts.etl.dbquickload.model.LoadedRecordsSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class DBQuickCopySearchParams extends SyncSearchParams<DatabaseObject> {
	
	private DBQuickCopyController relatedController;
	
	public DBQuickCopySearchParams(EtlConfiguration config, RecordLimits limits, DBQuickCopyController relatedController) {
		super(config, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(getMainSrcTableConf().getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		SyncTableConfiguration tableInfo = getMainSrcTableConf();
		
		String srsFullTableName = DBUtilities.determineSchemaName(conn) + ".";
		
		srsFullTableName += tableInfo.getTableName();
		
		searchClauses.addToClauseFrom(srsFullTableName);
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addColumnToSelect("patient.*, person.uuid");
			searchClauses.addToClauseFrom("inner join person on person.person_id = patient_id");
		} else {
			searchClauses.addColumnToSelect("*");
		}
		
		tryToAddLimits(searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses);
		
		return searchClauses;
	}
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
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
