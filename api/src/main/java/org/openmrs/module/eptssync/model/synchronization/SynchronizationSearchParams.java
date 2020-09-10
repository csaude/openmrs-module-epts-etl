package org.openmrs.module.eptssync.model.synchronization;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.load.SyncImportInfoVO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SynchronizationSearchParams extends SyncSearchParams<SyncImportInfoVO>{
	private SyncTableInfo tableInfo;
	
	public SynchronizationSearchParams(SyncTableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
		
		searchClauses.addColumnToSelect(tableInfo.generateFullStageTableName() + ".*");
		
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName());
		
		if (this.tableInfo.getMainParentRefInfo() != null) {
			String refMainParent = "";
			
			refMainParent += "INNER JOIN "+ this.tableInfo.getMainParentTableName();
			refMainParent += " ON ";
			refMainParent +=  "main_parent_id" ;
			refMainParent += " = ";
			refMainParent += this.tableInfo.getFullMainParentReferencedColumn() ;
			
			searchClauses.addToClauseFrom(refMainParent);
		}
		
		return searchClauses;
	}	
	
	@Override
	public Class<SyncImportInfoVO> getRecordClass() {
		return SyncImportInfoVO.class;
	}
}
