package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConditionClauseScope;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents an inner auxiliary table for data extraction. A {@link InnerAuxExtractTable} is used
 * as an auxiliary extraction table for an {@link AuxExtractTable}
 */
public class InnerAuxExtractTable extends AbstractTableConfiguration implements JoinableEntity, EtlDataSource {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private ConditionClauseScope joinExtraConditionScope;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	private AuxExtractTable mainExtractTable;
	
	private boolean doNotUseAsDatasource;
	
	public InnerAuxExtractTable() {
		this.joinExtraConditionScope = ConditionClauseScope.JOIN_CLAUSE;
	}
	
	@Override
	public ConditionClauseScope getJoinExtraConditionScope() {
		return this.joinExtraConditionScope;
	}
	
	@Override
	public void setJoinExtraConditionScope(ConditionClauseScope joinExtraConditionScope) {
		this.joinExtraConditionScope = joinExtraConditionScope;
	}
	
	public boolean isDoNotUseAsDatasource() {
		return doNotUseAsDatasource;
	}
	
	@Override
	public boolean doNotUseAsDatasource() {
		return isDoNotUseAsDatasource();
	}
	
	public void setDoNotUseAsDatasource(boolean doNotUseAsDatasource) {
		this.doNotUseAsDatasource = doNotUseAsDatasource;
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return mainExtractTable.getRelatedConnInfo();
	}
	
	@Override
	public List<FieldsMapping> getJoinFields() {
		return this.joinFields;
	}
	
	@Override
	public TableConfiguration getJoiningEntity() {
		return this.mainExtractTable;
	}
	
	@Override
	public String getJoinExtraCondition() {
		return this.joinExtraCondition;
	}
	
	@Override
	public JoinType getJoinType() {
		return this.joinType;
	}
	
	@Override
	public void setJoinFields(List<FieldsMapping> joinFields) {
		this.joinFields = joinFields;
	}
	
	public AuxExtractTable getMainExtractTable() {
		return mainExtractTable;
	}
	
	@Override
	public void setMainExtractTable(MainJoiningEntity mainJoiningTable) {
		this.mainExtractTable = (AuxExtractTable) mainJoiningTable;
	}
	
	@Override
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	@Override
	public void setJoinExtraCondition(String joinExtraCondition) {
		this.joinExtraCondition = joinExtraCondition;
	}
	
	@Override
	public boolean isMainJoiningEntity() {
		return false;
	}
	
	@Override
	public MainJoiningEntity parseToJoining() throws ForbiddenOperationException {
		throw new ForbiddenOperationException("Not joining entity!!!");
	}
	
	@Override
	public String getName() {
		return this.getTableName();
	}
	
	public void clone(InnerAuxExtractTable toCloneFrom, AuxExtractTable relatedMainExtractTable,
	        EtlDatabaseObject schemaInfoSrc, Connection conn) throws DBException {
		super.clone(toCloneFrom, schemaInfoSrc, conn);
		
		this.setJoinFields(toCloneFrom.getJoinFields());
		this.setJoinExtraCondition(toCloneFrom.getJoinExtraCondition());
		this.setJoinType(toCloneFrom.getJoinType());
		this.setMainExtractTable(relatedMainExtractTable);
		this.setDoNotUseAsDatasource(toCloneFrom.isDoNotUseAsDatasource());
		this.setJoinExtraConditionScope(toCloneFrom.getJoinExtraConditionScope());
	}
	
	public static void tryToReplacePlaceholders(List<InnerAuxExtractTable> auxExtractTable,
	        EtlDatabaseObject schemaInfoSrc) {
		
		if (auxExtractTable != null) {
			for (InnerAuxExtractTable i : auxExtractTable) {
				i.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
	}
	
	@Override
	public void tryToReplacePlaceholdersOnOwnElements(EtlDatabaseObject schemaInfoSrc) {
		FieldsMapping.tryToReplacePlaceholders(getJoinFields(), schemaInfoSrc);
		setJoinExtraCondition(utilities.tryToReplacePlaceholders(getJoinExtraCondition(), schemaInfoSrc));
	}
	
}
