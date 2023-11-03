package org.openmrs.module.epts.etl.controller.conf;

import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public abstract class BaseConfiguration {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<Extension> extension;
	
	public BaseConfiguration() {
	}
	
	public List<Extension> getExtension() {
		return extension;
	}
	
	public void setExtension(List<Extension> extension) {
		this.extension = extension;
	}
	
	public Extension findExtension(String coding) throws ForbiddenOperationException{
		if (!utilities.arrayHasElement(this.extension))
			throw new ForbiddenOperationException("Not defined extension '" + coding + "");
		
		for (Extension item : this.extension) {
			if (item.getCoding().equals(coding))
				return item;
		}
		
		throw new ForbiddenOperationException("Not defined extension '" + coding + "");
	}
}
