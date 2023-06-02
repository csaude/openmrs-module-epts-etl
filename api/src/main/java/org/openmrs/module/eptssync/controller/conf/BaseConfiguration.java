package org.openmrs.module.eptssync.controller.conf;

import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

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
