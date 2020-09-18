package org.openmrs.module.eptssync.controller.load;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.load.LoadSyncDataEngine;

/**
 * This class is responsible for control the loading of sync data to stage area.
 * <p>
 * This load concist on readding the JSON content from the sync directory and load them to temp tables on sync stage.
 * 
 * @author jpboane
 *
 */
public class SyncDataLoadController extends AbstractSyncController {
	
	public SyncDataLoadController() {
		
	}

	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) {
		return new LoadSyncDataEngine(syncInfo, limits, this);
	}

	@Override
	protected long getMinRecordId(SyncTableInfo tableInfo) {
		return 0;
	}

	@Override
	protected long getMaxRecordId(SyncTableInfo tableInfo) {
		// TODO Auto-generated method stub
		return 0;
	}
}
