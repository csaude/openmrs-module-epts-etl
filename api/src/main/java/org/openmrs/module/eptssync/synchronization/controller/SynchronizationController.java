package org.openmrs.module.eptssync.synchronization.controller;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncConf;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.load.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.synchronization.engine.SynchronizationSyncEngine;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the synchronization processs between
 * tables
 * 
 * @author jpboane
 *
 */
public class SynchronizationController extends AbstractSyncController {

	public SynchronizationController() {
		super();
	}
	
	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) {
		return new SynchronizationSyncEngine(syncInfo, limits, this);
	}

	@Override
	public void init(SyncConf sourceTableInfo) {
		setSyncTableInfoSource(sourceTableInfo);
		
		if (sourceTableInfo.isDoIntegrityCheckInTheEnd()) {
			
			OpenConnection conn = openConnection();
			
			try {
				DBUtilities.disableForegnKeyChecks(conn);
				//DBUtilities.enableForegnKeyChecks(conn);
				
				conn.markAsSuccessifullyTerminected();
			} catch (DBException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
			
			super.init(sourceTableInfo);
		}
		else{
			setSyncTableInfoSource(sourceTableInfo);
		
			//tryToCreateInitialConfigurationForAllAvaliableLocations();
		
			for (SyncTableInfo syncInfo: sourceTableInfo.getSyncTableInfo()) {
				initAndStartEngine(syncInfo);
			}
		}
	}

	@Override
	public OpenConnection openConnection() {
		OpenConnection conn = super.openConnection();
	
		if (getSyncTableInfoSource().isDoIntegrityCheckInTheEnd()) {
			try {
				DBUtilities.disableForegnKeyChecks(conn);
			} catch (DBException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return conn;
	}
	
	/*
	private void tryToCreateInitialConfigurationForAllAvaliableLocations() {
		OpenConnection conn = openConnection();

		SyncTableInfo rootTableInfo = getSyncTableInfoSource().getSyncTableInfo().get(0);

		try {
			List<SyncImportInfoVO> defaultImportRecordForAllLocations = SyncImportInfoDAO
					.getDefaultRecordForEachOriginAppLocatin(rootTableInfo.generateFullStageTableName(), conn);
		
			
			for (SyncImportInfoVO importRecord : defaultImportRecordForAllLocations) {
				tryToCreateInitialConfigurationForLocation(importRecord.getOriginAppLocationCode(), conn);
			}
		
			conn.markAsSuccessifullyTerminected();
			
		} catch (DBException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}

	}*/
	
	/*
	private void tryToCreateInitialConfigurationForLocation(String originAppLocationCode, OpenConnection conn) throws DBException {
		PersonVO person = OpenMRSObjectDAO.thinGetByOriginRecordId(PersonVO.class, 1, originAppLocationCode, conn);
		UsersVO  user = OpenMRSObjectDAO.thinGetByOriginRecordId(UsersVO.class, 1, originAppLocationCode, conn);
		
		DBUtilities.disableForegnKeyChecks(conn);
		
		if (person == null) {
			person = utilities().createInstance(PersonVO.class);
			person.setOriginRecordId(1);
			person.setOriginAppLocationCode(originAppLocationCode);
			person.setUuid(utilities().generateUUID().toString());
			person.setCreator(1);
			person.setDateCreated(DateAndTimeUtilities.getCurrentDate());
			
			OpenMRSObjectDAO.insert(person, conn);
		}
	
		if (user == null) {
			user = utilities().createInstance(UsersVO.class);
			user.setOriginRecordId(1);
			user.setOriginAppLocationCode(originAppLocationCode);
			user.setUuid(utilities().generateUUID().toString());
			user.setCreator(1);
			user.setPersonId(1);
			user.setDateCreated(DateAndTimeUtilities.getCurrentDate());
			user.setSystemId("admin");
			OpenMRSObjectDAO.insert(user, conn);
		}
		
		DBUtilities.enableForegnKeyChecks(conn);
		
	}
	*/
	
	@Override
	protected long getMinRecordId(SyncTableInfo tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			SyncImportInfoVO obj = SyncImportInfoDAO.getFirstRecord(tableInfo, conn);
		
			if (obj != null) return obj.getObjectId();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	protected long getMaxRecordId(SyncTableInfo tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			SyncImportInfoVO obj = SyncImportInfoDAO.getLastRecord(tableInfo, conn);
		
			if (obj != null) return obj.getObjectId();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public boolean mustRestartInTheEnd() {
		return true;
	}
	
	@Override
	public String getOperationName() {
		return AbstractSyncController.SYNC_OPERATION_SYNCHRONIZATION;
	}
}
