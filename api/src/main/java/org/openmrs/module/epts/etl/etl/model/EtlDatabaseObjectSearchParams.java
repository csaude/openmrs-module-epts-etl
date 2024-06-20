package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class EtlDatabaseObjectSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	public EtlDatabaseObjectSearchParams(Engine<EtlDatabaseObject> engine, ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray(getSrcTableConf().getTableAlias()));
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord intervalExtremeRecord,
	        Connection srcConn, Connection dstConn) throws DBException {
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
		
		tryToAddLimits(intervalExtremeRecord, searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses);
		
		if (getRelatedEngine() != null && getRelatedEngine().getFinalCheckStatus().onGoing()) {
			
			if (DBUtilities.isSameDatabaseServer(srcConn, dstConn) && getConfig().hasDstWithJoinFieldsToSrc()) {
				this.setExtraCondition(this.generateDestinationExclusionClause(srcConn, dstConn));
			} else {
				throw new RuntimeException(
				        "The application cannot performe the final check as there is not join fields between the src and dst");
			}
			
		}
		
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
	public AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		EtlDatabaseObjectSearchParams cloned = new EtlDatabaseObjectSearchParams(getRelatedEngine(), null);
		
		return cloned;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		
		if (!DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
			throw new ForbiddenOperationException("The database server must be the same to generate exlusion clause!!!");
		}
		
		String extraCondition = "";
		
		for (DstConf dst : getConfig().getDstConf()) {
			
			if (!dst.hasJoinFields() || getConfig().getSrcConf().hasRequiredExtraDataSource()) {
				continue;
			}
			
			if (!extraCondition.isEmpty()) {
				extraCondition = " OR ";
			} else {
				extraCondition = "(";
			}
			
			extraCondition += " NOT EXISTS (" + generateDestinationJoinSubquery(dst, dstConn) + ")";
		}
		
		return extraCondition + ")";
		
	}
	
	public String generateDestinationIntersetionClause(Connection srcConn, Connection dstConn) throws DBException {
		
		if (!DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
			throw new ForbiddenOperationException("The database server must be the same to generate exlusion clause!!!");
		}
		
		String extraCondition = "";
		
		for (DstConf dst : getConfig().getDstConf()) {
			
			if (!dst.hasJoinFields()) {
				continue;
			}
			
			if (!extraCondition.isEmpty()) {
				extraCondition = " AND ";
			} else {
				extraCondition = "(";
			}
			
			extraCondition += " EXISTS (" + generateDestinationJoinSubquery(dst, dstConn) + ")";
		}
		
		return extraCondition += ")";
	}
	
	private String generateDestinationJoinSubquery(DstConf dstConf, Connection dstConn) throws DBException {
		
		AbstractTableConfiguration srcTabConf = getSrcTableConf();
		
		String fromClause = dstConf.generateSelectFromClauseContent();
		
		String dstJoinSubquery = "";
		String joinCondition = dstConf.generateJoinConditionWithSrc();
		
		if (utilities.stringHasValue(joinCondition)) {
			dstJoinSubquery += " SELECT * ";
			dstJoinSubquery += " FROM    " + fromClause;
			dstJoinSubquery += " WHERE " + joinCondition;
		} else {
			throw new ForbiddenOperationException("There is no join condition between the src [" + srcTabConf.getTableName()
			        + "] and it destination table [" + dstConf.getTableName() + "]");
		}
		
		return dstJoinSubquery;
	}
	
}
