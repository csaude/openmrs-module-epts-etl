package org.openmrs.module.epts.etl.transport.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.transport.controller.TransportController;
import org.openmrs.module.epts.etl.transport.model.TransportRecord;
import org.openmrs.module.epts.etl.transport.model.TransportSyncSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * The processor responsible for transport synchronization files from origin to destination site
 * <p>
 * This is temporariy transportation method which suppose that the origin and destination are in the
 * same matchine, so the transport process consist on moving files from export directory to import
 * directory
 * <p>
 * In the future a propery transportation method should be implemented.
 * 
 * @author jpboane
 */
public class TransportEngine extends TaskProcessor<TransportRecord> {
	
	public TransportEngine(Engine<TransportRecord> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public void performeEtl(List<TransportRecord> migrationRecords, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		List<TransportRecord> migrationRecordAsTransportRecord = utilities.parseList(migrationRecords,
		    TransportRecord.class);
		
		this.getEngine().logInfo(
		    "COPYING  '" + migrationRecords.size() + "' " + getMainSrcTableName() + " SOURCE FILES TO IMPORT AREA");
		
		for (TransportRecord t : migrationRecordAsTransportRecord) {
			t.transport();
			
			if (t.getDestinationFile().length() == 0) {
				t.getDestinationFile().delete();
				t.getMinimalDestinationFile().delete();
				
				throw new ForbiddenOperationException(
				        "FILE " + t.getDestinationFile().getAbsolutePath() + " NOT TRANSPORTED!");
			}
			
			t.moveToBackUpDirectory();
			
			logInfo(
			    "TRANSPORTED FILE " + t.getDestinationFile().getPath() + " WITH SIZE " + t.getDestinationFile().length());
		}
		
		this.getEngine().logInfo(
		    "'" + migrationRecords.size() + "' " + getMainSrcTableName() + " SOURCE FILES COPIED TO IMPORT AREA");
		
		getTaskResultInfo().addAllToRecordsWithNoError(
		    EtlOperationItemResult.parseFromEtlDatabaseObject(migrationRecordAsTransportRecord));
	}
	
	@Override
	public TransportSyncSearchParams getSearchParams() {
		return (TransportSyncSearchParams) super.getSearchParams();
	}
	
	@Override
	public TransportController getRelatedOperationController() {
		return (TransportController) super.getRelatedOperationController();
	}
	
}
