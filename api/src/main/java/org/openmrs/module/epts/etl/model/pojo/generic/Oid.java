package org.openmrs.module.epts.etl.model.pojo.generic;

import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.PrimaryKey;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

public class Oid extends PrimaryKey {
	
	private boolean fullLoaded;
	
	public Oid() {
		super(null);
		
		this.fullLoaded = false;
	}
	
	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	public void setFullLoaded(boolean fullLoaded) {
		this.fullLoaded = fullLoaded;
	}
	
	public boolean isSimpleId() {
		return !this.isCompositeKey();
	}
	
	/**
	 * Retrieves the simple value for this Oid
	 * 
	 * @return the simple value for this oid
	 * @throws ForbiddenOperationException if this oid is not simple
	 */
	public Object getSimpleValue() throws ForbiddenOperationException {
		if (!isSimpleId()) {
			throw new ForbiddenOperationException("This oid is not simple. You cannot retrieve a simple value");
		}
		
		return retrieveSimpleKey().getValue();
	}
	
	public Integer getSimpleValueAsInt() throws ForbiddenOperationException {
		if (utilities.isNumeric(getSimpleValue().toString())) {
			return Integer.parseInt(getSimpleValue().toString());
		}
		
		throw new ForbiddenOperationException("The value for this pk is not numeric!");
	}
	
	/**
	 * Create a Oid populated with an initial entries passed by parameter
	 * 
	 * @param params the entries which will composite the Oid. It's an array which emulate a map
	 *            entries in this format [key1, val1, key2, val2, key3, val3, ..]
	 * @return the generated map
	 * @throws ForbiddenOperationException when the params array length is not odd
	 */
	public static Oid fastCreate(Object... params) throws ForbiddenOperationException {
		if (params.length % 2 != 0)
			throw new ForbiddenOperationException("The parameters for fastCreat must be pars <K1, V1>, <K2, V2>");
		
		Oid oid = new Oid();
		
		int paramsSize = params.length / 2;
		
		for (int set = 1; set <= paramsSize; set++) {
			int pos = set * 2 - 1;
			
			Key k = new Key(((String) params[pos - 1]));
			
			k.setValue(params[pos]);
			
			oid.addKey(k);
		}
		
		return oid;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Oid))
			return false;
		
		Oid oid = (Oid) obj;
		
		if (!super.equals(obj))
			return false;
		
		for (Key thisKey : this.getFields()) {
			Key otherKey = oid.getKey(thisKey.getName());
			
			if (!thisKey.getValue().equals(otherKey.getValue())) {
				return false;
			}
		}
		
		return true;
		
	}
	
	public boolean hasAtLeastOneField() {
		if (!utilities.arrayHasElement(this.getFields()))
			return false;
		
		for (Key field : this.getFields()) {
			if (field.getValue() != null) {
				return true;
			}
		}
		
		return false;
	}
	
	public Key asSimpleKey() {
		if (isSimpleKey()) {
			return getFields().get(0);
		}
		
		throw new ForbiddenOperationException("The key is composite");
	}
	
}
