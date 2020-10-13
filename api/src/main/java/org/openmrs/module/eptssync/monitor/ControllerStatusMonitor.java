package org.openmrs.module.eptssync.monitor;

import org.openmrs.module.eptssync.controller.Controller;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

/**
 * This class monitor the activitity of an {@link OperationController}
 * <p>Essencialy this monitor has the propose to trigger the {@link OperationController#getChild()} when all jobs of the controller are ended
 * 
 * @author jpboane
 */
public class ControllerStatusMonitor implements Runnable{
	private Controller controller;
	
	public ControllerStatusMonitor(Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public void run() {
		//String msg = "THE MONITOR OF CONTROLLER : " + controller.getControllerId() + " HAS STARTED THE MONITORING. THE RELATED OPERATION '" + controller.getRelatedOperationToBeRunInTheEnd().getOperationType() + "' WILL START AS SOON AS THIS CONTROLLER FINISH!";
		
		String msg = "THE MONITOR OF CONTROLLER : " + controller.getControllerId() + " HAS STARTED THE MONITORING. THE RELATED STATUS OPERATIONS WILL START AS SOON AS THE CORRESPONDENT STATUS TRIGGERD";
			
		this.controller.logInfo(msg);
		
		boolean running = true;
		
		while(running) {
			msg = "THE CONTROLLER : " + controller.getControllerId() + " IS STILL WORKING. THE RELATED STATUS OPERATIONS WILL START AS SOON AS THE CORRESPONDENT STATUS TRIGGED!";
			
			this.controller.logInfo(msg);
			TimeCountDown.sleep(controller.getWaitTimeToCheckStatus());
			
			if (this.controller.isFinished()) {
				this.controller.onFinish();
			
				running = false;
			}
		}
	}
}
