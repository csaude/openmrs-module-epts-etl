package org.openmrs.module.eptssync.controller.conf;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.OpenMRSObject;
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
	
	private boolean isFullLoaded;
	
	public ParentRefInfo() {
		this.isFullLoaded = false;
	}
	
	public String getReferenceColumnName() {
		loadFullRefInfo();
		
		return referenceColumnName;
	}

	public void setReferenceColumnName(String referenceColumnName) {
		this.referenceColumnName = referenceColumnName;
	}

	public String getReferenceColumnAsClassAttName() {
		loadFullRefInfo();
		
		return utilities.convertTableAttNameToClassAttName(this.getReferenceColumnName());
	}
	
	public String getReferencedColumnName() {
		loadFullRefInfo();
		
		return referencedColumnName;
	}

	public void setReferencedColumnName(String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
	}
	
	public String getTableName() {
		loadFullRefInfo();
		
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
	
	public synchronized void loadFullRefInfo() {
		if (isFullLoaded) return;
		
		if (!utilities.stringHasValue(this.referenceColumnName)) {
			this.setReferenceColumnName(this.tableName + "_id");
		}
		
		if (!utilities.stringHasValue(this.referencedColumnName)) {
			this.setReferencedColumnName(this.tableName + "_id");
		}
		
		this.isFullLoaded = true;
	}
	
	public String getFullParentReferencedColumn() {
		loadFullRefInfo();
		
		return  this.getTableName() + "." + this.getReferencedColumnName();
	}

	public String getFullReferenceColumn() {
		loadFullRefInfo();
		
		return  this.getTableInfo().getTableName() + "." + this.getReferenceColumnName();
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Class<OpenMRSObject> determineParentClass(){
		loadFullRefInfo();
		
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
		loadFullRefInfo();
		
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
}
