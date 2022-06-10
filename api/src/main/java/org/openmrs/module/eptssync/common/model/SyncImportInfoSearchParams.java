package org.openmrs.module.eptssync.common.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public abstract class SyncImportInfoSearchParams extends SyncSearchParams<SyncImportInfoVO>{
	private String appOriginLocationCode;
	
	public SyncImportInfoSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, String appOriginLocationCode) {
		super(tableInfo, limits);
	
		this.appOriginLocationCode = appOriginLocationCode;
	}
	
	public String getAppOriginLocationCode() {
		return appOriginLocationCode;
	}
	
	@Override
	public Class<SyncImportInfoVO> getRecordClass() {
		return SyncImportInfoVO.class;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return SearchParamsDAO.countAll(this, conn);
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		return 0;
	}
}
