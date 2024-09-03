package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents an auxiliary table for data extraction. A {@link AuxExtractTable} is used as an
 * auxiliary extraction table usually used to include additional extraction conditions
 */
public class AuxExtractTable extends AbstractTableConfiguration implements JoinableEntity, MainJoiningEntity {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	private MainJoiningEntity mainExtractTable;
	
	private List<InnerAuxExtractTable> auxExtractTable;
	
	public List<InnerAuxExtractTable> getAuxExtractTable() {
		return auxExtractTable;
	}
	
	public void setAuxExtractTable(List<InnerAuxExtractTable> auxExtractTable) {
		this.auxExtractTable = auxExtractTable;
	}
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	@Override
	public void setMainExtractTable(MainJoiningEntity mainExtractTable) {
		this.mainExtractTable = mainExtractTable;
	}
	
	@Override
	public MainJoiningEntity getMainExtractTable() {
		return mainExtractTable;
	}
	
	public String getJoinExtraCondition() {
		return joinExtraCondition;
	}
	
	public void setJoinExtraCondition(String joinExtraCondition) {
		this.joinExtraCondition = joinExtraCondition;
	}
	
	public List<FieldsMapping> getJoinFields() {
		return joinFields;
	}
	
	public void setJoinFields(List<FieldsMapping> joinFields) {
		this.joinFields = joinFields;
	}
	
	@Override
	public AbstractTableConfiguration getParentConf() {
		return (AbstractTableConfiguration) getMainExtractTable();
	}
	
	@Override
	public EtlConfiguration getRelatedEtlConf() {
		return getParentConf().getRelatedEtlConf();
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return this.getParentConf().getRelatedConnInfo();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public TableConfiguration getJoiningEntity() {
		return (TableConfiguration) mainExtractTable;
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		
		setRelatedEtlConfig(this.mainExtractTable.getRelatedEtlConf());
		
		loadJoinElements(conn);
		
		loadAlias();
		
		tryToLoadAuxExtraJoinTable(conn);
	}
	
	@Override
	public boolean isJoinable() {
		return true;
	}
	
	@Override
	public JoinableEntity parseToJoinable() throws ForbiddenOperationException {
		return this;
	}
	
}
