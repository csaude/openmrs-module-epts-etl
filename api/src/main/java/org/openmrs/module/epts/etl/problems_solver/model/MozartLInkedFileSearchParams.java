package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class MozartLInkedFileSearchParams extends EtlDatabaseObjectSearchParams {
	
	public MozartLInkedFileSearchParams(Engine<EtlDatabaseObject> engine,
	    ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
	}
	
	@Override
	public List<EtlDatabaseObject> searchNextRecordsInMultiThreads(IntervalExtremeRecord interval, Connection srcConn,
	        Connection dstConn) throws DBException {
		return search(interval, srcConn, dstConn);
	}
	
	@Override
	public GenericOperationController getRelatedController() {
		return (GenericOperationController) getRelatedEngine().getRelatedOperationController();
	}
	
	@Override
	public List<EtlDatabaseObject> search(IntervalExtremeRecord intervalExtremeRecord, Connection srcConn,
	        Connection dstCOnn) throws DBException {
		
		if (getRelatedController().isDone()) {
			return null;
		}
		
		return utilities.parseToList(getSrcConf().createRecordInstance());
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord recordLimits, Connection srcConn,
	        Connection dstConn) throws DBException {
		return null;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return 1;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return getRelatedController().isDone() ? 0 : 1;
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
