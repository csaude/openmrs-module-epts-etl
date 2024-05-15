package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents an auxiliary table for data extraction. A {@link AuxExtractTable} is used as an
 * auxiliary extraction trable usually used to include additional extraction conditions
 */
public class AuxExtractTable extends AbstractTableConfiguration implements JoinableEntity {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	private EtlDataSource mainExtractTable;
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	public void setMainExtractTable(EtlDataSource mainExtractTable) {
		this.mainExtractTable = mainExtractTable;
	}
	
	public EtlDataSource getMainExtractTable() {
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
	public EtlConfiguration getRelatedSyncConfiguration() {
		return getParentConf().getRelatedSyncConfiguration();
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return this.getParentConf().getRelatedAppInfo();
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
	public JoinType determineJoinType() {
		if (utilities.arrayHasMoreThanOneElements(this.getMainExtractTable().getSelfJoinTables())) {
			return JoinType.LEFT;
		} else {
			return JoinType.INNER;
		}
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		loadJoinElements(conn);
	}
}
