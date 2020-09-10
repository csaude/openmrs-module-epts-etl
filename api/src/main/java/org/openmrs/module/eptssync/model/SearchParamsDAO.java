package org.openmrs.module.eptssync.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.VO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.FuncoesGenericas;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

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

	
	public static <T extends VO>  List<T> search(AbstractSearchParams<T> searchParams, Connection conn) throws DBException{
		SearchClauses<T> searchClauses = searchParams.generateSearchClauses(conn);
		
		String sql = searchClauses.generateSQL(conn);
		
		return search(searchParams.getRecordClass(), sql, searchClauses.getParameters(), conn);
	}
}
