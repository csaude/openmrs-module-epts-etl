package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a child table
 */
public class ChildTable extends AbstractRelatedTable {
	
	private TableConfiguration parentTableConf;
	
	public ChildTable() {
	}
	
	public ChildTable(String tableName, String refCode) {
		super(tableName, refCode);
	}
	
	public static ChildTable init(String tableName, String refCoString) {
		return new ChildTable(tableName, refCoString);
	}
	
	public TableConfiguration getParentTableConf() {
		return parentTableConf;
	}
	
	public void setParentTableConf(TableConfiguration parentTableConf) {
		this.parentTableConf = parentTableConf;
		
		this.setRelatedEtlConfig(parentTableConf.getRelatedEtlConf());
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return this.parentTableConf.getRelatedConnInfo();
	}
	
	public boolean isSharedPk() {
		if (this.getSharePkWith() == null) {
			return false;
		} else if (utilities.arrayHasElement(this.getParentRefInfo())) {
			
			for (ParentTable parent : this.getParentRefInfo()) {
				if (parent.equals(this.parentTableConf.getSharedKeyRefInfo())) {
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
		
		for (RefMapping map : this.getRefMapping()) {
			uk.addKey(new Key(map.getChildFieldName()));
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
			
			conditionFields += this.getTableAlias() + "." + field.getChildFieldName() + " = "
			        + getRelatedTabConf().getTableAlias() + "." + field.getParentFieldName();
		}
		
		return conditionFields;
	}
	
	@Override
	public TableConfiguration getRelatedTabConf() {
		return this.parentTableConf;
	}
	
	@Override
	public void setRelatedTabConf(TableConfiguration relatedTabConf) {
		this.parentTableConf = relatedTabConf;
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String str = super.toString();
		
		str += this.hasRelated() ? " Child of " + this.getRelatedTabConf().getFullTableDescription() : "";
		
		if (hasMapping()) {
			str += ": ";
			
			for (RefMapping map : this.getRefMapping()) {
				if (utilities.stringHasValue(str)) {
					str += ",";
				}
				
				str += map.toString();
			}
		}
		
		return str;
	}
	
	@Override
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
}
