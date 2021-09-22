package fgh.spi.changedrecordsdetector;

import java.util.Date;

public interface ChangedRecord {
	public abstract String getTableName();
	public abstract int getRecordId();
	public abstract String getRecordUuid();
	public abstract Date getOperationDate();
	public abstract char getOperationType();
	public abstract String getOriginLocation();
	public abstract Date getDateCreated();
	public abstract Date getDateChanged();	
}
