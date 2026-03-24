package org.openmrs.module.epts.etl.conf;

import java.util.Map;

public class EtlTemplateInfo {
	
	private String name;
	
	private Map<String, String> parameters;
	
	public EtlTemplateInfo() {
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
}
