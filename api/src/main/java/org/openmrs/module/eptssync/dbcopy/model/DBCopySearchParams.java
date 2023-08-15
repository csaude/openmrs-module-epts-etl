package org.openmrs.module.eptssync.dbcopy.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbcopy.controller.DBCopyController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBCopySearchParams extends DatabaseObjectSearchParams {
	
	private DBCopyController relatedController;
	
	private boolean forProgressMeter;
	
	public DBCopySearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, DBCopyController relatedController) {
		super(tableInfo, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		Connection srcConn = conn;
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		String srsFullTableName = DBUtilities.determineSchemaName(conn) + ".";
		
		srsFullTableName += tableInfo.getTableName();
		
		searchClauses.addToClauseFrom(srsFullTableName + " src_");
		
		searchClauses.addColumnToSelect("*");
		
		if (limits != null) {
			searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
			searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
			searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
		}
		
		if (!forProgressMeter) {
			OpenConnection dstConn = null;
			
			try {
				dstConn = this.relatedController.openDestConnection();
				
				if (DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
					String destFullTableName = DBUtilities.determineSchemaName(dstConn) + ".";
					destFullTableName += tableInfo.getMappedTableInfo().getTableName();
					
					String srcPK = getTableInfo().getPrimaryKey();
					String dstPK = tableInfo.getMappedTableInfo().getMappedField(srcPK);
					
					String excludeExisting = "";
					
					excludeExisting += "not exists (	select * ";
					excludeExisting += "				from " + destFullTableName + " dest_";
					excludeExisting += "				where dest_." + dstPK + " = src_." + srcPK + ")";
					
					searchClauses.addToClauses(excludeExisting);
				}
			}
			finally {
				if (dstConn != null) {
					dstConn.finalizeConnection();
				}
			}
			
		}
		
		if (this.tableInfo.getExtraConditionForExport() != null) {
			searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
		return this.tableInfo.getSyncRecordClass(this.relatedController.getSrcAppInfo());
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.limits;
		
		boolean bkIfForProgressMeter = this.forProgressMeter;
		
		this.forProgressMeter = true;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		this.forProgressMeter = bkIfForProgressMeter;
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		
		OpenConnection destConn = relatedController.openDestConnection();
		
		try {
			SyncTableConfiguration destTableInfo = tableInfo.getMappedTableInfo();
			
			DatabaseObjectSearchParams destSearchParams = new DatabaseObjectSearchParams(destTableInfo, null);
			
			int processed = destSearchParams.countAllRecords(destConn);
			
			int allRecords = countAllRecords(conn);
			
			return allRecords - processed;
		}
		finally {
			destConn.finalizeConnection();
		}
	}
}
