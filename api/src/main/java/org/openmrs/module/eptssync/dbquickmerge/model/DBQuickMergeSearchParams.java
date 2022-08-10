package org.openmrs.module.eptssync.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBQuickMergeSearchParams extends OpenMRSObjectSearchParams{
	private DBQuickMergeController relatedController;
	
	public DBQuickMergeSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, DBQuickMergeController relatedController) {
		super(tableInfo, limits);

		this.relatedController = relatedController;
		setOrderByFields(tableInfo.getPrimaryKey());
	}

	public boolean isForMergeMissingRecords() {
		return this.relatedController.getMergeType().isMissing();
	}
	
	public boolean isForMergeExistingRecords() {
		return this.relatedController.getMergeType().isExisting();
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		OpenConnection srcConn = this.relatedController.openSrcConnection();
			
		String srcSchema;
		
		try {
			srcSchema = srcConn.getCatalog();
			 
			SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
			
			if (tableInfo.getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauseFrom(srcSchema + ".patient inner join " + srcSchema + ".person src_ on person_id = patient_id");
				searchClauses.addColumnToSelect("patient.*, src_.uuid");
			}
			else {
				searchClauses.addToClauseFrom(srcSchema + "." + tableInfo.getTableName() + " src_");
				
				searchClauses.addColumnToSelect("src_.*");
			}
		
			String normalFromClause;
			String patientFromClause;
			
			normalFromClause = tableInfo.getTableName() + " dest_";
			patientFromClause = "patient inner join person dest_ on person_id = patient_id ";
		
			this.extraCondition = "";
			
			this.extraCondition += "  (SELECT * ";
			this.extraCondition += "   FROM    " + (tableInfo.getTableName().equals("patient") ? patientFromClause : normalFromClause); 		
			this.extraCondition += "   WHERE   dest_.uuid = src_.uuid)";	
					
			
			if (isForMergeExistingRecords()) {
				String periodCondition = "(src_.date_changed >= ? or src_.date_voided >= ?)";
				
				if (utilities.isStringIn(tableInfo.getTableName(), "users", "location", "provider")) {
					periodCondition = "(src_.date_changed >= ? or src_.date_retired >= ?)";
				}
				
				if (utilities.isStringIn(tableInfo.getTableName(), "obs", "orders")) {
					periodCondition = "(src_.date_voided >= ? or src_.date_voided >= ?)";
				}
				
				if (utilities.isStringIn(tableInfo.getTableName(), "note")) {
					periodCondition = "(src_.date_changed >= ? or src_.date_changed >= ?)";
				}
				
				searchClauses.addToClauses(periodCondition);
				searchClauses.addToParameters(getSyncStartDate());
				searchClauses.addToParameters(getSyncStartDate());
				
				this.extraCondition = " EXISTS " + extraCondition;
			}
			else
			if (isForMergeMissingRecords()) {
				this.extraCondition = "NOT EXISTS " + extraCondition;
			}			
			
			if (limits != null) {
				searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
				searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
			}
			
			if (this.tableInfo.getExtraConditionForExport() != null) {
				searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
			}
			
			if (utilities.stringHasValue(getExtraCondition())) {
				searchClauses.addToClauses(getExtraCondition());
			}
			
			return searchClauses;
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			srcConn.finalizeConnection();
		}
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		return count;	
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
