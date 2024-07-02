package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ForbiddenException;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Extension {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String coding;
	
	private String valueString;
	
	private List<Extension> extension;
	
	public Extension() {
	}
	
	public Extension(String coding, String value) {
		this.coding = coding;
		this.valueString = value;
	}
	
	public List<Extension> getExtension() {
		return extension;
	}
	
	public String getCoding() {
		return coding;
	}
	
	public void setCoding(String coding) {
		this.coding = coding;
	}
	
	public String getValueString() {
		return valueString;
	}
	
	public void setValueString(String valueString) {
		this.valueString = valueString;
	}
	
	public void addData(Extension extensionData) {
		if (this.extension == null)
			this.extension = new ArrayList<Extension>();
		
		if (findExtension(extensionData.getCoding()) != null)
			throw new ForbiddenException("The item '" + extensionData.getCoding() + "' already exists on this extension!!");
		
		this.extension.add(extensionData);
	}
	
	public void setExtension(List<Extension> extension) {
		if (this.valueString != null)
			throw new ForbiddenOperationException("You cannot set both value and extension on any extension item!");
		
		this.extension = extension;
	}
	
	public static Extension fastCreate(String coding, String value) {
		Extension extension = new Extension();
		
		extension.addData(new Extension(coding, value));
		
		return extension;
	}
	
	public Extension findExtension(String coding) {
		if (utilities.arrayHasElement(this.extension))
			return null;
		
		for (Extension item : this.extension) {
			if (item.getCoding().equals(coding))
				return item;
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return utilities.parseToJSON(this);
	}
	
}
