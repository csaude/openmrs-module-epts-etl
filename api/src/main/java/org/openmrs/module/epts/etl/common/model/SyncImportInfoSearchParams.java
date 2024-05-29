package org.openmrs.module.epts.etl.common.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public abstract class SyncImportInfoSearchParams extends AbstractEtlSearchParams<SyncImportInfoVO>{
	private String appOriginLocationCode;
	
	public SyncImportInfoSearchParams(EtlItemConfiguration config, RecordLimits limits) {
		super(config, limits, null);
	}
	
	public SyncImportInfoSearchParams(EtlItemConfiguration config, RecordLimits limits, String appOriginLocationCode) {
		super(config, limits, null);
	
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
