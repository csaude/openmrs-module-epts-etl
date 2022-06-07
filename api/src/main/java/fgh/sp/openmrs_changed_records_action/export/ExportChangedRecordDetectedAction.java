package fgh.sp.openmrs_changed_records_action.export;


import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;

import fgh.spi.changedrecordsdetector.ChangedRecord;
import fgh.spi.changedrecordsdetector.DetectedRecordAction;

/**
 * @author jpboane
 *
 */
public class ExportChangedRecordDetectedAction implements DetectedRecordAction {
	
	public ExportChangedRecordDetectedAction() {
	}
	
	@Override
	public void performeAction(ChangedRecord record) {
		throw new ForbiddenOperationException("Not supported single performing mode");
	}

	@Override
	public String getAppCode() {
		return "quick-export";
	}

	@Override
	public boolean isDBServiceConfigured() {
		return true;
	}

	@Override
	public void configureDBService(DBConnectionInfo dbConnectionInfo) {
	}

	@Override
	public void performeAction(List<ChangedRecord> record) {
		// TODO Auto-generated method stub
		
	}	
}
