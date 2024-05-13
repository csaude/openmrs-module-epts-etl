package org.openmrs.module.epts.etl.conf.interfaces;

import java.util.List;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public interface BaseConfiguration {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	
	List<Extension> getExtension();
	
	
	public void setExtension(List<Extension> extension);
	
	default Extension findExtension(String coding) throws ForbiddenOperationException{
		if (!utilities.arrayHasElement(this.getExtension()))
			throw new ForbiddenOperationException("Not defined extension '" + coding + "");
		
		for (Extension item : this.getExtension()) {
			if (item.getCoding().equals(coding))
				return item;
		}
		
		throw new ForbiddenOperationException("Not defined extension '" + coding + "");
	}
}
