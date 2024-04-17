package org.openmrs.module.epts.etl.dbcopy.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.dbcopy.controller.DBCopyController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SearchSourceType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DBCopySearchParams extends DatabaseObjectSearchParams {
	
	private DBCopyController relatedController;
	
	private boolean forProgressMeter;
	
	public DBCopySearchParams(EtlConfiguration config, RecordLimits limits, DBCopyController relatedController) {
		super(config, limits);
		
		this.relatedController = relatedController;
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		Connection srcConn = conn;
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		String srsFullTableName = DBUtilities.determineSchemaName(conn) + ".";
		
		srsFullTableName += tableInfo.getTableName();
		
		searchClauses.addToClauseFrom(srsFullTableName + " src_");
		
		searchClauses.addColumnToSelect("*");
		
		tryToAddLimits(searchClauses);
		
		if (!forProgressMeter) {
			OpenConnection dstConn = null;
			
			try {
				dstConn = this.relatedController.openDstConnection();
				
				if (DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
					throw new ForbiddenOperationException("Rever este metodo!");
					/*
					String destFullTableName = DBUtilities.determineSchemaName(dstConn) + ".";
					
					DstConf lastMappedTable = utilities
					        .getLastRecordOnArray(getConfig().getDstConf());
					
					destFullTableName += lastMappedTable.getTableName();
					
					String srcPK = getConfig().getSrcConf().getPrimaryKey();
					String dstPK = lastMappedTable.getMappedField(srcPK);
					
					String excludeExisting = "";
					
					excludeExisting += "not exists (	select * ";
					excludeExisting += "				from " + destFullTableName + " dest_";
					excludeExisting += "				where dest_." + dstPK + " = src_." + srcPK + ")";
					
					searchClauses.addToClauses(excludeExisting);*/
				}
			}
			finally {
				if (dstConn != null) {
					dstConn.finalizeConnection();
				}
			}
			
		}
		
		tryToAddExtraConditionForExport(searchClauses);
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.getLimits();
		
		boolean bkIfForProgressMeter = this.forProgressMeter;
		
		this.forProgressMeter = true;
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		this.forProgressMeter = bkIfForProgressMeter;
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		
		OpenConnection destConn = relatedController.openDstConnection();
		
		try {
			
			DatabaseObjectSearchParams destSearchParams = new DatabaseObjectSearchParams(getConfig(), null);
			
			destSearchParams.setSearchSourceType(SearchSourceType.TARGET);
			
			int processed = destSearchParams.countAllRecords(destConn);
			
			int allRecords = countAllRecords(conn);
			
			return allRecords - processed;
		}
		finally {
			destConn.finalizeConnection();
		}
	}
}
