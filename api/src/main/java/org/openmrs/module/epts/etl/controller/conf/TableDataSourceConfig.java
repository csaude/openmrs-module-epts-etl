package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.SyncExtraDataSource;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.PojobleDatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents a query configuration. A query is used on data mapping between source and destination
 * table
 */
public class TableDataSourceConfig extends SyncTableConfiguration implements PojobleDatabaseObject, SyncDataSource {
	
	private SyncExtraDataSource relatedSrcExtraDataSrc;
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	public String getJoinExtraCondition() {
		return joinExtraCondition;
	}
	
	public void setJoinExtraCondition(String joinExtraCondition) {
		this.joinExtraCondition = joinExtraCondition;
	}
	
	public List<FieldsMapping> getJoinFields() {
		return joinFields;
	}
	
	public void setJoinFields(List<FieldsMapping> joinFields) {
		this.joinFields = joinFields;
	}
	
	public SyncExtraDataSource getRelatedSrcExtraDataSrc() {
		return relatedSrcExtraDataSrc;
	}
	
	public void setRelatedSrcExtraDataSrc(SyncExtraDataSource relatedSrcExtraDataSrc) {
		this.relatedSrcExtraDataSrc = relatedSrcExtraDataSrc;
	}
	
	@Override
	public DatabaseObject loadRelatedSrcObject(DatabaseObject mainObject, Connection srcConn, AppInfo srcAppInfo)
	        throws DBException {
		String condition = generateConditionsFields(mainObject);
		
		return DatabaseObjectDAO.find(this.getSyncRecordClass(srcAppInfo), condition, srcConn);
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
		
		if (utilities.stringHasValue(this.getJoinExtraCondition())) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	@Override
	public String getName() {
		return super.getTableName();
	}
}
