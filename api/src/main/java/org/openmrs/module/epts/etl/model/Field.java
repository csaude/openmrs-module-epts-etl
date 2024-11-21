package org.openmrs.module.epts.etl.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
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
	
	public static final Integer DEFAULT_INT_VALUE = -1;
	
	public static final Date DEFAULT_DATE_VALUE = DateAndTimeUtilities.createDate("1975-01-01");
	
	public static final String DEFAULT_STRING_VALUE = "UNDEFINED";
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private Object value;
	
	private String dataType;
	
	private AttDefinedElements attDefinedElements;
	
	private boolean allowNull;
	
	private boolean timeStamp;
	
	private TypePrecision precision;
	
	private Class<?> typeClass;
	
	private boolean autoIncrement;
	
	public Field() {
	}
	
	public Class<?> getTypeClass() {
		return typeClass;
	}
	
	public void setTypeClass(Class<?> typeClass) {
		this.typeClass = typeClass;
	}
	
	@JsonIgnore
	public TypePrecision getPrecision() {
		return precision;
	}
	
	public void setPrecision(TypePrecision precision) {
		this.precision = precision;
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
	
	public String getDataType() {
		return dataType;
	}
	
	public void setDataType(String dataType) {
		this.dataType = dataType;
		
		determineTypeClass();
	}
	
	public Field(String name) {
		this.name = name;
	}
	
	public static Field fastCreateWithValue(String name, Object value) {
		Field f = new Field(name);
		
		f.setValue(value);
		
		return f;
	}
	
	public static Field fastCreateWithType(String name, String dataType) {
		Field f = new Field(name);
		
		f.setDataType(dataType);
		
		return f;
	}
	
	public static Field fastCreateField(String name) {
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
	public boolean hasDataType() {
		return getDataType() != null;
	}
	
	public void setValue(Object value) {
		if (this.hasDataType() && value instanceof Double && (this.isIntegerField() || this.isLongField())) {
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
		return AttDefinedElements.isDateType(this.getDataType());
	}
	
	@JsonIgnore
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.getDataType());
	}
	
	@JsonIgnore
	public boolean isIntegerField() {
		return AttDefinedElements.isInteger(this.getDataType());
	}
	
	@JsonIgnore
	public boolean isLongField() {
		return AttDefinedElements.isLong(this.getDataType());
	}
	
	@JsonIgnore
	public boolean isString() {
		return AttDefinedElements.isString(this.getDataType());
	}
	
	@JsonIgnore
	public boolean isSmallIntType() {
		return AttDefinedElements.isSmallInt(this.getDataType());
	}
	
	@JsonIgnore
	public Field createACopy() {
		Field f = new Field(this.name);
		
		f.setDataType(this.getDataType());
		f.setAllowNull(this.allowNull());
		
		return f;
	}
	
	public void copyFrom(Field f) {
		this.dataType = f.dataType;
		this.name = f.name;
		this.value = f.value;
		this.allowNull = f.allowNull;
		this.typeClass = f.typeClass;
		this.precision = f.precision;
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
		
		if (tabConf.hasAlias()) {
			
			if (this.getName().startsWith(tabConf.getAlias())) {
				return this.getName();
			}
			
			return tabConf.getTableAlias() + "_" + this.getName();
		}
		
		return this.getName();
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
	
	public String getFormatedValue() {
		if (getValue() instanceof Date) {
			return DateAndTimeUtilities.formatToYYYYMMDD_HHMISS((Date) this.getValue());
		}
		
		return getValue().toString();
	}
	
	public void determineTypeClass() {
		
		if (hasDataType()) {
			if (this.isIntegerField()) {
				this.setTypeClass(Integer.class);
				
				if (this.getPrecision() == null) {
					this.setPrecision(TypePrecision.init(11, null));
				}
			} else if (this.isDateField()) {
				this.setTypeClass(Date.class);
				
			} else if (this.isLongField()) {
				this.setTypeClass(Long.class);
				
				if (this.getPrecision() == null) {
					this.setPrecision(TypePrecision.init(20, null));
				}
			} else if (this.isDecimalField()) {
				this.setTypeClass(Double.class);
				
				if (this.getPrecision() == null) {
					this.setPrecision(TypePrecision.init(20, 7));
				}
			} else if (this.isNumericColumnType()) {
				this.setTypeClass(Integer.class);
				
				if (this.getPrecision() == null) {
					this.setPrecision(TypePrecision.init(11, null));
				}
			} else if (this.isString()) {
				this.setTypeClass(String.class);
				
				if (this.getPrecision() == null) {
					this.setPrecision(TypePrecision.init(250, null));
				}
			} else {
				this.setTypeClass(Object.class);
			}
		} else {
			this.setTypeClass(null);
		}
	}
	
	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}
	
	public boolean isAutoIncrement() {
		return autoIncrement;
	}
	
	public boolean isTextField() {
		return utilities.isStringIn(this.getDataType(), "TEXT");
	}
	
	public boolean isClob() {
		return AttDefinedElements.isClob(this.getDataType());
	}
	
	public boolean isDecimalField() {
		return AttDefinedElements.isDecimal(this.getDataType());
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Field field = new Field();
		
		field.copyFrom(this);
		
		return field;
	}
	
	public Field cloneMe() {
		try {
			return (Field) clone();
		}
		catch (CloneNotSupportedException e) {
			throw new EtlExceptionImpl(e);
		}
	}
	
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
		this.setName(utilities.tryToReplacePlaceholders(this.getName(), schemaInfoSrc));
		
		if (this.hasValue() && this.getValue() instanceof String) {
			this.setValue(utilities.tryToReplacePlaceholders(this.getValue().toString(), schemaInfoSrc));
		}
		
		if (this.hasDataType()) {
			this.setValue(utilities.tryToReplacePlaceholders(this.getDataType(), schemaInfoSrc));
		}
	}
	
	public static void tryToReplacePlaceholders(List<? extends Field> conditionalFields, EtlDatabaseObject schemaInfoSrc) {
		if (conditionalFields != null) {
			for (Field f : conditionalFields) {
				f.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
	}
	
}
