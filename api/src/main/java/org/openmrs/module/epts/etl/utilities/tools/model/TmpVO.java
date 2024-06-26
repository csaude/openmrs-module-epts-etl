package org.openmrs.module.epts.etl.utilities.tools.model;

import org.openmrs.module.epts.etl.model.base.BaseVO;

public class TmpVO extends BaseVO {
	
	private int id;
	
	private String name;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return id + ", " + name;
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
		// TODO Auto-generated method stub
		
	}
	
}
