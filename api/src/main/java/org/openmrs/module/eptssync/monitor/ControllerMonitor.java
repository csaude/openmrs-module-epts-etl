package org.openmrs.module.eptssync.monitor;

import org.openmrs.module.eptssync.controller.Controller;
import org.openmrs.module.eptssync.controller.DestinationOperationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

/**
 * This class monitor the activitity of an {@link OperationController}
 * <p>Essencialy this monitor has the propose to trigger the {@link OperationController#getChild()} when all jobs of the controller are ended
 * 
 * @author jpboane
 */
public class ControllerMonitor implements Runnable{
	private Controller controller;
	
	private String monitorId;
	
	public ControllerMonitor(Controller controller) {
		this.controller = controller;
		this.monitorId = this.controller.getControllerId() + "_monitor"; 
	}
	
	public String getMonitorId() {
		return monitorId;
	}
	
	@Override
	public void run() {
		boolean running = true;
		
		while(running) {
			TimeCountDown.sleep(controller.getWaitTimeToCheckStatus());
			
			if (this.toString().equals("SOURCE_ZBZ_DERRE_CONTROLLER_DATABASE_PREPARATION_MONITOR")) {
				System.out.println("STOP");
			}
			
			if (controller instanceof DestinationOperationController) {
				System.out.println("STOP");
			}
			
			if (this.controller.isFinished()) {
				this.controller.markAsFinished();
				this.controller.onFinish();
			
				running = false;
			}
			else 
			if (controller.isStopped()) {
				running = false;
				
				controller.onStop();
			}
			else
			if (controller.stopRequested()) {
				controller.requestStop();
			}
		}
	}
	
	
	
	@Override
	public String toString() {
		return (this.controller.getControllerId() + "_MONITOR").toUpperCase();
	}
}
