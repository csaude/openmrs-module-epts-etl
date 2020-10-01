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
 * This class represent acontroller of {@link RecordLimits} of all {@link SyncEngine} controllerd by a {@link AbstractSyncController}
 * <p>When a {@link SyncEngine} process all records within the {@link RecordLimits} granted by the {@link AbstractSyncController} then
 * the controller must define new limits; that is the porpose os {@link EnginsMonitor}   
 * 
 * @author jpboane
 */
public class EnginsMonitor implements Runnable{
	private AbstractSyncController controller;
	private SyncTableInfo tableInfo;
	
	private List<SyncEngine> ownEngines;
	
	public EnginsMonitor(AbstractSyncController controller, SyncTableInfo tableInfo) {
		this.controller = controller;
		this.ownEngines = new ArrayList<SyncEngine>();
		
		for ( Entry<String, RunningEngineInfo> engineInfo : this.controller.getRunnungEngines().entrySet()) {
			if (engineInfo.getValue().getEngine().getSyncTableInfo().equals(tableInfo)) {
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
			String msg = "WAITING FOR ALL ENGINE REQUEST NEW LIMITS. CURRENT STATUS: " + generateNewLimitResquestStatus();
			
			if (!isAllEngineRequestedNewLimits()) {
				this.controller.logInfo(msg);
				
				TimeCountDown.sleep(300);
			}
			else {
				
				for (SyncEngine engine : this.ownEngines) {
					engine.setNewJobRequested(false);
				}
				
				controller.resetEngines(this);
			}
		}
	}
	
	String generateNewLimitResquestStatus() {
		String status = "";
		
		for ( Entry<String, RunningEngineInfo> engineInfo : this.controller.getRunnungEngines().entrySet()) {
			status += "[" + engineInfo.getKey() + (engineInfo.getValue().getEngine().isNewJobRequested() ? "REQUESTED" : "NOT REQUESTED") + "] ";
		}
		
		return status;
	}
	
	boolean isAllEngineRequestedNewLimits() {
		for ( Entry<String, RunningEngineInfo> engineInfo : this.controller.getRunnungEngines().entrySet()) {
			if (!engineInfo.getValue().getEngine().isNewJobRequested() ) return false;
		}
		
		return true;
	}
}
