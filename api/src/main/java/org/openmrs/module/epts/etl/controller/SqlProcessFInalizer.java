package org.openmrs.module.epts.etl.controller;

import java.util.List;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.interfaces.BaseConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

public class SqlProcessFInalizer extends AbstractProcessFinalizer implements BaseConfiguration {
	
	public SqlProcessFInalizer(ProcessController relatedProcessController) {
		super(relatedProcessController);
	}
	
	@Override
	public void performeFinalizationTasks() {
		String sql = getRelatedProcessController().getEtlConf().getFinalizer().getSqlFinalizerQuery();
		
		OpenConnection conn = null;
		
		try {
			if (getRelatedFinalizerConf().getConnectionToUse().isMain()) {
				conn = getRelatedProcessController().tryToOpenMainConnection(this);
			} else if (getRelatedFinalizerConf().getConnectionToUse().isDst()) {
				conn = getRelatedProcessController().tryToOpenMainConnection(this);
			} else {
				conn = getRelatedProcessController().openConnection(this);
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
			finalizeConnection(conn, this);
		}
	}

	@Override
	public List<Extension> getExtension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExtension(List<Extension> extension) {
		// TODO Auto-generated method stub
		
	}
	
}
