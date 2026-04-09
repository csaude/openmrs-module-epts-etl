package org.openmrs.module.epts.etl.conf;

import java.util.Map;

public class EtlTemplateInfo {
	
	private String name;
	
	private Map<String, Object> parameters;
	
	public EtlTemplateInfo() {
	}
	
	public EtlTemplateInfo(String name, Map<String, Object> parameters) {
		this(name);
		
		this.parameters = parameters;
	}
	
	public EtlTemplateInfo(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
}
