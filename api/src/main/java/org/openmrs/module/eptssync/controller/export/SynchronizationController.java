package org.openmrs.module.eptssync.controller.export;

import java.util.List;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.synchronization.SynchronizationSyncEngine;
import org.openmrs.module.eptssync.model.load.SyncImportInfoDAO;
import org.openmrs.module.eptssync.model.load.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.openmrs.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.model.openmrs.PersonVO;
import org.openmrs.module.eptssync.model.openmrs.UsersVO;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
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

	}

	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo) {
		return new SynchronizationSyncEngine(syncInfo);
	}

	@Override
	public void init() {
		List<SyncTableInfo> allSync = discoverSyncTableInfo();
		
		tryToCreateInitialConfigurationForAllAvaliableLocations();
		
		for (SyncTableInfo syncInfo: allSync) {
			initAndStartEngine(syncInfo);
		}
	}

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

	}
	
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
}
