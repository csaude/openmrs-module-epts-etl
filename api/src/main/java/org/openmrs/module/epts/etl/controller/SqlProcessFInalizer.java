package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

public class SqlProcessFInalizer extends AbstractProcessFinalizer {
	
	public SqlProcessFInalizer(ProcessController relatedProcessController) {
		super(relatedProcessController);
	}
	
	@Override
	public void performeFinalizationTasks() {
		String sql = getRelatedProcessController().getEtlConf().getFinalizer().getSqlFinalizerQuery();
		
		OpenConnection conn = null;
		
		try {
			if (getRelatedFinalizerConf().getConnectionToUse().isMain()) {
				conn = getRelatedProcessController().tryToOpenMainConnection();
			} else if (getRelatedFinalizerConf().getConnectionToUse().isDst()) {
				conn = getRelatedProcessController().tryToOpenMainConnection();
			} else {
				conn = getRelatedProcessController().openConnection();
			}
			
			if (getRelatedProcessController().getSchemaInfoSrc() != null) {
				sql = SQLUtilities.tryToReplaceParamsInQuery(sql, getRelatedProcessController().getSchemaInfoSrc())
				        .toString();
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
