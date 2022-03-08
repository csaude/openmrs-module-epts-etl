package org.openmrs.module.eptssync.utilities;

import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
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
	private String resultSetLoadDefinition;

	private String sqlInsertFirstPartDefinition;
	private String sqlInsertLastEndPartDefinition;
	private String sqlUpdateDefinition;

	private String sqlInsertParamDefinifion;
	private String sqlUpdateParamDefinifion;
	private String sqlInsertValues;

	private String attName;
	private String attType;

	//private boolean mainParentAtt;
	
	private String dbAttName;
	private String dbAttType;
	
	private boolean isObjectId;
	private boolean isLast;
	private SyncTableConfiguration syncTableInfo;
	
	private AttDefinedElements(String dbAttName, String dbAttType, boolean isLast, SyncTableConfiguration syncTableInfo) {
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
		
	public String getResultSetLoadDefinition() {
		return resultSetLoadDefinition;
	}
	
	

	private void generateElemets() {
		this.attType = convertMySQLTypeTOJavaType(dbAttType);
		this.attName = convertTableAttNameToClassAttName(dbAttName);

		this.attDefinition = defineAtt(attName, attType);
		this.setterDefinition = defineSetterMethod(attName, attType);
		this.getterDefinition = defineGetterMethod(attName, attType);
		this.resultSetLoadDefinition = defineResultSetLOadDefinition();
		
		String aspasAbrir = "\"\\\"\"+";
		String aspasFechar = "+\"\\\"\"";
		
		if (!isObjectId || isSharedKey() || isMetadata()) {
			this.sqlInsertFirstPartDefinition = dbAttName + (isLast ? "" : ", ");
			this.sqlInsertLastEndPartDefinition = "?" + (isLast ? "" : ", ");
			this.sqlUpdateDefinition = dbAttName + " = ?" + (isLast ? "" : ", ");
			
			if (isForeignKey(dbAttName) && isNumeric()) {
				this.sqlInsertParamDefinifion = "this." + attName + " == 0 ? null : this." + attName + (isLast ? "" : ", ");
				this.sqlUpdateParamDefinifion = "this." + attName + " == 0 ? null : this." + attName + (isLast ? "" : ", ");
				this.sqlInsertValues = "this." + attName + " == 0 ? null : this." + attName;
				
				this.sqlInsertValues = "(" + this.sqlInsertValues + (isLast ? ")" : ") + \",\" + "); 
				
			}
			else {
				this.sqlInsertParamDefinifion = "this." + attName + (isLast ? "" : ", ");
				this.sqlUpdateParamDefinifion = "this." + attName + (isLast ? "" : ", ");
	
				if (isNumeric()) {
					this.sqlInsertValues = "this." + attName;
				}
				else
				if (isDate()) {
					this.sqlInsertValues = "this." + attName + " != null ? " + aspasAbrir + " DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(" + attName + ")  " + aspasFechar + " : null";
				}
				else
				if (isString()) {
					this.sqlInsertValues = "this." + attName + " != null ? " + aspasAbrir + " utilities.scapeQuotationMarks(" + attName + ")  " + aspasFechar + " : null";
				}
				else {
					this.sqlInsertValues = "this." + attName + " != null ? " + aspasAbrir + attName + aspasFechar + " : null";
				}
				
				this.sqlInsertValues = "(" + this.sqlInsertValues + (isLast ? ")" : ") + \",\" + "); 
			}
		}	
	}
	
	private boolean isMetadata() {
		return this.syncTableInfo.isMetadata();
	}

	public static String removeStrangeCharactersOnString(String str) {
		if (!utilities.stringHasValue(str)) return str;
		
		return utilities.removeCharactersOnString(str, "\\\\");
	}
	
	public String getSqlInsertValues() {
		return sqlInsertValues;
	}
	
	private String defineResultSetLOadDefinition() {
		
		
		if (attType.equals("int")) {
			return "this." + this.attName + " = rs.getInt(\"" + dbAttName + "\");"; 
		}
		else 
		if (attType.equals("boolean")) {
			return "this." + this.attName + " = rs.getInt(\"" + dbAttName + "\") > 0;"; 
		} 
		else if (attType.equals("double")) {
			return "this." + this.attName + " = rs.getDouble(\"" + dbAttName + "\");";
		}
		else 
		if (attType.equals("float")) {
			return "this." + this.attName + " = rs.getFloat(\"" + dbAttName + "\");"; 
		}
		else 
		if (attType.equals("long")) {
			return "this." + this.attName + " = rs.getLong(\"" + dbAttName + "\");"; 
		}
		else 
		if (attType.equals("String")) {
			return "this." + this.attName + " = AttDefinedElements.removeStrangeCharactersOnString(rs.getString(\"" + dbAttName + "\") != null ? rs.getString(\"" + dbAttName + "\").trim() : null);"; 
		} 
		else 
		if (attType.equals("java.util.Date")) {
			return "this." + this.attName + " =  rs.getTimestamp(\"" + dbAttName + "\") != null ? new java.util.Date( rs.getTimestamp(\"" + dbAttName + "\").getTime() ) : null;"; 
		} 
		else 
		if (attType.equals("java.io.InputStream")) {
			return "this." + this.attName + " = rs.getBlob(\"" + dbAttName + "\") != null ? rs.getBlob(\"" + dbAttName + "\").getBinaryStream() : null;"; 
		} 
		else 
		if (attType.equals("byte")) {
			return "this." + this.attName + " = rs.getByte(\"" + dbAttName + "\");"; 
		}
		else 
		if (attType.equals("short")) {
			return "this." + this.attName + " = rs.getShort(\"" + dbAttName + "\");"; 
		}
		else
		if (attType.equals("byte[]")) {
			return "this." + this.attName + " = rs.getBytes(\"" + dbAttName + "\");"; 
		}
		else {
			return "this." + this.attName + " = rs.getObject(\"" + dbAttName + "\");"; 
		}		
		
	}
	
	private boolean isDate() {
		return utilities.isStringIn(this.attType, "java.util.Date", "Date");
	}
	
	private boolean isString() {
		return utilities.isStringIn(this.attType, "java.lang.String", "String");
	}
	
	private boolean isNumeric() {
		return utilities.isStringIn(this.attType, "int", "long", "byte", "short", "double", "float");
	}
	
	public static boolean isNumeric(String attType) {
		return utilities.isStringIn(attType, "int", "long", "byte", "short", "double", "float");
	}

	private boolean isPK() {
		return syncTableInfo.getPrimaryKeyAsClassAtt().equals(this.attName);
	}
	private boolean isSharedKey() {
		if (!isPK()) return false;
		
		if (syncTableInfo.getSharePkWith() != null) {
			return true;
		}
			
		return false;
	}

	private boolean isForeignKey(String dbAttName) {
		for (RefInfo parent : this.syncTableInfo.getParents()) {
			if (parent.getRefColumnName().equalsIgnoreCase(dbAttName)) {
				return true;
			}
		}
		
		return false;
	}

	public static AttDefinedElements define(String dbAttName, String dbAttType, boolean isLast, SyncTableConfiguration syncTableInfo) {
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

	
	public static String defineDefaultGetterMethod(String attName, String attType) {
		String cAttName = attName.toUpperCase().charAt(0) + attName.substring(1);

		if (isNumeric(attType)) return "	public " + attType + " get" + cAttName + "(){ \n" + "		return 0;\n" + "	}";

		return 	"	public " + attType + " get" + cAttName + "(){ \n" + "		return null;\n" + "	}";
		
	}

	public static String defineDefaultSetterMethod(String attName, String attType) {
		String cAttName = attName.toUpperCase().charAt(0) + attName.substring(1);

		return "	public void set" + cAttName + "(" + attType + " " + attName + "){ }";
	}

	
	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}

	public static String convertMySQLTypeTOJavaType(String mySQLTypeName) {
		mySQLTypeName = mySQLTypeName.toUpperCase();

		if (utilities.isStringIn(mySQLTypeName, "INT", "MEDIUMINT"))
			return "int";
		if (utilities.isStringIn(mySQLTypeName, "TINYINT", "BIT"))
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
		if (utilities.isStringIn(mySQLTypeName, "VARBINARY", "BLOB", "TEXT", "LONGBLOB"))
			return "byte[]";
		if (utilities.isStringIn(mySQLTypeName, "DATE", "DATETIME", "TIME", "TIMESTAMP"))
			return "java.util.Date";
		
		throw new ForbiddenOperationException("Unknown data type [" + mySQLTypeName + "]");
	}
	
}
