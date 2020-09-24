package org.openmrs.module.eptssync.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

/**
 * 
 * Classe auxiliar para representar um valor do tipo String ou de tipos primitivos.
 * O Objectivo desta classe é fornecer uma forma de criar lista [ArrayList] de tipos de dados primitivos (incluindo Strings).
 * Esta classe é útil por exemplo quando se pretende popular um dbselect ou listcheckbox com valores de tipos primitivos.
 * 
 * @author JPBOANE
 * @version 1.0 12/01/2013
 *
 */
public class SimpleValue extends BaseVO{
	private String value;
	private String designacao;
	
	static CommonUtilities utilities = CommonUtilities.getInstance();

	public SimpleValue(){
		setValue("");
	}
	
	public SimpleValue(String value){
		setValue(""+value);
	}
	
	public SimpleValue(int value){
		setValue(""+value);
	}
	
	public SimpleValue(double value){
		setValue(""+value);
	}
	
	public SimpleValue(float value){
		setValue(""+value);
	}
	
	public SimpleValue(char value){
		setValue(""+value);
	}
	
	public SimpleValue(boolean value){
		setValue(""+value);
	}
	
	public SimpleValue(long value){
		setValue(""+value);
	}

	public String getDesignacao() {
		return designacao;
	}

	public void setDesignacao(String designacao) {
		this.designacao = designacao;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
		this.designacao = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (this== obj) return true;
		
		if (obj instanceof SimpleValue) return this.value != null && this.value.equals(((SimpleValue)obj).value);
		
		return this.value.equals(obj);
	}
	
	public boolean hasValue(){
		return utilities.stringHasValue(this.value);
	}
	
	public int intValue(){
		return Integer.parseInt(this.value);
	}
	
	public long longValue(){
		return Long.parseLong(this.value);
	}
	
	public static ArrayList<SimpleValue> fillListByInts(int start, int end){
		ArrayList<SimpleValue> list = new ArrayList<SimpleValue>();
		
		for (int i = start; i <= end; i++){
			list.add(new SimpleValue(i));
		}
		
		return  list;
	}
	
	public static ArrayList<SimpleValue> fillList(String[] elements){
		ArrayList<SimpleValue> list = new ArrayList<SimpleValue>();
		
		for (int i=0; i < elements.length; i++){
			if (elements[i] != null) list.add(new SimpleValue(elements[i]));
		}
		
		return  list;
	}
	
	public static ArrayList<SimpleValue> fillList(String element, String... elements){
		ArrayList<SimpleValue> list = new ArrayList<SimpleValue>();
		
		list.add(new SimpleValue(element.toString()));
		
		if (elements != null){
			for (String e : elements){
				list.add(new SimpleValue(e));
			}
		}
		
		return  list;
	}
	
	public static SimpleValue loadFromProperties(String pathToPropertyFileFromContextClassLoader, String propertyName) throws IOException{
		Properties properties = new Properties();
		
		properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(pathToPropertyFileFromContextClassLoader));
		
		return new SimpleValue(properties.getProperty(propertyName));
	}
	
	
	public static SimpleValue create(String value){
		return new SimpleValue(value);
	}
	
	public static SimpleValue create(int value){
		return new SimpleValue(value);
	}
	
	public static SimpleValue create (double value){
		return new SimpleValue(value);
	}
	
	public static SimpleValue create(float value){
		return new SimpleValue(value);
	}
	
	public static SimpleValue create(char value){
		return new SimpleValue(value);
	}
	
	public static SimpleValue create(boolean value){
		return new SimpleValue(value);
	}
	
	public static SimpleValue create(long value){
		return new SimpleValue(value);
	}
	public static SimpleValue create(){
		return new SimpleValue();
	}
	
	public double doubleValue(){
		return Double.parseDouble(value);
	}
}
