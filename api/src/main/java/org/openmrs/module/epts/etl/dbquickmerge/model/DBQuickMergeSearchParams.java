package org.openmrs.module.epts.etl.dbquickmerge.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.TableDataSourceConfig;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class DBQuickMergeSearchParams extends DatabaseObjectSearchParams {
	
	private int savedCount;
	
	public DBQuickMergeSearchParams(EtlItemConfiguration config, RecordLimits limits,
	    DBQuickMergeController relatedController) {
		super(config, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		String srcSchema = DBUtilities.determineSchemaName(conn);
		AbstractTableConfiguration srcConfig = getSrcTableConf();
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		searchClauses.addColumnToSelect("distinct src_.*");
		
		if (getExtraTableDataSource() != null) {
			for (TableDataSourceConfig t : getExtraTableDataSource()) {
				for (Field f : t.getFields()) {
					if (!srcConfig.containsField(f.getName()) && !searchClauses.isToSelectColumn(f.getName())) {
						searchClauses.addColumnToSelect(t.getTableName() + "." + f.getName());
					}
				}
				
			}
		}
		
		String clauseFrom = srcSchema + "." + srcConfig.getTableName() + " src_ ";
		
		if (getExtraTableDataSource() != null) {
			
			String additionalLeftJoinFields = "";
			
			for (TableDataSourceConfig t : getExtraTableDataSource()) {
				
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
	
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		
		if (!DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
			throw new ForbiddenOperationException("The database server must be the same to generate exlusion clause!!!");
		}
		
		String extraCondition = "";
		
		for (DstConf dst : getConfig().getDstConf()) {
			
			if (!dst.hasJoinFields()) {
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
		String dstSchema = DBUtilities.determineSchemaName(dstConn);
		
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		String dstFullTableName = dstSchema + ".";
		dstFullTableName += tableInfo.getTableName();
		
		String normalFromClause;
		String patientFromClause;
		String fromClause;
		
		normalFromClause = dstFullTableName + " dest_";
		patientFromClause = dstSchema + ".patient inner join " + dstSchema + ".person dest_ on person_id = patient_id ";
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equals("patient")) {
			fromClause = patientFromClause;
		} else {
			fromClause = normalFromClause;
		}
		
		String dstJoinSubquery = "";
		String joinCondition = dstConf.generateJoinConditionWithSrc("src_", "dest_");
		
		if (utilities.stringHasValue(joinCondition)) {
			dstJoinSubquery += " SELECT * ";
			dstJoinSubquery += " FROM    " + fromClause;
			dstJoinSubquery += " WHERE " + joinCondition;
		} else {
			throw new ForbiddenOperationException("There is no join condition between the src [" + tableInfo.getTableName()
			        + "] and it destination table [" + dstConf.getTableName() + "]");
		}
		
		return dstJoinSubquery;
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
