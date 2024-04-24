package org.openmrs.module.epts.etl.conf;

public class TableParent extends AbstractTableConfiguration {
	
	private RefInfo ref;
	
	public RefInfo getRef() {
		return ref;
	}
	
	public void setRef(RefInfo ref) {
		this.ref = ref;
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return ref.getChildTableConf().getRelatedAppInfo();
	}
	
}
