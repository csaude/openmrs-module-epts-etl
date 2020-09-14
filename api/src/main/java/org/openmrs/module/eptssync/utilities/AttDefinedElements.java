package org.openmrs.module.eptssync.utilities;

import org.openmrs.module.eptssync.controller.conf.ParentRefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;

/**
 * Utilitie class which help to define att elements for class like Att
 * definition, getter and setter definition, etc
 * 
 * @author jpboane
 *
 */
public class AttDefinedElements {
	private static CommonUtilities utilities = CommonUtilities.getInstance();

	private String attDefinition;
	private String setterDefinition;
	private String getterDefinition;

	private String sqlInsertFirstPartDefinition;
	private String sqlInsertLastEndPartDefinition;
	private String sqlUpdateDefinition;

	private String sqlInsertParamDefinifion;
	private String sqlUpdateParamDefinifion;

	private String attName;
	private String attType;

	//private boolean mainParentAtt;
	
	private String dbAttName;
	private String dbAttType;
	
	private boolean isObjectId;
	private boolean isLast;
	private SyncTableInfo syncTableInfo;
	
	private AttDefinedElements(String dbAttName, String dbAttType, boolean isLast, SyncTableInfo syncTableInfo) {
		this.dbAttName = dbAttName;
		this.dbAttType = dbAttType;
		
		this.isObjectId = dbAttName.equalsIgnoreCase(syncTableInfo.getPrimaryKey());
		
		this.isLast = isLast;
		this.syncTableInfo = syncTableInfo;
	}

	public String getAttDefinition() {
		return attDefinition;
	}

	public void setAttDefinigtion(String attDefinigtion) {
		this.attDefinition = attDefinigtion;
	}

	public String getSetterDefinition() {
		return setterDefinition;
	}

	public void setSetterDefinition(String setterDefinition) {
		this.setterDefinition = setterDefinition;
	}

	public String getGetterDefinition() {
		return getterDefinition;
	}

	public void setGetterDefinition(String getterDefinition) {
		this.getterDefinition = getterDefinition;
	}

	public String getSqlInsertFirstPartDefinition() {
		return sqlInsertFirstPartDefinition;
	}

	public void setSqlInsertFirstPartDefinition(String sqlInsertFirstPartDefinition) {
		this.sqlInsertFirstPartDefinition = sqlInsertFirstPartDefinition;
	}

	public String getSqlInsertLastEndPartDefinition() {
		return sqlInsertLastEndPartDefinition;
	}

	public void setSqlInsertLastEndPartDefinition(String sqlInsertLastEndPartDefinition) {
		this.sqlInsertLastEndPartDefinition = sqlInsertLastEndPartDefinition;
	}

	public String getSqlUpdateDefinition() {
		return sqlUpdateDefinition;
	}

	public void setSqlUpdateDefinition(String sqlUpdateDefinition) {
		this.sqlUpdateDefinition = sqlUpdateDefinition;
	}

	public String getSqlInsertParamDefinifion() {
		return sqlInsertParamDefinifion;
	}

	public void setSqlInsertParamDefinifion(String sqlInsertParamDefinifion) {
		this.sqlInsertParamDefinifion = sqlInsertParamDefinifion;
	}

	public String getSqlUpdateParamDefinifion() {
		return sqlUpdateParamDefinifion;
	}

	public void setSqlUpdateParamDefinifion(String sqlUpdateParamDefinifion) {
		this.sqlUpdateParamDefinifion = sqlUpdateParamDefinifion;
	}

	public String getAttName() {
		return attName;
	}

	public void setAttName(String attName) {
		this.attName = attName;
	}

	public String getAttType() {
		return attType;
	}

	public void setAttType(String attType) {
		this.attType = attType;
	}
		
