package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.AbstractSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class OpenMRSObjectSearchParams <T extends OpenMRSObject> extends AbstractSearchParams<T>{
	private Class<T> openMRSObjectClass;
	
	private T defaultObject;
	private String originAppLocationCode;
	
	private CommonUtilities utilities;
	
	private SyncTableConfiguration tableConfiguration;
	
	public OpenMRSObjectSearchParams(SyncTableConfiguration tableConfiguration, Class<T> openMRSObjectClass){
		this.openMRSObjectClass = openMRSObjectClass;
		
		this.utilities = CommonUtilities.getInstance();
		
		this.defaultObject = utilities.createInstance(openMRSObjectClass);
		this.tableConfiguration = tableConfiguration;
	}
	
	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}
	
	@Override
	public SearchClauses<T> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<T> searchClauses = new SearchClauses<T>(this);
		
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(defaultObject.generateTableName());
		
		if (isByAppOriginLocation()) {
			String originDestin = tableConfiguration.isDestinationInstallationType() ? "record_destination_id" : "record_origin_id";
			
			searchClauses.addToClauseFrom("INNER JOIN " + this.tableConfiguration.generateFullStageTableName() + " ON " + this.tableConfiguration.getPrimaryKey() + " = " + originDestin);
			
			searchClauses.addToClauses("record_origin_location_code = ?");
			searchClauses.addToParameters(this.originAppLocationCode);
		}
	
		return searchClauses;
	}

	public boolean isByAppOriginLocation() {
		return utilities.stringHasValue(this.originAppLocationCode);
	}
	
	@Override
	public Class<T> getRecordClass() {
		return this.openMRSObjectClass;
	}

}
