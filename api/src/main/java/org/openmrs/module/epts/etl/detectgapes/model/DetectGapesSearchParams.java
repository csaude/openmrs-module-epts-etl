package org.openmrs.module.epts.etl.detectgapes.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.detectgapes.controller.DetectGapesController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class DetectGapesSearchParams extends DatabaseObjectSearchParams {
	
	private DetectGapesController relatedController;
	
	private int savedCount;
	
	public DetectGapesSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits,
	    DetectGapesController relatedController) {
		super(tableInfo, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		String srcSchema = DBUtilities.determineSchemaName(conn);
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		searchClauses.addToClauseFrom(srcSchema + "." + tableInfo.getTableName() + " src_");
		searchClauses.addColumnToSelect("src_.*");
		
		if (limits != null) {
			searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
			searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
			searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
		}
		
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
		if (this.savedCount > 0)
			return this.savedCount;
		
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		this.savedCount = count;
		
		return count;
	}
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
		return this.getTableInfo().getSyncRecordClass(this.relatedController.getDefaultApp());
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
