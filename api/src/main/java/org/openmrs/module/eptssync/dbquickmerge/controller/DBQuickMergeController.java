package org.openmrs.module.eptssync.dbquickmerge.controller;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.SiteOperationController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.engine.DBQuickMergeEngine;
import org.openmrs.module.eptssync.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.eptssync.dbquickmerge.model.MergeType;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the quick merge process. The quick merge process
 * immediately merge records from the source to the destination db This process assume that the
 * source and destination are located in the same network
 * 
 * @author jpboane
 */
public class DBQuickMergeController extends SiteOperationController {
	
	private AppInfo dstConn;
	
	private AppInfo srcApp;
	
	public DBQuickMergeController(ProcessController processController, SyncOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		this.srcApp = getConfiguration().find(AppInfo.init("main"));
		this.dstConn = getConfiguration().find(AppInfo.init("destination"));
	}
	
	public MergeType getMergeType() {
		if (getOperationConfig().isDBQuickMergeExistingRecords())
			return MergeType.EXISTING;
		if (getOperationConfig().isDBQuickMergeMissingRecords())
			return MergeType.MISSING;
		
		throw new ForbiddenOperationException("Not supported operation '" + getOperationConfig().getDesignation() + "'");
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
		//Try to skip merge of existing records if there is no info for winning records
		if (getOperationConfig().isDBQuickMergeExistingRecords()) {
			
			boolean existWinningRecInfo = utilities().arrayHasElement(tableInfo.getWinningRecordFieldsInfo());
			boolean existObservationDateFields = utilities().arrayHasElement(tableInfo.getObservationDateFields());
			
			if (!existWinningRecInfo && !existObservationDateFields) {
				return 0;
			}
			
			Connection srcConn = conn;
			OpenConnection dstConn = getDstApp().openConnection();
			
			try {
				if (!DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
					return 0;
				}
			}
			finally {
				dstConn.finalizeConnection();
			}
			
		}
		
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
