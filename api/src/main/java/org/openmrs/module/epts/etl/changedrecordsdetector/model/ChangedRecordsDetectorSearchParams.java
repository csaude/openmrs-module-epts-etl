package org.openmrs.module.epts.etl.changedrecordsdetector.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ChangedRecordsDetectorSearchParams extends EtlDatabaseObjectSearchParams {
	
	private boolean selectAllRecords;
	
	private String appCode;
	
	private EtlOperationType type;
	
	public ChangedRecordsDetectorSearchParams(Engine<EtlDatabaseObject> engine, String appCode,
	    ThreadRecordIntervalsManager<EtlDatabaseObject> limits, EtlOperationType type) {
		super(engine, limits);
		
		this.appCode = appCode;
		
		this.type = type;
	}
	
	public String getAppCode() {
		return appCode;
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord limits, Connection srcConn,
	        Connection dstConn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = getConfig().getSrcConf();
		
		searchClauses.addToClauseFrom(tableInfo.generateSelectFromClauseContent());
		
		searchClauses.addColumnToSelect(tableInfo.generateFullAliasedSelectColumns());
		
		if (this.type.isNewRecordsDetector()) {
			searchClauses.addToClauses(tableInfo.getTableName() + ".date_created >= ?");
			
			searchClauses.addToParameters(this.getSyncStartDate());
		} else if (!tableInfo.isMetadata() && !tableInfo.getTableName().equalsIgnoreCase("users")
		        && !tableInfo.getTableName().equalsIgnoreCase("obs")) {
			searchClauses.addToClauses(tableInfo.getTableName() + ".date_created < ? and (" + tableInfo.getTableName()
			        + ".date_changed >= ? or " + tableInfo.getTableName() + ".date_voided >= ?)");
			
			searchClauses.addToParameters(this.getSyncStartDate());
			searchClauses.addToParameters(this.getSyncStartDate());
			searchClauses.addToParameters(this.getSyncStartDate());
		} else if (tableInfo.getTableName().equalsIgnoreCase("obs")) {
			searchClauses.addToClauses(
			    tableInfo.getTableName() + ".date_created < ? and " + tableInfo.getTableName() + ".date_voided >= ?");
			
			searchClauses.addToParameters(this.getSyncStartDate());
			searchClauses.addToParameters(this.getSyncStartDate());
		} else {
			searchClauses.addToClauses(tableInfo.getTableName() + ".date_created < ? and (" + tableInfo.getTableName()
			        + ".date_changed >= ? or " + tableInfo.getTableName() + ".date_retired >= ?)");
			
			searchClauses.addToParameters(this.getSyncStartDate());
			searchClauses.addToParameters(this.getSyncStartDate());
			searchClauses.addToParameters(this.getSyncStartDate());
		}
		
		if (!this.selectAllRecords) {
			tryToAddLimits(limits, searchClauses);
		}
		
		tryToAddExtraConditionForExport(searchClauses);
		
		return searchClauses;
	}
	
	@Override
	public Class<EtlDatabaseObject> getRecordClass() {
		return DatabaseEntityPOJOGenerator
		        .tryToGetExistingCLass("org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject");
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		ThreadRecordIntervalsManager<EtlDatabaseObject> bkpLimits = this.getThreadRecordIntervalsManager();
		
		this.setThreadRecordIntervalsManager(null);
		
		int count = SearchParamsDAO.countAll(this, null, conn);
		
		this.setThreadRecordIntervalsManager(bkpLimits);
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
	
	public DatabaseObjectLoaderHelper getLoaderHealper() {
		return this.getConfig().getSrcConf().getLoadHealper();
	}
	
	@Override
	protected AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) {
		return null;
	}
}
