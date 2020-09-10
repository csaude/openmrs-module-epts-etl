package org.openmrs.module.eptssync.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;


/**
 * Esta classe representa o field de um formulário.
 * ATENCAO que o formField pode representar também o file.
 * @author JP. Boane
 * @version 1.0 29/10/2012
 *
 */
public class Field implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private Object value;
	
	public Field(String name, Object value){
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}	
	
	public static Object getParameter(List<? extends Field> fields, String name){
		Field  field = CommonUtilities.getInstance().findOnArray(fields, new Field(name,null));
		
		return field != null ? field.value : null;
	}
	
	public static void printAll(List<? extends Field> fields){
		for (Object field : fields) System.err.println(field);
	}
	
	@Override
	public String toString() {
		return "[Name: "+getName() + ", Value "+ value+"]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (obj instanceof Field){
			if (this.name.equals(((Field)obj).name)) return true;
		}
		
		if (obj instanceof String){
			return this.name.equals(obj);
		}
		
		return super.equals(obj);
	}
	
	/**
	 * Retorna o valor long correspondente ao valor do parametro identificado por 'paramName';
	 * Se o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static long getLong(List<? extends Field> fields, String paramName){
		Object value = getParameter(fields, paramName);
		
		if (value == null) return 0;
		
		try {
	        return Long.parseLong((String)value);
        } catch (NumberFormatException e) {
        	return 0;
	    }
	}
	
	/**
	 * Retorna o valor date correspondente ao valor do parametro identificado por 'paramName';
	 * Se o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static Date getDate(List<? extends Field> fields, String paramName){
		Object value = getParameter(fields, paramName);

		if (value == null) return null;
		
		try {
	        return DateAndTimeUtilities.createDate((String)value, DateAndTimeUtilities.DATE_FORMAT);
        } catch (Exception e) {
        	return null;
	    }
	}
	
	
	/**
	 * Retorna o valor int correspondente ao valor do parametro identificado por 'paramName';
	 * Se o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static int getInt(List<? extends Field> fields, String paramName){
		Object value = getParameter(fields, paramName);
		
		if (value == null) return 0;
		
		try {
	        return Integer.parseInt((String)value);
        } catch (NumberFormatException e) {
        	return 0;
	    }
	}
	
	/**
	 * Retorna o valor double correspondente ao valor do parametro identificado por 'paramName';
	 * Se o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static double getDouble(List<? extends Field> fields, String paramName){
		Object value = getParameter(fields, paramName);
		
		if (value == null) return 0;
		
		try {
	        return Double.parseDouble((String)value);
        } catch (NumberFormatException e) {
        	return 0;
	    }
	}
	
	
	/**
	 * Retorna o valor String correspondente ao valor do parametro identificado por 'paramName';
	 * Se o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static String getString(List<? extends Field> fields, String paramName){
		Object value = getParameter(fields, paramName);
		
		if (value == null) return "";
		
		return (String)value;
	}
}