	private void generateElemets() {
		this.attType = convertMySQLTypeTOJavaType(dbAttType);
		this.attName = convertTableAttNameToClassAttName(dbAttName);

		this.attDefinition = defineAtt(attName, attType);
		this.setterDefinition = defineSetterMethod(attName, attType);
		this.getterDefinition = defineGetterMethod(attName, attType);

		if (!isObjectId || isSharedKey()) {
			this.sqlInsertFirstPartDefinition = dbAttName + (isLast ? "" : ", ");
			this.sqlInsertLastEndPartDefinition = "?" + (isLast ? "" : ", ");
			this.sqlUpdateDefinition = dbAttName + " = ?" + (isLast ? "" : ", ");

			
			if (!isForeignKey(dbAttName)) {
				this.sqlInsertParamDefinifion = "this." + attName + (isLast ? "" : ", ");
				this.sqlUpdateParamDefinifion = "this." + attName + (isLast ? "" : ", ");
			}
			else {
				this.sqlInsertParamDefinifion = "this." + attName + " == 0 ? null : this." + attName + (isLast ? "" : ", ");
				this.sqlUpdateParamDefinifion = "this." + attName + " == 0 ? null : this." + attName + (isLast ? "" : ", ");
			}
		}	
	}
	
	private boolean isSharedKey() {
		for (ParentRefInfo parent : this.syncTableInfo.getParentRefInfo()) {
			if (parent.isSharedPk() && parent.getReferenceColumnAsClassAttName().equals(this.attName)) {
				return true;
			}
		}
		
		return false;
	}

	private boolean isForeignKey(String dbAttName) {
		for (ParentRefInfo parent : this.syncTableInfo.getParentRefInfo()) {
			if (parent.getReferenceColumnName().equalsIgnoreCase(dbAttName)) {
				return true;
			}
		}
		
		return false;
	}

	public static AttDefinedElements define(String dbAttName, String dbAttType, boolean isLast, SyncTableInfo syncTableInfo) {
		AttDefinedElements elements = new AttDefinedElements(dbAttName, dbAttType, isLast, syncTableInfo);
		elements.generateElemets();
		
		return elements;
	}
	
	public static String defineAtt(String attName, String attType) {
		return "	private " + attType + " " + attName + ";";
	}

	public static String defineGetterMethod(String attName, String attType) {
		String cAttName = attName.toUpperCase().charAt(0) + attName.substring(1);

		return "	public " + attType + " get" + cAttName + "(){ \n" + "		return this." + attName + ";\n" + "	}";

	}

	public static String defineSetterMethod(String attName, String attType) {
		String cAttName = attName.toUpperCase().charAt(0) + attName.substring(1);

		return "	public void set" + cAttName + "(" + attType + " " + attName + "){ \n" + "	 	this." + attName
				+ " = " + attName + ";\n" + "	}";
	}

	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}

	private static String convertMySQLTypeTOJavaType(String mySQLTypeName) {
		mySQLTypeName = mySQLTypeName.toUpperCase();

		if (utilities.isStringIn(mySQLTypeName, "INT", "MEDIUMINT"))
			return "int";
		if (utilities.isStringIn(mySQLTypeName, "TINYINT"))
			return "byte";
		if (utilities.isStringIn(mySQLTypeName, "YEAR", "SMALLINT"))
			return "short";
		if (utilities.isStringIn(mySQLTypeName, "BIGINT"))
			return "long";
		if (utilities.isStringIn(mySQLTypeName, "DECIMAL", "NUMERIC", "SMALLINT", "REAL", "DOUBLE"))
			return "double";
		if (utilities.isStringIn(mySQLTypeName, "FLOAT", "NUMERIC", "SMALLINT"))
			return "float";
		if (utilities.isStringIn(mySQLTypeName, "VARCHAR", "CHAR"))
			return "String";
		if (utilities.isStringIn(mySQLTypeName, "VARBINARY", "BLOB", "TEXT"))
			return "byte[]";
		if (utilities.isStringIn(mySQLTypeName, "DATE", "DATETIME", "TIME", "TIMESTAMP"))
			return "java.util.Date";

		throw new ForbiddenOperationException("Unknown data type [" + mySQLTypeName + "]");
	}
	
}
