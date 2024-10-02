package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class represent a table which can act as source of {@link EtlConfiguration}. This allow the
 * dynamic configuration of {@link EtlConfiguration}
 */
public class EtlConfigurationSrcConf extends SrcConf {
	
	private List<AuxExtractTable> auxExtractTable;
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	public List<AuxExtractTable> getAuxExtractTable() {
		return auxExtractTable;
	}
	
	public void setAuxExtractTable(List<AuxExtractTable> auxExtractTable) {
		this.auxExtractTable = auxExtractTable;
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration parent) {
		/*if (!(parent instanceof EtlConfiguration))
			throw new ForbiddenOperationException(
			        "Only 'EtlConfiguration' is allowed to be a parent of an EtlConfigurationSrcConf");*/
		
		super.setParentConf(parent);
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		this.tryToLoadAuxExtraJoinTable(conn);
		
		this.setFullLoaded(true);
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return ((EtlConfiguration) this.getParentConf()).getMainConnInfo();
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
	public boolean isJoinable() {
		return false;
	}
	
	@Override
	public boolean doNotUseAsDatasource() {
		return false;
	}
	
	@Override
	public JoinableEntity parseToJoinable() throws ForbiddenOperationException {
		throw new ForbiddenOperationException("Not joinable entity!!!");
	}
}
