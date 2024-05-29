package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class EtlDatabaseObjectSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	public EtlDatabaseObjectSearchParams(EtlItemConfiguration config, RecordLimits limits, EtlController relatedController) {
		super(config, limits, relatedController);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray(getSrcTableConf().getTableAlias()));
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SrcConf srcConfig = getSrcTableConf();
		
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addColumnToSelect("distinct " + srcConfig.generateFullAliasedSelectColumns() + "\n");
		
		String clauseFrom = srcConfig.generateSelectFromClauseContent();
		
		if (utilities.arrayHasElement(srcConfig.getSelfJoinTables())) {
			String additionalLeftJoinFields = "";
			
			for (AuxExtractTable aux : srcConfig.getSelfJoinTables()) {
				String joinType = aux.getJoinType().toString();
				String extraJoinQuery = aux.generateJoinConditionsFields();
				
				if (utilities.stringHasValue(extraJoinQuery)) {
					Object[] params = DBUtilities.loadParamsValues(extraJoinQuery,
					    getConfig().getRelatedSyncConfiguration());
					
					extraJoinQuery = DBUtilities.replaceSqlParametersWithQuestionMarks(extraJoinQuery);
					
					searchClauses.addToParameters(params);
				}
				
				String newLine = clauseFrom.toUpperCase().contains("JOIN") ? "\n" : "";
				
				clauseFrom = clauseFrom + " " + newLine + joinType + " join " + aux.getFullTableName() + " "
				        + aux.getTableAlias() + " on " + extraJoinQuery;
				
				if (aux.useSharedPKKey()) {
					
					ParentTable shrd = aux.getSharedTableConf();
					
					clauseFrom += "\n" + joinType + " join " + shrd.generateSelectFromClauseContent() + " on "
					        + shrd.generateJoinCondition();
				}
				
				if (aux.getJoinType().isLeftJoin()) {
					
					if (aux.getPrimaryKey() == null) {
						throw new ForbiddenOperationException("The aux table " + aux.getTableName() + " in relation "
						        + srcConfig.getTableName() + " does not have primary key");
					}
					
					additionalLeftJoinFields = utilities.concatCondition(additionalLeftJoinFields,
					    aux.getPrimaryKey().generateSqlNotNullCheckWithDisjunction(), "or");
				}
			}
			
			if (!additionalLeftJoinFields.isEmpty()) {
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
	public EtlController getRelatedController() {
		return (EtlController) super.getRelatedController();
	}
	

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}

	public DatabaseObjectLoaderHelper getLoaderHealper() {
		return this.getConfig().getSrcConf().getLoadHealper();
	}

	@Override
	protected AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		EtlDatabaseObjectSearchParams cloned = new EtlDatabaseObjectSearchParams(getConfig(), null, getRelatedController());
		
		return cloned;
	}
}
