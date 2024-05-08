package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

/**
 * Represents a child table
 */
public class ChildTable extends RelatedTable {
	
	private AbstractTableConfiguration parentTableConf;
	
	public ChildTable() {
	}
	
	public ChildTable(String tableName, String refCode) {
		super(tableName, refCode);
	}
	
	public static ChildTable init(String tableName, String refCoString) {
		return new ChildTable(tableName, refCoString);
	}
	
	public AbstractTableConfiguration getParentTableConf() {
		return parentTableConf;
	}
	
	public void setParentTableConf(AbstractTableConfiguration parentTableConf) {
		this.parentTableConf = parentTableConf;
		
		this.setRelatedSyncConfiguration(parentTableConf.getRelatedSyncConfiguration());
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return this.parentTableConf.getRelatedAppInfo();
	}
	
	public boolean isSharedPk() {
		if (this.getSharePkWith() == null) {
			return false;
		} else if (utilities.arrayHasElement(this.getParentRefInfo())) {
			
			for (ParentTable parent : this.getParentRefInfo()) {
				if (parent.equals(this.parentTableConf.getSharePkWith())) {
					return true;
				}
			}
		}
		
		throw new ForbiddenOperationException("The related table of shared pk " + this.parentTableConf.getSharePkWith()
		        + " of table " + this.parentTableConf.getTableName() + " is not listed inparents!");
	}
	
	@Override
	public UniqueKeyInfo parseRelationshipToSelfKey() {
		UniqueKeyInfo uk = new UniqueKeyInfo(this);
		
		for (RefMapping map : this.getMapping()) {
			uk.addKey(new Key(map.getChildFieldName()));
		}
		
		return uk;
	}
	
	@Override
	public String generateJoinCondition() {
		String conditionFields = "";
		
		for (int i = 0; i < this.getMapping().size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			RefMapping field = this.getMapping().get(i);
			
			conditionFields += this.getTableAlias() + "." + field.getChildFieldName() + " = "
			        + getRelatedTabConf().getTableAlias() + "." + field.getParentFieldName();
		}
		
		return conditionFields;
	}
	
	@Override
	public AbstractTableConfiguration getRelatedTabConf() {
		return this.parentTableConf;
	}
	
	@Override
	public void setRelatedTabConf(AbstractTableConfiguration relatedTabConf) {
		this.parentTableConf = relatedTabConf;
	}
	
}
