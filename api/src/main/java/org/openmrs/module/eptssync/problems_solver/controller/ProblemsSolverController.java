package org.openmrs.module.eptssync.problems_solver.controller;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.engine.ProblemsSolverEngine;
import org.openmrs.module.eptssync.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the quick merge process. The quick merge process imediatly merge records from the source to the destination db
 * This process assume that the source and destination are located in the same network
 * 
 * @author jpboane
 *
 */
public class ProblemsSolverController extends OperationController {
		
	public ProblemsSolverController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new ProblemsSolverEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return getExtremeRecord(tableInfo, "min", conn);
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return getExtremeRecord(tableInfo, "max", conn);
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	
	private long getExtremeRecord(SyncTableConfiguration tableInfo, String function, Connection conn) throws DBException {
		ProblemsSolverSearchParams searchParams = new ProblemsSolverSearchParams(tableInfo, null);
		searchParams.setSyncStartDate(getConfiguration().getObservationDate());
		
		SearchClauses<OpenMRSObject> searchClauses = searchParams.generateSearchClauses(conn);
		
		int bkpQtyRecsPerSelect = searchClauses.getSearchParameters().getQtdRecordPerSelected();
		
		searchClauses.setColumnsToSelect(function + "(" + tableInfo.getPrimaryKey() + ") as value");
		
		String sql =  searchClauses.generateSQL(conn);
				
		SimpleValue simpleValue =   BaseDAO.find(SimpleValue.class, sql, searchClauses.getParameters(), conn);
		
		searchClauses.getSearchParameters().setQtdRecordPerSelected(bkpQtyRecsPerSelect);
		
		if (simpleValue != null && CommonUtilities.getInstance().stringHasValue(simpleValue.getValue())){
			return simpleValue.intValue();
		}
		
		return 0;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
}
