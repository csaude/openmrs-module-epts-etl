package org.openmrs.module.eptssync.controller.export_;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.export.ExportSyncEngine;

/**
 * This class is responsible for control the data export in the synchronization processs
 * 
 * @author jpboane
 *
 */
public class SyncExportController extends AbstractSyncController {
	
	public SyncExportController() {
		
	}

	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo) {
		return new ExportSyncEngine(syncInfo, this);
	}
}
