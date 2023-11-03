package org.openmrs.module.epts.etl.transport.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.transport.controller.TransportController;
import org.openmrs.module.epts.etl.transport.model.TransportRecord;
import org.openmrs.module.epts.etl.transport.model.TransportSyncSearchParams;

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
public class TransportEngine extends Engine {

	public TransportEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	@Override
	protected void restart() {
	}
	@Override
	public void performeSync(List<SyncRecord> migrationRecords, Connection conn) {
		List<TransportRecord> migrationRecordAsTransportRecord = utilities.parseList(migrationRecords, TransportRecord.class);
	
		this.getMonitor().logInfo("COPYING  '"+migrationRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " SOURCE FILES TO IMPORT AREA");
		
		for (TransportRecord t : migrationRecordAsTransportRecord) {
			t.transport();
			
			if (t.getDestinationFile().length() == 0) {
				t.getDestinationFile().delete();
				t.getMinimalDestinationFile().delete();
				
				throw new ForbiddenOperationException("FILE " + t.getDestinationFile().getAbsolutePath() +  " NOT TRANSPORTED!");
			}
		
			t.moveToBackUpDirectory();
		
			logInfo("TRANSPORTED FILE " + t.getDestinationFile().getPath() + " WITH SIZE " + t.getDestinationFile().length());
		}
	
		this.getMonitor().logInfo("'"+migrationRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " SOURCE FILES COPIED TO IMPORT AREA");
	}

	@Override
	protected List<SyncRecord> searchNextRecords(Connection conn) {
		try {
			File[] files = getSyncDirectory().listFiles(this.getSearchParams());
			
			List<SyncRecord> syncRecords = new ArrayList<SyncRecord>();
			
			if (files != null && files.length > 0) {
				syncRecords.add(new TransportRecord(files[0], getSyncDestinationDirectory(), getSyncBkpDirectory()));
			}
			
			return syncRecords;
		} catch (IOException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public TransportSyncSearchParams getSearchParams() {
		return (TransportSyncSearchParams) super.getSearchParams();
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new TransportSyncSearchParams(getRelatedOperationController(), this.getSyncTableConfiguration(), limits);
		searchParams.setQtdRecordPerSelected(2500);

		return searchParams;
	}

	private File getSyncBkpDirectory() throws IOException {
		return getRelatedOperationController().getSyncBkpDirectory(getSyncTableConfiguration());
	}

	private File getSyncDestinationDirectory() throws IOException {
		return getRelatedOperationController().getSyncDestinationDirectory(getSyncTableConfiguration());
	}

	@Override
	public TransportController getRelatedOperationController() {
		return (TransportController) super.getRelatedOperationController();
	}

	private File getSyncDirectory() {
		return getRelatedOperationController().getSyncDirectory(getSyncTableConfiguration());
	}

	@Override
	public void requestStop() {
		// TODO Auto-generated method stub

	}
}
