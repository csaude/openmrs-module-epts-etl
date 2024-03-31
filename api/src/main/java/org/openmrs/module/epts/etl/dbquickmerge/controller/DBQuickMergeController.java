package org.openmrs.module.epts.etl.dbquickmerge.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.dbquickmerge.engine.DBQuickMergeEngine;
import org.openmrs.module.epts.etl.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the quick merge process. The quick merge process
 * immediately merge records from the source to the destination db This process assume that the
 * source and destination are located in the same network
 * 
 * @author jpboane
 */
public class DBQuickMergeController extends SiteOperationController {
	
	private static final int DEFAULT_NEXT_TREAD_ID = -1;
	
	private AppInfo dstApp;
	
	private AppInfo srcApp;
	
	private int currThreadStartId;
	
	private int currQtyRecords;
	
	private final String stringLock = new String("LOCK_STRING");
	
	private EtlConfiguration currSyncTableConf;
	
	public DBQuickMergeController(ProcessController processController, SyncOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		this.srcApp = getConfiguration().find(AppInfo.init("main"));
		this.dstApp = getConfiguration().find(AppInfo.init("destination"));
		
		this.currThreadStartId = DEFAULT_NEXT_TREAD_ID;
	}
	
	public int generateNextStartIdForThread(DBQuickMergeEngine engine, List<SyncRecord> syncRecords)
	        throws DBException, ForbiddenOperationException {
		
		synchronized (stringLock) {
			
			if (this.currSyncTableConf == null) {
				this.currSyncTableConf = engine.getEtlConfiguration();
			}
			
			if (!this.currSyncTableConf.equals(engine.getEtlConfiguration())) {
				this.currThreadStartId = DEFAULT_NEXT_TREAD_ID;
				this.currSyncTableConf = engine.getEtlConfiguration();
			}
			
			if (this.currThreadStartId == DEFAULT_NEXT_TREAD_ID) {
				this.currQtyRecords = syncRecords.size();
				
				OpenConnection destConn = this.openDstConnection();
				
				this.currThreadStartId = DatabaseObjectDAO
				        .getLastRecord(engine.getEtlConfiguration().getSrcConf(), destConn);
				
				this.currThreadStartId = this.currThreadStartId - this.currQtyRecords + 1;
			}
			
			this.currThreadStartId += this.currQtyRecords;
			this.currQtyRecords = syncRecords.size();
			
			return this.currThreadStartId;
		}
	}
	
	public static synchronized int generateNextStartIdForThread(int dbCurrId, int currThreadStartId,
	        int qtyRecordsPerProcessing) {
		if (currThreadStartId == DEFAULT_NEXT_TREAD_ID) {
			
			currThreadStartId = dbCurrId;
			
			if (currThreadStartId == 0) {
				currThreadStartId = 1 - qtyRecordsPerProcessing;
			} else {
				currThreadStartId = dbCurrId - qtyRecordsPerProcessing + 1;
			}
		}
		
		currThreadStartId += qtyRecordsPerProcessing;
		
		return currThreadStartId;
	}
	
	public static void print(int startId, int qtyRecordsPerProcessing) {
		for (int i = 0; i < qtyRecordsPerProcessing; i++) {
			System.out.println("insert into tab1(id) values (" + (startId + i) + ")");
		}
	}
	
	public static void main(String[] args) {
		int dbCurrId, currThreadStartId, next, qtyRecordsPerProcessing = 25;
		
		dbCurrId = 29;
		currThreadStartId = DEFAULT_NEXT_TREAD_ID;
		
		next = generateNextStartIdForThread(dbCurrId, currThreadStartId, qtyRecordsPerProcessing);
		
		print(next, qtyRecordsPerProcessing);
		
		currThreadStartId = next;
		
		next = generateNextStartIdForThread(dbCurrId, currThreadStartId, qtyRecordsPerProcessing);
		print(next, qtyRecordsPerProcessing);
		
	}
	
	public AppInfo getSrcApp() {
		return srcApp;
	}
	
	public AppInfo getDstApp() {
		return dstApp;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickMergeEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlConfiguration config) {
		OpenConnection conn = openConnection();
		
		try {
			return getExtremeRecord(config, "min", conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public long getMaxRecordId(EtlConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return getExtremeRecord(tableInfo, "max", conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private long getExtremeRecord(EtlConfiguration config, String function, Connection conn) throws DBException {
		DBQuickMergeSearchParams searchParams = new DBQuickMergeSearchParams(config, null, this);
		searchParams.setSyncStartDate(getConfiguration().getStartDate());
		
		SearchClauses<DatabaseObject> searchClauses = searchParams.generateSearchClauses(conn);
		
		int bkpQtyRecsPerSelect = searchClauses.getSearchParameters().getQtdRecordPerSelected();
		
		searchClauses.setColumnsToSelect(function + "(" + config.getSrcConf().getPrimaryKey() + ") as value");
		
		String sql = searchClauses.generateSQL(conn);
		
		SimpleValue simpleValue = BaseDAO.find(SimpleValue.class, sql, searchClauses.getParameters(), conn);
		
		searchClauses.getSearchParameters().setQtdRecordPerSelected(bkpQtyRecsPerSelect);
		
		if (simpleValue != null && CommonUtilities.getInstance().stringHasValue(simpleValue.getValue())) {
			return simpleValue.intValue();
		}
		
		return 0;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	public OpenConnection openSrcConnection() {
		return srcApp.openConnection();
	}
	
	public OpenConnection openDstConnection() {
		return dstApp.openConnection();
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
