package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.types.EtlDstType;
import org.openmrs.module.epts.etl.conf.types.EtlOperationType;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.EtlProgressMeter;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.OperationProgressInfo;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represent a controller of an synchronization operation. Eg. Export data from tables to
 * JSON files.
 * 
 * @author jpboane
 */
public abstract class OperationController<T extends EtlDatabaseObject> implements Controller {
	
	protected EptsEtlLogger logger;
	
	protected ProcessController processController;
	
	protected List<Engine<T>> enginesActivititieMonitor;
	
	protected List<Engine<T>> allGeneratedEngineMonitor;
	
	protected List<OperationController<? extends EtlDatabaseObject>> children;
	
	protected String controllerId;
	
	protected int operationStatus;
	
	protected boolean stopRequested;
	
	protected EtlOperationConfig operationConfig;
	
	protected OperationController<? extends EtlDatabaseObject> parent;
	
	protected TimeController timer;
	
	protected boolean selfTreadKilled;
	
	protected Exception lastException;
	
	protected OperationProgressInfo progressInfo;
	
	private List<EtlItemConfiguration> finalizedItems;
	
	public OperationController(ProcessController processController, EtlOperationConfig operationConfig) {
		this.logger = new EptsEtlLogger(OperationController.class);
		
		this.processController = processController;
		this.operationConfig = operationConfig;
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		
		this.controllerId = (getDstType() + "_" + getOperationType().name().toLowerCase() + "_on_"
		        + processController.getControllerId()).toLowerCase();
		
		OpenConnection conn = null;
		try {
			conn = openSrcConnection();
			
			this.progressInfo = this.processController.initOperationProgressMeter(this, conn);
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
		
	}
	
	private List<EtlItemConfiguration> getFinalizedItems() {
		return finalizedItems;
	}
	
	private void setFinalizedItems(List<EtlItemConfiguration> finalizedItems) {
		this.finalizedItems = finalizedItems;
	}
	
	public EtlDstType getDstType() {
		return getOperationConfig().getDstType();
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return getEtlConfiguration().getSrcConnInfo();
	}
	
	public DBConnectionInfo getDstConnInfo() {
		return getEtlConfiguration().getDstConnInfo();
	}
	
	public void resetProgressInfo(Connection conn) throws DBException {
		if (this.progressInfo != null) {
			this.progressInfo.reset(conn);
		}
		
		this.progressInfo = this.processController.initOperationProgressMeter(this, conn);
	}
	
	public OperationProgressInfo getProgressInfo() {
		return progressInfo;
	}
	
	public EptsEtlLogger getLogger() {
		return logger;
	}
	
	public OperationController<? extends EtlDatabaseObject> getParentConf() {
		return parent;
	}
	
	public boolean hasParent() {
		return this.parent != null;
	}
	
	public boolean hasChild() {
		return this.children != null;
	}
	
	public boolean hasNestedController() {
		return hasChild() || hasParent();
	}
	
	public void setParent(OperationController<? extends EtlDatabaseObject> parent) {
		this.parent = parent;
	}
	
	public EtlOperationConfig getOperationConfig() {
		return operationConfig;
	}
	
	public List<OperationController<? extends EtlDatabaseObject>> getChildren() {
		return children;
	}
	
	public void setChildren(List<OperationController<? extends EtlDatabaseObject>> children) {
		this.children = children;
	}
	
	public ProcessController getProcessController() {
		return processController;
	}
	
	public boolean isParallelModeProcessing() {
		return this.getOperationConfig().isParallelModeProcessing();
	}
	
	public List<Engine<T>> getAllGeneratedEngineMonitor() {
		return allGeneratedEngineMonitor;
	}
	
	public void setAllGeneratedEngineMonitor(List<Engine<T>> allGeneratedEngineMonitor) {
		this.allGeneratedEngineMonitor = allGeneratedEngineMonitor;
	}
	
	private synchronized void runInSequencialMode() throws DBException {
		changeStatusToRunning();
		
		List<EtlItemConfiguration> allSync = getProcessController().getConfiguration().getEtlItemConfiguration();
		
		this.enginesActivititieMonitor = new ArrayList<Engine<T>>();
		
		logTrace("Running the Process in Sequencial mode!");
		
		for (EtlItemConfiguration config : allSync) {
			if (config.isDisabled()) {
				logDebug(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Confinguration '"
				        + config.getConfigCode() + "' is disabled! Skipping...").toUpperCase());
				
				continue;
			} else if (operationTableIsAlreadyFinished(config)) {
				logDebug(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Confinguration '"
				        + config.getConfigCode() + "' was already finished!").toUpperCase());
			} else if (stopRequested()) {
				logWarn("ABORTING THE ENGINE PROCESS DUE STOP REQUESTED!");
				break;
			} else {
				
				logInfo(("Starting operation '" + getOperationType().name().toLowerCase() + "' On Etl Confinguration '"
				        + config.getConfigCode() + "'").toUpperCase());
				
				if (!config.isFullLoaded()) {
					try {
						logDebug("Performing the full load of etl item configuration");
						
						config.fullLoad(this.getOperationConfig());
					}
					catch (DBException e) {
						e.printStackTrace();
						
						throw new RuntimeException(e);
					}
				}
				
				TableOperationProgressInfo progressInfo = null;
				
				try {
					progressInfo = this.progressInfo.retrieveProgressInfo(config);
				}
				catch (NullPointerException e) {
					logErr("Error on thread " + this.getControllerId()
					        + ": Progress meter not found for Etl Confinguration [" + config.getConfigCode() + "].");
					
					e.printStackTrace();
					
					throw e;
				}
				
				if (this.progressInfo.getItemsProgressInfo() == null) {
					progressInfo = this.progressInfo.retrieveProgressInfo(config);
				}
				
				Engine<T> engine = Engine.init(this, config, progressInfo);
				
				logTrace("Opening connection for saving Progress Info");
				
				OpenConnection conn = getDefaultConnInfo().openConnection();
				
				try {
					if (isResumable()) {
						logTrace("Saving Progress Info....");
						
						progressInfo.save(conn);
						
						logTrace("Progress Info Saved!");
						
					}
					conn.markAsSuccessifullyTerminated();
				}
				catch (DBException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				finally {
					conn.finalizeConnection();
				}
				
				this.enginesActivititieMonitor.add(engine);
				
				engine.run();
				
				if (stopRequested() && engine.isStopped()) {
					logInfo(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Configuration '"
					        + config.getConfigCode() + "' is stopped successifuly!").toUpperCase());
					break;
				} else {
					logInfo(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Configuration '"
					        + config.getConfigCode() + "' is finished!").toUpperCase());
					
					if (getOperationConfig().isRunOnce()) {
						break;
					}
				}
			}
		}
		
		if (!stopRequested()) {
			markAsFinished();
		} else {
			changeStatusToStopped();
		}
	}
	
	private synchronized void runInParallelMode() throws DBException {
		this.enginesActivititieMonitor = new ArrayList<>();
		
		logInfo("Starting operations in parallel");
		
		List<EtlItemConfiguration> avaliableItems = this.determineAvaliableItems();
		
		this.getOperationConfig().recalculateThreads(avaliableItems);
		
		for (EtlItemConfiguration config : avaliableItems) {
			if (operationTableIsAlreadyFinished(config)) {
				logDebug(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Configuration '"
				        + config.getConfigCode() + "' was already finished!").toUpperCase());
			} else if (stopRequested()) {
				logWarn("ABORTING THE ENGINE INITIALIZER DUE STOP REQUESTED!");
				
				break;
			} else {
				logInfo("INITIALIZING '" + getOperationType().name().toLowerCase() + "' ENGINE FOR ETL CONFIGURATION '"
				        + config.getConfigCode().toUpperCase() + "'");
				
				if (!config.isFullLoaded()) {
					try {
						logDebug("Performing the full load of etl item configuration");
						
						config.fullLoad(this.getOperationConfig());
					}
					catch (DBException e) {
						throw new RuntimeException(e);
					}
				}
				
				TableOperationProgressInfo progressInfo = null;
				
				try {
					progressInfo = this.progressInfo.retrieveProgressInfo(config);
				}
				catch (NullPointerException e) {
					logErr("Error on thread " + this.getControllerId()
					        + ": Progress meter not found for Etl Confinguration [" + config.getConfigCode() + "].");
					
					e.printStackTrace();
					
					throw e;
				}
				
				if (this.progressInfo.getItemsProgressInfo() == null) {
					progressInfo = this.progressInfo.retrieveProgressInfo(config);
				}
				
				Engine<T> engine = Engine.init(this, config, progressInfo);
				
				OpenConnection conn = getDefaultConnInfo().openConnection();
				
				try {
					if (isResumable()) {
						logTrace("Saving Progress Info....");
						
						progressInfo.save(conn);
						
						logTrace("Progress Info Saved!");
						
					}
					conn.markAsSuccessifullyTerminated();
				}
				catch (DBException e) {
					throw new RuntimeException(e);
				}
				finally {
					conn.finalizeConnection();
				}
				
				startAndAddToEnginesActivititieMonitor(engine);
			}
		}
		
		changeStatusToRunning();
	}
	
	private List<EtlItemConfiguration> determineAvaliableItems() {
		List<EtlItemConfiguration> allSync = getProcessController().getConfiguration().getEtlItemConfiguration();
		
		logDebug("Determine finalized operations...");
		
		List<EtlItemConfiguration> avaliableItems = new ArrayList<>();
		
		for (EtlItemConfiguration config : allSync) {
			if (operationTableIsAlreadyFinished(config)) {
				logDebug(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Configuration '"
				        + config.getConfigCode() + "' was already finished!").toUpperCase());
				
				this.addItemToFinalized(config);
			} else {
				avaliableItems.add(config);
			}
		}
		
		return avaliableItems;
	}
	
	public boolean operationTableIsAlreadyFinished(EtlItemConfiguration etlConfig) {
		try {
			TableOperationProgressInfo tableOpPm = retrieveProgressInfo(etlConfig);
			
			if (tableOpPm == null) {
				logWarn("No Table Operation Info found for [" + etlConfig.getConfigCode() + "]");
			} else {
				EtlProgressMeter sPm = tableOpPm.getProgressMeter();
				
				if (sPm == null) {
					logWarn("The progress meter for etl configuration is not exists [" + etlConfig.getConfigCode() + "]");
				} else {
					return sPm.isFinished();
				}
			}
		}
		catch (Exception e) {}
		
		return false;
	}
	
	public boolean operationIsAlreadyFinished() {
		
		for (EtlItemConfiguration config : getEtlItemConfiguration()) {
			if (!operationTableIsAlreadyFinished(config)) {
				return false;
			}
		}
		
		return true;
		
	}
	
	public String getControllerId() {
		return controllerId;
	}
	
	public List<Engine<T>> getEnginesActivititieMonitor() {
		return enginesActivititieMonitor;
	}
	
	@JsonIgnore
	public CommonUtilities utilities() {
		return CommonUtilities.getInstance();
	}
	
	private void startAndAddToEnginesActivititieMonitor(Engine<T> activitityMonitor) {
		this.enginesActivititieMonitor.add(activitityMonitor);
		
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(activitityMonitor.getEngineId())
		        .execute(activitityMonitor);
	}
	
	@Override
	public String toString() {
		return this.controllerId;
	}
	
	@Override
	public void run() {
		try {
			
			logDebug("Starting Processs...");
			
			timer = new TimeController();
			timer.start();
			
			onStart();
			
			if (stopRequested()) {
				logWarn("THE OPERATION " + getControllerId() + " COULD NOT BE INITIALIZED DUE STOP REQUESTED!!!!");
				
				changeStatusToStopped();
				
				if (hasChild()) {
					for (OperationController<? extends EtlDatabaseObject> child : getChildren()) {
						child.requestStop();
					}
				}
			} else if (operationIsAlreadyFinished()) {
				logWarn("THE OPERATION " + getControllerId() + " WAS ALREADY FINISHED!");
				
				changeStatusToFinished();
			} else {
				if (isParallelModeProcessing()) {
					runInParallelMode();
				} else {
					runInSequencialMode();
				}
			}
			
			boolean running = true;
			
			while (running) {
				TimeCountDown.sleep(getWaitTimeToCheckStatus());
				
				if (this.isFinished()) {
					this.markAsFinished();
					this.onFinish();
					
					running = false;
				} else if (this.isStopped()) {
					running = false;
					
					this.onStop();
				}
				
				if (getOperationConfig().isParallelModeProcessing()) {
					
					if (this.enginesActivititieMonitor != null) {
						
						int qty = 0;
						
						String msg = "\nRUNNING ITEMS...\n";
						
						msg += "----------------------------------------\n";
						
						for (Engine<T> engine : this.enginesActivititieMonitor) {
							
							if (engine.isRunning()) {
								qty++;
								
								msg += qty + "." + engine.getEtlConfigCode() + "\n";
							}
						}
						
						msg += "----------------------------------------";
						
						logWarn(msg, 60);
					}
				}
				
			}
		}
		catch (Exception e) {
			this.requestStopDueError(null, e);
		}
	}
	
	@Override
	public TimeController getTimer() {
		return this.timer;
	}
	
	@Override
	public boolean stopRequested() {
		return this.stopRequested;
	}
	
	public boolean isInitialized() {
		return this.operationStatus != MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	@Override
	public boolean isNotInitialized() {
		return this.operationStatus == MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	@Override
	public boolean isRunning() {
		return this.operationStatus == MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public boolean isStopped() {
		if (isNotInitialized())
			return false;
		
		if (isParallelModeProcessing() && this.enginesActivititieMonitor != null) {
			for (Engine<T> monitor : this.enginesActivititieMonitor) {
				if (!monitor.isStopped()) {
					return false;
				}
			}
			
			return true;
		}
		
		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		if (isNotInitialized()) {
			return false;
		}
		
		if (isParallelModeProcessing() && this.enginesActivititieMonitor != null) {
			for (Engine<T> monitor : this.enginesActivititieMonitor) {
				if (!monitor.isFinished()) {
					return false;
				}
			}
			
			return true;
		} else {
			return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
		}
	}
	
	public synchronized void markTableOperationAsFinished(EtlItemConfiguration conf) throws DBException {
		
		logDebug("FINISHING OPERATION ON TABLE " + conf.getConfigCode().toUpperCase());
		
		TableOperationProgressInfo progressInfo = this.retrieveProgressInfo(conf);
		
		progressInfo.getProgressMeter().changeStatusToFinished();
		
		if (isResumable()) {
			OpenConnection conn = getDefaultConnInfo().openConnection();
			
			try {
				progressInfo.save(conn);
				conn.markAsSuccessifullyTerminated();
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
		}
	}
	
	public EtlConfiguration getEtlConfiguration() {
		return this.getProcessController().getConfiguration();
	}
	
	public List<EtlItemConfiguration> getEtlItemConfiguration() {
		return getEtlConfiguration().getEtlItemConfiguration();
	}
	
	public File generateTableProcessStatusFile_(AbstractTableConfiguration conf) {
		String operationId = this.getControllerId() + "_" + conf.getTableName();
		
		String fileName = generateOperationStatusFolder() + FileUtilities.getPathSeparator() + operationId;
		
		return new File(fileName);
	}
	
	public File generateOperationStatusFile() {
		return new File(generateOperationStatusFolder() + FileUtilities.getPathSeparator() + getControllerId());
	}
	
	public String generateOperationStatusFolder() {
		String rootFolder = getProcessController().getProcessInfo().generateProcessStatusFolder();
		
		String subFolder = "";
		
		if (operationConfig.getRelatedEtlConfig().isSupposedToRunInOrigin()) {
			subFolder = getOperationType().name().toLowerCase() + FileUtilities.getPathSeparator()
			        + getEtlConfiguration().getOriginAppLocationCode();
		} else if (operationConfig.getRelatedEtlConfig().isSupposedToHaveOriginAppCode()) {
			subFolder = getOperationType().name().toLowerCase() + FileUtilities.getPathSeparator()
			        + getEtlConfiguration().getOriginAppLocationCode();
		} else {
			subFolder = getOperationType().name().toLowerCase();
		}
		
		return rootFolder + FileUtilities.getPathSeparator() + subFolder;
	}
	
	public void markAsFinished() {
		logDebug("FINISHING OPERATION " + getControllerId());
		
		logDebug("WRITING OPERATION STATUS ON FILE [" + generateOperationStatusFile().getAbsolutePath() + "]");
		
		if (!this.progressInfo.isFinished())
			this.progressInfo.changeStatusToFinished();
		
		changeStatusToFinished();
		
		logInfo("OPERATION FINISHED!");
	}
	
	@Override
	public boolean isPaused() {
		return this.operationStatus == MonitoredOperation.STATUS_PAUSED;
	}
	
	@Override
	public boolean isSleeping() {
		return this.operationStatus == MonitoredOperation.STATUS_SLEEPING;
	}
	
	@Override
	public void changeStatusToSleeping() {
		this.operationStatus = MonitoredOperation.STATUS_SLEEPING;
		
		logTrace("Operation Changed to Sleeping");
	}
	
	@Override
	public void changeStatusToRunning() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
		
		logTrace("Operation Changed to Running");
	}
	
	@Override
	public void changeStatusToStopped() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;
		
		logTrace("Operation Changed to Stopped");
	}
	
	@Override
	public void changeStatusToFinished() {
		this.operationStatus = MonitoredOperation.STATUS_FINISHED;
		
		logTrace("Operation Changed to Finished");
	}
	
	@Override
	public void changeStatusToPaused() {
		this.operationStatus = MonitoredOperation.STATUS_PAUSED;
		
		logTrace("Operation Paused");
	}
	
	@Override
	public void onStart() {
		if (!generateOperationStatusFile().exists()) {
			if (this.progressInfo.getStartTime() == null) {
				this.progressInfo.setStartTime(DateAndTimeUtilities.getCurrentDate());
			}
			
			this.progressInfo.changeStatusToRunning();
		}
		
		if (this.progressInfo.getStartTime() == null) {
			this.progressInfo.setStartTime(DateAndTimeUtilities.getCurrentDate());
		}
	}
	
	@Override
	public void onSleep() {
	}
	
	@Override
	public void onStop() {
		if (lastException == null) {
			logWarn("THE PROCESS " + getControllerId().toUpperCase() + " WAS STOPPED!!!");
		} else {
			logErr("THE PROCESS " + getControllerId().toUpperCase() + " WAS STOPPED DUE ERROR!!!");
			
			lastException.printStackTrace();
		}
		
		this.processController.finalize(this);
	}
	
	@Override
	public void onFinish() {
		getTimer().stop();
		
		logDebug("FINISHING OPERATION " + getControllerId());
		
		this.processController.finalize(this);
	}
	
	@Override
	public void killSelfCreatedThreads() {
		if (selfTreadKilled)
			return;
		
		if (this.enginesActivititieMonitor != null) {
			for (Engine<T> monitor : this.enginesActivititieMonitor) {
				ThreadPoolService.getInstance().terminateTread(logger, monitor.getEngineId(), monitor);
			}
		}
		
		selfTreadKilled = true;
	}
	
	@Override
	public void requestStop() {
		logTrace("Requesting stop of the operation...");
		
		synchronized (this.getControllerId()) {
			if (isNotInitialized()) {
				logDebug("The operation was not initialized! Stopping now!");
				
				changeStatusToStopped();
			} else if (!stopRequested() && !isFinished() && !isStopped()) {
				if (this.enginesActivititieMonitor != null) {
					for (Engine<T> monitor : this.enginesActivititieMonitor) {
						monitor.requestStop();
					}
				}
				
				this.stopRequested = true;
			}
			
			if (getChildren() != null) {
				for (OperationController<? extends EtlDatabaseObject> child : getChildren()) {
					child.requestStop();
				}
			}
		}
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return this.getEtlConfiguration().getWaitTimeToCheckStatus();
	}
	
	@JsonIgnore
	public abstract boolean mustRestartInTheEnd();
	
	@JsonIgnore
	public EtlOperationType getOperationType() {
		return this.operationConfig.getOperationType();
	}
	
	public abstract TaskProcessor<T> initRelatedTaskProcessor(Engine<T> monitor, IntervalExtremeRecord limits,
	        boolean runningInConcurrency);
	
	public abstract long getMinRecordId(Engine<? extends EtlDatabaseObject> engine);
	
	public abstract long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine);
	
	public void refresh() {
	}
	
	public void requestStopDueError(Engine<T> monitor, Exception e) {
		
		lastException = e;
		
		lastException.printStackTrace();
		
		logger.warn("STOP REQUESTED DUE ABOVE ERROR! THE OPERATION WILL PERFORME THE STOP");
		
		requestStop();
		
		if (utilities().arrayHasElement(this.enginesActivititieMonitor)) {
			for (Engine<T> m : this.enginesActivititieMonitor) {
				m.requestStopDueError();
			}
			
			while (!isStopped()) {
				logger.warn("STOP REQUESTED DUE AN ERROR AND WAITING FOR ALL ENGINES TO BE STOPPED", 120);
				TimeCountDown.sleep(5);
			}
		} else {
			logger.warn("STOPPING THE OPERATION...");
			
			changeStatusToStopped();
		}
		
		if (getChildren() != null) {
			logWarn("Requesting children to stop...");
			
			for (OperationController<? extends EtlDatabaseObject> child : getChildren()) {
				child.requestStop();
			}
			
			for (OperationController<? extends EtlDatabaseObject> child : getChildren()) {
				while (!child.isStopped()) {
					logger.warn("WAITING FOR CHILD " + child.getControllerId() + " TO STOP...", 120);
					TimeCountDown.sleep(5);
				}
			}
			
		}
		
		logTrace("Requestin the ProcessController to Stop");
		
		this.getProcessController().requestStop();
	}
	
	public TableOperationProgressInfo retrieveProgressInfo(EtlItemConfiguration config) {
		if (progressInfo != null && utilities().arrayHasElement(progressInfo.getItemsProgressInfo())) {
			for (TableOperationProgressInfo item : progressInfo.getItemsProgressInfo()) {
				if (item.getEtlConfiguration().equals(config))
					return item;
			}
		}
		
		return null;
	}
	
	@JsonIgnore
	public DBConnectionInfo getDefaultConnInfo() {
		return getProcessController().getDefaultConnInfo();
	}
	
	public OpenConnection openSrcConnection() throws DBException {
		return getProcessController().openConnection();
	}
	
	public OpenConnection tryToOpenDstConn() throws DBException {
		return getProcessController().tryToOpenDstConn();
	}
	
	public void logWarn(String msg) {
		this.processController.logWarn(msg);
	}
	
	public void logTrace(String msg) {
		this.processController.logTrace(msg);
	}
	
	public void logWarn(String msg, long interval) {
		this.processController.logWarn(msg, interval);
	}
	
	public void logInfo(String msg) {
		this.processController.logInfo(msg);
	}
	
	public void logErr(String msg) {
		this.processController.logErr(msg);
	}
	
	public void logDebug(String msg) {
		this.processController.logDebug(msg);
	}
	
	public boolean isResumable() {
		return !getOperationConfig().isDoNotSaveOperationProgress();
	}
	
	public abstract boolean canBeRunInMultipleEngines();
	
	public int getQtyRecordsPerProcessing() {
		return this.getOperationConfig().getProcessingBatch();
	}
	
	public abstract void afterEtl(List<T> objs, Connection srcConn, Connection dstConn) throws DBException;
	
	public abstract AbstractEtlSearchParams<T> initMainSearchParams(ThreadRecordIntervalsManager<T> intervalsMgt,
	        Engine<T> engine);
	
	public synchronized void finalize(Engine<T> engine) {
		this.addItemToFinalized(engine.getEtlItemConfiguration());
	}
	
	private synchronized void addItemToFinalized(EtlItemConfiguration item) {
		if (getFinalizedItems() == null) {
			this.setFinalizedItems(new ArrayList<>());
		}
		
		getFinalizedItems().add(item);
		
	}
	
}
