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
	
	private AbstractTableConfiguration tableConfiguration;
	
	private List<Field> fields;
	
	public GenericDatabaseObject() {
	}
	
	@Override
	public void setTableConfiguration(AbstractTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}
	
	@Override
	public AbstractTableConfiguration getTableConfiguration() {
		return this.tableConfiguration;
	}
	
	public void load(ResultSet rs) throws SQLException {
		try {
			
			if (this.tableConfiguration == null) {
				throw new ForbiddenOperationException("The tableConfiguration  is not set");
			}
			
			super.load(rs);
			
			this.fields = this.tableConfiguration.cloneFields();
			
			for (Field field : this.fields) {
				field.setValue(retrieveFieldValue(field.getName(), field.getType(), rs));
			}
			
		}
		catch (SQLException e) {}
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		return this.tableConfiguration.generateInsertParamsWithoutObjectId(this);
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		return this.tableConfiguration.generateInsertValuesWithoutObjectId(this);
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		return this.tableConfiguration.generateInsertParamsWithObjectId(this);
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		return this.tableConfiguration.getInsertSQLWithObjectId();
	}
	
	@Override
	@JsonIgnore
	public String getUpdateSQL() {
		return this.tableConfiguration.getUpdateSQL();
	}
	
	@Override
	@JsonIgnore
	public Object[] getUpdateParams() {
		return this.tableConfiguration.generateUpdateParams(this);
	}
	
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return this.tableConfiguration.generateInsertValuesWithoutObjectId(this);
	}
	
	@Override
	public String generateInsertValuesWithObjectId() {
		return this.tableConfiguration.generateInsertValuesWithObjectId(this);
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
		return this.tableConfiguration.getTableName();
	}
	
	public static GenericDatabaseObject fastCreate(SyncImportInfoVO syncImportInfo, AbstractTableConfiguration tableConf) {
		GenericDatabaseObject obj = new GenericDatabaseObject();
		
		obj.setTableConfiguration(tableConf);
		
		obj.setObjectId(Oid.fastCreate("", syncImportInfo.getRecordOriginId()));
		
		return obj;
	}
	
}
