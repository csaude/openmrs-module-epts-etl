package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.model.Field;

public class Key extends Field {
	
	private static final long serialVersionUID = 3689136559355049310L;
	
	public Key() {
		
	}
	
	public Key(String name) {
		super(name);
	}
	
	public static Key fastCreateKey(String name) {
		Key f = new Key(name);
		
		return f;
	}
	
	public static Key fastCreateValued(String name, Object value) {
		Key k = new Key(name);
		
		k.setValue(value);
		
		return k;
	}
	
	public static Key fastCreateTyped(String name, String type) {
		Key k = new Key(name);
		k.setType(type);
		
		return k;
	}
	
	public Key(String name, String type, Object value) {
		this(name);
		
		setType(type);
		setValue(value);
	}
	
	@Override
	public Key createACopy() {
		Key k = new Key();
		
		k.copyFrom(this);
		
		return k;
	}
	
	@Override
	public Key createACopyWithDefaultValue() {
		Key k = createACopy();
		
		k.loadWithDefaultValue();
		
		return k;
	}
	
}
