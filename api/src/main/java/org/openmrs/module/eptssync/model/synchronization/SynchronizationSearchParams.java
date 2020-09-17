package org.openmrs.module.eptssync.model.synchronization;

import java.sql.Connection;
import java.util.Date;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.load.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectSearchParams;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SynchronizationSearchParams extends SyncSearchParams<SyncImportInfoVO>{
	private SyncTableInfo tableInfo;
	private Date syncStartDate;
	
	public SynchronizationSearchParams(SyncTableInfo tableInfo, RecordLimits limits) {
		this.tableInfo = tableInfo;
		
		this.syncStartDate = DateAndTimeUtilities.getCurrentDate();
	}
	
	public void setSyncStartDate(Date syncStartDate) {
		this.syncStartDate = syncStartDate;
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
		
		searchClauses.addColumnToSelect(tableInfo.generateFullStageTableName() + ".*");
		
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName());
		
		searchClauses.addToClauses("last_migration_try_date is null or last_migration_try_date < ?");
		searchClauses.addToParameters(this.syncStartDate);
		
		return searchClauses;
	}	
	
	@Override
	public Class<SyncImportInfoVO> getRecordClass() {
		return SyncImportInfoVO.class;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		Class<OpenMRSObject> clazz = tableInfo.getSyncRecordClass();
		
		OpenMRSObjectSearchParams<OpenMRSObject> migratedRecordSearchParams = new OpenMRSObjectSearchParams<OpenMRSObject>(clazz);
		
		int migrated = SearchParamsDAO.countAll(migratedRecordSearchParams, conn);
		int notMigrated = countNotProcessedRecords(conn);
		
		return migrated + notMigrated;
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		return SearchParamsDAO.countAll(this, conn);
	}
}
