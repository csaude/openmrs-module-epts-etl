package org.openmrs.module.epts.etl.utilities.concurrent;

import java.util.concurrent.ThreadFactory;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class EtlThreadFactory<T extends EtlDatabaseObject> implements ThreadFactory {
	
	CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Engine<T> engine;
	
	public EtlThreadFactory(Engine<T> engine) {
		this.engine = engine;
	}
	
	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, engine.getEngineId() + "_"
		        + utilities.garantirXCaracterOnNumber(engine.getCurrentTaskProcessor().size(), 2));
	}
	
}
