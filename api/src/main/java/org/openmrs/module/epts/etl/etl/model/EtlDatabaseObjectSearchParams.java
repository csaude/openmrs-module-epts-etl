package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.datasource.PreparedQuery;
import org.openmrs.module.epts.etl.conf.datasource.QueryDataSourceConfig;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DbmsType;

public class EtlDatabaseObjectSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	public EtlDatabaseObjectSearchParams(Engine<EtlDatabaseObject> engine,
	    ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
		
		if (getSrcConf() != null && getSrcConf().hasPK()) {
			setOrderByFields(getSrcConf().getPrimaryKey().parseFieldNamesToArray(getSrcConf().getTableAlias()));
		}
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord intervalExtremeRecord,
	        Connection srcConn, Connection dstConn) throws DBException {
		SrcConf srcConfig = getSrcConf();
		
		AuxQueryInfo auxQueryInfo = new AuxQueryInfo(new SearchClauses<EtlDatabaseObject>(this));
		
		auxQueryInfo.addColumnToSelect("distinct " + srcConfig.generateFullAliasedSelectColumns() + "\n");
		
		auxQueryInfo.setClauseFrom(srcConfig.generateSelectFromClauseContent());
		
		if (utilities.arrayHasElement(srcConfig.getAuxExtractTable())) {
			for (AuxExtractTable aux : srcConfig.getAuxExtractTable()) {
				loadAllAuxExtractTable(auxQueryInfo, aux, srcConn);
			}
			
			if (!auxQueryInfo.getAdditionalLeftJoinFields().isEmpty()) {
				auxQueryInfo.getSearchClauses().addToClauses(auxQueryInfo.getAdditionalLeftJoinFields());
			}
		}
		
		tryToAddLimits(intervalExtremeRecord, auxQueryInfo.getSearchClauses());
		
		tryToAddExtraConditionForExport(auxQueryInfo.getSearchClauses(), DbmsType.determineFromConnection(srcConn));
		
		if (getRelatedEngine() != null && getRelatedEngine().getFinalCheckStatus().onGoing()) {
			
			if (DBUtilities.isSameDatabaseServer(srcConn, dstConn) && getConfig().hasDstWithJoinFieldsToSrc()) {
				this.setExtraCondition(this.generateDestinationExclusionClause(srcConn, dstConn));
			} else {
				throw new RuntimeException(
				        "The application cannot performe the final check as there is not join fields between the src and dst");
			}
			
		}
		
		if (utilities.stringHasValue(getExtraCondition())) {
			auxQueryInfo.getSearchClauses().addToClauses(getExtraCondition());
		}
		
		return auxQueryInfo.getSearchClauses();
	}
	
	private void loadAllAuxExtractTable(AuxQueryInfo queryInfo, MainJoiningEntity aux, Connection srcConn)
	        throws ForbiddenOperationException, DBException {
		
		loadAuxExtractTable(queryInfo, aux.parseToJoinable(), srcConn);
		
		if (aux.hasAuxExtractTable()) {
			for (JoinableEntity jEntity : aux.getJoiningTable()) {
				loadAuxExtractTable(queryInfo, jEntity, srcConn);
			}
		}
	}
	
	private void loadAuxExtractTable(AuxQueryInfo queryInfo, JoinableEntity aux, Connection srcConn)
	        throws ForbiddenOperationException, DBException {
		SrcConf srcConfig = getSrcConf();
		
		SearchClauses<EtlDatabaseObject> searchClauses = queryInfo.getSearchClauses();
		String clauseFrom = queryInfo.getClauseFrom();
		String additionalLeftJoinFields = queryInfo.getAdditionalLeftJoinFields();
		
		String joinType = aux.getJoinType().toString();
		String extraJoinQuery = aux.generateJoinConditionsFields();
		
		if (aux.getJoinExtraConditionScope().isWhereClause() && aux.hasJoinExtraCondition()) {
			searchClauses.addToClauses(aux.getJoinExtraCondition());
		}
		
		if (utilities.stringHasValue(extraJoinQuery)) {
			PreparedQuery pQ = PreparedQuery.prepare(QueryDataSourceConfig.fastCreate(extraJoinQuery, getSrcConf()),
			    getConfig().getRelatedEtlConf(), true, DbmsType.determineFromConnection(srcConn));
			
			List<Object> paramsAsList = pQ.generateQueryParameters();
			
			Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
			
			extraJoinQuery = pQ.generatePreparedQuery();
			
			searchClauses.addToParameters(params);
		}
		
		String newLine = clauseFrom.toUpperCase().contains("JOIN") ? "\n" : "";
		
		clauseFrom = clauseFrom + " " + newLine + joinType + " join " + aux.getFullTableName() + " " + aux.getTableAlias()
		        + " on " + extraJoinQuery;
		
		if (aux.useSharedPKKey()) {
			
			ParentTable shrd = aux.getSharedTableConf();
			
			clauseFrom += "\n" + joinType + " join " + shrd.generateSelectFromClauseContent() + " on "
			        + shrd.generateJoinCondition();
		}
		
		if (aux.getMainExtractTable().getJoiningTable().size() > 1 && aux.getJoinType().isLeftJoin()) {
			if (aux.getPrimaryKey() == null) {
				throw new ForbiddenOperationException("The aux table " + aux.getTableName() + " in relation "
				        + srcConfig.getTableName() + " does not have primary key");
			}
			
			additionalLeftJoinFields = utilities.concatCondition(additionalLeftJoinFields,
			    aux.getPrimaryKey().generateSqlNotNullCheckWithDisjunction(), "or");
		}
		
		if (!aux.doNotUseAsDatasource()) {
			String fullSelectColumns = queryInfo.getColumnsToSelect() + ","
			        + ((TableConfiguration) aux).generateFullAliasedSelectColumns();
			
			queryInfo.setColumnsToSelect(fullSelectColumns);
		}
		
		queryInfo.setSearchClauses(searchClauses);
		queryInfo.setClauseFrom(clauseFrom);
		queryInfo.setAdditionalLeftJoinFields(additionalLeftJoinFields);
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
		return this.getSrcConf().getLoadHealper();
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
		
		return extraCondition.isEmpty() ? extraCondition : (extraCondition + ")");
		
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
		
		AbstractTableConfiguration srcTabConf = getSrcConf();
		
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
