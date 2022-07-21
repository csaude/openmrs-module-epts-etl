	package org.openmrs.module.eptssync.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncOperationType;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.OperationProgressInfo;
import org.openmrs.module.eptssync.model.TableOperationProgressInfo;
import org.openmrs.module.eptssync.monitor.ControllerMonitor;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represent a controller of an synchronization operation. Eg. Export data from tables to JSON files.

 * @author jpboane
 *
 */
public abstract class OperationController implements Controller{
	protected Log logger;
	
	protected ProcessController processController;
	
	protected List<EngineMonitor> enginesActivititieMonitor;
	protected List<EngineMonitor> allGeneratedEngineMonitor;
	
	protected ControllerMonitor activititieMonitor;
	
	protected List<OperationController> children;
	
	protected String controllerId;
	
	protected int operationStatus;
	protected boolean stopRequested;
	
	protected SyncOperationConfig operationConfig;
	protected OperationController parent;
	
	protected TimeController timer;
	
	protected boolean selfTreadKilled;

	protected Exception lastException;
	
	protected OperationProgressInfo progressInfo;

	public OperationController(ProcessController processController, SyncOperationConfig operationConfig) {
		this.logger = LogFactory.getLog(this.getClass());
		
		this.processController = processController;
		this.operationConfig = operationConfig;
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
		
		this.controllerId = processController.getControllerId();
		
		if (operationConfig.getRelatedSyncConfig().isSupposedToRunInOrigin()) {
			this.controllerId += "_" + getOperationType().name().toLowerCase() + "_from_" + operationConfig.getRelatedSyncConfig().getOriginAppLocationCode();
		}
		else
		if (operationConfig.getRelatedSyncConfig().isSupposedToHaveOriginAppCode() && !operationConfig.isDatabasePreparationOperation()) {
			this.controllerId += "_" + getOperationType().name().toLowerCase() + "_from_" + operationConfig.getRelatedSyncConfig().getOriginAppLocationCode();
		}
		else {
			this.controllerId += "_" + getOperationType().name().toLowerCase();
		}
			
		this.controllerId = this.controllerId.toLowerCase();
			
		OpenConnection conn = openConnection();
		
		try {
			this.progressInfo = this.processController.initOperationProgressMeter(this, conn);
			
			conn.markAsSuccessifullyTerminected();
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
		
	}
		
	public OperationProgressInfo getProgressInfo() {
		return progressInfo;
	}
	
	public Log getLogger() {
		return logger;
	}
	
	public OperationController getParent() {
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
	
	public SyncOperationConfig getOperationConfig() {
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
	
	private void runInSequencialMode() {
		changeStatusToRunning();
		
		List<SyncTableConfiguration> allSync = getProcessController().getConfiguration().getTablesConfigurations();
		
		for (SyncTableConfiguration syncInfo: allSync) {
			if (operationTableIsAlreadyFinished(syncInfo)) {
				logInfo(("The operation '" + getOperationType().name().toLowerCase() + "' On table '" + syncInfo.getTableName() + "' was already finished!").toUpperCase());
			}
			else 
			if (stopRequested()) {
				logInfo("ABORTING THE ENGINE PROCESS DUE STOP REQUESTED!");
				break;		
			}
			else {
				logInfo(("Starting operation '" + getOperationType().name().toLowerCase() + "' On table '" + syncInfo.getTableName() + "'").toUpperCase());
				
				TableOperationProgressInfo progressInfo = this.progressInfo.retrieveProgressInfo(syncInfo);
				
				EngineMonitor engineMonitor = EngineMonitor.init(this, syncInfo, progressInfo);
				
				OpenConnection conn = getDefaultApp().openConnection();
				
				try {
					progressInfo.save(conn);
					conn.markAsSuccessifullyTerminected();
				} catch (DBException e) {
					throw new RuntimeException(e);
				}
				finally {
					conn.finalizeConnection();
				}
				
				engineMonitor.run();
				
				if (stopRequested() && engineMonitor.isStopped()) {
					logInfo(("The operation '" + getOperationType().name().toLowerCase() + "' On table '" + syncInfo.getTableName() + "'  is stopped successifuly!").toUpperCase());
					break;
				}
				else {
					if (engineMonitor.getMainEngine() != null) {
						markTableOperationAsFinished(syncInfo, engineMonitor.getMainEngine(), engineMonitor.getMainEngine().getTimer());
					}
					else {
						markTableOperationAsFinished(syncInfo, null, null);
					}
					
					logInfo(("The operation '" + getOperationType().name().toLowerCase() + "' On table '" + syncInfo.getTableName() + "' is finished!").toUpperCase());
				}
			}
		}
		
		if (!stopRequested()) {
			markAsFinished();
		}
		else {
			changeStatusToStopped();
		}
	}

	private void runInParallelMode() {
		List<SyncTableConfiguration> allSync = getProcessController().getConfiguration().getTablesConfigurations();
		
		this.enginesActivititieMonitor = new ArrayList<EngineMonitor>();
		
		for (SyncTableConfiguration syncInfo: allSync) {
			if (operationTableIsAlreadyFinished(syncInfo)) {
				logInfo(("The operation '" + getOperationType().name().toLowerCase() + "' On table '" + syncInfo.getTableName() + "' was already finished!").toUpperCase());
			}
			else 
			if (stopRequested()) {
				logInfo("ABORTING THE ENGINE INITIALIZER DUE STOP REQUESTED!");
				
				break;
			}
			else{
				logInfo("INITIALIZING '" + getOperationType().name().toLowerCase() + "' ENGINE FOR TABLE '" + syncInfo.getTableName().toUpperCase() + "'");
					
				TableOperationProgressInfo progressInfo = this.progressInfo.retrieveProgressInfo(syncInfo);
				
				EngineMonitor engineMonitor = EngineMonitor.init(this, syncInfo, progressInfo);

				OpenConnection conn = getDefaultApp().openConnection();
				
				try {
					progressInfo.save(conn);
					conn.markAsSuccessifullyTerminected();
				} catch (DBException e) {
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
	
	public boolean operationTableIsAlreadyFinished(SyncTableConfiguration tableConfiguration) {
		return retrieveProgressInfo(tableConfiguration).getProgressMeter().isFinished();
	}

	private boolean operationIsAlreadyFinished() {
		return this.progressInfo.isFinished();
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
	
	public void logInfo(String msg) {
		utilities().logInfo(msg, logger);
	}
	
	public void logError(String msg) {
		utilities().logErr(msg, logger);
	}
	
	public void logDebug(String msg) {
		utilities().logDebug(msg, logger);
	}
	
	private void startAndAddToEnginesActivititieMonitor(EngineMonitor activitityMonitor) {
		this.enginesActivititieMonitor.add(activitityMonitor);
		
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(activitityMonitor.getEngineMonitorId()).execute(activitityMonitor);
	}
	
	@Override
	public String toString() {
		return this.controllerId;
	}
	
	@Override
	public void run() {
		timer = new TimeController();
		timer.start();
		
		onStart();
		
		this.activititieMonitor = new ControllerMonitor(this);
		
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.activititieMonitor.getMonitorId());
		executor.execute(this.activititieMonitor);

		if (stopRequested()) {
			logInfo("THE OPERATION " + getControllerId()  + " COULD NOT BE INITIALIZED DUE STOP REQUESTED!!!!");
			
			changeStatusToStopped();
			
			if (hasChild()) {
				for (OperationController child : getChildren()) {
					child.requestStop();
				}
			}
		}
		else
		if (operationIsAlreadyFinished()) {
			logInfo("THE OPERATION " + getControllerId() + " WAS ALREADY FINISHED!");
			
			changeStatusToFinished();
		}
		else
		if (isParallelModeProcessing()) {
			runInParallelMode();
		}
		else {
			runInSequencialMode();
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
		if (isNotInitialized()) return false;
		
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
		if(isNotInitialized()) {
			return false;
		}
		
		if (isParallelModeProcessing() && this.enginesActivititieMonitor != null) {
			for (EngineMonitor monitor : this.enginesActivititieMonitor) {
				if (!monitor.isFinished()) {
					return false;
				}
			}
			
			return true;
		}
		else {
			return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
		}
	}
	
	public void markTableOperationAsFinished(SyncTableConfiguration conf, Engine engine, TimeController timer) {
		
		logInfo("FINISHING OPERATION ON TABLE " + conf.getTableName().toUpperCase());
		
		TableOperationProgressInfo progressInfo = this.retrieveProgressInfo(conf);
				
		//String fileName = generateTableProcessStatusFile(conf).getAbsolutePath();
			
		//logInfo("WRITING OPERATION STATUS ON "+ fileName);
		
		progressInfo.getProgressMeter().changeStatusToFinished();
		
		OpenConnection conn = getDefaultApp().openConnection();
		
		try {
			progressInfo.save(conn);
			conn.markAsSuccessifullyTerminected();
		} catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
		
		logInfo("FILE WROTE");
	}
	
	public SyncConfiguration getConfiguration() {
		return this.getProcessController().getConfiguration();
	}
	
	public File generateTableProcessStatusFile_(SyncTableConfiguration conf) {
		String operationId = this.getControllerId() + "_" + conf.getTableName();
		
		String fileName = generateOperationStatusFolder() + FileUtilities.getPathSeparator() +  operationId;
		
		return new File(fileName);
	}
	
	public File generateOperationStatusFile() {
		return  new File(generateOperationStatusFolder() + FileUtilities.getPathSeparator() + getControllerId());
	}
	
	public String generateOperationStatusFolder() {
		String rootFolder = getProcessController().generateProcessStatusFolder();
		
		String subFolder = "";
		
		if (operationConfig.getRelatedSyncConfig().isSupposedToRunInOrigin()) {
			subFolder = getOperationType().name().toLowerCase() + FileUtilities.getPathSeparator() + getConfiguration().getOriginAppLocationCode(); 
		}
		else
		if (operationConfig.getRelatedSyncConfig().isSupposedToHaveOriginAppCode() && !operationConfig.isDatabasePreparationOperation()) {
			subFolder = getOperationType().name().toLowerCase() + FileUtilities.getPathSeparator() + getConfiguration().getOriginAppLocationCode(); 
		}
		else {
			subFolder = getOperationType().name().toLowerCase();
		}
		
		return rootFolder + FileUtilities.getPathSeparator()  + subFolder;
	}
	
	public void markAsFinished() {
		logInfo("FINISHING OPERATION "+ getControllerId());
		
		logInfo("WRITING OPERATION STATUS ON FILE ["+ generateOperationStatusFile().getAbsolutePath() + "]");
		
		if (!this.progressInfo.isFinished()) this.progressInfo.changeStatusToFinished();
		
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
			logInfo("THE PROCESS "+getControllerId().toUpperCase() + " WAS STOPPED!!!");
		}
		else {
			logInfo("THE PROCESS "+getControllerId().toUpperCase() + " WAS STOPPED DUE ERROR!!!");
		
			lastException.printStackTrace();
		}
		
		if (this.enginesActivititieMonitor != null)
			for (EngineMonitor monitor : this.enginesActivititieMonitor) {
				monitor.killSelfCreatedThreads();
				
				ThreadPoolService.getInstance().terminateTread(logger, monitor.getEngineMonitorId());
			}
	}
	
	@Override
	public void onFinish() {
		getTimer().stop();
		
		logInfo("FINISHING OPERATION " + getControllerId());
		List<OperationController> nextOperation = getChildren();
		
		logInfo("TRY TO INIT NEXT OPERATION");
		
		
		//Remember, if one of multiple child is disabled, then all other children are disabled
		while (nextOperation != null && !nextOperation.isEmpty() && nextOperation.get(0).getOperationConfig().isDisabled()) {
			nextOperation = nextOperation.get(0).getChildren();
		}
		
		if (nextOperation != null) {
			if (!stopRequested()) {
				for (OperationController controller : nextOperation) {
					logInfo("STARTING NEXT OPERATION " + controller.getControllerId());
					
					ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId());
					executor.execute(controller);
				}
			}
			else {
				String nextOperations = "[";
				for (OperationController controller : nextOperation) {
					nextOperations += controller.getControllerId() + ";";
				}
				
				nextOperations += "]";
				
				logInfo("THE OPERATION " + nextOperations.toUpperCase() + "NESTED COULD NOT BE INITIALIZED BECAUSE THERE WAS A STOP REQUEST!!!");
			}
		}
		else {
			logInfo("THERE IS NO MORE OPERATION TO EXECUTE... FINALIZING PROCESS... "+this.getProcessController().getControllerId());
		}
		
		killSelfCreatedThreads();
	}
	
	public void killSelfCreatedThreads() {
		if (selfTreadKilled) return;
		
		
		if (this.enginesActivititieMonitor != null) {
			for (EngineMonitor monitor : this.enginesActivititieMonitor) {
				monitor.killSelfCreatedThreads();
				
				ThreadPoolService.getInstance().terminateTread(logger, monitor.getEngineMonitorId());
			}
		}
		
		if (this.activititieMonitor != null) ThreadPoolService.getInstance().terminateTread(logger, this.activititieMonitor.getMonitorId());
		
		selfTreadKilled = true;
	}

	@Override
	public synchronized void requestStop() {
		if (isNotInitialized()) {
			changeStatusToStopped();
		}
		else
		if (!stopRequested()) {
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
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 5;
	}
	
	@JsonIgnore
	public abstract boolean mustRestartInTheEnd();
	
	@JsonIgnore
	public SyncOperationType getOperationType() {
		return  this.operationConfig.getOperationType();
	}
	
	public abstract Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) ;

	public abstract long getMinRecordId(SyncTableConfiguration tableInfo);
	public abstract long getMaxRecordId(SyncTableConfiguration tableInfo);
	
	public void refresh() {
	}

	public void requestStopDueError(EngineMonitor monitor, Exception e) {
		lastException = e;
		this.stopRequested = true;
		
		if (utilities().arrayHasElement(this.enginesActivititieMonitor)) {
			for (EngineMonitor m : this.enginesActivititieMonitor) {
				m.requestStopDueError();
			}
		
			while(!isStopped()) {
				logInfo("STOP REQUESTED DUE AN ERROR AND WAITING FOR ALL ENGINES TO BE STOPPED");
				TimeCountDown.sleep(5);
			}
		}
		else {
			monitor.requestStopDueError();
			
			while(!monitor.isStopped()) {
				logInfo("STOP REQUESTED DUE AN ERROR AND WAITING FOR ALL ENGINES TO BE STOPPED");
				TimeCountDown.sleep(5);
			}
		}
		
		if (getChildren() != null) {
			for (OperationController child : getChildren()) {
				child.requestStop();
			}
		}
	}

	public TableOperationProgressInfo retrieveProgressInfo(SyncTableConfiguration tableConfiguration) {
		if (progressInfo != null) {
			for (TableOperationProgressInfo item : progressInfo.getItemsProgressInfo()) {
				if (item.getTableConfiguration().equals(tableConfiguration)) return item;
			}
		}
		
		return null;
	}

	@JsonIgnore
	public AppInfo getDefaultApp() {
		return getProcessController().getDefaultApp();
	}
	
	public OpenConnection openConnection() {
		return getProcessController().openConnection();
	}

} 
