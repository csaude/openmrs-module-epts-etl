package fgh.spi.changedrecordsdetector;

import java.util.Date;

import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;

public interface ChangedRecord extends OpenMRSObject{
	public abstract String getTableName();
	public abstract Date getOperationDate();
	public abstract char getOperationType();
	public abstract String getOriginLocation();	
}
