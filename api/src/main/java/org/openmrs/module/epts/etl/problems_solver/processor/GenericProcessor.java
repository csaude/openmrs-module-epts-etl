package org.openmrs.module.epts.etl.problems_solver.processor;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;

/**
 * @author jpboane
 */
public abstract class GenericProcessor extends EtlProcessor {
	
	public static boolean done;
	
	public GenericProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
}
