package org.openmrs.module.epts.etl.common.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public abstract class SyncImportInfoSearchParams extends AbstractEtlSearchParams<EtlStageRecordVO> {
	
	private String appOriginLocationCode;
	
	private Engine<EtlStageRecordVO> relatedEngine;
	
	public SyncImportInfoSearchParams(Engine<EtlStageRecordVO> engine,
	    ThreadRecordIntervalsManager<EtlStageRecordVO> limits) {
		super(engine.getSrcConf(), limits);
		
		this.relatedEngine = engine;
	}
	
	public SyncImportInfoSearchParams(Engine<EtlStageRecordVO> engine, ThreadRecordIntervalsManager<EtlStageRecordVO> limits,
	    String appOriginLocationCode) {
		super(engine.getSrcConf(), limits);
		
		this.appOriginLocationCode = appOriginLocationCode;
	}
	
	public Engine<EtlStageRecordVO> getRelatedEngine() {
		return relatedEngine;
	}
	
	public String getAppOriginLocationCode() {
		return appOriginLocationCode;
	}
	
	@Override
	public Class<EtlStageRecordVO> getRecordClass() {
		return EtlStageRecordVO.class;
	}
	
	@Override
	public int countAllRecords(OperationController<EtlStageRecordVO> controller, Connection conn) throws DBException {
		return SearchParamsDAO.countAll(this, null, conn);
	}
	
	@Override
	public int countNotProcessedRecords(OperationController<EtlStageRecordVO> controller, Connection conn)
	        throws DBException {
		return 0;
	}
}
