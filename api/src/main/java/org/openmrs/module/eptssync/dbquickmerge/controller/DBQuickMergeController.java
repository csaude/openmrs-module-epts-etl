package org.openmrs.module.eptssync.dbquickmerge.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.SiteOperationController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.engine.DBQuickMergeEngine;
import org.openmrs.module.eptssync.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the quick merge process. The quick merge process
 * immediately merge records from the source to the destination db This process assume that the
 * source and destination are located in the same network
 * 
 * @author jpboane
 */
public class DBQuickMergeController extends SiteOperationController {
	
	private static final int DEFAULT_NEXT_TREAD_ID = -1;
	
	private AppInfo dstConn;
	
	private AppInfo srcApp;
	
	private int currThreadStartId;
	
	private int currQtyRecords;
	
	private final String stringLock = new String("LOCK_STRING");
	
	private SyncTableConfiguration currSyncTableConf;
	
	public DBQuickMergeController(ProcessController processController, SyncOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		this.srcApp = getConfiguration().find(AppInfo.init("main"));
		this.dstConn = getConfiguration().find(AppInfo.init("destination"));
		
		this.currThreadStartId = DEFAULT_NEXT_TREAD_ID;
	}
	
	public int generateNextStartIdForThread(DBQuickMergeEngine engine, List<SyncRecord> syncRecords)
	        throws DBException, ForbiddenOperationException {
		
		synchronized (stringLock) {
			
			if (this.currSyncTableConf == null) {
				this.currSyncTableConf = engine.getSyncTableConfiguration();
			}
			
			if (!this.currSyncTableConf.equals(engine.getSyncTableConfiguration())) {
				this.currThreadStartId = DEFAULT_NEXT_TREAD_ID;
				this.currSyncTableConf = engine.getSyncTableConfiguration();
			}
			
			if (this.currThreadStartId == DEFAULT_NEXT_TREAD_ID) {
				this.currQtyRecords = syncRecords.size();
				
				OpenConnection destConn = this.openDstConnection();
				
				this.currThreadStartId = DatabaseObjectDAO.getLastRecord(engine.getSyncTableConfiguration(), destConn);
				
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
		return dstConn;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickMergeEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return getExtremeRecord(tableInfo, "min", conn);
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
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
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
	
	private long getExtremeRecord(SyncTableConfiguration tableInfo, String function, Connection conn) throws DBException {
		DBQuickMergeSearchParams searchParams = new DBQuickMergeSearchParams(tableInfo, null, this);
		searchParams.setSyncStartDate(getConfiguration().getObservationDate());
		
		SearchClauses<DatabaseObject> searchClauses = searchParams.generateSearchClauses(conn);
		
		int bkpQtyRecsPerSelect = searchClauses.getSearchParameters().getQtdRecordPerSelected();
		
		searchClauses.setColumnsToSelect(function + "(" + tableInfo.getPrimaryKey() + ") as value");
		
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
	
	public OpenConnection openDstConnection() {
		return dstConn.openConnection();
	}
}
