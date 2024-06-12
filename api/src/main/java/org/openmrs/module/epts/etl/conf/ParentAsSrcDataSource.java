package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ParentAsSrcDataSource extends ParentTableImpl implements EtlAdditionalDataSource {
	
	private SrcConf relatedSrcConf;
	
	public ParentAsSrcDataSource() {
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
	
	public static ParentAsSrcDataSource generateFromSrcConfSharedPkParent(SrcConf mainSrcConf, ParentTable parent,
	        Connection conn) throws ForbiddenOperationException, DBException {
		ParentAsSrcDataSource ds = new ParentAsSrcDataSource();
		
		ds.setChildTableConf(parent.getChildTableConf());
		
		ds.clone(parent, conn);
		
		ds.relatedSrcConf = mainSrcConf;
		
		ds.setRefCode(parent.getRefCode());
		ds.setRefMapping(parent.getRefMapping());
		
		ds.setConditionalFields(parent.getConditionalFields());
		ds.setDefaultValueDueInconsistency(parent.getDefaultValueDueInconsistency());
		ds.setSetNullDueInconsistency(parent.isSetNullDueInconsistency());
		
		return ds;
	}
	
	@Override
	public List<AuxExtractTable> getSelfJoinTables() {
		return null;
	}
	
	@Override
	public void setSelfJoinTables(List<AuxExtractTable> setSelfJoinTables) {
	}
}
