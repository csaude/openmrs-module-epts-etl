package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.TableDataSourceConfig;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
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
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SrcConf srcConfig = getSrcTableConf();
		
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addColumnToSelect("distinct " + srcConfig.generateFullAliasedSelectColumns() + "\n");
		
		String clauseFrom = srcConfig.generateSelectFromClauseContentOnSpecificSchema(conn);
		
		String additionalLeftJoinFields = "";
		
		if (utilities.arrayHasElement(srcConfig.getSelfJoinTables())) {
			for (AuxExtractTable aux : srcConfig.getSelfJoinTables()) {
				String joinType = aux.getJoinType().toString();
				String extraJoinQuery = aux.generateConditionsFields();
				
				if (utilities.stringHasValue(extraJoinQuery)) {
					Object[] params = DBUtilities.loadParamsValues(extraJoinQuery,
					    getConfig().getRelatedSyncConfiguration());
					
					extraJoinQuery = DBUtilities.replaceSqlParametersWithQuestionMarks(extraJoinQuery);
					
					searchClauses.addToParameters(params);
				}
				
				clauseFrom = clauseFrom + " " + joinType + " join " + aux.getTableName() + " " + aux.getTableAlias() + " on "
				        + extraJoinQuery;
				
				if (aux.getJoinType().isLeftJoin()) {
					additionalLeftJoinFields = utilities.concatCondition(additionalLeftJoinFields,
					    aux.getPrimaryKey().generateSqlNotNullCheckWithDisjunction(), "or");
				}
			}
			
		}
		
		if (srcConfig.hasExtraTableDataSourceConfig()) {
			for (TableDataSourceConfig t : getExtraTableDataSource()) {
				searchClauses.addColumnToSelect(t.generateFullAliasedSelectColumns());
				
				String joinType = t.getJoinType().toString();
				
				String extraJoinQuery = t.generateJoinCondition();
				
				if (utilities.stringHasValue(extraJoinQuery)) {
					Object[] params = DBUtilities.loadParamsValues(extraJoinQuery,
					    getConfig().getRelatedSyncConfiguration());
					
					extraJoinQuery = DBUtilities.replaceSqlParametersWithQuestionMarks(extraJoinQuery);
					
					searchClauses.addToParameters(params);
				}
				
				clauseFrom = clauseFrom + " " + joinType + " join " + t.getTableName() + " " + t.getTableAlias() + " on "
				        + extraJoinQuery + "\n";
				
				if (t.useSharedPKKey()) {
					
					ParentTable tshared = t.getSharedKeyRefInfo();
					
					clauseFrom += "LEFT join " + tshared.generateFullTableNameWithAlias(conn) + " ON "
					        + tshared.generateJoinCondition() + "\n";
				}
				
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
						
					}
					
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
