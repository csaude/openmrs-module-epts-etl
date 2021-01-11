	package org.openmrs.module.eptssync.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.ControllerMonitor;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
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
	
	private ProcessController processController;
	
	private List<EngineMonitor> enginesActivititieMonitor;
	private ControllerMonitor activititieMonitor;
	
	private OperationController child;
	
	protected String controllerId;
	
	private int operationStatus;
	private boolean stopRequested;
	
	private SyncOperationConfig operationConfig;
	private OperationController parent;
	
	private TimeController timer;
	
	private boolean selfTreadKilled;

	private Exception lastException;
	
	public OperationController(ProcessController processController, SyncOperationConfig operationConfig) {
		this.logger = LogFactory.getLog(this.getClass());
		
		this.processController = processController;
		this.operationConfig = operationConfig;
		
		this.controllerId = processController.getControllerId() + "_" + getOperationType();	
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
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
		return this.child != null;
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
	
	public OperationController getChild() {
		return child;
	}
	
	public void setChild(OperationController child) {
		this.child = child;
	}
	
	public ProcessController getProcessController() {
		return processController;
	}
	
	public boolean isParallelModeProcessing() {
		return this.getOperationConfig().isParallelModeProcessing();
	}
	
	private void runInSequencialMode() {
		changeStatusToRunning();
		
		List<SyncTableConfiguration> allSync = getProcessController().getConfiguration().getTablesConfigurations();
		
		for (SyncTableConfiguration syncInfo: allSync) {
			if (operationTableIsAlreadyFinished(syncInfo)) {
				logInfo(("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "' was already finished!").toUpperCase());
			}
			else 
			if (stopRequested()) {
				logInfo("ABORTING THE ENGINE PROCESS DUE STOP REQUESTED!");
				break;		
			}
			else {
				logInfo(("Starting operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "'").toUpperCase());
				
				EngineMonitor engineMonitor = EngineMonitor.init(this, syncInfo);
				engineMonitor.run();
				
				if (stopRequested() && engineMonitor.isStopped()) {
					logInfo(("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "'  is stopped successifuly!").toUpperCase());
					break;
				}
				else {
					if (engineMonitor.getMainEngine() != null) {
						markTableOperationAsFinished(syncInfo, engineMonitor.getMainEngine(), engineMonitor.getMainEngine().getTimer());
					}
					else {
						markTableOperationAsFinished(syncInfo, null, null);
					}
					
					logInfo(("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "' is finished!").toUpperCase());
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
				logInfo(("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "' was already finished!").toUpperCase());
			}
			else 
			if (stopRequested()) {
				logInfo("ABORTING THE ENGINE INITIALIZER DUE STOP REQUESTED!");
				
				break;
			}
			else{
				logInfo("INITIALIZING '" + getOperationType().toUpperCase() + "' ENGINE FOR TABLE '" + syncInfo.getTableName().toUpperCase() + "'");
					
				startAndAddToEnginesActivititieMonitor(EngineMonitor.init(this, syncInfo));
			}
		}
		
		changeStatusToRunning();
	}
	
	private boolean operationTableIsAlreadyFinished(SyncTableConfiguration conf) {
		return generateTableProcessStatusFile(conf).exists();
	}

	private boolean operationIsAlreadyFinished() {
		return generateProcessStatusFile().exists(); 
	}

	public String getControllerId() {
		return controllerId;
	}
	
	public List<EngineMonitor> getEnginesActivititieMonitor() {
		return enginesActivititieMonitor;
	}
	
	@JsonIgnore
	public OpenConnection openConnection() {
		return processController.openConnection();
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
			
			if (getChild() != null) {
				getChild().requestStop();
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
		String fileName = generateTableProcessStatusFile(conf).getAbsolutePath();
		
		logInfo("FINISHING OPERATION ON TABLE " + conf.getTableName().toUpperCase());
		
		if (!new File(fileName).exists()) {
			logInfo("WRITING OPERATION STATUS ON "+ fileName);
			
			String desc = "";
			
			int qtyRecords = engine != null && engine.getProgressMeter() != null ? engine.getProgressMeter().getTotal() : 0;
			
			desc += "{\n";
			desc += "	operationName: \"" + this.getControllerId() + "\",\n";
			desc += "	operationTable: \"" + conf.getTableName() + "\"\n";
			desc += "	qtyRecords: " + qtyRecords + ",\n";
			desc += "	startTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(timer.getStartTime()) + "\",\n";
			desc += "	finishTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(DateAndTimeUtilities.getCurrentDate()) + "\",\n";
			desc += "	elapsedTime: " + (timer != null ? timer.getDuration(TimeController.DURACAO_IN_MINUTES) : 0) + "\n";
			desc += "}";
			
			FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
			
			FileUtilities.write(fileName, desc);
			
			logInfo("FILE WROTE");
		} 
		else {
			logInfo("THE FILE WAS ALREADY EXISTS");
		}
	}
	
	public SyncConfiguration getConfiguration() {
		return this.getProcessController().getConfiguration();
	}
	
	private File generateTableProcessStatusFile(SyncTableConfiguration conf) {
		String operationId = this.getControllerId() + "_" + conf.getTableName();
		
		String fileName = generateProcessStatusFolder() + FileUtilities.getPathSeparator() +  operationId;
		
		return new File(fileName);
	}
	
	private String generateProcessStatusFolder() {
		String subFolder = "";
		
		if (getConfiguration().isSourceInstallationType()) {
			subFolder = "source" + FileUtilities.getPathSeparator() + getOperationType() + FileUtilities.getPathSeparator() + getConfiguration().getOriginAppLocationCode(); 
		}
		else {
			String appOrigin =  this instanceof DestinationOperationController ?  FileUtilities.getPathSeparator() + ((DestinationOperationController)this).getAppOriginLocationCode() : "";
					
			subFolder = "destination" + FileUtilities.getPathSeparator() + getOperationType() + appOrigin; 
		}
		
		return getConfiguration().getSyncRootDirectory() + FileUtilities.getPathSeparator() +  "process_status" + FileUtilities.getPathSeparator()  + subFolder;
	}
	
	private File generateProcessStatusFile() {
		String operationId = this.getControllerId();
		
		String fileName = generateProcessStatusFolder() + FileUtilities.getPathSeparator() +  operationId;
		
		return new File(fileName);
	}
	
	public void markAsFinished() {
		logInfo("FINISHING OPERATION "+ getControllerId());
		
		if (!generateProcessStatusFile().exists()) {
			logInfo("WRITING OPERATION STATUS ON FILE ["+ generateProcessStatusFile().getAbsolutePath() + "]");
			
			String desc = "";
			
			desc += "{\n";
			desc += "	operationName: \"" + this.getControllerId() + "\",\n";
			desc += "	startTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(this.getTimer().getStartTime()) + "\",\n";
			desc += "	finishTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(DateAndTimeUtilities.getCurrentDate()) + "\",\n";
			desc += "	elapsedTime: \"" + this.getTimer().getDuration(TimeController.DURACAO_IN_HOURS) + "\"\n";
			desc += "}";
			
			FileUtilities.tryToCreateDirectoryStructureForFile(generateProcessStatusFile().getAbsolutePath());
			
			FileUtilities.write(generateProcessStatusFile().getAbsolutePath(), desc);
			
			logInfo("FILE WROTE");
		}
		else {
			logInfo("THE FILE WAS ALREADY EXISTS");
		}
		
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
		OperationController nextOperation = getChild();
		
		logInfo("TRY TO INIT NEXT OPERATION");
		
		while (nextOperation != null && nextOperation.getOperationConfig().isDisabled()) {
			nextOperation = nextOperation.getChild();
		}
		
		if (nextOperation != null) {
			if (!stopRequested()) {
				if (nextOperation instanceof DestinationOperationController && utilities().arrayHasElement(nextOperation.getOperationConfig().getSourceFolders())) {
					for (String appOriginCode : nextOperation.getOperationConfig().getSourceFolders()) {
						logInfo("STARTING DESTINATION OPERATION " + nextOperation.getControllerId() + " ON ORIGIN " + appOriginCode);
						
						OperationController clonedOperation = ((DestinationOperationController)nextOperation).cloneForOrigin(appOriginCode);
						
						ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(clonedOperation.getControllerId());
						executor.execute(clonedOperation);
					}
				}
				else {
					logInfo("STARTING NEXT OPERATION " + nextOperation.getControllerId());
					
					if (this instanceof DestinationOperationController) {
						nextOperation = ((DestinationOperationController)nextOperation).cloneForOrigin( ((DestinationOperationController)this).getAppOriginLocationCode());
					}
					
					ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(nextOperation.getControllerId());
					executor.execute(nextOperation);
				}
			}
			else {
				logInfo("THE OPERATION " + nextOperation.getControllerId().toUpperCase() + " COULD NOT BE INITIALIZED BECAUSE THERE WAS A STOP REQUEST!!!");
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
		
		ThreadPoolService.getInstance().terminateTread(logger, this.activititieMonitor.getMonitorId());
		
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
		
		if (getChild() != null) getChild().requestStop();
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 5;
	}
	
	@JsonIgnore
	public abstract boolean mustRestartInTheEnd();
	
	@JsonIgnore
	public abstract String getOperationType();
	
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
		
		if (getChild() != null) getChild().requestStop();
		
		
		
	}
}
