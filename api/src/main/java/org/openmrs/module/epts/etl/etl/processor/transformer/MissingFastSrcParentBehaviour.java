package org.openmrs.module.epts.etl.etl.processor.transformer;

public enum MissingFastSrcParentBehaviour {
	
	COMPLAIN,
	CREATE_ON_DST;
	
	public boolean complainOnMissingSrcParent() {
		return this.equals(COMPLAIN);
	}
	
	public boolean createOnDst() {
		return this.equals(CREATE_ON_DST);
	}
}
