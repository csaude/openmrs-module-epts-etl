package org.openmrs.module.eptssync.controller.conf;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Define the refencial information betwen a {@link SyncTableInfo} and its main parent;
 * 
 * @author jpboane
 *
 */
public class ParentRefInfo {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	
	private String referenceColumnName;
	private String referencedColumnName;
	private String tableName;
	private SyncTableInfo tableInfo;
	
	private Class<OpenMRSObject> parentClass;
	
	/*
	 * Indicate if this parent is metadata or not
	 */
	private boolean metadata;
	
	/*
	 * Indicate if this parent can be ignored if not found in referenced table or not
	 */
	private boolean ignorable;
	
	/*
	 * Indicate if this parent's PK is the same with the main table.
	 * EX: The patient table and person, share the same primary key
	 */
	private boolean sharedPk;
	
	public ParentRefInfo() {
	}
	
	/*
	public ParentRefInfo(String referenceColumnName, String referencedColumnName, String tableName, boolean ignorable, SyncTableInfo tableInfo) {
		this.referenceColumnName = referenceColumnName;
		this.referencedColumnName = referencedColumnName;
		this.tableName = tableName;
		this.tableInfo = tableInfo;
		this.ignorable = ignorable;
	}
	*/
	
	public String getReferenceColumnName() {
		return referenceColumnName;
	}

	public void setReferenceColumnName(String referenceColumnName) {
		this.referenceColumnName = referenceColumnName;
	}

	public String getReferenceColumnAsClassAttName() {
		return utilities.convertTableAttNameToClassAttName(this.getReferenceColumnName());
	}
	
	public String getReferencedColumnName() {
		return referencedColumnName;
	}

	public void setReferencedColumnName(String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public SyncTableInfo getTableInfo() {
		return tableInfo;
	}
	
	public void setTableInfo(SyncTableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public String getFullParentReferencedColumn() {
		return  this.getTableName() + "." + this.getReferencedColumnName();
	}

	public String getFullReferenceColumn() {
		return  this.getTableInfo().getTableName() + "." + this.getReferenceColumnName();
	}
	
	public boolean isIgnorable() {
		return ignorable;
	}
	
	public void setIgnorable(boolean ignorable) {
		this.ignorable = ignorable;
	}

	public boolean isMetadata() {
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}
	
	public boolean isSharedPk() {
		return sharedPk;
	}

	public void setSharedPk(boolean sharedPk) {
		this.sharedPk = sharedPk;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Class<OpenMRSObject> determineParentClass(){
		try {
			if (this.parentClass != null) return this.parentClass;
			
			if (this.tableName == null)
				throw new ForbiddenOperationException("No main parent info defined!");

			String fullClassName = "org.openmrs.module.eptssync.model.openmrs." + generateClassName(this.tableName);
			
			this.parentClass = (Class<OpenMRSObject>) Class.forName(fullClassName);

			return this.parentClass;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}

	public String getParentFullClassName() {
		return this.determineParentClass().getCanonicalName();
	}
	
	private String generateClassName(String tableName) {
		String[] nameParts = tableName.split("_");

		String className = utilities.capitalize(nameParts[0]);

		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}

		return className + "VO";
	}
	
	@Override
	public String toString() {
		return "TABLE: " + this.tableName + ", REFECENCE: " + this.referenceColumnName + ", REFERENCEDE: " + this.referencedColumnName;
	}
}
