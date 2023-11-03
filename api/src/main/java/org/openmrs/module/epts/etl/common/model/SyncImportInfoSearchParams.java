package org.openmrs.module.epts.etl.common.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public abstract class SyncImportInfoSearchParams extends SyncSearchParams<SyncImportInfoVO>{
	private String appOriginLocationCode;
	
	public SyncImportInfoSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits) {
		super(tableInfo, limits);
	}
	
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
