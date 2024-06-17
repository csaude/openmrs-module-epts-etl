package org.openmrs.module.epts.etl.problems_solver.engine;

import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;

/**
 * @author jpboane
 */
public abstract class GenericEngine extends EtlEngine {
	
	public static boolean done;
	
	public GenericEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits) {
		super(monitor, limits);
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
}
