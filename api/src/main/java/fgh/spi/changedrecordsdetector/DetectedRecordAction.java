package fgh.spi.changedrecordsdetector;

import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;

/**
 * @author jpboane
 *
 */
public interface DetectedRecordAction extends GenericOperation{
	public abstract void performeAction(ChangedRecord record);

	public abstract String getAppCode();
	public abstract boolean isDBServiceConfigured();
	public abstract void configureDBService(DBConnectionInfo dbConnectionInfo);
}
