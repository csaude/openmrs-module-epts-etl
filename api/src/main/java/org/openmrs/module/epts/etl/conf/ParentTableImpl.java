package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a parent table.
 */
public class ParentTableImpl extends AbstractRelatedTable implements ParentTable {
	
	private TableConfiguration childTableConf;
	
	private List<Field> conditionalFields;
	
	/*
	 * Generic defaultValueDueInconsistency value which will be applied to all auto mapped field if the ref is not specified
	 */
	private Object defaultValueDueInconsistency;
	
	/*
	 * Generic setNullDueInconsistency value which will be applied to all auto mapped field if the ref is not specified
	 */
	private boolean setNullDueInconsistency;
	
	public ParentTableImpl() {
	}
	
	public ParentTableImpl(String tableName, String refCode) {
		super(tableName, refCode);
	}
	
	public static ParentTableImpl init(String tableName, String refCode) {
		ParentTableImpl p = new ParentTableImpl(tableName, refCode);
		
		return p;
	}
	
	public TableConfiguration getChildTableConf() {
		return childTableConf;
	}
	
	public void setChildTableConf(TableConfiguration childTableConf) {
		this.childTableConf = (TableConfiguration) childTableConf;
		
		this.setRelatedEtlConfig(childTableConf.getRelatedEtlConf());
	}
	
	public List<Field> getConditionalFields() {
		return conditionalFields;
	}
	
	public void setConditionalFields(List<Field> conditionalFields) {
		this.conditionalFields = conditionalFields;
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public DBConnectionInfo	getRelatedConnInfo() {
		return this.childTableConf.getRelatedConnInfo();
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
	
	@Override
	public UniqueKeyInfo parseRelationshipToSelfKey() {
		UniqueKeyInfo uk = new UniqueKeyInfo(this);
		
		for (RefMapping map : this.getRefMapping()) {
			uk.addKey(new Key(map.getParentFieldName()));
		}
		
		return uk;
	}
	
	@Override
	public String generateJoinCondition() {
		String conditionFields = "";
		
		for (int i = 0; i < this.getRefMapping().size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			RefMapping field = this.getRefMapping().get(i);
			
			conditionFields += getRelatedTabConf().getTableAlias() + "." + field.getChildFieldName() + " = "
			        + this.getTableAlias() + "." + field.getParentFieldName();
		}
		
		return conditionFields;
	}
	
	@Override
	public TableConfiguration getRelatedTabConf() {
		return this.childTableConf;
	}
	
	@Override
	public void setRelatedTabConf(TableConfiguration relatedTabConf) {
		this.childTableConf = (TableConfiguration) relatedTabConf;
	}
	
	public boolean hasConditionalFields() {
		return utilities.arrayHasElement(this.conditionalFields);
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String str = super.toString();
		
		if (this.hasRelated()) {
			str += " Parent of " + this.getRelatedTabConf().getFullTableDescription();
		}
		
		String mappingStr = "";
		
		if (hasMapping()) {
			
			for (RefMapping map : this.getRefMapping()) {
				if (utilities.stringHasValue(mappingStr)) {
					mappingStr += ",";
				}
				
				mappingStr += map.toString();
			}
			
			str += ": " + mappingStr;
		}
		
		return str;
	}
	
	@Override
	public Oid generateParentOidFromChild(EtlDatabaseObject obj) {
		Oid oid = super.generateParentOidFromChild(obj);
		
		oid.setTabConf(this);
		
		return oid;
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
}
