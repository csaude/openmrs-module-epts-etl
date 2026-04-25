package org.openmrs.module.epts.etl.conf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class EtlTemplateInfo extends AbstractEtlDataConfiguration {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String name;
	
	private Map<String, Object> parameters;
	
	private List<TemplateOverride> override;
	
	private EtlTemplateInfo parentTemplate;
	
	public EtlTemplateInfo() {
	}
	
	public EtlTemplateInfo(String name, Map<String, Object> parameters) {
		this(name);
		
		this.parameters = parameters;
	}
	
	public boolean hasParentTemplate() {
		return this.getParentTemplate() != null;
	}
	
	public EtlTemplateInfo getParentTemplate() {
		return parentTemplate;
	}
	
	public void setParentTemplate(EtlTemplateInfo parentTemplate) {
		this.parentTemplate = parentTemplate;
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
	
	public Map<String, Object> getAllAvailableParameters() {
		return getAllAvaliableParameters(new HashSet<>());
	}
	
	private Map<String, Object> getAllAvaliableParameters(Set<EtlTemplateInfo> visited) {
		
		if (visited.contains(this)) {
			throw new EtlExceptionImpl("Circular reference detected in template hierarchy");
		}
		
		visited.add(this);
		
		Map<String, Object> avaliableParameters = new HashMap<>();
		
		if (this.parentTemplate != null) {
			avaliableParameters.putAll(this.parentTemplate.getAllAvaliableParameters(visited));
		}
		
		if (this.parameters != null) {
			avaliableParameters.putAll(this.parameters);
		}
		
		return avaliableParameters;
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
	
	@Override
	public EtlDataConfiguration getParentConf() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
		// TODO Auto-generated method stub
		
	}
	
}
