package org.openmrs.module.eptssync.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectSearchParams;
import org.openmrs.module.eptssync.utilities.OpenMRSPOJOGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBQuickMergeSearchParams extends OpenMRSObjectSearchParams{
	private DBQuickMergeController relatedController;
	
	public DBQuickMergeSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, DBQuickMergeController relatedController) {
		super(tableInfo, limits);

		this.relatedController = relatedController;
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		 return OpenMRSPOJOGenerator.tryToGetExistingCLass("org.openmrs.module.eptssync.model.pojo.generic.GenericOpenMRSObject");
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		  OpenConnection srcConn = this.relatedController.openSrcConnection();
		
		  int count = super.countAllRecords(srcConn);
		
		  srcConn.finalizeConnection();
		
		  return count;
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		OpenMRSObjectSearchParams syncSearchParams = new OpenMRSObjectSearchParams(tableInfo, null);
		
		String normalFromClause;
		String patientFromClause;
		try {
			normalFromClause = conn.getSchema() + "." +  tableInfo.getTableName() + " dest_";
			patientFromClause = conn.getSchema() + "." + "patient inner join " + conn.getSchema()  + ".person dest_ on person_id = patient_id ";
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		String extraCondition = "";
		
		extraCondition += "NOT EXISTS (SELECT * ";
		extraCondition += "			   FROM    " + (tableInfo.getTableName().equals("patient") ? patientFromClause : normalFromClause); 		
		extraCondition += "			   WHERE   dest_.uuid = src_.uuid";	
		
		syncSearchParams.setExtraCondition(extraCondition);
		
		
		int processed = SearchParamsDAO.countAll(syncSearchParams, conn);
		
		int allRecords = countAllRecords(conn);
		
		return allRecords - processed;
	}
}
