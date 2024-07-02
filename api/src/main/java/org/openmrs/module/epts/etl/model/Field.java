package org.openmrs.module.epts.etl.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Esta classe representa o field de um formulário. ATENCAO que o formField pode representar também
 * o file.
 * 
 * @author JP. Boane
 * @version 1.0 29/10/2012
 */
public class Field implements Serializable {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private Object value;
	
	private String type;
	
	private AttDefinedElements attDefinedElements;
	
	private boolean allowNull;
	
	private boolean timeStamp;
	
	public static final Integer DEFAULT_INT_VALUE = -1;
	
	public static final Date DEFAULT_DATE_VALUE = DateAndTimeUtilities.createDate("1975-01-01");
	
	public static final String DEFAULT_STRING_VALUE = "UNDEFINED";
	
	public Field() {
	}
	
	@JsonIgnore
	public boolean allowNull() {
		return this.allowNull;
	}
	
	@JsonIgnore
	public boolean isAllowNull() {
		return allowNull;
	}
	
	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}
	
	@JsonIgnore
	public AttDefinedElements getAttDefinedElements() {
		return attDefinedElements;
	}
	
	public void setAttDefinedElements(AttDefinedElements attDefinedElements) {
		this.attDefinedElements = attDefinedElements;
	}
	
	@JsonIgnore
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Field(String name) {
		this.name = name;
	}
	
	public static Field fastCreateWithValue(String name, Object value) {
		Field f = new Field(name);
		
		f.setValue(value);
		
		return f;
	}
	
	public static Field fastCreateWithType(String name, String type) {
		Field f = new Field(name);
		
		f.setType(type);
		
		return f;
	}
	
	public static Field fastCreate(String name) {
		Field f = new Field(name);
		
		return f;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public String getNameAsClassAtt() {
		return AttDefinedElements.convertTableAttNameToClassAttName(this.name);
	}
	
	public Object getValue() {
		return value;
	}
	
	@JsonIgnore
	boolean hasType() {
		return type != null;
	}
	
	public void setValue(Object value) {
		if (this.hasType() && value instanceof Double && (this.isIntegerField() || this.isLongField())) {
			this.value = utilities.forcarAproximacaoPorExcesso((Double) value);
		} else {
			this.value = value;
		}
	}
	
	public static Object getParameter(List<? extends Field> fields, String name) {
		Field field = CommonUtilities.getInstance().findOnArray(fields, new Field(name));
		
		return field != null ? field.value : null;
	}
	
	public static void printAll(List<? extends Field> fields) {
		for (Object field : fields)
			System.err.println(field);
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String toString = "[Name: " + getName();
		
		if (hasValue())
			toString += ", Value " + value + "]";
		else
			toString += "]";
		
		return toString;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (obj instanceof Field) {
			if (this.name.equals(((Field) obj).name))
				return true;
		}
		
		if (obj instanceof String) {
			return this.name.equals(obj);
		}
		
		return super.equals(obj);
	}
	
	/**
	 * Retorna o valor Integer correspondente ao valor do parametro identificado por 'paramName'; Se
	 * o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static Integer getInteger(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return 0;
		
		try {
			return Integer.parseInt((String) value);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Retorna o valor date correspondente ao valor do parametro identificado por 'paramName'; Se o
	 * parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static Date getDate(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return null;
		
		try {
			return DateAndTimeUtilities.createDate((String) value, DateAndTimeUtilities.DATE_FORMAT);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Retorna o valor int correspondente ao valor do parametro identificado por 'paramName'; Se o
	 * parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static int getInt(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return 0;
		
		try {
			return Integer.parseInt((String) value);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Retorna o valor double correspondente ao valor do parametro identificado por 'paramName'; Se
	 * o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static double getDouble(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return 0;
		
		try {
			return Double.parseDouble((String) value);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Retorna o valor String correspondente ao valor do parametro identificado por 'paramName'; Se
	 * o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static String getString(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return "";
		
		return (String) value;
	}
	
	@JsonIgnore
	public boolean isDateField() {
		return AttDefinedElements.isDateType(this.type);
	}
	
	@JsonIgnore
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.type);
	}
	
	@JsonIgnore
	public boolean isIntegerField() {
		return AttDefinedElements.isInteger(this.type);
	}
	
	@JsonIgnore
	public boolean isLongField() {
		return AttDefinedElements.isLong(this.type);
	}
	
	@JsonIgnore
	public boolean isString() {
		return AttDefinedElements.isString(this.type);
	}
	
	@JsonIgnore
	public boolean isSmallIntType() {
		return AttDefinedElements.isSmallInt(this.type);
	}
	
	@JsonIgnore
	public Field createACopy() {
		Field f = new Field(this.name);
		
		f.setType(this.getType());
		f.setAllowNull(this.allowNull());
		
		return f;
	}
	
	public void copyFrom(Field f) {
		this.type = f.type;
		this.name = f.name;
		this.value = f.value;
		this.allowNull = f.allowNull;
	}
	
	@JsonIgnore
	public Field createACopyWithDefaultValue() {
		Field f = createACopy();
		
		if (f.isDateField()) {
			f.setValue(DEFAULT_DATE_VALUE);
		} else if (f.isNumericColumnType()) {
			f.setValue(DEFAULT_INT_VALUE);
		} else {
			f.setValue(DEFAULT_STRING_VALUE);
		}
		
		return f;
	}
	
	public static String parseAllToCommaSeparatedName(List<Field> fields) {
		
		if (!utilities.arrayHasElement(fields))
			return null;
		
		String commaSeparatedNames = "";
		
		for (Field f : fields) {
			if (!commaSeparatedNames.isEmpty()) {
				commaSeparatedNames += ", ";
			}
			
			commaSeparatedNames += f.getName();
		}
		
		return commaSeparatedNames;
	}
	
	public static List<String> parseAllToListOfName(List<Field> fields) {
		
		if (!utilities.arrayHasElement(fields))
			return null;
		
		List<String> list = new ArrayList<>(fields.size());
		
		for (Field f : fields) {
			list.add(f.getName());
		}
		
		return list;
	}
	
	@JsonIgnore
	public boolean hasValue() {
		return getValue() != null;
	}
	
	public void loadWithDefaultValue() {
		if (isDateField()) {
			setValue(DEFAULT_DATE_VALUE);
		} else if (isNumericColumnType()) {
			setValue(DEFAULT_INT_VALUE);
		} else {
			setValue(DEFAULT_STRING_VALUE);
		}
	}
	
	public String generateAliasedSelectColumn(TableConfiguration tabConf) {
		return tabConf.getTableAlias() + "." + this.name + " " + tabConf.getTableAlias() + "_" + this.name;
	}
	
	public String generateAliasedColumn(TableConfiguration tabConf) {
		return tabConf.getTableAlias() + "_" + this.name;
	}
	
	@JsonIgnore
	public String getValueAsSqlPart() {
		String v = "";
		
		String aspasAbrir = AttDefinedElements.aspasAbrir;
		String aspasFechar = AttDefinedElements.aspasFechar;
		
		if (getValue() == null) {
			v = "null";
		} else if (isNumericColumnType()) {
			v = getValue().toString();
		} else if (isDateField()) {
			v = aspasAbrir + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS((Date) getValue()) + aspasFechar;
		} else if (isString()) {
			v = aspasAbrir + utilities.scapeQuotationMarks(getValue().toString()) + aspasFechar;
		} else {
			v = aspasAbrir + getValue().toString() + aspasFechar;
		}
		
		return v;
	}
	
	@JsonIgnore
	public boolean isTimeStamp() {
		return timeStamp;
	}
	
	@JsonIgnore
	public void setTimeStamp(boolean timeStamp) {
		this.timeStamp = timeStamp;
	}
	
}
