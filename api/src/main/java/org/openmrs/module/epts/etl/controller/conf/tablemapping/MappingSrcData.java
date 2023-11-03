package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class represent a data from any related table
 */
public class MappingSrcData extends SyncTableConfiguration {
	
	private List<FieldsMapping> joinFields;
	
	public MappingSrcData() {
	}
	
	public List<FieldsMapping> getJoinFields() {
		return joinFields;
	}
	
	public void setJoinFields(List<FieldsMapping> joinFields) {
		this.joinFields = joinFields;
	}
	
	public DatabaseObject loadRelatedSrcObject(DatabaseObject mainObject, AppInfo appInfo, Connection conn)
	        throws DBException {
		String condition = generateConditionsFields(mainObject);
		
		return DatabaseObjectDAO.find(this.getSyncRecordClass(appInfo), condition, conn);
	}
	
	private String generateConditionsFields(DatabaseObject dbObject) {
		String conditionFields = "";
		
		for (int i = 0; i < this.joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.joinFields.get(i);
			
			Object value = dbObject.getFieldValue(field.getSrcFieldAsClassField());
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getDestField(), value);
		}
		
		return conditionFields;
	}
	
}
