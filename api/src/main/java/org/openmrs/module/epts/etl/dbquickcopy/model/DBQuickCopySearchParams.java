package org.openmrs.module.epts.etl.dbquickcopy.model;

import java.sql.Connection;

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
	
	public DBQuickCopySearchParams(SyncTableConfiguration tableInfo, RecordLimits limits,
	    DBQuickCopyController relatedController) {
		super(tableInfo, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		String srsFullTableName = DBUtilities.determineSchemaName(conn) + ".";
		
		srsFullTableName += tableInfo.getTableName();
		
		searchClauses.addToClauseFrom(srsFullTableName);
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addColumnToSelect("patient.*, person.uuid");
			searchClauses.addToClauseFrom("inner join person on person.person_id = patient_id");
		} else {
			searchClauses.addColumnToSelect("*");
		}
		
		if (limits != null) {
			searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
			searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
			searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
		}
		
		if (this.tableInfo.getExtraConditionForExport() != null) {
			searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
		return DatabaseEntityPOJOGenerator
		        .tryToGetExistingCLass("org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject");
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
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		LoadedRecordsSearchParams syncSearchParams = new LoadedRecordsSearchParams(tableInfo, null,
		        relatedController.getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(conn);
		
		int allRecords = countAllRecords(conn);
		
		return allRecords - processed;
	}
}
