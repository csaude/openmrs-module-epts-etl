package org.openmrs.module.eptssync.model;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.VO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.FuncoesGenericas;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.SQLUtilitie;

public class SearchParamsDAO extends BaseDAO{
	public static <T extends VO> int countAll(AbstractSearchParams<T> parametros, Connection conn) throws DBException {
		SearchClauses<T> searchClauses = parametros.generateSearchClauses(conn);
		
		String originalColumnsToSelect = searchClauses.getColumnsToSelect();
		
		if (searchClauses.isDistinctSelect() && !FuncoesGenericas.stringHasValue(searchClauses.getDefaultColumnToSelect())) throw new ForbiddenOperationException("O campo 'defaultColumnToSelect' deve ser preenchido para 'SELECT DISTINCT'");
		
		searchClauses.setColumnsToSelect("  count(" + (searchClauses.isDistinctSelect() ? ("DISTINCT " + searchClauses.getDefaultColumnToSelect()) : "*")  + ") value");
		
		String sql = searchClauses.generateSQL(conn);
		
		//logger.error(sql);
		//logger.error(searchClauses.getParameters());
		
		SimpleValue simpleValue = find(SimpleValue.class, sql, searchClauses.getParameters(), conn);
		
		searchClauses.setColumnsToSelect(originalColumnsToSelect);
		
		if (CommonUtilities.getInstance().stringHasValue(simpleValue.getValue())){
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
		

		/*if (utilities.createInstance(searchParams.getRecordClass()).generateTableName().equals("obs")) {
			utilities.logInfo("[EXPORT:obs] SQL = "+ sql, l);
		}*/
		
		return search(searchParams.getRecordClass(), sql, searchClauses.getParameters(), conn);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends VO>  List<T> search(SyncEngine engine, Connection conn) throws DBException{
		SearchClauses<T> searchClauses = (SearchClauses<T>) engine.getSearchParams().generateSearchClauses(conn);
		
		if (engine.getSearchParams().getOrderByFields() != null) {
			searchClauses.addToOrderByFields(engine.getSearchParams().getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(conn);
		
		String sqlToLog = SQLUtilitie.transformPreparedStatmentToFullSQLStatment(sql, searchClauses.getParameters(), conn);
		

		
		List<T> records = (List<T>) search(engine.getSearchParams().getRecordClass(), sql, searchClauses.getParameters(), conn);
		
		if (!utilities.arrayHasElement(records) && utilities.createInstance(engine.getSearchParams().getRecordClass()).generateTableName().equals("obs")) {
			engine.getSyncController().logInfo(sqlToLog);
			
			records = (List<T>) search(engine.getSearchParams().getRecordClass(), sql, searchClauses.getParameters(), conn);
		}
		
		return records;	
	}
}
