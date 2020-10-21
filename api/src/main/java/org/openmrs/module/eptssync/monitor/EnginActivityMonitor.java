package org.openmrs.module.eptssync.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

/**
 * This class monitor all {@link Engine}s of an {@link OperationController}
 * <p>When a {@link Engine} process all records within the {@link RecordLimits} granted by the {@link OperationController} then
 * the engine went to sleeping state and is put back to the controller pull. When All the engines related to a specific engine went sleep, the controller allocate new job
 * fore these engine. The purpose of {@link EnginActivityMonitor} is to controller the correct time to realocate new jobs to sleeping engines. This is done by calling {@link OperationController#realocateJobToEngines(EnginActivityMonitor)}
 * 
 * @author jpboane
 */
public class EnginActivityMonitor implements Runnable{
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private OperationController controller;
	private SyncTableConfiguration syncTableInfo;
	
	private List<Engine> ownEngines;
	
	public EnginActivityMonitor(OperationController controller, SyncTableConfiguration syncTableInfo) {
		this.controller = controller;
		this.ownEngines = new ArrayList<Engine>();
		this.syncTableInfo = syncTableInfo;
	}
	
	public SyncTableConfiguration getSyncTableInfo() {
		return syncTableInfo;
	}
	
	public Engine getMainEngine() {
		for ( Engine engine : this.ownEngines) {
			if (engine.getChildren() != null) {
				return engine;
			}
		}
		
		if (utilities.arrayHasElement(this.ownEngines)) {
			return this.ownEngines.get(0);
		}
		
		throw new RuntimeException("No engine defined for this monitor "+getController().getControllerId() + "_" + getSyncTableInfo().getTableName());
	}
	
	@Override
	public void run() {
		while(true) {
			//String msg = "WAITING FOR ALL ENGINE REQUEST NEW JOB REALOCATION. CURRENT STATUS: " + generateEngineNewJobRequestStatus();
			
			TimeCountDown.sleep(60);
			
			if (!isAllEnginesRequestedNewJob()) {
				//this.controller.logInfo(msg);
			}
			else {
				if (utilities.arrayHasElement(this.ownEngines)) {
					for (Engine engine : this.ownEngines) {
						engine.setNewJobRequested(false);
					}
					
					this.realocateJobToEngines();
				}
				else {
					initEngine();
				}
			}
		}
	}
	
	public OperationController getController() {
		return controller;
	}
	
	public Engine initEngine() {
		SyncTableConfiguration syncInfo = getSyncTableInfo();
		
		long minRecId = getController().getMinRecordId(getSyncTableInfo());
		long maxRecId =  getController().getMaxRecordId(getSyncTableInfo());
			
		if (maxRecId == 0 && minRecId == 0) {
			String msg = "NO RECORD TO PROCESS FOR TABLE '"+ getSyncTableInfo().getTableName().toUpperCase() + "' NO ENGINE WILL BE CRIETED BY NOW!";
			
			logInfo(msg);
			
			if (mustRestartInTheEnd()) {
				msg += " GOING SLEEP....";
				
				//this.changeStatusToSleeping();
			}
			else {
				msg += " FINISHING....";
				
				getController().markTableOperationAsFinished(syncInfo);
				
				//this.changeStatusToFinished();
			}
			
			logInfo(msg);
			
			return null;
		}
		else {
			long qtyRecords = maxRecId - minRecId;
			long qtyEngines = determineQtyEngines(qtyRecords);
			long qtyRecordsPerEngine = 0;
			
			if (qtyEngines == 0) qtyEngines = 1;
			
			qtyRecordsPerEngine = determineQtyRecordsPerEngine(qtyEngines, qtyRecords);
			
			long currMax = minRecId + qtyRecordsPerEngine;
			
			if (qtyEngines == 1) currMax = maxRecId;
			
			RecordLimits limits = new RecordLimits(minRecId, currMax);
			
			Engine mainEngine = retrieveAndRemoveMainSleepingEngine();
			
			//If there was no main engine, retrieve onother engine and make it main
			mainEngine =  mainEngine == null ? retrieveAndRemoveSleepingEngin() : mainEngine;
			
			mainEngine = mainEngine == null ? controller.initRelatedEngine(this, limits) : mainEngine; 
			mainEngine.setEngineId(getController().getControllerId() + "_" + syncInfo.getTableName() + "_" + utilities.garantirXCaracterOnNumber(0, 2));
			
			mainEngine.resetLimits(limits);
			
			logInfo("REALOCATED NEW RECORDS [" + mainEngine.getLimits() + "] FOR ENGINE [" + mainEngine.getEngineId()  + "]");
			
			if (mainEngine.getChildren() == null) {
				mainEngine.setChildren(new ArrayList<Engine>());
			}
			
			int i = 1;
			
			for (i = 1; i < qtyEngines; i++) {
				 limits  = new RecordLimits(limits.getLastRecordId() + 1, limits.getLastRecordId() + qtyRecordsPerEngine + 1);
				 
				 if (i == qtyEngines) limits.setLastRecordId(maxRecId);
				 
				 Engine engine = retrieveAndRemoveSleepingEngin();
				 
				 if (engine == null) {
					 engine = getController().initRelatedEngine(this, limits);
					 
					 mainEngine.getChildren().add(engine);
					 engine.setEngineId(getController().getControllerId() +"_" + syncInfo.getTableName() + "_" + utilities.garantirXCaracterOnNumber(i, 2));
					 engine.setParent(mainEngine);
				 }
				 else {
					 engine.resetLimits(limits);
				 }
				 
				 logInfo("REALOCATED NEW RECORDS [" + engine.getLimits() + "] FOR ENGINE [" + engine.getEngineId()  + "]");
			}
		
			//If this engine is new must be not initialized, otherwise must be on STOPPED STATE
			if (mainEngine.isNotInitialized()) {
				this.ownEngines.add(mainEngine);
			}
			
			ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(mainEngine.getEngineId());
			executor.execute(mainEngine);
			
			while(mainEngine.getProgressMeter() == null) {
				logInfo("WAINTING FOR PROGRESS METER OF '" + mainEngine.getEngineId() + "' BEEN CREATED TO START RELATED CHILDREN ENGINES!!!");
				TimeCountDown.sleep(15);
			}
			
			if (mainEngine.getChildren() != null) {
				for (Engine childEngine : mainEngine.getChildren()) {
					if (mainEngine.isNotInitialized()) {
						this.ownEngines.add(mainEngine);
					}
			
					executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(childEngine.getEngineId());
					executor.execute(childEngine);
				}
			}
			
			logInfo("ENGINE FOR TABLE '" + syncInfo.getTableName() + "' INITIALIZED!");
		
			return mainEngine;
		}
	}
	
