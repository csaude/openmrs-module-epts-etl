package org.openmrs.module.epts.etl.conf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EtlConfigurationTemplate {
	
	public static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static final Map<String, List<EtlConfigurationTemplate>> CACHE = new HashMap<>();
	
	private String name;
	
	private Set<String> parameters;
	
	private JsonNode template;
	
	@JsonProperty("extends")
	private String extendsTemplate;
	
	public void setExtendsTemplate(String extendsTemplate) {
		this.extendsTemplate = extendsTemplate;
	}
	
	public String getExtendsTemplate() {
		return extendsTemplate;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<String> getParameters() {
		return parameters;
	}
	
	public void setParameters(Set<String> parameters) {
		this.parameters = parameters;
	}
	
	public JsonNode getTemplate() {
		return template;
	}
	
	public void setTemplate(JsonNode template) {
		this.template = template;
	}
	
	public boolean isExtension() {
		return utilities.stringHasValue(this.getExtendsTemplate());
	}
	
	public <T extends EtlDataConfiguration> T parseToEtlDataConfiguration(Class<T> clazz, Map<String, Object> inputParams) {
		String json = null;
		
		try {
			
			validateAllowedParanms(inputParams);
			validateMissingParanms(inputParams);
			
			json = EtlDataConfiguration.resolvePlaceholders(this.template.toString(), this.getParameters(), null, null,
			    inputParams);
			
			return new ObjectMapperProvider().getContext(clazz).readValue(json, clazz);
			
		}
		catch (IOException | IllegalArgumentException e) {
			throw new EtlExceptionImpl("Error happened loading template " + this.name, e);
		}
	}
	
	void validateMissingParanms(Map<String, Object> inputParams) {
		
		Set<String> allowedParams = this.getParameters();
		
		if (allowedParams == null) {
			allowedParams = new HashSet<>();
		}
		
		List<String> missingParams = allowedParams.stream().filter(p -> !inputParams.containsKey(p)).toList();
		
		if (!missingParams.isEmpty()) {
			throw new EtlExceptionImpl(
			        "The following parameters are missing in template (" + this.getName() + "): " + missingParams);
		}
	}
	
	void validateAllowedParanms(Map<String, Object> inputParams) {
		if (inputParams == null)
			return;
		
		Set<String> allowedSet = this.getParameters() != null ? new HashSet<>(this.getParameters()) : Collections.emptySet();
		
		List<String> unknownParams = inputParams.keySet().stream().filter(key -> !allowedSet.contains(key)).toList();
		
		if (!unknownParams.isEmpty()) {
			throw new EtlExceptionImpl(
			        "The following parameters are not allowed for template (" + this.getName() + "): " + unknownParams);
		}
	}
	
	public static EtlConfigurationTemplate findTemplate(EtlConfiguration relatedEtlConf, String templateName) {
		
		String templatesFileLocation = relatedEtlConf.getEtlTemplatesFilePath();
		
		if (templatesFileLocation == null || templatesFileLocation.isBlank()) {
			throw new EtlExceptionImpl("Templates file path is not defined.");
		}
		
		List<EtlConfigurationTemplate> templates = CACHE.computeIfAbsent(templatesFileLocation, path -> {
			try {
				ObjectMapper mapper = new ObjectMapperProvider().getContext(EtlConfigurationTemplate.class);
				return mapper.readValue(new File(path),
				    mapper.getTypeFactory().constructCollectionType(List.class, EtlConfigurationTemplate.class));
			}
			catch (IOException e) {
				throw new EtlExceptionImpl("Error reading templates file: " + path, e);
			}
		});
		
		return templates.stream().filter(t -> templateName.equals(t.getName())).findFirst().orElseThrow(
		    () -> new EtlExceptionImpl("Template not found: " + templateName + " in file: " + templatesFileLocation));
	}
	
	@Override
	public String toString() {
		return name + "("
		        + (this.getParameters() != null && !this.getParameters().isEmpty() ? this.getParameters().toString() : "")
		        + ")";
	}
}
