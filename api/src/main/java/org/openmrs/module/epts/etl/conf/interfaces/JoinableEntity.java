package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public interface JoinableEntity extends TableConfiguration {
	
	List<FieldsMapping> getJoinFields();
	
	String getTableAlias();
	
	TableConfiguration getJoiningEntity();
	
	String getJoinExtraCondition();
	
	JoinType getJoinType();
	
	void setJoinFields(List<FieldsMapping> tryToLoadJoinFields);
	
	default void addJoinField(FieldsMapping fm) {
		if (this.getJoinFields() == null) {
			this.setJoinFields(new ArrayList<>());
		}
		
		this.getJoinFields().add(fm);
	}
	
	default boolean hasJoinType() {
		return this.getJoinType() != null;
	}
	
	default boolean hasJoinFields() {
		return this.getJoinFields() != null && this.getJoinFields().size() > 0;
	}
	
	default void tryToLoadJoinFields() {
		if (!hasJoinFields()) {
			this.setJoinFields(this.tryToLoadJoinFields(getJoiningEntity()));
		}
	}
	
	default String generateJoinConditionsFields() {
		String conditionFields = "";
		
		for (int i = 0; i < this.getJoinFields().size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.getJoinFields().get(i);
			
			conditionFields += getTableAlias() + "." + field.getSrcField() + " = " + getJoiningEntity().getTableAlias() + "."
			        + field.getDstField();
		}
		
		if (this.getJoinExtraCondition() != null && !this.getJoinExtraCondition().isEmpty()) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	default void loadJoinElements(Connection conn) throws DBException {
		tryToLoadJoinFields();
		
		if (hasJoinExtraCondition()) {
			this.setJoinExtraCondition(
			    this.getJoinExtraCondition().replaceAll(getTableName() + "\\.", getTableAlias() + "\\."));
		}
		
		if (!hasJoinFields()) {
			throw new ForbiddenOperationException("No join fields were difined between "
			        + this.getJoiningEntity().getTableName() + " And " + this.getTableName());
		}
		
		if (!hasJoinType()) {
			setJoinType(determineJoinType());
		}
	}
	
	JoinType determineJoinType();
	
	void setJoinType(JoinType joinType);
	
	void setJoinExtraCondition(String replaceAll);
	
	/**
	 * @return
	 */
	default boolean hasJoinExtraCondition() {
		return utilities.stringHasValue(this.getJoinExtraCondition());
	}
	
}
