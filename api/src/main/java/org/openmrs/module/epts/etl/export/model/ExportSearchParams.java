package org.openmrs.module.epts.etl.export.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class ExportSearchParams extends SyncSearchParams<DatabaseObject> {
	
	private boolean selectAllRecords;
	
	public ExportSearchParams(EtlItemConfiguration config, RecordLimits limits, Connection conn) {
		super(config, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		String schema = DBUtilities.determineSchemaName(conn);
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addColumnToSelect("patient.*, person.uuid");
			searchClauses.addToClauseFrom("inner join " + schema + ".person on person.person_id = patient_id");
		} else {
			searchClauses.addColumnToSelect("*");
		}
		
		searchClauses.addToClauseFrom(
		    "inner join " + tableInfo.generateFullStageTableName() + " on record_origin_id  = " + tableInfo.getPrimaryKey());
		
		if (!this.selectAllRecords) {
			tryToAddLimits(searchClauses);
			tryToAddExtraConditionForExport(searchClauses);
		}
		
		searchClauses.addToClauses("consistent = 1");
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		ExportSearchParams auxSearchParams = new ExportSearchParams(getConfig(), this.getLimits(), conn);
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.getLimits();
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		
		return count;
	}
}
