package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class SqlProcessFInalizer extends AbstractProcessFinalizer {
	
	public SqlProcessFInalizer(ProcessController relatedProcessController) {
		super(relatedProcessController);
	}
	
	@Override
	public void performeFinalizationTasks() {
		String sql = getRelatedProcessController().getConfiguration().getFinalizer().getSqlFinalizerQuery();
		
		OpenConnection conn = null;
		
		try {
			conn = getRelatedProcessController().openConnection();
			
			if (getRelatedProcessController().getSchemaInfoSrc() != null) {
				sql = DBUtilities.tryToReplaceParamsInQuery(sql, getRelatedProcessController().getSchemaInfoSrc());
			}
			
			BaseDAO.executeQueryWithRetryOnError(sql, null, conn);
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (DBException e) {
			throw new EtlExceptionImpl(e);
		}
		finally {
			if (conn != null) {
				conn.finalizeConnection();
			}
		}
	}
	
}
