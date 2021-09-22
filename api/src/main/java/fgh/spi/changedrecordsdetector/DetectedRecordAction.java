package fgh.spi.changedrecordsdetector;

/**
 * @author jpboane
 *
 */
public interface DetectedRecordAction extends GenericOperation{
	public abstract void performeAction(ChangedRecord record);

	public abstract String getAppCode();
}
