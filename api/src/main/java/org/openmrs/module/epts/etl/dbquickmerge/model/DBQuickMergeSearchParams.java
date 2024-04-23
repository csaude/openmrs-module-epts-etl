package org.openmrs.module.epts.etl.dbquickmerge.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.DstConf;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class DBQuickMergeSearchParams extends DatabaseObjectSearchParams {
	
	private int savedCount;
	
	public DBQuickMergeSearchParams(EtlItemConfiguration config, RecordLimits limits, DBQuickMergeController relatedController) {
		super(config, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		String srcSchema = DBUtilities.determineSchemaName(conn);
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addToClauseFrom(
			    srcSchema + ".patient inner join " + srcSchema + ".person src_ on person_id = patient_id");
			searchClauses.addColumnToSelect("patient.*, src_.uuid");
		} else {
			searchClauses.addToClauseFrom(srcSchema + "." + tableInfo.getTableName() + " src_");
			
			searchClauses.addColumnToSelect("src_.*");
		}
		
		tryToAddLimits(searchClauses);
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.replaceColumnOnClause("patient_id", "person_id");
		}
		
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
			}
			else {
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
			}
			else {
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