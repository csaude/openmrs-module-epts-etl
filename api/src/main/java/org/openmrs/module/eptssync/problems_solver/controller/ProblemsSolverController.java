package org.openmrs.module.eptssync.problems_solver.controller;

import java.lang.reflect.Constructor;

import javax.ws.rs.ForbiddenException;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.EngineMonitor;

/**
 * This class is responsible for control the quick merge process. The quick merge process imediatly
 * merge records from the source to the destination db This process assume that the source and
 * destination are located in the same network
 * 
 * @author jpboane
 */
public class ProblemsSolverController extends OperationController {
	
	public ProblemsSolverController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
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
			throw new ForbiddenException(e);
		}
	}
	
	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}
	
	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
		
}
