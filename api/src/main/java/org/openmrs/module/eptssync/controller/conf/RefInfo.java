package org.openmrs.module.eptssync.controller.conf;

import java.sql.Connection;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Define the refencial information betwen a {@link SyncTableConfiguration} and its main parent;
 * 
 * @author jpboane
 *
 */
public class RefInfo {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	public static final String PARENT_REF_TYPE = "PARENT";
	public static final String CHILD_REF_TYPE = "CHILD";
	
	private SyncTableConfiguration refTableConfiguration;
	private SyncTableConfiguration relatedSyncTableConfiguration;
	
	private boolean setNullDueInconsistency;
	
	private Integer defaultValueDueInconsistency;
	private String tableName;
	
	private String refColumnName;
	private String refColumnType;
	
	private String refType;
	
	private String conditionField;
	private Integer conditionValue;
	
	/*
	 * Indicate if this parent can be ignored if not found in referenced table or not
	 */
	private boolean ignorable;
	
	public RefInfo() {
		this.refType = PARENT_REF_TYPE;
	}
	
	public boolean isSetNullDueInconsistency() {
		return setNullDueInconsistency;
	}
	
	public String getConditionField() {
		return conditionField;
	}
	
	public void setConditionField(String conditionField) {
		this.conditionField = conditionField;
	}
	
	public Integer getConditionValue() {
		return conditionValue;
	}
	
	
	public void setConditionValue(Integer conditionValue) {
		this.conditionValue = conditionValue;
	}
	
	public void setSetNullDueInconsistency(boolean setNullDueInconsistency) {
		this.setNullDueInconsistency = setNullDueInconsistency;
	}

	public void setRelatedSyncTableConfiguration(SyncTableConfiguration relatedSyncTableConfiguration) {
		this.relatedSyncTableConfiguration = relatedSyncTableConfiguration;
	}
	
	@JsonIgnore
	public SyncTableConfiguration getRelatedSyncTableConfiguration() {
		return relatedSyncTableConfiguration;
	}
	
	@JsonIgnore
	public String getRefType() {
		return refType;
	}
	
	@JsonIgnore
	public Class<DatabaseObject> getRefObjectClass(AppInfo application) {
		return this.refTableConfiguration.getSyncRecordClass(application);
	}
	
	public void setRefType(String refType) {
		if (!utilities.isStringIn(refType, CHILD_REF_TYPE, PARENT_REF_TYPE)) {
			throw new ForbiddenOperationException("The RefInfo Type must be in [" + PARENT_REF_TYPE + ", " + CHILD_REF_TYPE + "]");
		}
		
		this.refType = refType;
	}
	
	public Integer getDefaultValueDueInconsistency() {
		return defaultValueDueInconsistency;
	}
	
	@JsonIgnore
	public SyncTableConfiguration getRefTableConfiguration() {
		return refTableConfiguration;
	}

	public void setRefTableConfiguration(SyncTableConfiguration refTableConfiguration) {
		this.refTableConfiguration = refTableConfiguration;
		
		this.tableName = refTableConfiguration.getTableName();
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setDefaultValueDueInconsistency(Integer defaultValueDueInconsistency) {
		this.defaultValueDueInconsistency = defaultValueDueInconsistency;
	}

	@JsonIgnore
	public boolean isNumericRefColumn() {
		return AttDefinedElements.isNumeric(getRefColumnType());
	}
	
	public String getRefColumnName() {
		return refColumnName;
	}
	
	public void setRefColumnName(String refColumnName) {
		this.refColumnName = refColumnName;
	}
	
	@JsonIgnore
	public String getRefColumnAsClassAttName() {
		return utilities.convertTableAttNameToClassAttName(this.getRefColumnName());
	}
	
	@JsonIgnore
	public String getRefConditionFieldAsClassAttName() {
		return utilities.convertTableAttNameToClassAttName(this.getConditionField());
	}
	
	public String getRefColumnType() {
		return refColumnType;
	}
	
	public void setRefColumnType(String refColumnType) {
		this.refColumnType = refColumnType;
	}

	public String getFullReferencedColumn(Connection conn) {
		return  this.getRefTableConfiguration().getTableName() + "." + this.getRefColumnName();
	}
	
	public boolean isIgnorable() {
		return ignorable;
	}
	
	public void setIgnorable(boolean ignorable) {
		this.ignorable = ignorable;
	}

	@JsonIgnore
	public boolean isSharedPk() {
		if (getRefTableConfiguration().getSharePkWith() == null) {
			return false;
		}
		else
		for (RefInfo refInfo : getRefTableConfiguration().getParents()) {
			if (refInfo.getRefTableConfiguration().getTableName().equalsIgnoreCase(this.getRefTableConfiguration().getSharePkWith())) {
				return true;
			}
		}
		
		throw new ForbiddenOperationException("The related table of shared pk " + this.getRefTableConfiguration().getSharePkWith() + " of table " + this.getRefTableConfiguration().getTableName() + " is not listed inparents!");
	}
	
	@JsonIgnore
	public boolean isParent() {
		return this.refType.equals(PARENT_REF_TYPE);
	}
	
	@JsonIgnore
	public boolean isChild() {
		return this.refType.equals(CHILD_REF_TYPE);
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String str = "[TYPE: " + this.refType;
		
		if (isChild()) {
			str += " REF: " + getTableName() + "." + this.getRefColumnName() + " > "  + this.getRelatedSyncTableConfiguration().getTableName() + "." + this.getRelatedSyncTableConfiguration().getPrimaryKey()  + "]";
		}
		else {
			str += " REF: " + this.getRelatedSyncTableConfiguration().getTableName() + "." + this.getRefColumnName() + " > "  +  getTableName() + "." + this.getRefTableConfiguration().getPrimaryKey()  + "]";
		}
		
		return str;
	}

	public static RefInfo init(String tableName) {
		RefInfo refInfo = new RefInfo();
		refInfo.setTableName(tableName);
		
		return refInfo;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof RefInfo)) return false;
		
		RefInfo other = (RefInfo)obj;
		
		String thisRefCol = this.getRefColumnName() != null ? this.getRefColumnName() : "";
		
		return thisRefCol.equals(other.getRefColumnName()) && this.tableName.equalsIgnoreCase(other.getTableName());
	}
}
