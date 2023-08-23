package org.openmrs.module.eptssync.dbquickmerge.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBQuickMergeSearchParams extends DatabaseObjectSearchParams {
	
	private DBQuickMergeController relatedController;
	
	private int savedCount;
	
	public DBQuickMergeSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits,
	    DBQuickMergeController relatedController) {
		super(tableInfo, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		String srcSchema = DBUtilities.determineSchemaName(conn);
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addToClauseFrom(
			    srcSchema + ".patient inner join " + srcSchema + ".person src_ on person_id = patient_id");
			searchClauses.addColumnToSelect("patient.*, src_.uuid");
		} else {
			searchClauses.addToClauseFrom(srcSchema + "." + tableInfo.getTableName() + " src_");
			
			searchClauses.addColumnToSelect("src_.*");
		}
		
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
	
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		
		if (!DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
			throw new ForbiddenOperationException("The database server must be the same to generate exlusion clause!!!");
		}
		
		return " NOT EXISTS (" + generateDestinationJoinSubquery(dstConn) + ")";
	}
	
	public String generateDestinationIntersetionClause(Connection srcConn, Connection dstConn) throws DBException {
		
		if (!DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
			throw new ForbiddenOperationException("The database server must be the same to generate exlusion clause!!!");
		}
		
		return " EXISTS (" + generateDestinationJoinSubquery(dstConn) + ")";
	}
	
	private String generateDestinationJoinSubquery(Connection dstConn) throws DBException {
		String dstSchema = DBUtilities.determineSchemaName(dstConn);
		
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
		
		dstJoinSubquery += " SELECT * ";
		dstJoinSubquery += " FROM    " + fromClause;
		dstJoinSubquery += " WHERE " + this.tableInfo.generateUniqueKeysJoinCondition("src_", "dest_");
		
		return dstJoinSubquery;
		
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
		return this.getTableInfo().getSyncRecordClass(this.relatedController.getSrcApp());
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
