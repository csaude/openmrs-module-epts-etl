package org.openmrs.module.epts.etl.conf.interfaces;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.ArrayList;
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
import org.openmrs.module.epts.etl.conf.TemplateOverride;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

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
	
	default String getTemplateName() {
		return hasTemplate() ? getTemplate().getName() : null;
	}
	
	default void tryToLoadFromTemplate() {
		if (this.hasTemplate()) {
			EtlConfigurationTemplate template = EtlConfigurationTemplate.findTemplate(this.getRelatedEtlConf(),
			    this.getTemplate().getName());
			
			template.setRelatedEtlConf(getRelatedEtlConf());
			
			EtlDataConfiguration fromTemplate = template.parseToEtlDataConfiguration(this.getClass(),
			    this.getTemplate().getParameters());
			
			fromTemplate.setRelatedEtlConfig(getRelatedEtlConf());
			
			this.copyFromTemplate(fromTemplate, this.getTemplate().getName());
			
			this.ensureTemplateOverride();
		}
		
	}
	
	default void ensureTemplateOverride() {
		if (this.hasTemplate() && this.getTemplate().hasOverride()) {
			for (TemplateOverride override : this.getTemplate().getOverride()) {
				override.setParent(this.getTemplate());
				override.applyOverride(this);
			}
		}
	}
	
	default boolean hasTemplate() {
		return this.getTemplate() != null;
	}
	
	default OpenConnection replicateConnection(Connection conn) throws DBException {
		
		if (!(conn instanceof OpenConnection)) {
			throw new EtlExceptionImpl("Only OpenConnection");
		}
		
		return ((OpenConnection) conn).getDbConnInfo().openConnection();
	}
	
	@SuppressWarnings("unchecked")
	default void copyFromTemplate(EtlDataConfiguration template, String templateName) {
		
		if (template == null) {
			return;
		}
		
		String errorSufix = "Error happened Within template: " + templateName;
		
		if (!this.getClass().isAssignableFrom(template.getClass())
		        && !template.getClass().isAssignableFrom(this.getClass())) {
			throw new EtlExceptionImpl(errorSufix + "> Incompatible template type: " + template.getClass().getName());
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
							throw new EtlExceptionImpl(errorSufix + "> Field '" + field.getName()
							        + "' is not a List but template provides a List.");
						}
						
					} else {
						if (!canBeOverriten(currentValue, field)) {
							throw new EtlExceptionImpl(errorSufix + ">  Field '" + field.getName()
							        + "' already has a value and cannot be overridden by template.");
						}
						
						field.set(this, templateValue);
					}
					
				}
				catch (IllegalAccessException e) {
					throw new EtlExceptionImpl(
					        errorSufix + ">  Error copying field '" + field.getName() + "' from template.", e);
				}
			}
			
			currentClass = currentClass.getSuperclass();
		}
	}
	
	static String[] SAFE_FIELDS = { "relatedEtlConf", "loadHealper", "onMultipleDataSourceForSameMapping", "onMultipleDataSourceWithSameName" };
	
	public static boolean canBeOverriten(Object value, Field field) {
		
		Class<?> type = field.getType();
		
		if (value == null) {
			return true;
		}
		
		if (type.isPrimitive()) {
			if (type == boolean.class)
				return !(Boolean) value;
			if (type == char.class)
				return ((Character) value) == '\u0000';
			if (type == byte.class)
				return ((Byte) value) == 0;
			if (type == short.class)
				return ((Short) value) == 0;
			if (type == int.class)
				return ((Integer) value) == 0;
			if (type == long.class)
				return ((Long) value) == 0L;
			if (type == float.class)
				return ((Float) value) == 0f;
			if (type == double.class)
				return ((Double) value) == 0d;
		} else {
			try {
				return utilities.getPosOnArray(SAFE_FIELDS, field.getName()) >= 0;
			}
			catch (RuntimeException e) {
				return false;
			}
		}
		
		return false;
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
