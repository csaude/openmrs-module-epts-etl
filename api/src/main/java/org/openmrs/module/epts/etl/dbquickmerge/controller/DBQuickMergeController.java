package org.openmrs.module.epts.etl.dbquickmerge.controller;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.dbquickmerge.engine.DBQuickMergeEngine;
import org.openmrs.module.epts.etl.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
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
	
	private AppInfo dstApp;
	
	private AppInfo srcApp;
	
	public DBQuickMergeController(ProcessController processController, EtlOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		this.srcApp = getConfiguration().find(AppInfo.init("main"));
		this.dstApp = getConfiguration().find(AppInfo.init("destination"));
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
	public long getMinRecordId(EtlItemConfiguration config) {
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
	public long getMaxRecordId(EtlItemConfiguration tableInfo) {
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
	
	private long getExtremeRecord(EtlItemConfiguration config, String function, Connection conn) throws DBException {
		if (!config.getSrcConf().getPrimaryKey().isSimpleNumericKey()) {
			throw new ForbiddenOperationException("Composite and non numeric keys are not supported for src tables");
		}
		
		DBQuickMergeSearchParams searchParams = new DBQuickMergeSearchParams(config, null, this);
		searchParams.setSyncStartDate(getConfiguration().getStartDate());
		
		SearchClauses<DatabaseObject> searchClauses = searchParams.generateSearchClauses(conn);
		
		int bkpQtyRecsPerSelect = searchClauses.getSearchParameters().getQtdRecordPerSelected();
		
		searchClauses.setColumnsToSelect(
		    function + "(src_." + config.getSrcConf().getPrimaryKey().retrieveSimpleKeyColumnName() + ") as value");
		
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
