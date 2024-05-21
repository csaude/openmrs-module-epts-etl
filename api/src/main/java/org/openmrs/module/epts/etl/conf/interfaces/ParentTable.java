package org.openmrs.module.epts.etl.conf.interfaces;

import java.util.List;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents a parent table.
 */
@JsonDeserialize(as = ParentTableImpl.class)
public interface ParentTable extends RelatedTable {
	
	TableConfiguration getChildTableConf();
	
	void setChildTableConf(TableConfiguration childTableConf);
	
	public List<Field> getConditionalFields();
	
	public void setConditionalFields(List<Field> conditionalFields);
	
	default boolean isGeneric() {
		return false;
	}
	
	default AppInfo getRelatedAppInfo() {
		return this.getChildTableConf().getRelatedAppInfo();
	}
	
	default UniqueKeyInfo parseRelationshipToSelfKey() {
		UniqueKeyInfo uk = new UniqueKeyInfo(this);
		
		for (RefMapping map : this.getRefMapping()) {
			uk.addKey(new Key(map.getParentFieldName()));
		}
		
		return uk;
	}
	
	@Override
	default String generateJoinCondition() {
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
	
	default boolean hasConditionalFields() {
		return utilities.arrayHasElement(this.getConditionalFields());
	}
	
	default DstConf findRelatedDstConf() throws DBException {
		for (EtlItemConfiguration conf : getRelatedSyncConfiguration().getEtlItemConfiguration()) {
			
			if (!conf.isFullLoaded()) {
				conf.fullLoad();
			}
			
			for (DstConf dst : conf.getDstConf()) {
				if (dst.getTableName().equals(this.getTableName())) {
					return dst;
				}
			}
		}
		
		return null;
	}
	
	Object getDefaultValueDueInconsistency();
	
	void setDefaultValueDueInconsistency(Object defaultValueDueInconsistency);
	
	boolean isSetNullDueInconsistency();
	
	void setSetNullDueInconsistency(boolean setNullDueInconsistency);
	
}
