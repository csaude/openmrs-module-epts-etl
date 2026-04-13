package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class represent a table which can act as source of {@link EtlConfiguration}. This allow the
 * dynamic configuration of {@link EtlConfiguration}
 */
public class EtlConfigurationSrcConf extends SrcConf {
	
	@Override
	public Boolean isGeneric() {
		return false_();
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration parent) {
		super.setParentConf(parent);
	}
	
	@Override
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		this.tryToLoadAuxExtraJoinTable(schemaInfo, conn);
		
		this.setFullLoaded(true);
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return (this.getParentConf()).getRelatedEtlConf().getMainConnInfo();
	}
	
	@Override
	public List<? extends JoinableEntity> getJoiningTable() {
		return this.getAuxExtractTable();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setJoiningTable(List<? extends JoinableEntity> joiningTable) {
		setAuxExtractTable((List<AuxExtractTable>) joiningTable);
	}
	
	@Override
	public Boolean isJoinable() {
		return false_();
	}
	
	@Override
	public Boolean doNotUseAsDatasource() {
		return false_();
	}
	
	@Override
	public JoinableEntity parseToJoinable() throws ForbiddenOperationException {
		throw new ForbiddenOperationException("Not joinable entity!!!");
	}
}
