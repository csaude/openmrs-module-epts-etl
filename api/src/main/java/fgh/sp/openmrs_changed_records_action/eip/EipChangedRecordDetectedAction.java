package fgh.sp.openmrs_changed_records_action.eip;


import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import fgh.sp.openmrs_changed_records_action.eip.model.Event;
import fgh.sp.openmrs_changed_records_action.eip.model.SenderRetryQueueItem;
import fgh.sp.openmrs_changed_records_action.eip.model.SenderRetryQueueItemDAO;
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
	public void performeAction(ChangedRecord record, SyncTableConfiguration syncTableConfiguration) {
		SenderRetryQueueItem item = new SenderRetryQueueItem();
		
		Event event = new Event();
		
		event.setTableName(record.getTableName());
		event.setPrimaryKeyId(""+record.getObjectId());
		event.setIdentifier(record.getUuid());
		event.setOperation(""+record.getOperationType());
		event.setSnapshot(Boolean.FALSE);
		
		item.setAttemptCount(0);
		item.setMessage("RE-SYNC FOR FILL GAPES");
		item.setDateCreated(record.getDateCreated());
		item.setDateChanged(record.getDateChanged());
		
		item.setEvent(event);
		
		OpenConnection conn = openConnection();
		
		try {
			SenderRetryQueueItemDAO.insert(item, conn);
			
			conn.markAsSuccessifullyTerminated();
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

	@Override
	public void performeAction(List<ChangedRecord> record, SyncTableConfiguration syncTableConfiguration) {
		throw new ForbiddenOperationException("Not supported batch performing");
	}	
}
