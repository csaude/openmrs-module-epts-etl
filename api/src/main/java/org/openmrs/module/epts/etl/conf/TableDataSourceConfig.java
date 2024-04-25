package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

/**
 * Represents a source table configuration. A {@link TableDataSourceConfig} is used as an auxiliary
 * extraction table as well as an extra datasource
 */
public class TableDataSourceConfig extends AbstractTableConfiguration implements DatabaseObjectConfiguration, EtlDataSource {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private SrcConf relatedSrcConf;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	@Override
	public boolean isRequired() {
		return this.joinType.isLeftJoin();
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		super.fullLoad(conn);
		
		if (!utilities.arrayHasElement(this.joinFields)) {
			//Try to autoload join fields
			
			List<FieldsMapping> fm = new ArrayList<>();
			
			//Assuming that this datasource is parent
			List<RefInfo> pInfo = this.relatedSrcConf.findAllRefToParent(this.getTableName());
			
			if (pInfo != null) {
				for (RefInfo ref : pInfo) {
					for (RefMapping map : ref.getFieldsMapping()) {
						fm.add(new FieldsMapping(map.getParentField().getName(), "", map.getChildField().getName()));
					}
				}
			} else {
				
				//Assuning that the this data src is child
				pInfo = this.findAllRefToParent(this.relatedSrcConf.getTableName());
				
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
			throw new ForbiddenOperationException("No join fields were difined between " + this.relatedSrcConf.getTableName()
			        + " And " + this.getTableName());
		}
		
		if (this.joinType == null) {
			if (utilities.arrayHasMoreThanOneElements(this.getParent().getExtraTableDataSource())) {
				this.joinType = JoinType.LEFT;
			} else {
				this.joinType = JoinType.INNER;
			}
		}
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
	
	@Override
	public SrcConf getRelatedSrcConf() {
		return relatedSrcConf;
	}
	
	@Override
	public void setRelatedSrcConf(SrcConf relatedSrcConf) {
		this.relatedSrcConf = relatedSrcConf;
	}
	
	@Override
	public SrcConf getParent() {
		return this.relatedSrcConf;
	}
	
	@Override
	public EtlConfiguration getRelatedSyncConfiguration() {
		return getParent().getRelatedSyncConfiguration();
	}
	
	@Override
	public DatabaseObject loadRelatedSrcObject(DatabaseObject mainObject, Connection srcConn, AppInfo srcAppInfo)
	        throws DBException {
		String condition = generateConditionsFields(mainObject);
		
		return DatabaseObjectDAO.find(this.getLoadHealper(), this.getSyncRecordClass(srcAppInfo), condition, srcConn);
	}
	
	private String generateConditionsFields(DatabaseObject dbObject) {
		String conditionFields = "";
		
		for (int i = 0; i < this.joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.joinFields.get(i);
			
			Object value;
			
			try {
				value = dbObject.getFieldValue(field.getSrcField());
			}
			catch (ForbiddenOperationException e) {
				value = dbObject.getFieldValue(field.getSrcFieldAsClassField());
			}
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getDstField(), value);
		}
		
		if (utilities.stringHasValue(this.getJoinExtraCondition())) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	public String generateConditionsFields() {
		String conditionFields = "";
		
		for (int i = 0; i < this.joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.joinFields.get(i);
			
			String tableName = DBUtilities.extractTableNameFromFullTableName(getTableName());
			
			conditionFields += "src_." + field.getSrcField() + " = " + tableName + "." + field.getDstField();
		}
		
		if (utilities.stringHasValue(this.getJoinExtraCondition())) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	public void addJoinField(FieldsMapping fm) {
		if (this.joinFields == null) {
			this.joinFields = new ArrayList<>();
		}
		
		this.joinFields.add(fm);
	}
	
	@Override
	public String getName() {
		return super.getTableName();
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return this.relatedSrcConf.getRelatedAppInfo();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
}
