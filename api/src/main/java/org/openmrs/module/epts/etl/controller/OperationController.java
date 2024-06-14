package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.EtlProgressMeter;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.OperationProgressInfo;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
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
public abstract class OperationController implements Controller {
	
	protected EptsEtlLogger logger;
	
	protected ProcessController processController;
	
	protected List<EngineMonitor> enginesActivititieMonitor;
	
	protected List<EngineMonitor> allGeneratedEngineMonitor;
	
	protected List<OperationController> children;
	
	protected String controllerId;
	
	protected int operationStatus;
	
	protected boolean stopRequested;
	
	protected EtlOperationConfig operationConfig;
	
	protected OperationController parent;
	
	protected TimeController timer;
	
	protected boolean selfTreadKilled;
	
	protected Exception lastException;
	
	protected OperationProgressInfo progressInfo;
	
	public OperationController(ProcessController processController, EtlOperationConfig operationConfig) {
		this.logger = new EptsEtlLogger(OperationController.class);
		
		this.processController = processController;
		this.operationConfig = operationConfig;
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		
		this.controllerId = (getOperationType().name().toLowerCase() + "_on_" + processController.getControllerId())
		        .toLowerCase();
		
		OpenConnection conn = null;
		try {
			conn = openConnection();
			
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
	
	public OperationController getParentConf() {
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
	
	public void setParent(OperationController parent) {
		this.parent = parent;
	}
	
	public EtlOperationConfig getOperationConfig() {
		return operationConfig;
	}
	
	public List<OperationController> getChildren() {
		return children;
	}
	
	public void setChildren(List<OperationController> children) {
		this.children = children;
	}
	
	public ProcessController getProcessController() {
		return processController;
	}
	
	public boolean isParallelModeProcessing() {
		return this.getOperationConfig().isParallelModeProcessing();
	}
	
	public List<EngineMonitor> getAllGeneratedEngineMonitor() {
		return allGeneratedEngineMonitor;
	}
	
	public void setAllGeneratedEngineMonitor(List<EngineMonitor> allGeneratedEngineMonitor) {
		this.allGeneratedEngineMonitor = allGeneratedEngineMonitor;
	}
	
	private synchronized void runInSequencialMode() throws DBException {
		changeStatusToRunning();
		
		List<EtlItemConfiguration> allSync = getProcessController().getConfiguration().getEtlItemConfiguration();
		
		this.enginesActivititieMonitor = new ArrayList<EngineMonitor>();
		
		for (EtlItemConfiguration config : allSync) {
			if (operationTableIsAlreadyFinished(config)) {
				logDebug(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Confinguration '"
				        + config.getConfigCode() + "' was already finished!").toUpperCase());
			} else if (stopRequested()) {
				logWarn("ABORTING THE ENGINE PROCESS DUE STOP REQUESTED!");
				break;
			} else {
				
				if (!config.isFullLoaded()) {
					try {
						config.fullLoad();
					}
					catch (DBException e) {
						e.printStackTrace();
						
						throw new RuntimeException(e);
					}
				}
				
				logInfo(("Starting operation '" + getOperationType().name().toLowerCase() + "' On Etl Confinguration '"
				        + config.getConfigCode() + "'").toUpperCase());
				
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
				
				EngineMonitor engineMonitor = EngineMonitor.init(this, config, progressInfo);
				
				OpenConnection conn = getDefaultApp().openConnection();
				
				try {
					if (isResumable()) {
						progressInfo.save(conn);
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
				
				this.enginesActivititieMonitor.add(engineMonitor);
				
				engineMonitor.run();
				
				if (stopRequested() && engineMonitor.isStopped()) {
					logInfo(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Configuration '"
					        + config.getConfigCode() + "' is stopped successifuly!").toUpperCase());
					break;
				} else {
					if (engineMonitor.getMainEngine() != null) {
						
						if (!getOperationConfig().isRunOnce()) {
							markTableOperationAsFinished(config);
						}
					} else {
						if (!getOperationConfig().isRunOnce()) {
							markTableOperationAsFinished(config);
						}
					}
					
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
		List<EtlItemConfiguration> allSync = getProcessController().getConfiguration().getEtlItemConfiguration();
		
		this.enginesActivititieMonitor = new ArrayList<EngineMonitor>();
		
		for (EtlItemConfiguration config : allSync) {
			if (operationTableIsAlreadyFinished(config)) {
				logDebug(("The operation '" + getOperationType().name().toLowerCase() + "' On Etl Configuration '"
				        + config.getConfigCode() + "' was already finished!").toUpperCase());
			} else if (stopRequested()) {
				logWarn("ABORTING THE ENGINE INITIALIZER DUE STOP REQUESTED!");
				
				break;
			} else {
				logInfo("INITIALIZING '" + getOperationType().name().toLowerCase() + "' ENGINE FOR ETL CONFIGURATION '"
				        + config.getConfigCode().toUpperCase() + "'");
				
				TableOperationProgressInfo progressInfo = this.progressInfo.retrieveProgressInfo(config);
				
				EngineMonitor engineMonitor = EngineMonitor.init(this, config, progressInfo);
				
				OpenConnection conn = getDefaultApp().openConnection();
				
				try {
					
					if (isResumable()) {
						progressInfo.save(conn);
						conn.markAsSuccessifullyTerminated();
					}
				}
				catch (DBException e) {
					throw new RuntimeException(e);
				}
				finally {
					conn.finalizeConnection();
				}
				
				startAndAddToEnginesActivititieMonitor(engineMonitor);
			}
		}
		
		changeStatusToRunning();
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
		
		for (EtlItemConfiguration config : getEtlConfiguration()) {
			if (!operationTableIsAlreadyFinished(config)) {
				return false;
			}
		}
		
		return true;
		
	}
	
	public String getControllerId() {
		return controllerId;
	}
	
	public List<EngineMonitor> getEnginesActivititieMonitor() {
		return enginesActivititieMonitor;
	}
	
	@JsonIgnore
	public CommonUtilities utilities() {
		return CommonUtilities.getInstance();
	}
	
	private void startAndAddToEnginesActivititieMonitor(EngineMonitor activitityMonitor) {
		this.enginesActivititieMonitor.add(activitityMonitor);
		
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(activitityMonitor.getEngineMonitorId())
		        .execute(activitityMonitor);
	}
	
	@Override
	public String toString() {
		return this.controllerId;
	}
	
	@Override
	public void run() {
		try {
			timer = new TimeController();
			timer.start();
			
			onStart();
			
			if (stopRequested()) {
				logWarn("THE OPERATION " + getControllerId() + " COULD NOT BE INITIALIZED DUE STOP REQUESTED!!!!");
				
				changeStatusToStopped();
				
				if (hasChild()) {
					for (OperationController child : getChildren()) {
						child.requestStop();
					}
				}
			} else if (operationIsAlreadyFinished()) {
				logWarn("THE OPERATION " + getControllerId() + " WAS ALREADY FINISHED!");
				
				changeStatusToFinished();
			} else if (isParallelModeProcessing()) {
				runInParallelMode();
			} else {
				runInSequencialMode();
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
			for (EngineMonitor monitor : this.enginesActivititieMonitor) {
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
			for (EngineMonitor monitor : this.enginesActivititieMonitor) {
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
		
		OpenConnection conn = getDefaultApp().openConnection();
		
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
	
	public EtlConfiguration getConfiguration() {
		return this.getProcessController().getConfiguration();
	}
	
	public List<EtlItemConfiguration> getEtlConfiguration() {
		return getConfiguration().getEtlItemConfiguration();
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
		
		if (operationConfig.getRelatedSyncConfig().isSupposedToRunInOrigin()) {
			subFolder = getOperationType().name().toLowerCase() + FileUtilities.getPathSeparator()
			        + getConfiguration().getOriginAppLocationCode();
		} else if (operationConfig.getRelatedSyncConfig().isSupposedToHaveOriginAppCode()
		        && !operationConfig.isDatabasePreparationOperation()) {
			subFolder = getOperationType().name().toLowerCase() + FileUtilities.getPathSeparator()
			        + getConfiguration().getOriginAppLocationCode();
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
	}
	
	@Override
	public void changeStatusToRunning() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public void changeStatusToStopped() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public void changeStatusToFinished() {
		this.operationStatus = MonitoredOperation.STATUS_FINISHED;
	}
	
	@Override
	public void changeStatusToPaused() {
		this.operationStatus = MonitoredOperation.STATUS_PAUSED;
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
			for (EngineMonitor monitor : this.enginesActivititieMonitor) {
				monitor.killSelfCreatedThreads();
				
				ThreadPoolService.getInstance().terminateTread(logger, monitor.getEngineMonitorId(), monitor);
			}
		}
		
		selfTreadKilled = true;
	}
	
	@Override
	public void requestStop() {
		
		synchronized (this.getControllerId()) {
			if (isNotInitialized()) {
				changeStatusToStopped();
			} else if (!stopRequested() && !isFinished() && !isStopped()) {
				if (this.enginesActivititieMonitor != null) {
					for (EngineMonitor monitor : this.enginesActivititieMonitor) {
						monitor.requestStop();
					}
				}
				
				this.stopRequested = true;
			}
			
			if (getChildren() != null) {
				for (OperationController child : getChildren()) {
					child.requestStop();
				}
			}
		}
		
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 5;
	}
	
	@JsonIgnore
	public abstract boolean mustRestartInTheEnd();
	
	@JsonIgnore
	public EtlOperationType getOperationType() {
		return this.operationConfig.getOperationType();
	}
	
	public abstract TaskProcessor initRelatedEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits);
	
	public abstract long getMinRecordId(EtlItemConfiguration tableInfo);
	
	public abstract long getMaxRecordId(EtlItemConfiguration tableInfo);
	
	public void refresh() {
	}
	
	public void requestStopDueError(EngineMonitor monitor, Exception e) {
		e.printStackTrace();
		
		lastException = e;
		this.stopRequested = true;
		
		if (utilities().arrayHasElement(this.enginesActivititieMonitor)) {
			for (EngineMonitor m : this.enginesActivititieMonitor) {
				m.requestStopDueError();
			}
			
			while (!isStopped()) {
				logger.warn("STOP REQUESTED DUE AN ERROR AND WAITING FOR ALL ENGINES TO BE STOPPED", 120);
				TimeCountDown.sleep(5);
			}
		} else {
			
			if (monitor != null) {
				monitor.requestStopDueError();
				
				while (!monitor.isStopped() && !monitor.isNotInitialized()) {
					logger.warn("STOP REQUESTED DUE AN ERROR AND WAITING FOR ALL ENGINES TO BE STOPPED", 120);
					TimeCountDown.sleep(5);
				}
			}
		}
		
		if (getChildren() != null) {
			for (OperationController child : getChildren()) {
				child.requestStop();
			}
		}
		
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
	public AppInfo getDefaultApp() {
		return getProcessController().getDefaultApp();
	}
	
	public OpenConnection openConnection() throws DBException {
		return getProcessController().openConnection();
	}
	
	public void logWarn(String msg) {
		this.processController.logWarn(msg);
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
		return !getOperationConfig().isNonResumable();
	}
	
	public abstract boolean canBeRunInMultipleEngines();
	
	public int getQtyRecordsPerProcessing() {
		return this.getOperationConfig().getMaxRecordPerProcessing();
	}
	
	public abstract AbstractEtlSearchParams<?> initMainSearchParams();
}
