package org.openmrs.module.epts.etl.pojogeneration.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.pojogeneration.processor.PojoGenerationProcessor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class PojoGenerationSearchParams extends AbstractEtlSearchParams<PojoGenerationRecord> {
	
	private PojoGenerationProcessor processor;
	
	public PojoGenerationSearchParams(Engine<PojoGenerationRecord> engine,
	    ThreadRecordIntervalsManager<PojoGenerationRecord> limits) {
		super(engine, limits);
		
	}
	
	public void setProcessor(PojoGenerationProcessor processor) {
		this.processor = processor;
	}
	
	@Override
	public List<PojoGenerationRecord> search(IntervalExtremeRecord intervalExtremeRecord, Connection srcConn,
	        Connection dstCOnn) throws DBException {
		
		if (processor.isPojoGenerated())
			return null;
		
		List<PojoGenerationRecord> records = new ArrayList<>();
		
		records.add(new PojoGenerationRecord(getSrcConf()));
		
		return records;
	}
	
	@Override
	public SearchClauses<PojoGenerationRecord> generateSearchClauses(IntervalExtremeRecord recordLimits, Connection srcConn,
	        Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return 1;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return processor.isPojoGenerated() ? 0 : 1;
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		return null;
	}
	
	@Override
	public AbstractEtlSearchParams<PojoGenerationRecord> cloneMe() {
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
