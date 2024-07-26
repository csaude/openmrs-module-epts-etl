package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

/**
 * This search params allow the count query in multi-thread in a {@link PreparedQuery}
 */
public class PreparedCountQuerySearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	PreparedQuery preparedQuery;
	
	public PreparedCountQuerySearchParams(PreparedQuery preparedQuery) {
		super(null, null);
		
		this.preparedQuery = preparedQuery;
	}
	
	public PreparedQuery getPreparedQuery() {
		return preparedQuery;
	}
	
	public TableConfiguration getMainTableConf() {
		return getPreparedQuery().getCountFunctionInfo().getMainTable();
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord intervalExtremeRecord,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addColumnToSelect("1");
		
		searchClauses.addToClauseFrom(
		    DBUtilities.extractFromClauseOnSqlSelectQuery(this.getPreparedQuery().generatePreparedQuery()));
		searchClauses
		        .addToClauses(DBUtilities.extractWhereClauseInASelectQuery(this.getPreparedQuery().generatePreparedQuery()));
		
		TableConfiguration tabConf = this.getPreparedQuery().getCountFunctionInfo().getMainTable();
		
		searchClauses.addToClauses(
		    tabConf.getTableAlias() + "." + tabConf.getPrimaryKey().retrieveSimpleKeyColumnName() + " between ? and ?");
		
		searchClauses.addToParameters(this.getPreparedQuery().generateQueryParameters());
		
		searchClauses.addToParameters(intervalExtremeRecord.getMinRecordId());
		searchClauses.addToParameters(intervalExtremeRecord.getMaxRecordId());
		
		return searchClauses;
	}
	
}
