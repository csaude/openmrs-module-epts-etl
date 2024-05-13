package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class SharedPkDataSource extends ParentTableImpl implements EtlAdditionalDataSource {
	
	private SrcConf relatedSrcConf;
	
	public SharedPkDataSource() {
	}
	
	@Override
	public String getName() {
		return getTableName();
	}
	
	@Override
	public SrcConf getRelatedSrcConf() {
		return this.relatedSrcConf;
	}
	
	@Override
	public void setRelatedSrcConf(SrcConf relatedSrcConf) {
		this.relatedSrcConf = relatedSrcConf;
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(EtlDatabaseObject mainObject, Connection srcConn, AppInfo srcAppInfo)
	        throws DBException {
		
		return mainObject.getSharedPkObj();
	}
	
	@Override
	public boolean isRequired() {
		return true;
	}
	
	public static SharedPkDataSource generateFromSrcConfSharedPkParent(SrcConf src) throws ForbiddenOperationException {
		
		if (!src.useSharedPKKey())
			throw new ForbiddenOperationException("The source table '" + src.getTableName() + "' does not use shared pk!");
		
		
		ParentTable parent = src.getSharedKeyRefInfo();
		
		SharedPkDataSource ds = new SharedPkDataSource();
		
		ds.clone(parent);
		
		ds.relatedSrcConf = src;
		
		return ds;
	}
}
