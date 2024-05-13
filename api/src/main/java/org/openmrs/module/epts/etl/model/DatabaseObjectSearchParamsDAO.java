package org.openmrs.module.epts.etl.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseObjectSearchParamsDAO extends SearchParamsDAO {
	
	public static List<EtlDatabaseObject> search(DatabaseObjectSearchParams searchParams, Connection conn) throws DBException {
		
		SearchClauses<EtlDatabaseObject> searchClauses = searchParams.generateSearchClauses(conn);
		
		if (searchParams.getOrderByFields() != null) {
			searchClauses.addToOrderByFields(searchParams.getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(conn);
		
		return search(searchParams.getLoaderHealper(), searchParams.getRecordClass(), sql, searchClauses.getParameters(),
		    conn);
	}
}