	private long determineQtyRecordsPerEngine(long qtyEngines, long qtyRecords) {
		return qtyRecords/qtyEngines;
	}

	private long determineQtyEngines(long qtyRecords) {
		long qtyRecordsPerEngine = qtyRecords/getController().getOperationConfig().getMaxSupportedEngines();
		
		if (qtyRecordsPerEngine > getController().getOperationConfig().getMinRecordsPerEngine()) {
			return getController().getOperationConfig().getMaxSupportedEngines();
		}
		
		return qtyRecords/getController().getOperationConfig().getMinRecordsPerEngine();
	}

	public void realocateJobToEngines() {
		logInfo("REALOCATING ENGINES FOR '" + getSyncTableInfo().getTableName() + "'");
		
		initEngine();
	}

	private Engine retrieveAndRemoveSleepingEngin() {
		Engine sleepingEngine = null;
		
		for ( Engine engine : this.ownEngines) {
			if (engine.isSleeping()) {
				sleepingEngine = engine;
			}
		}
		
		if (sleepingEngine != null) {
			this.ownEngines.remove(sleepingEngine);
		}
		
		return sleepingEngine;
	}
	
	private Engine retrieveAndRemoveMainSleepingEngine() {
		Engine sleepingEngine = null;
		
		for ( Engine engine : this.ownEngines) {
			if (engine.isSleeping() && engine.getChildren() != null) {
				sleepingEngine = engine;
				
				break;
			}
		}
		
		if (sleepingEngine != null) this.ownEngines.remove(sleepingEngine);
		
		return sleepingEngine;
	}
	
	private boolean mustRestartInTheEnd() {
		return getController().mustRestartInTheEnd();
	}

	public void logInfo(String msg) {
		getController().logInfo(msg);
	}

	String generateEngineNewJobRequestStatus() {
		String status = "";
		
		for (Engine engine : ownEngines) {
			status += "[" + engine.getEngineId() + " > " + (engine.isNewJobRequested() ? "REQUESTED" : "NOT REQUESTED") + "] ";
		}
		
		return status;
	}
	
	/**
	 * Schedule new job for this job. This is controller by {@link EnginActivityMonitor}
	 * 
	 * @param syncEngine
	 */
	public void scheduleNewJobForEngine(Engine syncEngine) {
		syncEngine.setNewJobRequested(true);
		syncEngine.changeStatusToSleeping();
		logInfo("THE ENGINE '" + syncEngine.getEngineId() + "' HAS FINISHED ITS JOB AND NOW IS WATING FOR NEW ALOCATION WORK");	
	}
	
	boolean isAllEnginesRequestedNewJob() {
		for (Engine engine : ownEngines) {
			if (!engine.isNewJobRequested() ) return false;
		}
		
		return true;
	}
}
