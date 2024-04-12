package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.AuxiliaryExtractionSrcTable;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class EtlSearchParams extends DatabaseObjectSearchParams {
	
	private int savedCount;
	
	public EtlSearchParams(EtlConfiguration config, RecordLimits limits, EtlController relatedController) {
		super(config, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		String srcSchema = DBUtilities.determineSchemaName(conn);
		SyncTableConfiguration tableInfo = getSrcTableConf();
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		searchClauses.addColumnToSelect("distinct src_.*");
		
		String clauseFrom = srcSchema + "." + tableInfo.getTableName() + " src_ ";
		
		if (getAuxiliaryExtractionSrcTable() != null) {
			
			String additionalLeftJoinFields = "";
			
			for (AuxiliaryExtractionSrcTable t : getAuxiliaryExtractionSrcTable()) {
				String joinType = t.getJoinType().toString();
				
				String extraJoinQuery = t.generateConditionsFields();
				
				if (utilities.stringHasValue(extraJoinQuery)) {
					Object[] params = DBUtilities.loadParamsValues(extraJoinQuery,
					    getConfig().getRelatedSyncConfiguration());
					
					extraJoinQuery = DBUtilities.replaceSqlParametersWithQuestionMarks(extraJoinQuery);
					
					searchClauses.addToParameters(params);
				}
				
				clauseFrom = clauseFrom + " " + joinType + " join " + t.getTableName() + " on " + extraJoinQuery;
				
				if (t.getJoinType().isLeftJoin()) {
					additionalLeftJoinFields = utilities.concatCondition(additionalLeftJoinFields,
					    t.getPrimaryKey() + " is not null ", "or");
				}
			}
			
			if (utilities.stringHasValue(additionalLeftJoinFields)) {
				searchClauses.addToClauses(additionalLeftJoinFields);
			}
			
		}
		
		searchClauses.addToClauseFrom(clauseFrom);
		
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
