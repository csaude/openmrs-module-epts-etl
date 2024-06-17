package org.openmrs.module.epts.etl.problems_solver.controller;

import java.lang.reflect.Constructor;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.problems_solver.model.MozartLInkedFileSearchParams;

/**
 * @author jpboane
 */
public class GenericOperationController extends EtlController {
	
	public GenericOperationController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt,
	        Engine<EtlDatabaseObject> engine) {
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new MozartLInkedFileSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedEngine(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits) {
		
		Class[] parameterTypes = { Engine.class, ThreadRecordIntervalsManager.class };
		
		try {
			Constructor<TaskProcessor<? extends EtlDatabaseObject>> a = getOperationConfig().getEngineClazz()
			        .getConstructor(parameterTypes);
			
			return (TaskProcessor<EtlDatabaseObject>) a.newInstance(monitor, limits);
		}
		catch (Exception e) {
			throw new ForbiddenOperationException(e);
		}
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		return 1;
	}
	
	@Override
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
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
