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
		boolean running = true;
		
		while(running) {
			TimeCountDown.sleep(controller.getWaitTimeToCheckStatus());
			
			if (this.controller.isFinished()) {
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
}
