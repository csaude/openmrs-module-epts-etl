package org.openmrs.module.epts.etl.controller.conf;

import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;

public class Key extends Field {
	
	private static final long serialVersionUID = 3689136559355049310L;
	
	public Key() {
		
	}
	
	public Key(String name) {
		super(name);
	}
	
	public Key(String name, Object value) {
		super(name, value);
	}
	
	public Key(String name, String type) {
		this(name);
		
		setType(type);
	}
	
	public Key(String name, String type, Object value) {
		this(name);
		
		setType(type);
		setValue(value);
	}
	
	public String getNameAsClassAtt() {
		return AttDefinedElements.convertTableAttNameToClassAttName(getName());
	}
	
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.getType());
	}
	
}
