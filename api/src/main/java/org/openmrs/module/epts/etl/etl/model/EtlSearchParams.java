package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.TableDataSourceConfig;
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
	
	public EtlSearchParams(EtlItemConfiguration config, RecordLimits limits, EtlController relatedController) {
		super(config, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray(getSrcTableConf().getTableAlias()));
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		AbstractTableConfiguration srcConfig = getSrcTableConf();
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		searchClauses.addColumnToSelect("distinct " + srcConfig.generateFullAliasedSelectColumns());
		
		if (getExtraTableDataSource() != null) {
			for (TableDataSourceConfig t : getExtraTableDataSource()) {
				searchClauses.addColumnToSelect(t.generateFullAliasedSelectColumns());
			}
		}
		
		String clauseFrom = srcConfig.generateSelectFromClauseContentOnSpecificSchema(conn);
		
		if (getExtraTableDataSource() != null) {
			
			String additionalLeftJoinFields = "";
			
			for (TableDataSourceConfig t : getExtraTableDataSource()) {
				String joinType = t.getJoinType().toString();
				
				String extraJoinQuery = t.generateJoinCondition();
				
				if (utilities.stringHasValue(extraJoinQuery)) {
					Object[] params = DBUtilities.loadParamsValues(extraJoinQuery,
					    getConfig().getRelatedSyncConfiguration());
					
					extraJoinQuery = DBUtilities.replaceSqlParametersWithQuestionMarks(extraJoinQuery);
					
					searchClauses.addToParameters(params);
				}
				
				clauseFrom = clauseFrom + " " + joinType + " join " + t.getTableName() + " " + t.getTableAlias() + " on "
				        + extraJoinQuery;
				
				if (utilities.arrayHasElement(t.getSelfJoinTables())) {
					for (AuxExtractTable aux : t.getSelfJoinTables()) {
						
						joinType = aux.getJoinType().toString();
						
						extraJoinQuery = aux.generateConditionsFields();
						
						if (utilities.stringHasValue(extraJoinQuery)) {
							Object[] params = DBUtilities.loadParamsValues(extraJoinQuery,
							    getConfig().getRelatedSyncConfiguration());
							
							extraJoinQuery = DBUtilities.replaceSqlParametersWithQuestionMarks(extraJoinQuery);
							
							searchClauses.addToParameters(params);
						}
						
						clauseFrom = clauseFrom + " " + joinType + " join " + aux.getTableName() + " " + aux.getTableAlias()
						        + " on " + extraJoinQuery;
						
						if (aux.getJoinType().isLeftJoin()) {
							additionalLeftJoinFields = utilities.concatCondition(additionalLeftJoinFields,
							    aux.getPrimaryKey().generateSqlNotNullCheckWithDisjunction(), "or");
						}
					}
					
				}
				
				if (t.getJoinType().isLeftJoin() && !utilities.arrayHasElement(t.getSelfJoinTables())) {
					additionalLeftJoinFields = utilities.concatCondition(additionalLeftJoinFields,
					    t.getPrimaryKey().generateSqlNotNullCheckWithDisjunction(), "or");
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
