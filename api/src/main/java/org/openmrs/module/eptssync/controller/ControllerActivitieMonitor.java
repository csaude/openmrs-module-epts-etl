package org.openmrs.module.eptssync.controller;

import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

/**
 * This class monitor the activitity of an {@link AbstractSyncController}
 * <p>Essencialy this monitor has the propose to trigger the {@link AbstractSyncController#getRelatedOperationToBeRunInTheEnd()} when all jobs of the controller are ended
 * 
 * @author jpboane
 */
public class ControllerActivitieMonitor implements Runnable{
	private AbstractSyncController controller;
	
	public ControllerActivitieMonitor(AbstractSyncController controller) {
		this.controller = controller;
	}
	
	@Override
	public void run() {
		String msg = "THE MONITOR OF CONTROLLER : " + controller.getControllerId() + " HAS STARTED THE MONITORING. THE RELATED OPERATION '" + controller.getRelatedOperationToBeRunInTheEnd().getOperationType() + "' WILL START AS SOON AS THIS CONTROLLER FINISH!";
		
		this.controller.logInfo(msg);
		
		while(!this.controller.isFininished()) {
			msg = "THE CONTROLLER : " + controller.getControllerId() + " IS STILL WORKING. THE RELATED OPERATION '" + controller.getRelatedOperationToBeRunInTheEnd().getOperationType() + "' WILL START AS SOON AS THIS CONTROLLER FINISH!";
			
			this.controller.logInfo(msg);
			TimeCountDown.sleep(60);
		}
		
		this.controller.startRelatedOperationToBeRunInTheEnd();
	}
}
