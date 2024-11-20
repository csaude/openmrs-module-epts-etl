package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents any table related to etl configuration. Ex: "table_operation_progress_info",
 * "inconsistence_info"
 */
public class EtlConfigurationTableConf extends AbstractTableConfiguration {
	
	public EtlConfigurationTableConf(String tableName, EtlConfiguration relatedConf) {
		super.setTableName(tableName);
		
		setRelatedEtlConfig(relatedConf);
		setSchema(getSyncStageSchema());
		
		setOnConflict(ConflictResolutionType.KEEP_EXISTING);
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public Class<? extends EtlDatabaseObject> getSyncRecordClass(DBConnectionInfo connInfo)
	        throws ForbiddenOperationException {
		return GenericDatabaseObject.class;
	}
	
	@Override
	public void fullLoad(Connection conn) throws DBException {
		setIgnorableFields(utilities.parseToList("creation_date"));
		
		super.fullLoad(conn);
	}
	
	@Override
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return getSrcConnInfo();
	}
	
	@Override
	public void generateRecordClass(DBConnectionInfo app, boolean fullClass) {
		// TODO Auto-generated method stub
		
	}
}
