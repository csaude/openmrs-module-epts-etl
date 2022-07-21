package org.openmrs.module.eptssync.dbquickcopy.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.eptssync.dbquickload.model.LoadedRecordsSearchParams;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.OpenMRSPOJOGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBQuickCopySearchParams extends SyncSearchParams<OpenMRSObject>{
	private DBQuickCopyController relatedController;
	
	public DBQuickCopySearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, DBQuickCopyController relatedController) {
		super(tableInfo, limits);

		this.relatedController = relatedController;
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		searchClauses.addToClauseFrom(tableInfo.getTableName());
	
		if (tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addColumnToSelect("patient.*, person.uuid");
			searchClauses.addToClauseFrom("inner join person on person.person_id = patient_id");
		}
		else {
			searchClauses.addColumnToSelect("*");
		}
			
		if (limits != null) {
			searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
			searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
			searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
		}
		
		if (this.tableInfo.getExtraConditionForExport() != null) {
			searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		 return OpenMRSPOJOGenerator.tryToGetExistingCLass("org.openmrs.module.eptssync.model.pojo.generic.GenericOpenMRSObject");
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		OpenConnection srcConn = this.relatedController.openSrcConnection();
		
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, srcConn);
		
		this.limits = bkpLimits;
		
		srcConn.finalizeConnection();
		
		return count;
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		LoadedRecordsSearchParams syncSearchParams = new LoadedRecordsSearchParams(tableInfo, null, relatedController.getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(conn);
		
		int allRecords = countAllRecords(conn);
		
		return allRecords - processed;
	}
}
