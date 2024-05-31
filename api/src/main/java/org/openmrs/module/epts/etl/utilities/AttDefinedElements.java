package org.openmrs.module.epts.etl.utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;

/**
 * Utilities class which help to define att elements for class like Att definition, getter and
 * setter definition, etc
 * 
 * @author jpboane
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
	
	private String dbAttName;
	
	private String dbAttType;
	
	private boolean isPartOfObjectId;
	
	private boolean isLast;
	
	private DatabaseObjectConfiguration pojoble;
	
	static String aspasAbrir = "\"";
	
	static String aspasFechar = "\"";
	
	private AttDefinedElements(String dbAttName, String dbAttType, boolean isLast, DatabaseObjectConfiguration pojoble) {
		this.dbAttName = dbAttName;
		this.dbAttType = dbAttType;
		this.isLast = isLast;
		this.pojoble = pojoble;
		
		Key key = new Key(dbAttName);
		
		if (this.pojoble.getPrimaryKey() != null) {
			this.isPartOfObjectId = this.pojoble.getPrimaryKey().containsKey(key);
		}
	}
	
	public boolean isLast() {
		return isLast;
	}
	
	public boolean isPartOfObjectId() {
		return isPartOfObjectId;
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
		this.attType = convertDatabaseTypeTOJavaType(this.dbAttName, dbAttType);
		this.attName = convertTableAttNameToClassAttName(dbAttName);
		
		this.attDefinition = defineAtt(attName, attType);
		this.setterDefinition = defineSetterMethod(attName, attType);
		this.getterDefinition = defineGetterMethod(attName, attType);
		this.resultSetLoadDefinition = defineResultSetLOadDefinition();
		
		String aspasAbrir = "\"\\\"\"+";
		String aspasFechar = "+\"\\\"\"";
		
		this.sqlInsertFirstPartDefinition = dbAttName + (isLast ? "" : ", ");
		this.sqlInsertLastEndPartDefinition = "?" + (isLast ? "" : ", ");
		this.sqlUpdateDefinition = dbAttName + " = ?" + (isLast ? "" : ", ");
		
		this.sqlInsertParamDefinifion = "this." + attName + (isLast ? "" : ", ");
		this.sqlUpdateParamDefinifion = "this." + attName + (isLast ? "" : ", ");
		
		if (isNumeric()) {
			this.sqlInsertValues = "this." + attName;
		} else if (isDate()) {
			this.sqlInsertValues = "this." + attName + " != null ? " + aspasAbrir
			        + " DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(" + attName + ")  " + aspasFechar + " : null";
		} else if (isString()) {
			this.sqlInsertValues = "this." + attName + " != null ? " + aspasAbrir + " utilities.scapeQuotationMarks("
			        + attName + ")  " + aspasFechar + " : null";
		} else {
			this.sqlInsertValues = "this." + attName + " != null ? " + aspasAbrir + attName + aspasFechar + " : null";
		}
		
		this.sqlInsertValues = "(" + this.sqlInsertValues + (isLast ? ")" : ") + \",\" + ");
	}
	
	public String defineSqlInsertValue(EtlDatabaseObject obj) {
		String sqlInsertValues = "";
		
		Object value = null;
		
		try {
			value = obj.getFieldValue(this.dbAttName);
		}
		catch (ForbiddenOperationException e) {
			value = obj.getFieldValue(this.attName);
		}
		
		if (value == null) {
			sqlInsertValues = "null";
		} else if (isNumeric()) {
			sqlInsertValues = value.toString();
		} else if (isDate()) {
			sqlInsertValues = aspasAbrir + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS((Date) value) + aspasFechar;
		} else if (isString()) {
			sqlInsertValues = aspasAbrir + utilities.scapeQuotationMarks(value.toString()) + aspasFechar;
		} else {
			sqlInsertValues = aspasAbrir + value.toString() + aspasFechar;
		}
		
		sqlInsertValues = "(" + sqlInsertValues + (this.isLast ? ")" : ")" + ",");
		
		return sqlInsertValues;
	}
	
	public static String removeStrangeCharactersOnString(String str) {
		if (!utilities.stringHasValue(str))
			return str;
		
		return utilities.removeCharactersOnString(str, "\\\\");
	}
	
	public String getSqlInsertValues() {
		return sqlInsertValues;
	}
	
	private String defineResultSetLOadDefinition() {
		
		if (attType.equals("Integer") || attType.toLowerCase().equals("int")) {
			String loadStr = "if (rs.getObject(\"" + dbAttName + "\") != null) ";
			loadStr += "this." + this.attName + " = rs.getInt(\"" + dbAttName + "\");";
			
			return loadStr;
		} else if (attType.toLowerCase().equals("double")) {
			String loadStr = "if (rs.getObject(\"" + dbAttName + "\") != null) ";
			loadStr += "this." + this.attName + " = rs.getDouble(\"" + dbAttName + "\");";
			
			return loadStr;
		} else if (attType.toLowerCase().equals("long")) {
			String loadStr = "if (rs.getObject(\"" + dbAttName + "\") != null) ";
			loadStr += "this." + this.attName + " = rs.getLong(\"" + dbAttName + "\");";
			
			return loadStr;
		} else if (attType.toLowerCase().equals("float")) {
			String loadStr = "if (rs.getObject(\"" + dbAttName + "\") != null) ";
			loadStr += "this." + this.attName + " = rs.getFloat(\"" + dbAttName + "\");";
			
			return loadStr;
		} else if (attType.toLowerCase().equals("boolean")) {
			return "this." + this.attName + " = rs.getBoolean(\"" + dbAttName + "\") > 0;";
		} else if (attType.equals("String")) {
			return "this." + this.attName + " = AttDefinedElements.removeStrangeCharactersOnString(rs.getString(\""
			        + dbAttName + "\") != null ? rs.getString(\"" + dbAttName + "\").trim() : null);";
		} else if (attType.equals("java.util.Date")) {
			return "this." + this.attName + " =  rs.getTimestamp(\"" + dbAttName
			        + "\") != null ? new java.util.Date( rs.getTimestamp(\"" + dbAttName + "\").getTime() ) : null;";
		} else if (attType.equals("java.io.InputStream")) {
			return "this." + this.attName + " = rs.getBlob(\"" + dbAttName + "\") != null ? rs.getBlob(\"" + dbAttName
			        + "\").getBinaryStream() : null;";
		} else if (attType.toLowerCase().equals("byte")) {
			return "this." + this.attName + " = rs.getByte(\"" + dbAttName + "\");";
		} else if (attType.toLowerCase().equals("short")) {
			String loadStr = "if (rs.getObject(\"" + dbAttName + "\") != null) ";
			loadStr += "this." + this.attName + " = rs.getShort(\"" + dbAttName + "\");";
			
			return loadStr;
		} else if (attType.equals("byte[]")) {
			return "this." + this.attName + " = rs.getBytes(\"" + dbAttName + "\");";
		} else {
			return "this." + this.attName + " = rs.getObject(\"" + dbAttName + "\");";
		}
	}
	
	public static String defineSqlAtribuitionString(String attName, Object attValue) {
		String sqlAtribuitionString = "";
		
		if (utilities.isNumeric(attValue.toString())) {
			sqlAtribuitionString = attName + " = " + attValue;
		} else if (attValue instanceof Date) {
			sqlAtribuitionString = attName + " = " + aspasAbrir
			        + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS((Date) attValue) + aspasFechar;
		} else if (attValue instanceof String) {
			sqlAtribuitionString = attName + " = " + aspasAbrir + utilities.scapeQuotationMarks(attValue.toString())
			        + aspasFechar;
		} else {
			sqlAtribuitionString = attName + " = " + aspasAbrir + attName + aspasFechar;
		}
		
		return sqlAtribuitionString;
	}
	
	private boolean isDate() {
		return utilities.isStringIn(this.attType, "java.util.Date", "Date");
	}
	
	private boolean isString() {
		return utilities.isStringIn(this.attType, "java.lang.String", "String");
	}
	
	private boolean isNumeric() {
		return utilities.isStringIn(this.attType.toLowerCase(), "int", "integer", "long", "byte", "short", "double",
		    "float");
	}
	
	public static boolean isNumeric(String attType) {
		return utilities.isStringIn(attType.toLowerCase() , "int", "integer", "long", "byte", "short", "double", "float", "bit", "tinyint");
	}
	
	public static AttDefinedElements define(String dbAttName, String dbAttType, boolean isLast,
	        DatabaseObjectConfiguration pojoble) {
		AttDefinedElements elements = new AttDefinedElements(dbAttName, dbAttType, isLast, pojoble);
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
		
		return "	public void set" + cAttName + "(" + attType + " " + attName + "){ \n" + "	 	this." + attName + " = "
		        + attName + ";\n" + "	}";
	}
	
	public static String defineDefaultGetterMethod(String attName, String attType) {
		String cAttName = attName.toUpperCase().charAt(0) + attName.substring(1);
		
		if (isNumeric(attType))
			return "	public " + attType + " get" + cAttName + "(){ \n" + "		return 0;\n" + "	}";
		
		return "	public " + attType + " get" + cAttName + "(){ \n" + "		return null;\n" + "	}";
		
	}
	
	public static String defineDefaultSetterMethod(String attName, String attType) {
		String cAttName = attName.toUpperCase().charAt(0) + attName.substring(1);
		
		return "	public void set" + cAttName + "(" + attType + " " + attName + "){ }";
	}
	
	public static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}
	
	public static String convertDatabaseTypeTOJavaType(String fieldName, String databaseType) {
		databaseType = databaseType.toUpperCase();
		
		/*NOTE: Temporary Convert INT8 and SERIAL as Integer as Postgres use INT8 for serial columns (PK) Which is INT8.
		  note that this type should be converted to LONG but as if the core of Epts-Etl use Integer for PK for now we 
		  are forcing INT8 to be Integer*/
		if (utilities.isStringIn(databaseType, "INT", "MEDIUMINT", "INT8", "BIGINT", "SERIAL", "SERIAL4"))
			return "Integer";
		if (utilities.isStringIn(databaseType, "TINYINT", "BIT"))
			return "Byte";
		if (utilities.isStringIn(databaseType, "YEAR", "SMALLINT"))
			return "Short";
		if (utilities.isStringIn(databaseType, "BIGINT", "INT8", "SERIAL"))
			return "Long";
		if (utilities.isStringIn(databaseType, "DECIMAL", "NUMERIC", "SMALLINT", "REAL", "DOUBLE"))
			return "Double";
		if (utilities.isStringIn(databaseType, "FLOAT", "NUMERIC", "SMALLINT"))
			return "Float";
		if (utilities.isStringIn(databaseType, "VARCHAR", "CHAR", "TEXT", "MEDIUMTEXT"))
			return "String";
		if (utilities.isStringIn(databaseType, "VARBINARY", "BLOB", "LONGBLOB"))
			return "byte[]";
		if (utilities.isStringIn(databaseType, "DATE", "DATETIME", "TIME", "TIMESTAMP"))
			return "java.util.Date";
		
		throw new ForbiddenOperationException("Unknown data type for field " + fieldName + " [" + databaseType + "]");
	}
	
	public static boolean isDateType(String type) {
		boolean isDatabaseDateType = utilities.isStringIn(type.toUpperCase(), "DATE", "DATETIME", "TIME", "TIMESTAMP");
		boolean isJavaDateType = utilities.isStringIn(type.toUpperCase(), "java.util.Date");
		
		return isDatabaseDateType || isJavaDateType;
	}
	
	public static String[] convertTableAttNameToClassAttName(String[] dbAtts) {
		List<String> atts = new ArrayList<String>();
		
		for (String att : dbAtts) {
			atts.add(convertTableAttNameToClassAttName(att));
		}
		
		return utilities.parseListToArray(atts);
	}
	
	public static boolean isPrimitive(String type) {
		return isNumeric(type) || utilities.isStringIn(type, "char", "boolean");
	}
	
	public static boolean isSmallInt(String type) {
		return utilities.isStringIn(type.toLowerCase(), "tinyint", "bit");
	}
	
}
