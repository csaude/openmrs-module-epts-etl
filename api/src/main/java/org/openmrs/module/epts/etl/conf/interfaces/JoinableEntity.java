package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

/**
 * Represents an database object which can be joined to other database object (the
 * {@link MainJoiningEntity})
 */
public interface JoinableEntity extends TableConfiguration, EtlDataSource {
	
	List<FieldsMapping> getJoinFields();
	
	String getTableAlias();
	
	TableConfiguration getJoiningEntity();
	
	String getJoinExtraCondition();
	
	JoinType getJoinType();
	
	boolean doNotUseAsDatasource();
	
	void setJoinFields(List<FieldsMapping> joinFields);
	
	void setJoinType(JoinType joinType);
	
	void setJoinExtraCondition(String joinExtraCondition);
	
	void setMainExtractTable(MainJoiningEntity mainJoiningTable);
	
	/**
	 * Tells whether this joinable entity is also joining ({@link MainJoiningEntity}) or not.
	 * Example of known main joining entities are {@link AuxExtractTable}
	 * 
	 * @return 'true' if this joinable entity is also main joining entity
	 */
	boolean isMainJoiningEntity();
	
	/**
	 * @return return this joinable entity as {@link MainJoiningEntity}
	 * @throws ForbiddenOperationException if this main joinable entity is not joining
	 */
	MainJoiningEntity parseToJoining() throws ForbiddenOperationException;
	
	MainJoiningEntity getMainExtractTable();
	
	default boolean hasJoinExtraCondition() {
		return utilities.stringHasValue(this.getJoinExtraCondition());
	}
	
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
	
	default void loadAlias() {
		if (hasJoinExtraCondition() && !isUsingManualDefinedAlias()) {
			this.setJoinExtraCondition(
			    this.getJoinExtraCondition().replaceAll(getTableName() + "\\.", getTableAlias() + "\\."));
			
			this.setJoinExtraCondition(this.getJoinExtraCondition().replaceAll(
			    this.getMainExtractTable().getObjectName() + "\\.", this.getMainExtractTable().getAlias() + "\\."));
			
			String condition = DBUtilities.tryToPutTableNameInFieldsInASqlClause(this.getJoinExtraCondition(),
			    this.getTableAlias(), this.getFields());
			
			this.setJoinExtraCondition(condition);
		}
	}
	
	default JoinType determineJoinType() {
		if (utilities.arrayHasMoreThanOneElements(this.getMainExtractTable().getJoiningTable())) {
			return JoinType.LEFT;
		} else {
			return JoinType.INNER;
		}
	}
	
}
