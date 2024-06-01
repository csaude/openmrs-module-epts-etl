package org.openmrs.module.epts.etl.transport.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.transport.controller.TransportController;
import org.openmrs.module.epts.etl.transport.model.TransportRecord;
import org.openmrs.module.epts.etl.transport.model.TransportSyncSearchParams;

/**
 * The engine responsible for transport synchronization files from origin to destination site
 * <p>
 * This is temporariy transportation method which suppose that the origin and destination are in the
 * same matchine, so the transport process consist on moving files from export directory to import
 * directory
 * <p>
 * In the future a propery transportation method should be implemented.
 * 
 * @author jpboane
 */
public class TransportEngine extends EtlEngine {
	
	public TransportEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> migrationRecords, Connection conn) {
		List<TransportRecord> migrationRecordAsTransportRecord = utilities.parseList(migrationRecords,
		    TransportRecord.class);
		
		this.getMonitor().logInfo(
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
		
		this.getMonitor().logInfo(
		    "'" + migrationRecords.size() + "' " + getMainSrcTableName() + " SOURCE FILES COPIED TO IMPORT AREA");
	}
	
	@Override
	public TransportSyncSearchParams getSearchParams() {
		return (TransportSyncSearchParams) super.getSearchParams();
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadLimitsManager limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new TransportSyncSearchParams(
		        this, this.getEtlConfiguration(), limits);
		searchParams.setQtdRecordPerSelected(2500);
		
		return searchParams;
	}
	
	@Override
	public TransportController getRelatedOperationController() {
		return (TransportController) super.getRelatedOperationController();
	}
	
}
