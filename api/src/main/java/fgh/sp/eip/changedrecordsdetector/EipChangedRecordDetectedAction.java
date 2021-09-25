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
		DBConnectionInfo dbConnInfo = new DBConnectionInfo();
		/*dbConnInfo.setConnectionURI("jdbc:h2:tcp://localhost/./sender/openmrs_eip_mgt");
		dbConnInfo.setDataBaseUserName("admin");
		dbConnInfo.setDataBaseUserPassword("admin123");
		dbConnInfo.setDriveClassName("org.h2.Driver");*/
		
		dbConnInfo.setConnectionURI("jdbc:mysql://localhost:3306/openmrs_eip_sender_mgt");
		dbConnInfo.setDataBaseUserName("root");
		dbConnInfo.setDataBaseUserPassword("root");
		dbConnInfo.setDriveClassName("com.mysql.jdbc.Driver");
		
		this.dbService = DBConnectionService.init(dbConnInfo );
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
			SenderRetryQueueItemDAO.insert(item, conn);
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
}
