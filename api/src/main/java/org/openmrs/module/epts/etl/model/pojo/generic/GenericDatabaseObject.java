package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.RefMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericDatabaseObject extends AbstractDatabaseObject {
	
	private PojobleDatabaseObject tableConfiguration;
	
	private List<Field> fields;
	
	public GenericDatabaseObject() {
	}
	
	public GenericDatabaseObject(PojobleDatabaseObject tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
		
		this.fields = this.tableConfiguration.cloneFields();
	}
	
	@Override
	public Object getFieldValue(String fieldName) {
		try {
			return utilities.getFieldValueOnFieldList(utilities.parseList(this.fields, Field.class), fieldName);
		}
		catch (ForbiddenOperationException e) {
			return super.getFieldValue(fieldName);
		}
		
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
		super.setFieldValue(fieldName, value);
		
		for (Field field : this.fields) {
			
			if (field.getName().equals(fieldName) || field.getNameAsClassAtt().equals(fieldName)) {
				field.setValue(value);
				
				break;
			}
		}
		
	}
	
	@Override
	public void setTableConfiguration(PojobleDatabaseObject tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
		
		this.fields = this.tableConfiguration.cloneFields();
	}
	
	@Override
	public PojobleDatabaseObject getTableConfiguration() {
		return this.tableConfiguration;
	}
	
	public void load(ResultSet rs) throws SQLException {
		try {
			
			if (this.tableConfiguration == null) {
				throw new ForbiddenOperationException("The tableConfiguration  is not set");
			}
			
			super.load(rs);
			
			for (Field field : this.fields) {
				field.setValue(retrieveFieldValue(field.getName(), field.getType(), rs));
			}
			
			if (this.tableConfiguration instanceof AbstractTableConfiguration) {
				loadObjectIdData((AbstractTableConfiguration) this.tableConfiguration);
			}
		}
		catch (SQLException e) {}
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).generateInsertParamsWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).generateInsertValuesWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).generateInsertParamsWithObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).getInsertSQLWithObjectId();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getUpdateSQL() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).getUpdateSQL();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public Object[] getUpdateParams() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).generateUpdateParams(this);
		}
		
		return null;
	}
	
	@Override
	public String generateInsertValuesWithoutObjectId() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).generateInsertValuesWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	public String generateInsertValuesWithObjectId() {
		if (this.tableConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.tableConfiguration).generateInsertValuesWithObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean hasParents() {
		if (utilities.arrayHasElement(this.tableConfiguration.getParentRefInfo())) {
			for (RefInfo refInfo : this.tableConfiguration.getParentRefInfo()) {
				for (RefMapping map : refInfo.getFieldsMapping()) {
					if (getFieldValue(map.getChildFieldName()) != null) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public Integer getParentValue(String parentAttName) {
		if (utilities.arrayHasElement(this.tableConfiguration.getParentRefInfo())) {
			for (RefInfo refInfo : this.tableConfiguration.getParentRefInfo()) {
				for (RefMapping map : refInfo.getFieldsMapping()) {
					
					if (map.getChildFieldName().equals(parentAttName)) {
						return (Integer) getFieldValue(map.getChildFieldName());
					}
				}
			}
		}
		
		return 0;
	}
	
	@Override
	public String generateTableName() {
		return this.tableConfiguration.getObjectName();
	}
	
	public static GenericDatabaseObject fastCreate(SyncImportInfoVO syncImportInfo, AbstractTableConfiguration tableConf) {
		GenericDatabaseObject obj = new GenericDatabaseObject();
		
		obj.setTableConfiguration(tableConf);
		
		obj.setObjectId(Oid.fastCreate("", syncImportInfo.getRecordOriginId()));
		
		return obj;
	}
	
}
