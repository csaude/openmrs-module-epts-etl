package org.openmrs.module.epts.etl.conf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EtlConfigurationTemplate {
	
	private static final Map<String, List<EtlConfigurationTemplate>> CACHE = new HashMap<>();
	
	private String name;
	
	private List<String> parameters;
	
	private JsonNode template;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	
	public JsonNode getTemplate() {
		return template;
	}
	
	public void setTemplate(JsonNode template) {
		this.template = template;
	}
	
	public <T extends EtlDataConfiguration> T parseToEtlDataConfiguration(Class<T> clazz, Map<String, String> inputParams) {
		String json = null;
		
		try {
			json = EtlDataConfiguration.resolvePlaceholders(this.template.toString(), null, null, inputParams);
			
			return new ObjectMapperProvider().getContext(clazz).readValue(json, clazz);
		}
		catch (IOException | IllegalArgumentException e) {
			throw new EtlExceptionImpl("Error happened loading template " + this.name, e);
		}
	}
	
	public static EtlConfigurationTemplate findTemplate(EtlConfiguration relatedEtlConf, EtlTemplateInfo template) {
		
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
		
		return templates.stream().filter(t -> template.getName().equals(t.getName())).findFirst().orElseThrow(
		    () -> new EtlExceptionImpl("Template not found: " + template.getName() + " in file: " + templatesFileLocation));
	}
}
