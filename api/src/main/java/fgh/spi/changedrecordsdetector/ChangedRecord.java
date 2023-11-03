package fgh.spi.changedrecordsdetector;

import java.util.Date;

import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;

public interface ChangedRecord extends DatabaseObject{
	public abstract String getTableName();
	public abstract Date getOperationDate();
	public abstract char getOperationType();
	public abstract String getOriginLocation();	
}
