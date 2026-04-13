package org.openmrs.module.epts.etl.conf;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class EtlTemplateInfo {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String name;
	
	private Map<String, Object> parameters;
	
	private List<TemplateOverride> override;
	
	public EtlTemplateInfo() {
	}
	
	public EtlTemplateInfo(String name, Map<String, Object> parameters) {
		this(name);
		
		this.parameters = parameters;
	}
	
	public List<TemplateOverride> getOverride() {
		return override;
	}
	
	public void setOverride(List<TemplateOverride> override) {
		this.override = override;
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
	
	public boolean hasOverride() {
		return utilities.listHasElement(this.getOverride());
	}
	
	public void ensureReplacementOfParametersPlaceHolders(Map<String, Object> params) {
		for (Entry<String, Object> e : this.getParameters().entrySet()) {
			if (e.getValue() instanceof String) {
				e.setValue(EtlDataConfiguration.resolvePlaceholders(e.getValue().toString(), null, null, null, params));
			}
		}
	}
	
}
