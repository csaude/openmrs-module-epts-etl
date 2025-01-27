package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.types.ConditionClauseScope;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
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
	
	ConditionClauseScope getJoinExtraConditionScope();
	
	void setJoinExtraConditionScope(ConditionClauseScope joinExtraConditionScope);
	
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
			
			if (field.hasSrcField() && field.hasDstField()) {
				conditionFields += getTableAlias() + "." + field.getSrcField() + " = " + getJoiningEntity().getTableAlias()
				        + "." + field.getDstField();
			} else if (field.hasSrcField() && field.hasSrcValue()) {
				conditionFields += getTableAlias() + "." + field.getSrcField() + " = " + field.getSrcValue();
			} else if (field.hasDstField() && field.hasDstValue()) {
				conditionFields += getJoiningEntity().getTableAlias() + "." + field.getDstField() + " = "
				        + field.getDstValue();
			} else {
				throw new ForbiddenOperationException(
				        "The join fields mapping must either have 'srcField and dstField', 'srcField and srcValue' or 'dstField and dstValue'");
			}
			
		}
		
		if (this.getJoinExtraConditionScope().isJoinClause() && this.getJoinExtraCondition() != null
		        && !this.getJoinExtraCondition().isEmpty()) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	default void loadJoinElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		tryToLoadJoinFields();
		
		if (hasJoinExtraCondition() && !isUsingManualDefinedAlias()) {
			this.setJoinExtraCondition(
			    this.getJoinExtraCondition().replaceAll(getTableName() + "\\.", getTableAlias() + "\\."));
			
			if (schemaInfo != null) {
				this.setJoinExtraCondition(DBUtilities.tryToReplaceParamsInQuery(this.getJoinExtraCondition(), schemaInfo));
			}
		}
		
		if (!hasJoinFields()) {
			throw new ForbiddenOperationException("No join fields were difined between "
			        + this.getJoiningEntity().getTableName() + " And " + this.getTableName());
		} else {
			
			for (FieldsMapping joiningField : this.getJoinFields()) {
				if (schemaInfo != null) {
					joiningField.setSrcField(DBUtilities.tryToReplaceParamsInQuery(joiningField.getSrcField(), schemaInfo));
					joiningField.setSrcValue(DBUtilities.tryToReplaceParamsInQuery(joiningField.getSrcValue(), schemaInfo));
					joiningField.setDstField(DBUtilities.tryToReplaceParamsInQuery(joiningField.getDstField(), schemaInfo));
					joiningField.setDstValue(DBUtilities.tryToReplaceParamsInQuery(joiningField.getDstValue(), schemaInfo));
				}
			}
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
