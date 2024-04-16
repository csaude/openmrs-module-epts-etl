package org.openmrs.module.epts.etl.controller.conf;

public class TableParent extends SyncTableConfiguration {
	
	private RefInfo ref;
	
	public RefInfo getRef() {
		return ref;
	}
	
	public void setRef(RefInfo ref) {
		this.ref = ref;
	}
	
}
