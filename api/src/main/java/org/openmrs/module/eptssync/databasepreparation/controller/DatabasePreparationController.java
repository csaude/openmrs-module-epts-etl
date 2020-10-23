package org.openmrs.module.eptssync.databasepreparation.controller;

import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.databasepreparation.engine.DatabasePreparationEngine;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for data base preparation
 * 
 * @author jpboane
 *
 */
public class DatabasePreparationController extends OperationController {
	
	public DatabasePreparationController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public void run() {
		if (getSyncConfiguration().mustCreateStageSchemaElements() && !this.isImportStageSchemaExists()) {
			this.createStageSchema();
		}
			
		super.run();
	}
	
	private void createStageSchema() {
		OpenConnection conn = openConnection();
		
		try {
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE " + getSyncConfiguration().getSyncStageSchema());

			st.executeBatch();

			st.close();
			
			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private SyncConfiguration getSyncConfiguration() {
		return getProcessController().getConfiguration();
	}
	
	private boolean isImportStageSchemaExists() {
		OpenConnection conn = openConnection();
		
		try {
			return DBUtilities.isResourceExist(null, DBUtilities.RESOURCE_TYPE_SCHEMA, getSyncConfiguration().getSyncStageSchema(), conn);
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DatabasePreparationEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}
	    
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}

	@Override
	public String getOperationType() {
		return SyncOperationConfig.SYNC_OPERATION_DATABASE_PREPARATION;
	}	
}
