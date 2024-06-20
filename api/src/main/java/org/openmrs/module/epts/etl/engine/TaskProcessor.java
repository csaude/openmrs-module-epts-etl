package org.openmrs.module.epts.etl.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represent a Synchronization TaskProcessor. A Synchronization engine performes the task which will
 * end up producing or consuming the synchronization info.
 * <p>
 * There are several kinds of engines that performes diferents kind of operations. All the avaliable
 * operations are listed in {@link EtlOperationType} enum
 * 
 * @author jpboane
 */
public abstract class TaskProcessor<T extends EtlDatabaseObject> {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected Engine<T> monitor;
	
	private String engineId;
	
	protected IntervalExtremeRecord limits;
	
	public TaskProcessor(Engine<T> monitr, IntervalExtremeRecord limits) {
		this.monitor = monitr;
		this.limits = limits;
	}
	
	public IntervalExtremeRecord getLimits() {
		return limits;
	}
	
	public Engine<T> getMonitor() {
		return monitor;
	}
	
	public String getEngineId() {
		return engineId;
	}
	
	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}
	
	public OperationController<T> getRelatedOperationController() {
		return this.monitor.getController();
	}
	
	public EtlOperationConfig getRelatedEtlOperationConfig() {
		return getRelatedOperationController().getOperationConfig();
	}
	
	public EtlConfiguration getRelatedEtlConfiguration() {
		return getRelatedOperationController().getEtlConfiguration();
	}
	
	public EtlItemConfiguration getEtlConfiguration() {
		return monitor.getEtlItemConfiguration();
	}
	
	public String getMainSrcTableName() {
		return getSrcConf().getTableName();
	}
	
	public SrcConf getSrcConf() {
		return monitor.getSrcConf();
	}
	
	public AbstractEtlSearchParams<T> getSearchParams() {
		return getMonitor().getSearchParams();
	}
	
	public EtlOperationResultHeader<T> performe(Connection ownSrcConn, Connection ownDstCon) throws DBException {
		if (getLimits() != null) {
			logDebug("SERCHING NEXT RECORDS FOR LIMITS " + getLimits());
		} else {
			logDebug("SERCHING NEXT RECORDS");
		}
		
		List<T> records = getSearchParams().search(getLimits(), ownSrcConn, ownDstCon);
		
		logDebug("SERCH NEXT MIGRATION RECORDS FOR ETL '" + this.getEtlConfiguration().getConfigCode() + "' ON TABLE '"
		        + getSrcConf().getTableName() + "' FINISHED. FOUND: '" + utilities.arraySize(records) + "' RECORDS.");
		
		EtlOperationResultHeader<T> r = null;
		
		if (utilities.arrayHasElement(records)) {
			logDebug("INITIALIZING " + getRelatedOperationController().getOperationType().name().toLowerCase() + " OF '"
			        + records.size() + "' RECORDS OF TABLE '" + this.getSrcConf().getTableName() + "'");
			
			beforeSync(records, ownSrcConn, ownDstCon);
			
			r = performeSync(records, ownSrcConn, ownDstCon);
		}
		
		refreshProgressMeter(r.countAllSuccessfulyProcessedRecords(), ownSrcConn);
		
		reportProgress();
		
		return r;
		
	}
	
	private void beforeSync(List<T> records, Connection srcConn, Connection dstConn) {
		for (EtlObject rec : records) {
			if (rec instanceof EtlDatabaseObject) {
				((EtlDatabaseObject) rec).loadObjectIdData(getSrcConf());
			}
		}
	}
	
	public void reportProgress() {
		this.monitor.reportProgress();
	}
	
	@Override
	public String toString() {
		return getEngineId() + " Limits [" + getSearchParams().getThreadRecordIntervalsManager() + "]";
	}
	
	public void logError(String msg) {
		monitor.logErr(msg);
	}
	
	public void logInfo(String msg) {
		monitor.logInfo(msg);
	}
	
	public void logDebug(String msg) {
		monitor.logDebug(msg);
	}
	
	public void logWarn(String msg) {
		monitor.logWarn(msg);
	}
	
	public void logWarn(String msg, long interval) {
		monitor.logWarn(msg, interval);
	}
	
	public boolean writeOperationHistory() {
		return getRelatedEtlOperationConfig().writeOperationHistory();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof TaskProcessor)) {
			return false;
		}
		
		TaskProcessor<T> e = (TaskProcessor<T>) obj;
		
		return this.getEngineId().equals(e.getEngineId());
	}
	
	public abstract EtlOperationResultHeader<T> performeSync(List<T> records, Connection srcConn, Connection dstConn)
	        throws DBException;
}
