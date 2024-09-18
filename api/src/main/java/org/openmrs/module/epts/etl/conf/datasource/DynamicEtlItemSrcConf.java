package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class represent a table which can act as source of {@link SrcConf} or {@link DstConf}. This
 * allow the dynamic configuration of {@link EtlItemConfiguration}
 */
public class DynamicEtlItemSrcConf extends AbstractTableConfiguration implements MainJoiningEntity {
	
	private List<AuxExtractTable> auxExtractTable;
	
	private EtlItemConfiguration relatedItemConf;
	
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
	
	public EtlItemConfiguration getRelatedItemConf() {
		return relatedItemConf;
	}
	
	public void setRelatedItemConf(EtlItemConfiguration relatedItemConf) {
		this.relatedItemConf = relatedItemConf;
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		this.tryToLoadAuxExtraJoinTable(conn);
		
		this.setFullLoaded(true);
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return this.getRelatedItemConf().getSrcConnInfo();
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
