package fgh.spi.changedrecordsdetector;

import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;

/**
 * @author jpboane
 *
 */
public interface DetectedRecordAction extends GenericOperation{
	public abstract void performeAction(ChangedRecord record, SyncTableConfiguration tableConfiguration);
	public abstract void performeAction(List<ChangedRecord> record, SyncTableConfiguration tableConfiguration);

	public abstract String getAppCode();
	public abstract boolean isDBServiceConfigured();
	public abstract void configureDBService(DBConnectionInfo dbConnectionInfo);
}
