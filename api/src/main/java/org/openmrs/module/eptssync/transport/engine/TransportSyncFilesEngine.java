package org.openmrs.module.eptssync.transport.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.transport.controller.SyncTransportController;
import org.openmrs.module.eptssync.transport.model.TransporRecord;
import org.openmrs.module.eptssync.transport.model.TransportSyncFilesSearchParams;

/**
 * The engine responsible for transport synchronization files from origin to
 * destination site
 * <p>
 * This is temporariy transportation method which suppose that the origin and
 * destination are in the same matchine, so the transport process consist on
 * moving files from export directory to import directory
 * <p>
 * In the future a propery transportation method should be implemented.
 * 
 * @author jpboane
 */
public class TransportSyncFilesEngine extends SyncEngine {

	public TransportSyncFilesEngine(SyncTableInfo syncTableInfo, RecordLimits limits,
			SyncTransportController syncController) {
		super(syncTableInfo, limits, syncController);
	}

	@Override
	protected void restart() {
	}
	@Override
	public void performeSync(List<SyncRecord> migrationRecords) {
		List<TransporRecord> migrationRecordAsTransportRecord = utilities.parseList(migrationRecords, TransporRecord.class);
	
		this.syncController.logInfo("COPYING  '"+migrationRecords.size() + "' " + getSyncTableInfo().getTableName() + " SORUCE FILES TO IMPORT AREA");
		
		
		for (TransporRecord t : migrationRecordAsTransportRecord) {
			t.transport();
			t.moveToBackUpDirectory();
		}
	
		this.syncController.logInfo("'"+migrationRecords.size() + "' " + getSyncTableInfo().getTableName() + " SORUCE FILES COPIED TO IMPORT AREA");
	}

	@Override
	protected List<SyncRecord> searchNextRecords() {
		try {
			File[] files = getSyncDirectory().listFiles(this.getSearchParams());
			
			List<SyncRecord> syncRecords = new ArrayList<SyncRecord>();
			
			if (files != null) {
				for (File f : files) {
					syncRecords.add(new TransporRecord(f, getSyncDestinationDirectory(), getSyncBkpDirectory()));
				}
			}
			
			return syncRecords;
		} catch (IOException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public TransportSyncFilesSearchParams getSearchParams() {
		return (TransportSyncFilesSearchParams) super.getSearchParams();
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits) {
		SyncSearchParams<? extends SyncRecord> searchParams = new TransportSyncFilesSearchParams(this.syncTableInfo);
		searchParams.setQtdRecordPerSelected(2500);

		return searchParams;
	}

	private File getSyncBkpDirectory() throws IOException {
		return SyncTransportController.getSyncBkpDirectory(getSyncTableInfo());
	}

	private File getSyncDestinationDirectory() throws IOException {
		return SyncTransportController.getSyncDestinationDirectory(getSyncTableInfo());
	}

	@Override
	public SyncTransportController getSyncController() {
		return (SyncTransportController) super.getSyncController();
	}

	private File getSyncDirectory() {
		return SyncTransportController.getSyncDirectory(getSyncTableInfo());
	}

	@Override
	public void requestStop() {
		// TODO Auto-generated method stub

	}
}
