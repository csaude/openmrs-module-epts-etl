package fgh.spi.changedrecordsdetector;

import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;

/**
 * @author jpboane
 *
 */
public interface DetectedRecordAction extends GenericOperation{
	public abstract void performeAction(ChangedRecord record, AbstractTableConfiguration tableConfiguration);
	public abstract void performeAction(List<ChangedRecord> record, AbstractTableConfiguration tableConfiguration);

	public abstract String getAppCode();
	public abstract boolean isDBServiceConfigured();
	public abstract void configureDBService(DBConnectionInfo dbConnectionInfo);
}
