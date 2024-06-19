package org.openmrs.module.epts.etl.common.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public abstract class SyncImportInfoSearchParams extends AbstractEtlSearchParams<SyncImportInfoVO>{
	private String appOriginLocationCode;
	
	public SyncImportInfoSearchParams(Engine<SyncImportInfoVO> engine, ThreadRecordIntervalsManager<SyncImportInfoVO> limits) {
		super(engine, limits);
	}
	
	public SyncImportInfoSearchParams(Engine<SyncImportInfoVO> engine, ThreadRecordIntervalsManager<SyncImportInfoVO> limits, String appOriginLocationCode) {
		super(engine, limits);
	
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
		return SearchParamsDAO.countAll(this, null, conn);
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		return 0;
	}
}
