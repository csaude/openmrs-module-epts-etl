package org.openmrs.module.eptssync.engine;

import java.sql.Connection;

import org.openmrs.module.eptssync.model.AbstractSearchParams;
import org.openmrs.module.eptssync.model.base.VO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public abstract class SyncSearchParams<T extends VO> extends AbstractSearchParams <T> {
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected RecordLimits limits;
	
	public RecordLimits getLimits() {
		return limits;
	}
	
	public void setLimits(RecordLimits limits) {
		this.limits = limits;
	}
	
	public abstract int countAllRecords(Connection conn) throws DBException;
	public abstract int countNotProcessedRecords(Connection conn) throws DBException;
}
