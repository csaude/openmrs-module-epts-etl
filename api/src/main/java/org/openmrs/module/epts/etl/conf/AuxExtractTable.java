package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		super.fullLoad(conn);
		
		tryToLoadJoinFields();
		
		if (utilities.stringHasValue(this.joinExtraCondition)) {
			this.joinExtraCondition = this.joinExtraCondition.replaceAll(getTableName() + "\\.", getTableAlias() + "\\.");
		}
		
		if (utilities.arrayHasNoElement(this.joinFields)) {
			throw new ForbiddenOperationException("No join fields were difined between " + this.getParent().getTableName()
			        + " And " + this.getTableName());
		}
		
		this.joinType = JoinType.LEFT;
	}
	
	/**
	 * 
	 */
	public void tryToLoadJoinFields() {
		if (!utilities.arrayHasElement(this.joinFields)) {
			//Try to autoload join fields
			
			List<FieldsMapping> fm = new ArrayList<>();
			
			//Assuming that this datasource is parent
			List<RefInfo> pInfo = this.getParent().findAllRefToParent(this.getTableName());
			
			if (utilities.arrayHasElement(pInfo)) {
				for (RefInfo ref : pInfo) {
					for (RefMapping map : ref.getMapping()) {
						fm.add(new FieldsMapping(map.getChildField().getName(), "", map.getParentField().getName()));
					}
				}
			} else {
				
				//Assuning that the this data src is child
				pInfo = this.findAllRefToParent(this.getParent().getTableName());
				
				if (utilities.arrayHasElement(pInfo)) {
					for (RefInfo ref : pInfo) {
						for (RefMapping map : ref.getMapping()) {
							fm.add(new FieldsMapping(map.getParentField().getName(), "", map.getChildField().getName()));
						}
					}
				}
			}
			
			if (fm != null) {
				this.joinFields = new ArrayList<>();
				for (FieldsMapping f : fm) {
					this.joinFields.add(f);
				}
			}
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
	public TableDataSourceConfig getParent() {
		return (TableDataSourceConfig) super.getParent();
	}
	
	@Override
	public EtlConfiguration getRelatedSyncConfiguration() {
		return getParent().getRelatedSyncConfiguration();
	}
	
	public String generateConditionsFields() {
		String conditionFields = "";
		
		for (int i = 0; i < this.joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.joinFields.get(i);
			
			conditionFields += getParent().getTableAlias() + "." + field.getSrcField() + " = " + getTableAlias() + "."
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
		return this.getParent().getRelatedAppInfo();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
}
