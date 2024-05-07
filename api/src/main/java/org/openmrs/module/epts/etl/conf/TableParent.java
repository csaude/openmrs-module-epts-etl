package org.openmrs.module.epts.etl.conf;

public class TableParent extends AbstractTableConfiguration {
	
	private RefInfo ref;
	
	/*
	 * Generic defaultValueDueInconsistency value which will be applied to all auto mapped field if the ref is not specified
	 */
	private Object defaultValueDueInconsistency;
	
	/*
	 * Generic setNullDueInconsistency value which will be applied to all auto mapped field if the ref is not specified
	 */
	private boolean setNullDueInconsistency;
	
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
	
	public Object getDefaultValueDueInconsistency() {
		return defaultValueDueInconsistency;
	}
	
	public void setDefaultValueDueInconsistency(Object defaultValueDueInconsistency) {
		this.defaultValueDueInconsistency = defaultValueDueInconsistency;
	}
	
	public boolean isSetNullDueInconsistency() {
		return setNullDueInconsistency;
	}
	
	public void setSetNullDueInconsistency(boolean setNullDueInconsistency) {
		this.setNullDueInconsistency = setNullDueInconsistency;
	}
	
}
