package org.openmrs.module.epts.etl.conf.interfaces;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			    this.getTemplate());
			
			EtlDataConfiguration fromTemplate = template.parseToEtlDataConfiguration(this.getClass(),
			    this.getTemplate().getParameters());
			
			this.copyFromTemplate(fromTemplate);
		}
	}
	
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
				
				try {
					field.setAccessible(true);
					
					Object templateValue = field.get(template);
					
					if (templateValue != null && !field.getName().equals("template")) {
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
	
	public static String resolvePlaceholders(String text, Properties fileProps, Properties sysProps,
	        Map<String, String> env) {
		
		Matcher m = PLACEHOLDER.matcher(text);
		StringBuffer sb = new StringBuffer();
		
		while (m.find()) {
			String key = m.group(1);
			
			String value = null;
			
			// 1. ENV
			value = env.get(key);
			
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
			
			m.appendReplacement(sb, Matcher.quoteReplacement(value));
		}
		
		m.appendTail(sb);
		
		return sb.toString();
	}
}
