package org.openmrs.module.epts.etl.conf.interfaces;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.DefaultEtlValidator;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfigurationTemplate;
import org.openmrs.module.epts.etl.conf.EtlTemplateInfo;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;

public interface EtlDataConfiguration extends BaseConfiguration {
	
	Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");
	
	EtlConfiguration getRelatedEtlConf();
	
	EtlDataConfiguration getParentConf();
	
	boolean hasValidator();
	
	public List<DefaultEtlValidator> getValidators();
	
	void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration);
	
	default DBConnectionInfo getSrcConnInfo() {
		return this.getRelatedEtlConf().getSrcConnInfo();
	}
	
	void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc);
	
	ActionOnEtlException getGeneralBehaviourOnEtlException();
	
	EtlTemplateInfo getTemplate();
	
	void setTemplate(EtlTemplateInfo template);
	
	default void tryToLoadFromTemplate() {
		if (this.getTemplate() != null) {
			EtlConfigurationTemplate template = EtlConfigurationTemplate.findTemplate(this.getRelatedEtlConf(),
			    this.getTemplate().getName());
			
			EtlDataConfiguration parentFromTemplate = null;
			
			if (template.isExtension()) {
				EtlConfigurationTemplate baseTemplate = EtlConfigurationTemplate.findTemplate(this.getRelatedEtlConf(),
				    template.getExtendsTemplate());
				
				Set<String> childParams = template.getParameters() != null ? template.getParameters() : new HashSet<>();
				Set<String> parentParams = baseTemplate.getParameters() != null ? baseTemplate.getParameters()
				        : new HashSet<>();
				
				Map<String, Object> inputParams = this.getTemplate().getParameters() != null
				        ? this.getTemplate().getParameters()
				        : new HashMap<>();
				
				Map<String, Object> params = new HashMap<>();
				
				for (String paramFromChild : childParams) {
					if (parentParams.contains(paramFromChild)) {
						params.put(paramFromChild, inputParams.get(paramFromChild));
					}
				}
				
				parentFromTemplate = baseTemplate.parseToEtlDataConfiguration(this.getClass(),
				    this.getTemplate().getParameters());
				
			}
			
			EtlDataConfiguration fromTemplate = template.parseToEtlDataConfiguration(this.getClass(),
			    this.getTemplate().getParameters());
			
			fromTemplate.setRelatedEtlConfig(getRelatedEtlConf());
			
			fromTemplate.tryToLoadFromTemplate();
			
			if (parentFromTemplate != null) {
				fromTemplate.copyFromTemplate(parentFromTemplate);
			}
			
			this.copyFromTemplate(fromTemplate);
		}
	}
	
	@SuppressWarnings("unchecked")
	default void copyFromTemplate(EtlDataConfiguration template) {
		
		if (template == null) {
			return;
		}
		
		if (!this.getClass().isAssignableFrom(template.getClass())
		        && !template.getClass().isAssignableFrom(this.getClass())) {
			throw new EtlExceptionImpl("Incompatible template type: " + template.getClass().getName());
		}
		
		Class<?> currentClass = this.getClass();
		
		while (currentClass != null && currentClass != Object.class) {
			
			Field[] fields = currentClass.getDeclaredFields();
			
			for (Field field : fields) {
				
				int modifiers = field.getModifiers();
				
				if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
					continue;
				}
				
				if ("template".equals(field.getName())) {
					continue;
				}
				
				try {
					field.setAccessible(true);
					
					Object templateValue = field.get(template);
					
					if (templateValue == null) {
						continue;
					}
					
					Object currentValue = field.get(this);
					
					if (templateValue instanceof List<?>) {
						
						List<?> templateList = (List<?>) templateValue;
						
						if (currentValue == null) {
							field.set(this, new ArrayList<>(templateList));
						} else if (currentValue instanceof List<?>) {
							List<Object> currentList = (List<Object>) currentValue;
							currentList.addAll(templateList);
						} else {
							throw new EtlExceptionImpl(
							        "Field '" + field.getName() + "' is not a List but template provides a List.");
						}
						
					} else {
						if (currentValue != null) {
							throw new EtlExceptionImpl("Field '" + field.getName()
							        + "' already has a value and cannot be overridden by template.");
						}
						
						field.set(this, templateValue);
					}
					
				}
				catch (IllegalAccessException e) {
					throw new EtlExceptionImpl("Error copying field '" + field.getName() + "' from template.", e);
				}
			}
			
			currentClass = currentClass.getSuperclass();
		}
	}
	
	public static String resolvePlaceholders(String text, Set<String> allowedPlaceholders, Properties fileProps,
	        Properties sysProps, Map<String, ?> env) {
		
		if (text == null || text.isBlank()) {
			return text;
		}
		
		Matcher m = PLACEHOLDER.matcher(text);
		StringBuffer sb = new StringBuffer();
		
		while (m.find()) {
			
			String key = m.group(1);
			
			// 🔹 Se há whitelist e key não está nela → ignora
			if (allowedPlaceholders != null && !allowedPlaceholders.contains(key)) {
				m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0))); // mantém ${key}
				continue;
			}
			
			Object value = null;
			
			// 1. ENV
			if (env != null) {
				value = env.get(key);
			}
			
			// 2. System props
			if (value == null && sysProps != null) {
				value = sysProps.getProperty(key);
			}
			
			// 3. File props
			if (value == null && fileProps != null) {
				value = fileProps.getProperty(key);
			}
			
			if (value == null) {
				throw new IllegalArgumentException("Missing placeholder value for: " + key);
			}
			
			m.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
		}
		
		m.appendTail(sb);
		
		return sb.toString();
	}
}
