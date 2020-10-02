package org.openmrs.module.eptssync.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.RunningEngineInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

/**
 * This class monitor all {@link SyncEngine}s of an {@link AbstractSyncController}
 * <p>When a {@link SyncEngine} process all records within the {@link RecordLimits} granted by the {@link AbstractSyncController} then
 * the engine went to sleeping state and is put back to the controller pull. When All the engines related to a specific engine went sleep, the controller allocate new job
 * fore these engine. The purpose of {@link EnginActivitieMonitor} is to controller the correct time to realocate new jobs to sleeping engines. This is done by calling {@link AbstractSyncController#realocateJobToEngines(EnginActivitieMonitor)}
 * 
 * @author jpboane
 */
public class EnginActivitieMonitor implements Runnable{
	private AbstractSyncController controller;
	private SyncTableInfo tableInfo;
	
	private List<SyncEngine> ownEngines;
	
	public EnginActivitieMonitor(AbstractSyncController controller, SyncTableInfo syncTableInfo) {
		this.controller = controller;
		this.ownEngines = new ArrayList<SyncEngine>();
		this.tableInfo = syncTableInfo;
		
		//Discover all the engines related to the syncTableInfo from the pull
		for ( Entry<String, RunningEngineInfo> engineInfo : this.controller.getRunnungEngines().entrySet()) {
			if (engineInfo.getValue().getEngine().getSyncTableInfo().equals(syncTableInfo)) {
				this.ownEngines.add(engineInfo.getValue().getEngine());
			}
		}
	}
	
	public List<SyncEngine> getOwnEngines() {
		return ownEngines;
	}
	
	public SyncTableInfo getTableInfo() {
		return tableInfo;
	}
	
	@Override
	public void run() {
		while(true) {
			String msg = "WAITING FOR ALL ENGINE REQUEST NEW LIMITS. CURRENT STATUS: " + generateEngineNewJobRequestStatus();
			
			if (!isAllEnginePulled()) {
				this.controller.logInfo(msg);
				
				TimeCountDown.sleep(60);
			}
			else {
				
				for (SyncEngine engine : this.ownEngines) {
					engine.setNewJobRequested(false);
				}
				
				controller.realocateJobToEngines(this);
			}
		}
	}
	
	String generateEngineNewJobRequestStatus() {
		String status = "";
		
		for (SyncEngine engine : ownEngines) {
			status += "[" + engine.getEngineId() + " > " + (engine.isNewJobRequested() ? "REQUESTED" : "NOT REQUESTED") + "] ";
		}
		
		return status;
	}
	
	
	boolean isAllEnginePulled() {
		for (SyncEngine engine : ownEngines) {
			if (!engine.isNewJobRequested() ) return false;
		}
		
		return true;
	}
}
