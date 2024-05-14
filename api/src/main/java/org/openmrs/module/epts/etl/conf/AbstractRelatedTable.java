package org.openmrs.module.epts.etl.conf;

import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.RelatedTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;

/**
 * Represents an related table, to an {@link AbstractTableConfiguration}
 */
public abstract class AbstractRelatedTable extends AbstractTableConfiguration implements RelatedTable {
	
	public static final String PARENT_REF_TYPE = "PARENT";
	
	public static final String CHILD_REF_TYPE = "CHILD";
	
	private String refCode;
	
	private List<RefMapping> refMapping;
	
	public AbstractRelatedTable() {
	}
	
	public AbstractRelatedTable(String tableName, String refCode) {
		super(tableName);
		
		this.refCode = refCode;
	}
	
	@Override
	public void clone(TableConfiguration toCloneFrom) {
		super.clone(toCloneFrom);
		
		if (toCloneFrom instanceof AbstractRelatedTable) {
			AbstractRelatedTable toCloneFromAsRelated = (AbstractRelatedTable) toCloneFrom;
			
			this.refCode = toCloneFromAsRelated.refCode;
			this.refMapping = toCloneFromAsRelated.refMapping;
		}
	}
	
	public String getRefCode() {
		return refCode;
	}
	
	@Override
	public void setRefCode(String refCode) {
		this.refCode = refCode;
	}
	
	@Override
	public List<RefMapping> getRefMapping() {
		return refMapping;
	}
	
	@Override
	public void setRefMapping(List<RefMapping> refMapping) {
		this.refMapping = refMapping;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (obj instanceof String) {
			return this.refCode.equals(obj);
		}
		
		if (!(obj instanceof AbstractRelatedTable))
			return false;
		
		AbstractRelatedTable other = (AbstractRelatedTable) obj;
		
		if (utilities.stringHasValue(this.refCode) && utilities.stringHasValue(other.refCode)) {
			return this.refCode.equals(other.refCode);
		}
		
		if (!this.getRelatedTabConf().equals(other.getRelatedTabConf())) {
			return false;
		}
		
		for (RefMapping map : this.refMapping) {
			if (!other.refMapping.contains(map)) {
				return false;
			}
		}
		
		for (RefMapping map : other.refMapping) {
			if (!this.refMapping.contains(map)) {
				return false;
			}
		}
		
		return true;
	}
}
