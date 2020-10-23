package org.openmrs.module.eptssync.pojogeneration.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
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
		
		for (RefInfo i: getSyncTableConfiguration().getChildRefInfo(conn)) {
			if (!i.getReferenceTableInfo().isFullLoaded()) {
				
				logInfo("THE REF INFO IS NOT FULL LOADED. LOADING NOW ...["+ i.getReferenceTableInfo().getTableName() + "]");
				i.getReferenceTableInfo().fullLoad(conn);
			}
			else {
				logInfo("THE REF INFO WAS ALREADY LOADED ["+ i.getReferenceTableInfo().getTableName() + "]");
			}
			
			try {
				Class<OpenMRSObject> referenceClass = i.getReferenceTableInfo().getRecordClass();
				
				if (utilities.createInstance(referenceClass).isGeneratedFromSkeletonClass()) {
					logInfo("THE POJO IS SKELETON... NOW RECOMPILING REFERENCE POJO AGAIN["+ i.getReferenceTableInfo().getTableName()+ "]");
					
					i.generateRelatedReferenceClass(true, conn);
				}
			} catch (ForbiddenOperationException e) {
				logInfo("THE TABLE POJO DOES NOT EXISTT... CREATING IT ["+i.getReferenceTableInfo().getTableName() + "]");
				i.generateRelatedReferenceClass(true, conn);
			}
		}
		
		logInfo("PREPARING PARENT INFO OF TABLE["+ getSyncTableConfiguration() + "]");
		
		for (RefInfo i: getSyncTableConfiguration().getParentRefInfo(conn)) {
			if (!i.getReferencedTableInfo().isFullLoaded()) {
				logInfo("THE REF INFO IS NOT FULL LOADED. LOADING NOW ...["+ i.getReferencedTableInfo().getTableName() + "]");
				
				i.getReferencedTableInfo().fullLoad(conn);
			}
			else {
				logInfo("THE REF INFO WAS ALREADY LOADED ["+ i.getReferencedTableInfo().getTableName() + "]");
			}
			
			try {
				Class<OpenMRSObject> referencedClass = i.getReferencedTableInfo().getRecordClass();
				
				if (utilities.createInstance(referencedClass).isGeneratedFromSkeletonClass()) {
					logInfo("THE POJO IS SKELETON... NOW RECOMPILING REFERENCE POJO AGAIN["+ i.getReferencedTableInfo().getTableName()+ "]");
											
					i.generateRelatedReferencedClass(true, conn);
				}
			} catch (ForbiddenOperationException e) {
				logInfo("THE TABLE POJO DOES NOT EXISTT... CREATING IT ["+i.getReferenceTableInfo().getTableName() + "]");
				i.generateRelatedReferencedClass(true, conn);
			}
		}
		
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
				return 0;
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
