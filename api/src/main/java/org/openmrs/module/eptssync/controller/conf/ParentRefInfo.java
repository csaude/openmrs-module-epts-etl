package org.openmrs.module.eptssync.controller.conf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.OpenMRSClassGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Define the refencial information betwen a {@link SyncTableConfiguration} and its main parent;
 * 
 * @author jpboane
 *
 */
public class ParentRefInfo {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	private SyncTableConfiguration referenceTableInfo;
	private String referenceColumnName;
	private Class<OpenMRSObject> relatedReferenceClass;
	
	private SyncTableConfiguration referencedTableInfo;
	private String referencedColumnName;
	private Class<OpenMRSObject> relatedReferencedClass;
	
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
	
	public String refColumnType;
	
	public ParentRefInfo() {
	}
	
	public String getRefColumnType() {
		return refColumnType;
	}
	
	public void setRefColumnType(String refColumnType) {
		this.refColumnType = refColumnType;
	}
	public String getReferenceColumnName() {
		return referenceColumnName;
	}

	public boolean isNumericRefColumn() {
		return AttDefinedElements.isNumeric(this.refColumnType);
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
	
	public SyncTableConfiguration getReferenceTableInfo() {
		return referenceTableInfo;
	}
	
	public void setReferenceTableInfo(SyncTableConfiguration referenceTableInfo) {
		this.referenceTableInfo = referenceTableInfo;
	}
	
	public SyncTableConfiguration getReferencedTableInfo() {
		return referencedTableInfo;
	}
	
	public void setReferencedTableInfo(SyncTableConfiguration referencedTableInfo) {
		this.referencedTableInfo = referencedTableInfo;
	}

	public String getFullReferencedColumn() {
		return  this.getReferencedTableInfo().getTableName() + "." + this.getReferencedColumnName();
	}

	public String getFullReferenceColumn() {
		return  this.getReferenceTableInfo().getTableName() + "." + this.getReferenceColumnName();
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
	public Class<OpenMRSObject> determineRelatedReferencedClass(){
		try {
			if (this.relatedReferencedClass != null) return this.relatedReferencedClass;
			
			if (this.referencedTableInfo == null)
				throw new ForbiddenOperationException("No referenced parent info defined!");

			String fullClassName = "org.openmrs.module.eptssync.model.openmrs." + getReferencedTableInfo().getClasspackage() + "." + generateRelatedReferencedClassName();
			
			this.relatedReferencedClass = (Class<OpenMRSObject>) Class.forName(fullClassName);

			return this.relatedReferencedClass;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Class<OpenMRSObject> determineRelatedReferenceClass(){
		try {
			if (this.relatedReferenceClass != null) return this.relatedReferenceClass;
			
			if (this.referenceTableInfo == null)
				throw new ForbiddenOperationException("No reference parent info defined!");

			String fullClassName = "org.openmrs.module.eptssync.model.openmrs." + getReferenceTableInfo().getClasspackage() + "." + generateRelatedReferenceClassName();
			
			this.relatedReferenceClass = (Class<OpenMRSObject>) Class.forName(fullClassName);

			return this.relatedReferenceClass;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean existsRelatedReferenceClass() {
		try {
			return determineRelatedReferenceClass() != null;
		} catch (RuntimeException e) {
			return false;
		}
	}
	
	public boolean existsRelatedReferencedClass() {
		try {
			return determineRelatedReferencedClass() != null;
		} catch (RuntimeException e) {
			return false;
		}
	}
	
	public String getReferencedClassFullName() {
		return this.determineRelatedReferencedClass().getCanonicalName();
	}
	
	public String getReferenceClassFullName() {
		return this.determineRelatedReferenceClass().getCanonicalName();
	}
	
	private String generateRelatedReferencedClassName() {
		String[] nameParts = this.referencedTableInfo.getTableName().split("_");

		String className = utilities.capitalize(nameParts[0]);

		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}

		return className + "VO";
	}
	
	@SuppressWarnings("unused")
	private String generateRelatedReferenceClassName() {
		String[] nameParts = this.referenceTableInfo.getTableName().split("_");

		String className = utilities.capitalize(nameParts[0]);

		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}

		return className + "VO";
	}
	
	@Override
	public String toString() {
		return "REFERENCE [TABLE: " + this.referenceTableInfo.getTableName() + ", COLUMN: " + this.referenceColumnName + "]," +
					"REFERENCED[TABLE: " + this.referencedTableInfo.getTableName() + ", COLUMN: " + this.referencedColumnName + "]";
	}

	public void generateRelatedReferencedClass(Connection conn) {
		try {
			this.relatedReferencedClass = OpenMRSClassGenerator.generate(this.getReferencedTableInfo(), conn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
		finally {
		}
		
	}
	
	public void generateRelatedReferenceClass(Connection conn) {
		
		try {
			this.relatedReferenceClass = OpenMRSClassGenerator.generate(this.getReferenceTableInfo(), conn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}
	
	public void generateSkeletonOfRelatedReferencedClass(Connection conn) {
		try {
			this.relatedReferencedClass = OpenMRSClassGenerator.generateSkeleton(this.getReferencedTableInfo(), conn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
		finally {
		}
		
	}
	
	public void generateSkeletonRelatedReferenceClass(Connection conn) {
		
		try {
			this.relatedReferenceClass = OpenMRSClassGenerator.generateSkeleton(this.getReferenceTableInfo(), conn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}
}
