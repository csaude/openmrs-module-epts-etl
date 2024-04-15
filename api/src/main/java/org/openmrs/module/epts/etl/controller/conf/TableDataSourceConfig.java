package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
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
	
	private EtlExtraDataSource relatedSrcExtraDataSrc;
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private boolean required;
	
	@Override
	public boolean isRequired() {
		return this.required;
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		super.fullLoad(conn);
		
		if (!utilities.arrayHasElement(this.joinFields)) {
			//Try to autoload join fields
			
			List<FieldsMapping> fm = new ArrayList<>();
			
			//Assuming that this datasource is parent
			List<RefInfo> pInfo = this.relatedSrcExtraDataSrc.getRelatedSrcConf().findAllRefToParent(this.getTableName());
			
			if (pInfo != null) {
				for (RefInfo ref : pInfo) {
					for (RefMapping map : ref.getFieldsMapping()) {
						fm.add(new FieldsMapping(map.getParentField().getName(), "", map.getChildField().getName()));
					}
				}
			} else {
				
				//Assuning that the this data src is child
				pInfo = this.findAllRefToParent(this.relatedSrcExtraDataSrc.getRelatedSrcConf().getTableName());
				
				if (pInfo != null) {
					for (RefInfo ref : pInfo) {
						for (RefMapping map : ref.getFieldsMapping()) {
							fm.add(new FieldsMapping(map.getChildField().getName(), "", map.getParentField().getName()));
						}
					}
				}
			}
			
			if (fm != null) {
				this.joinFields = new ArrayList<>();
				for (FieldsMapping f : fm) {
					this.joinFields.add(f);
				}
			}
		}
		
		if (utilities.arrayHasNoElement(this.joinFields)) {
			throw new ForbiddenOperationException("No join fields were difined between "
			        + this.relatedSrcExtraDataSrc.getRelatedSrcConf().getTableName() + " And " + this.getTableName());
		}
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
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
	
	public EtlExtraDataSource getRelatedSrcExtraDataSrc() {
		return relatedSrcExtraDataSrc;
	}
	
	public void setRelatedSrcExtraDataSrc(EtlExtraDataSource relatedSrcExtraDataSrc) {
		this.relatedSrcExtraDataSrc = relatedSrcExtraDataSrc;
		setParent(relatedSrcExtraDataSrc);
	}
	
	@Override
	public SyncDataConfiguration getParent() {
		return this.relatedSrcExtraDataSrc;
	}
	
	@Override
	public SyncConfiguration getRelatedSyncConfiguration() {
		return getParent().getRelatedSyncConfiguration();
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
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getDstField(), value);
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
