package org.openmrs.module.epts.etl.problems_solver.controller;

import java.lang.reflect.Constructor;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;

/**
 * @author jpboane
 */
public class GenericOperationController extends EtlController {
	
	public GenericOperationController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		
		Class[] parameterTypes = {EngineMonitor.class, RecordLimits.class};
		
		try {
			Constructor<Engine> a = getOperationConfig().getEngineClazz().getConstructor(parameterTypes);
			
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
