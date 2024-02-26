package org.openmrs.module.epts.etl.detectgapes.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
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
	
	public DetectGapesSearchParams(EtlConfiguration config, RecordLimits limits, DetectGapesController relatedController) {
		super(config, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(getSrcTableConfiguration().getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		String srcSchema = DBUtilities.determineSchemaName(conn);
		SyncTableConfiguration tableInfo = getSrcTableConfiguration();
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		searchClauses.addToClauseFrom(srcSchema + "." + tableInfo.getTableName() + " src_");
		searchClauses.addColumnToSelect("src_.*");
		
		tryToAddLimits(searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses);
		
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
}
