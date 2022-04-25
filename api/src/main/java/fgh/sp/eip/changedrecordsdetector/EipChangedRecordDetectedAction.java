package fgh.sp.eip.changedrecordsdetector;


import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import fgh.sp.eip.changedrecordsdetector.model.Event;
import fgh.sp.eip.changedrecordsdetector.model.SenderRetryQueueItem;
import fgh.sp.eip.changedrecordsdetector.model.SenderRetryQueueItemDAO;
import fgh.spi.changedrecordsdetector.ChangedRecord;
import fgh.spi.changedrecordsdetector.DetectedRecordAction;

/**
 * @author jpboane
 *
 */
public class EipChangedRecordDetectedAction implements DetectedRecordAction {

	private DBConnectionService dbService;
	
	public EipChangedRecordDetectedAction() {
	}
	
	public OpenConnection openConnection() {
		return dbService.openConnection();
	}
	
	@Override
	public void performeAction(ChangedRecord record) {
		SenderRetryQueueItem item = new SenderRetryQueueItem();
		
		Event event = new Event();
		
		event.setTableName(record.getTableName());
		event.setPrimaryKeyId(""+record.getRecordId());
		event.setIdentifier(record.getRecordUuid());
		event.setOperation(""+record.getOperationType());
		event.setSnapshot(Boolean.FALSE);
		
		item.setDestination("out-bound-db-sync");
		item.setAttemptCount(0);
		item.setMessage("RE-SYNC FOR FILL GAPES");
		item.setDateCreated(record.getDateCreated());
		item.setDateChanged(record.getDateChanged());
		
		item.setEvent(event);
		
		OpenConnection conn = openConnection();
		
		try {
			SenderRetryQueueItem existing = SenderRetryQueueItemDAO.getByUUID(event.getIdentifier(), conn);
			
			if (existing == null) SenderRetryQueueItemDAO.insert(item, conn);
			
			conn.markAsSuccessifullyTerminected();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public String getAppCode() {
		return "eip";
	}


	@Override
	public boolean isDBServiceConfigured() {
		return this.dbService != null;
	}

	@Override
	public void configureDBService(DBConnectionInfo dbConnectionInfo) {
		this.dbService = DBConnectionService.init(dbConnectionInfo );
	}	
}
