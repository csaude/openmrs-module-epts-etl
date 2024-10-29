package org.openmrs.module.epts.etl.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.IdGeneratorManager;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.types.EtlDstType;
import org.openmrs.module.epts.etl.conf.types.EtlOperationType;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represent a Synchronization TaskProcessor. A Synchronization processor performes the task which
 * will end up producing or consuming the synchronization info.
 * <p>
 * There are several kinds of engines that performes diferents kind of operations. All the avaliable
 * operations are listed in {@link EtlOperationType} enum
 * 
 * @author jpboane
 */
public abstract class TaskProcessor<T extends EtlDatabaseObject> {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected Engine<T> monitor;
	
	private String processorId;
	
	protected IntervalExtremeRecord limits;
	
	protected boolean runningInConcurrency;
	
	protected EtlOperationResultHeader<T> taskResultInfo;
	
	protected List<IdGeneratorManager> idGeneratorManager;
	
	public TaskProcessor(Engine<T> monitr, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		this.monitor = monitr;
		this.limits = limits;
		this.runningInConcurrency = runningInConcurrency;
		this.taskResultInfo = new EtlOperationResultHeader<>(limits);
	}
	
	public List<IdGeneratorManager> getIdGeneratorManager() {
		return idGeneratorManager;
	}
	
	public EtlOperationResultHeader<T> getTaskResultInfo() {
		return taskResultInfo;
	}
	
	public void setTaskResultInfo(EtlOperationResultHeader<T> taskResultInfo) {
		this.taskResultInfo = taskResultInfo;
	}
	
	public boolean isRunningInConcurrency() {
		return runningInConcurrency;
	}
	
	public void setRunningInConcurrency(boolean runningInConcurrency) {
		this.runningInConcurrency = runningInConcurrency;
	}
	
	public IntervalExtremeRecord getLimits() {
		return limits;
	}
	
	public Engine<T> getEngine() {
		return monitor;
	}
	
	public String getProcessorId() {
		return processorId;
	}
	
	public void setProcessorId(String processorId) {
		this.processorId = processorId;
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
	
	public EtlItemConfiguration getEtlItemConfiguration() {
		return monitor.getEtlItemConfiguration();
	}
	
	public String getMainSrcTableName() {
		return getSrcConf().getTableName();
	}
	
	public SrcConf getSrcConf() {
		return monitor.getSrcConf();
	}
	
	public AbstractEtlSearchParams<T> getSearchParams() {
		return getEngine().getSearchParams();
	}
	
	private void addIdGeneratorManager(IdGeneratorManager im) {
		if (idGeneratorManager == null)
			idGeneratorManager = new ArrayList<>();
		
		if (!idGeneratorManager.contains(im)) {
			idGeneratorManager.add(im);
		}
	}
	
	private void tryToInitIdGenerator(List<T> etlObjects, Connection conn) throws DBException, ForbiddenOperationException {
		
		for (DstConf dst : getEtlItemConfiguration().getDstConf()) {
			
			if (dst.useManualGeneratedObjectId()) {
				
				addIdGeneratorManager(dst.initIdGenerator(this, etlObjects, conn));
			}
		}
	}
	
	public IdGeneratorManager findIdGenerator(DstConf dstConf) {
		for (IdGeneratorManager mgt : this.getIdGeneratorManager()) {
			if (mgt.getDstConf() == dstConf) {
				return mgt;
			}
		}
		
		throw new ForbiddenOperationException(
		        "No IdGeneratorManager found on processor " + getProcessorId() + " For table " + dstConf.getFullTableName());
	}
	
	@SuppressWarnings("unchecked")
	public void performe(boolean useMultiThreadSearch, Connection srcConn, Connection dstConn) throws DBException {
		
		if (getRelatedEtlOperationConfig().isDisableMultithreadingSearch()) {
			useMultiThreadSearch = false;
		}
		
		String threads = useMultiThreadSearch ? " USING MULTI-THREAD" : " USING SINGLE THREAD";
		
		if (getLimits() != null) {
			logDebug("SERCHING NEXT RECORDS FOR LIMITS " + getLimits() + threads);
		} else {
			logDebug("SERCHING NEXT RECORDS " + threads);
		}
		
		List<T> records = null;
		
		if (useMultiThreadSearch) {
			records = getSearchParams().searchNextRecordsInMultiThreads(getLimits(), srcConn, dstConn);
		} else {
			records = getSearchParams().search(getLimits(), srcConn, dstConn);
		}
		
		logDebug("SERCH NEXT MIGRATION RECORDS FOR ETL '" + this.getEtlItemConfiguration().getConfigCode() + "' ON TABLE '"
		        + getSrcConf().getTableName() + "' FINISHED. FOUND: '" + utilities.arraySize(records) + "' RECORDS.");
		
		if (utilities.arrayHasElement(records)) {
			
			this.tryToInitIdGenerator(records, dstConn);
			
			logDebug("INITIALIZING " + getRelatedOperationController().getOperationType().name().toLowerCase() + " OF '"
			        + records.size() + "' RECORDS OF TABLE '" + this.getSrcConf().getTableName() + "'");
			
			beforeSync(records, srcConn, dstConn);
			
			getTaskResultInfo().setProcessedRecords((List<EtlDatabaseObject>) records);
			
			performeEtl(records, srcConn, dstConn);
			
			logDebug("TASK ON " + records.size() + " DONE!");
		} else {
			logDebug("NO SRC RECORD FOUND FOR ETL!");
		}
	}
	
	public EtlDstType determineDstType(DstConf dstConf) {
		EtlDstType dstType = dstConf.getDstType();
		
		if (dstType == null) {
			dstType = dstConf.getSrcConf().getDstType();
		}
		
		if (dstType == null) {
			dstType = getEngine().getGlobalDstType();
		}
		
		return dstType;
	}
	
	private void beforeSync(List<T> records, Connection srcConn, Connection dstConn) {
		for (EtlObject rec : records) {
			if (rec instanceof EtlDatabaseObject) {
				((EtlDatabaseObject) rec).loadObjectIdData(getSrcConf());
			}
		}
	}
	
	@Override
	public String toString() {
		return getProcessorId() + " Limits [" + getSearchParams().getThreadRecordIntervalsManager() + "]";
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
	
	public void logTrace(String msg) {
		monitor.logTrace(msg);
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
	
	public boolean isDbDst() {
		return getRelatedEtlOperationConfig().isDbDst();
	}
	
	public boolean isJsonDst() {
		return getRelatedEtlOperationConfig().isJsonDst();
	}
	
	public boolean isDumpDst() {
		return getRelatedEtlOperationConfig().isDumpDst();
	}
	
	public boolean isCsvDst() {
		return getRelatedEtlOperationConfig().isCsvDst();
	}
	
	public boolean isFileDst() {
		return getRelatedEtlOperationConfig().isFileDst();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof TaskProcessor)) {
			return false;
		}
		
		TaskProcessor<T> e = (TaskProcessor<T>) obj;
		
		return this.getProcessorId().equals(e.getProcessorId());
	}
	
	public abstract void performeEtl(List<T> records, Connection srcConn, Connection dstConn) throws DBException;
	
	public abstract TaskProcessor<T> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits);
	
}
