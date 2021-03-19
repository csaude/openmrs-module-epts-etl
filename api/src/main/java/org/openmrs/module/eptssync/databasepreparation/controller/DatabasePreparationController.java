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
		if (!this.isImportStageSchemaExists()) {
			this.createStageSchema();
		}
		
		if (!existInconsistenceInfoTable()) {
			generateInconsistenceInfoTable();
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
	
	public boolean existInconsistenceInfoTable() {
		OpenConnection conn = openConnection();
		
		String schema = getSyncConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "inconsistence_info";

		try {
			return DBUtilities.isResourceExist(schema, resourceType, tabName, conn);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminected();
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
	
	private void generateInconsistenceInfoTable() {
		OpenConnection conn = openConnection();
		
		String sql = "";
		
		sql += "CREATE TABLE " + getSyncConfiguration().getSyncStageSchema() + ".inconsistence_info (\n";
		sql += "id int(11) NOT NULL AUTO_INCREMENT,\n";
		sql += "table_name varchar(100) NOT NULL,\n";
		sql += "record_id int(11) NOT NULL,\n";
		sql += "parent_table_name varchar(100) NOT NULL,\n";
		sql += "parent_id int(11) NOT NULL,\n";
		sql += "default_parent_id int(11) DEFAULT NULL,\n";
		sql += "creation_date datetime DEFAULT CURRENT_TIMESTAMP,\n";
		sql += "PRIMARY KEY (id),\n";
		sql += "UNIQUE KEY inconsistence_info_unq (table_name,record_id, parent_table_name, parent_id)\n";
		sql += ") ENGINE=InnoDB;\n";
				
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();

			st.close();
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} 
		finally {
			conn.markAsSuccessifullyTerminected();
			conn.finalizeConnection();
		}
	}
}
