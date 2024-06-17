package org.openmrs.module.epts.etl.etl.controller;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the Etl process.
 * 
 * @author jpboane
 */
public class EtlController extends SiteOperationController<EtlDatabaseObject> {
	
	public EtlController(ProcessController processController, EtlOperationConfig operationConfig,
	    String originLocationCode) {
		super(processController, operationConfig, originLocationCode);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedEngine(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits) {
		if (getOperationConfig().getEngineClazz() != null) {
			
			Class[] parameterTypes = { Engine.class, ThreadRecordIntervalsManager.class };
			
			try {
				Constructor<TaskProcessor<? extends EtlDatabaseObject>> a = getOperationConfig().getEngineClazz()
				        .getConstructor(parameterTypes);
				
				return (TaskProcessor<EtlDatabaseObject>) a.newInstance(monitor, limits);
			}
			catch (Exception e) {
				throw new ForbiddenOperationException(e);
			}
		} else {
			return new EtlEngine(monitor, limits);
		}
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			return getExtremeRecord(engine.getEtlItemConfiguration(), "min", conn);
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
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			return getExtremeRecord(engine.getEtlItemConfiguration(), "max", conn);
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
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
	
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt,
	        Engine<EtlDatabaseObject> engine) {
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new EtlDatabaseObjectSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;
	}
	
	@Override
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
	}
	
}
