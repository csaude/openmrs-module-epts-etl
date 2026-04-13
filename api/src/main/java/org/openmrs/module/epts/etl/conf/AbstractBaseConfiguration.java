package org.openmrs.module.epts.etl.conf;

import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.BaseConfiguration;

public abstract class AbstractBaseConfiguration implements BaseConfiguration {
	
	private Object comments;
	
	private List<Extension> extension;
	
	private List<DefaultEtlValidator> validators;
	
	public AbstractBaseConfiguration() {
	}
	
	public List<Extension> getExtension() {
		return extension;
	}
	
	public void setExtension(List<Extension> extension) {
		this.extension = extension;
	}
	
	public Object getComments() {
		return comments;
	}
	
	public void setComments(Object comments) {
		this.comments = comments;
	}
	
	public List<DefaultEtlValidator> getValidators() {
		return validators;
	}
	
	public void setValidators(List<DefaultEtlValidator> validators) {
		this.validators = validators;
	}
	
	public boolean hasValidator() {
		return utilities.listHasElement(this.validators);
	}
	
	public static Boolean isTrue(Boolean b) {
		return b != null && b;
	}
	
	public static Boolean false_() {
		return Boolean.FALSE;
	}
	
	public static Boolean true_() {
		return Boolean.TRUE;
	}
}
