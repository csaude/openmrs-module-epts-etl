package org.openmrs.module.epts.etl.problems_solver.controller;

import java.lang.reflect.Constructor;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.monitor.Engine;

/**
 * @author jpboane
 */
public class GenericOperationController extends EtlController {
	
	public GenericOperationController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public TaskProcessor initRelatedEngine(Engine monitor, ThreadRecordIntervalsManager limits) {
		
		Class[] parameterTypes = {Engine.class, ThreadRecordIntervalsManager.class};
		
		try {
			Constructor<TaskProcessor> a = getOperationConfig().getEngineClazz().getConstructor(parameterTypes);
			
			return a.newInstance(monitor, limits);
		}
		catch (Exception e) {
			throw new ForbiddenOperationException(e);
		}
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		return 1;
	}
	
	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		return 1;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
		
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
}
