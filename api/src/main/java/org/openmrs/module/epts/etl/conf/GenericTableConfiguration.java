package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class GenericTableConfiguration extends AbstractTableConfiguration {
	
	private AbstractTableConfiguration relatedTableConf;
	
	public GenericTableConfiguration() {
	}
	
	@Override
	public EtlConfiguration getRelatedEtlConf() {
		if (super.getRelatedEtlConf() != null) {
			return super.getRelatedEtlConf();
		} else if (this.getRelatedTableConf() != null) {
			return this.getRelatedTableConf().getRelatedEtlConf();
		} else {
			return null;
		}
		
	}
	
	public AbstractTableConfiguration getRelatedTableConf() {
		return relatedTableConf;
	}
	
	public GenericTableConfiguration(String tableName) {
		setTableName(tableName);
	}
	
	public GenericTableConfiguration(String tableName, TableConfiguration relatedTableConf) {
		this(tableName);
		
		this.relatedTableConf = (AbstractTableConfiguration) relatedTableConf;
		
		if (this.relatedTableConf.isGeneric()) {
			throw new ForbiddenOperationException(
			        "The generic table Conf cannot be related to another generic configuration");
		}
		
	}
	
	@Override
	public boolean isGeneric() {
		return true;
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		
		if (this.relatedTableConf == null) {
			throw new ForbiddenOperationException("The generic table Conf should have a related to use this method");
			
		}
		
		return this.relatedTableConf.getRelatedConnInfo();
	}
	
	@Override
	public Class<? extends EtlDatabaseObject> getSyncRecordClass(DBConnectionInfo application)
	        throws ForbiddenOperationException {
		return GenericDatabaseObject.class;
	}
	
	@Override
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void tryToReplacePlaceholdersOnOwnElements(EtlDatabaseObject schemaInfoSrc) {
		// TODO Auto-generated method stub
		
	}
}
