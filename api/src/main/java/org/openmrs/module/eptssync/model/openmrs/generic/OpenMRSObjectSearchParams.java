package org.openmrs.module.eptssync.model.openmrs.generic;

import java.sql.Connection;

import org.openmrs.module.eptssync.model.AbstractSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class OpenMRSObjectSearchParams <T extends OpenMRSObject> extends AbstractSearchParams<T>{
	private Class<T> openMRSObjectClass;
	
	private T defaultObject;
	private String originAppLocationCode;
	
	private CommonUtilities utilities;
	
	public OpenMRSObjectSearchParams(Class<T> openMRSObjectClass){
		this.openMRSObjectClass = openMRSObjectClass;
		
		this.utilities = CommonUtilities.getInstance();
		
		this.defaultObject = utilities.createInstance(openMRSObjectClass);
	}
	
	@Override
	public SearchClauses<T> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<T> searchClauses = new SearchClauses<T>(this);
		
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(defaultObject.generateTableName());
		
		if (isByAppOriginLocation()) {
			searchClauses.addToClauses("origin_app_location_code = ?");
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
