package org.openmrs.module.eptssync.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBQuickMergeSearchParams extends DatabaseObjectSearchParams{
	private DBQuickMergeController relatedController;
	private int savedCount;

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
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		OpenConnection srcConn = this.relatedController.openSrcConnection();
			
		String srcSchema;
		String dstSchema = DBUtilities.determineSchemaName(conn);
			
		try {
			srcSchema = DBUtilities.determineSchemaName(srcConn);
			 
			SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
			
			if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addToClauseFrom(srcSchema + ".patient inner join " + srcSchema + ".person src_ on person_id = patient_id");
				searchClauses.addColumnToSelect("patient.*, src_.uuid");
			}
			else {
				searchClauses.addToClauseFrom(srcSchema + "." + tableInfo.getTableName() + " src_");
				
				searchClauses.addColumnToSelect("src_.*");
			}
		
			
			String srsFullTableName = DBUtilities.determineSchemaName(conn) + ".";
			
			srsFullTableName += tableInfo.getTableName();
			
			String normalFromClause;
			String patientFromClause;
			
			normalFromClause = srsFullTableName + " dest_";
			patientFromClause = dstSchema + ".patient inner join " + dstSchema + ".person dest_ on person_id = patient_id ";
		
			this.extraCondition = "";
			
			this.extraCondition += "  (SELECT * ";
			this.extraCondition += "   FROM    " + (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equals("patient") ? patientFromClause : normalFromClause); 		
			
			
			if (isForMergeExistingRecords()) {
				
				if (utilities.arrayHasElement(tableInfo.getUniqueKeys())){
					String periodCondition = "";
					
					if (utilities.arrayHasElement(tableInfo.getObservationDateFields())) {
						for (int i = 0; i < tableInfo.getObservationDateFields().size(); i++) {
							if (!periodCondition.isEmpty()) periodCondition += " or ";
							
							periodCondition += "src_." + tableInfo.getObservationDateFields().get(i) + " >= ? ";
							searchClauses.addToParameters(getSyncStartDate());
						}
						
						searchClauses.addToClauses(periodCondition);
					}
					
					this.extraCondition += " WHERE " + this.tableInfo.generateUniqueKeysJoinCondition("src_", "dest_");
				}
				else {
					//No joind field so nothing to query
					this.extraCondition += " WHERE 1 != 1";
				}
				
				this.extraCondition = " EXISTS " + this.extraCondition + ")";
			}
			if (isForMergeMissingRecords()) {
				if (utilities.arrayHasElement(tableInfo.getUniqueKeys())){
					this.extraCondition += " WHERE " + this.tableInfo.generateUniqueKeysJoinCondition("src_", "dest_");	
				}
				else {
					//No joind field so select all
					this.extraCondition += " WHERE 1 != 1";
				}
			
				this.extraCondition = "NOT EXISTS " + this.extraCondition + ")";
				
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
		if (this.savedCount > 0) return this.savedCount; 
			
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		this.savedCount = count;
		
		return count;	
	}
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
		return this.getTableInfo().getSyncRecordClass(this.relatedController.getRemoteApp());
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
