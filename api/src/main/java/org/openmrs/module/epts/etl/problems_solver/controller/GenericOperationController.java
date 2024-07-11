package org.openmrs.module.epts.etl.problems_solver.controller;

import java.lang.reflect.Constructor;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.problems_solver.model.MozartLInkedFileSearchParams;

/**
 * @author jpboane
 */
public class GenericOperationController extends EtlController {
	
	boolean done;
	
	public GenericOperationController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt, Engine<EtlDatabaseObject> engine) {
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new MozartLInkedFileSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void markAsDone() {
		this.done = true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		
		Class[] parameterTypes = { Engine.class, IntervalExtremeRecord.class, Boolean.class };
		
		try {
			Constructor<TaskProcessor<? extends EtlDatabaseObject>> a = getOperationConfig().getProcessorClazz()
			        .getConstructor(parameterTypes);
			
			return (TaskProcessor<EtlDatabaseObject>) a.newInstance(monitor, limits, runningInConcurrency);
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
