package org.openmrs.module.epts.etl.conf;

import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.BaseConfiguration;

public abstract class AbstractBaseConfiguration implements BaseConfiguration{

	private List<Extension> extension;
	
	public AbstractBaseConfiguration() {
	}
	
	public List<Extension> getExtension() {
		return extension;
	}
	
	public void setExtension(List<Extension> extension) {
		this.extension = extension;
	}
}
