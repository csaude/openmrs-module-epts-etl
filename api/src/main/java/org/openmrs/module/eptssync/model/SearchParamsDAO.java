package org.openmrs.module.eptssync.model;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.VO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SearchParamsDAO extends BaseDAO{
	public static <T extends VO> int countAll(AbstractSearchParams<T> parametros, Connection conn) throws DBException {
		SearchClauses<T> searchClauses = parametros.generateSearchClauses(conn);
		
		int bkpQtyRecsPerSelect = searchClauses.getSearchParameters().getQtdRecordPerSelected();
		searchClauses.getSearchParameters().setQtdRecordPerSelected(0);
		
		String sql = "select count(*) value from (" + searchClauses.generateSQL(conn) + ") inner_result;";
			
		SimpleValue simpleValue = find(SimpleValue.class, sql, searchClauses.getParameters(), conn);
		
		searchClauses.getSearchParameters().setQtdRecordPerSelected(bkpQtyRecsPerSelect);
		
		if (simpleValue != null && CommonUtilities.getInstance().stringHasValue(simpleValue.getValue())){
			return simpleValue.intValue();
		}
		
		return 0;
	}

	static Logger l = Logger.getLogger(SearchParamsDAO.class);
	
	public static <T extends VO>  List<T> search(AbstractSearchParams<T> searchParams, Connection conn) throws DBException{
		SearchClauses<T> searchClauses = searchParams.generateSearchClauses(conn);
		
		if (searchParams.getOrderByFields() != null) {
			searchClauses.addToOrderByFields(searchParams.getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(conn);

		return search(searchParams.getRecordClass(), sql, searchClauses.getParameters(), conn);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends VO>  List<T> search(Engine engine, Connection conn) throws DBException{
		SearchClauses<T> searchClauses = (SearchClauses<T>) engine.getSearchParams().generateSearchClauses(conn);
		
		if (engine.getSearchParams().getOrderByFields() != null) {
			searchClauses.addToOrderByFields(engine.getSearchParams().getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(conn);
		
		List<T> records = (List<T>) search(engine.getSearchParams().getRecordClass(), sql, searchClauses.getParameters(), conn);
		
		return records;	
	}
}
