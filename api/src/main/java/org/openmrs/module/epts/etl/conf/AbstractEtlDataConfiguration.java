package org.openmrs.module.epts.etl.conf;

import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;

public abstract class AbstractEtlDataConfiguration extends AbstractBaseConfiguration implements EtlDataConfiguration {
	
	private EtlConfiguration relatedEtlConf;
	
	private EtlTemplateInfo template;
	
	private List<String> dynamicElements;
	
	public EtlConfiguration getRelatedEtlConf() {
		return relatedEtlConf;
	}
	
	@Override
	public EtlTemplateInfo getTemplate() {
		return template;
	}
	
	@Override
	public void setTemplate(EtlTemplateInfo template) {
		this.template = template;
	}
	
	public void setRelatedEtlConfig(EtlConfiguration relatedEtlConf) {
		this.relatedEtlConf = relatedEtlConf;
	}
	
	@Override
	public ActionOnEtlException getGeneralBehaviourOnEtlException() {
		return relatedEtlConf.getGeneralBehaviourOnEtlException();
	}
	
	public EtlTemplateInfo retrieveNearestTemplate() {
		return this.getTemplate();
	}
	
	public List<String> getDynamicElements() {
		return dynamicElements;
	}
	
	public void setDynamicElements(List<String> dynamicElements) {
		this.dynamicElements = dynamicElements;
	}
	
}
