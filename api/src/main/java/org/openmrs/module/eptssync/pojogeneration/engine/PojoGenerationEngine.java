package org.openmrs.module.eptssync.pojogeneration.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.eptssync.pojogeneration.model.PojoGenerationRecord;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

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
public class PojoGenerationEngine extends Engine {
	
	private boolean pojoGenerated;
	
	public PojoGenerationEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> migrationRecords, Connection conn) throws DBException {
		this.pojoGenerated = true;
		
		getSyncTableConfiguration().generateRecordClass(true, conn);
	}
	
	@Override
	protected List<SyncRecord> searchNextRecords(Connection conn) {
		if (pojoGenerated) return null;
		
		List<SyncRecord> records = new ArrayList<SyncRecord>();
		
		records.add(new PojoGenerationRecord(getSyncTableConfiguration()));
		
		return records;
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new SyncSearchParams<SyncRecord>(null, limits) {
			@Override
			public int countAllRecords(Connection conn) throws DBException {
				return 1;
			}

			@Override
			public int countNotProcessedRecords(Connection conn) throws DBException {
				return 0;
			}

			@Override
			public SearchClauses<SyncRecord> generateSearchClauses(Connection conn) throws DBException {
				return null;
			}

			@Override
			public Class<SyncRecord> getRecordClass() {
				return null;
			}
		};

		return searchParams;
	}
	
	@Override
	public PojoGenerationController getRelatedOperationController() {
		return (PojoGenerationController) super.getRelatedOperationController();
	}

	@Override
	public void requestStop() {
	}
}
