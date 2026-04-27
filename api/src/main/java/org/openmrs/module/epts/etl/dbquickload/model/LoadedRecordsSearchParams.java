package org.openmrs.module.epts.etl.dbquickload.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class LoadedRecordsSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	private String appOriginLocationCode;
	
	public LoadedRecordsSearchParams(Engine<EtlDatabaseObject> engine,
	    ThreadRecordIntervalsManager<EtlDatabaseObject> limits, String appOriginLocationCode) {
		super(engine.getSrcConf(), limits);
		
		setOrderByFields("id");
		
		this.appOriginLocationCode = appOriginLocationCode;
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord limits,
	        EtlDatabaseObject parentObject, List<EtlDatabaseObject> auxDataSourceObjects, Connection srcConn,
	        Connection dstConn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<>(this);
		
		searchClauses.addColumnToSelect(getSrcConf().generateFullSrcStageTableName() + ".*");
		
		searchClauses.addToClauseFrom(getSrcConf().generateFullSrcStageTableName());
		
		searchClauses.addToClauses("record_origin_location_code = ?");
		searchClauses.addToParameters(this.appOriginLocationCode);
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<EtlDatabaseObject> getRecordClass() {
		return EtlDatabaseObject.class;
	}
	
	@Override
	public int countAllRecords(OperationController<EtlDatabaseObject> controller, Connection conn) throws DBException {
		return SearchParamsDAO.countAll(this, null, conn);
	}
	
	@Override
	public int countNotProcessedRecords(OperationController<EtlDatabaseObject> controller, Connection conn)
	        throws DBException {
		return 0;
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		return null;
	}
}
