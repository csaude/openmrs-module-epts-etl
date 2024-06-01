package org.openmrs.module.epts.etl.etl.controller;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.re_etl.engine.ReEtlEngine;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the Etl process.
 * 
 * @author jpboane
 */
public class EtlController extends SiteOperationController {
	
	private AppInfo dstApp;
	
	private AppInfo srcApp;
	
	public EtlController(ProcessController processController, EtlOperationConfig operationConfig,
	    String originLocationCode) {
		super(processController, operationConfig, originLocationCode);
		
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
	public Engine initRelatedEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		return new ReEtlEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			return getExtremeRecord(config, "min", conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@Override
	public long getMaxRecordId(EtlItemConfiguration tableInfo) {
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			return getExtremeRecord(tableInfo, "max", conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	private long getExtremeRecord(EtlItemConfiguration config, String function, Connection conn) throws DBException {
		
		if (!config.getSrcConf().getPrimaryKey().isSimpleNumericKey()) {
			throw new ForbiddenOperationException("Composite and non numeric keys are not supported for src tables");
		}
		
		SrcConf srcConfig = config.getSrcConf();
		
		String sql = "SELECT " + (function + "(" + config.getSrcConf().getTableAlias() + "."
		        + config.getSrcConf().getPrimaryKey().retrieveSimpleKeyColumnName() + ") as value");
		
		sql += " FROM " + srcConfig.generateSelectFromClauseContent() + "\n";
		
		SimpleValue simpleValue = BaseDAO.find(SimpleValue.class, sql, null, conn);
		
		if (simpleValue != null && CommonUtilities.getInstance().stringHasValue(simpleValue.getValue())) {
			return simpleValue.intValue();
		}
		
		return 0;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	public OpenConnection openSrcConnection() throws DBException {
		return srcApp.openConnection();
	}
	
	public OpenConnection openDstConnection() throws DBException {
		return dstApp.openConnection();
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
