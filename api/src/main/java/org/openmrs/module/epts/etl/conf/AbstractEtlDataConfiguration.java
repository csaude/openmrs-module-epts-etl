package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;

public abstract class AbstractEtlDataConfiguration extends AbstractBaseConfiguration implements EtlDataConfiguration {
	
	private EtlConfiguration relatedEtlConf;
	
	private EtlTemplateInfo template;
	
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
	
}
