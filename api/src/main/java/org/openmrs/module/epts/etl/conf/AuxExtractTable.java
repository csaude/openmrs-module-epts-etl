package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

/**
 * Represents an auxiliary table for data extraction. A {@link AuxExtractTable} is used as an
 * auxiliary extraction trable usually used to include additional extraction conditions
 */
public class AuxExtractTable extends AbstractTableConfiguration {
	
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
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		super.fullLoad(conn);
		
		tryToLoadJoinFields();
		
		if (utilities.stringHasValue(this.joinExtraCondition)) {
			this.joinExtraCondition = this.joinExtraCondition.replaceAll(getTableName() + "\\.", getTableAlias() + "\\.");
		}
		
		if (utilities.arrayHasNoElement(this.joinFields)) {
			throw new ForbiddenOperationException("No join fields were difined between "
			        + this.getParentConf().getTableName() + " And " + this.getTableName());
		}
		
		if (!hasJoinType()) {
			if (getMainExtractTable().getSelfJoinTables().size() == 1) {
				this.joinType = JoinType.INNER;
			} else {
				this.joinType = JoinType.LEFT;
			}
		}
	}
	
	public boolean hasJoinType() {
		return this.getJoinType() != null;
	}
	
	public boolean hasJoinFields() {
		return utilities.arrayHasElement(this.joinFields);
	}
	
	public void tryToLoadJoinFields() {
		if (!hasJoinFields()) {
			this.joinFields = tryToLoadJoinFields(this.getParentConf());
		}
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
		return (AbstractTableConfiguration) super.getParentConf();
	}
	
	@Override
	public EtlConfiguration getRelatedSyncConfiguration() {
		return getParentConf().getRelatedSyncConfiguration();
	}
	
	public String generateConditionsFields() {
		String conditionFields = "";
		
		for (int i = 0; i < this.joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.joinFields.get(i);
			
			conditionFields += getParentConf().getTableAlias() + "." + field.getSrcField() + " = " + getTableAlias() + "."
			        + field.getDstField();
		}
		
		if (utilities.stringHasValue(this.getJoinExtraCondition())) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	public void addJoinField(FieldsMapping fm) {
		if (this.joinFields == null) {
			this.joinFields = new ArrayList<>();
		}
		
		this.joinFields.add(fm);
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return this.getParentConf().getRelatedAppInfo();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
}
