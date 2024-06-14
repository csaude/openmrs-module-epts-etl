package org.openmrs.module.epts.etl.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.VO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class SearchParamsDAO extends BaseDAO {
	
	public static <T extends VO> int countAll(AbstractSearchParams<T> parametros, Connection conn) throws DBException {
		SearchClauses<T> searchClauses = parametros.generateSearchClauses(conn);
		
		int bkpQtyRecsPerSelect = searchClauses.getSearchParameters().getQtdRecordPerSelected();
		searchClauses.getSearchParameters().setQtdRecordPerSelected(0);
		
		String sql = "select count(*) value from (" + searchClauses.generateSQL(conn) + ") inner_result;";
		
		SimpleValue simpleValue = find(SimpleValue.class, sql, searchClauses.getParameters(), conn);
		
		searchClauses.getSearchParameters().setQtdRecordPerSelected(bkpQtyRecsPerSelect);
		
		if (simpleValue != null && CommonUtilities.getInstance().stringHasValue(simpleValue.getValue())) {
			return simpleValue.intValue();
		}
		
		return 0;
	}
	
	public static <T extends VO> List<T> search(AbstractSearchParams<T> searchParams, Connection conn) throws DBException {
		
		SearchClauses<T> searchClauses = searchParams.generateSearchClauses(conn);
		
		if (searchParams.getOrderByFields() != null) {
			searchClauses.addToOrderByFields(searchParams.getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(conn);
		
		return search(searchParams.getRecordClass(), sql, searchClauses.getParameters(), conn);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends VO> List<T> search_(TaskProcessor taskProcessor, Connection conn) throws DBException {
		SearchClauses<T> searchClauses = (SearchClauses<T>) taskProcessor.getSearchParams().generateSearchClauses(conn);
		
		if (taskProcessor.getSearchParams().getOrderByFields() != null) {
			searchClauses.addToOrderByFields(taskProcessor.getSearchParams().getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(conn);
		
		List<T> records = (List<T>) search(taskProcessor.getSearchParams().getRecordClass(), sql, searchClauses.getParameters(),
		    conn);
		
		return records;
	}
}
