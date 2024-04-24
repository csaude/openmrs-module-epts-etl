package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.RefInfo;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.TableDataSourceConfig;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericDatabaseObject extends AbstractDatabaseObject {
	
	private DatabaseObjectConfiguration relatedConfiguration;
	
	private List<Field> fields;
	
	public GenericDatabaseObject() {
	}
	
	public GenericDatabaseObject(DatabaseObjectConfiguration relatedConfiguration) {
		setRelatedConfiguration(relatedConfiguration);
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
	public void setRelatedConfiguration(DatabaseObjectConfiguration tableConfiguration) {
		this.relatedConfiguration = tableConfiguration;
		
		this.fields = this.relatedConfiguration.cloneFields();
		
		if (this.relatedConfiguration instanceof SrcConf) {
			List<TableDataSourceConfig> allDs = ((SrcConf) this.relatedConfiguration).getExtraTableDataSource();
			
			if (utilities.arrayHasElement(allDs)) {
				for (TableDataSourceConfig ds : allDs) {
					for (Field f : ds.getFields()) {
						if (!this.fields.contains(f)) {
							this.fields.add(f.createACopy());
						}
					}
				}
			}
			
		}
	}
	
	@Override
	public DatabaseObjectConfiguration getRelatedConfiguration() {
		return this.relatedConfiguration;
	}
	
	public void load(ResultSet rs) throws SQLException {
		try {
			
			if (this.relatedConfiguration == null) {
				throw new ForbiddenOperationException("The relatedConfiguration  is not set");
			}
			
			super.load(rs);
			
			for (Field field : this.fields) {
				field.setValue(retrieveFieldValue(field.getName(), field.getType(), rs));
			}
			
			if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
				loadObjectIdData((AbstractTableConfiguration) this.relatedConfiguration);
			}
		}
		catch (SQLException e) {}
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).generateInsertParamsWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).generateInsertValuesWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).generateInsertParamsWithObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).getInsertSQLWithObjectId();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getUpdateSQL() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).getUpdateSQL();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public Object[] getUpdateParams() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).generateUpdateParams(this);
		}
		
		return null;
	}
	
	@Override
	public String generateInsertValuesWithoutObjectId() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).generateInsertValuesWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	public String generateInsertValuesWithObjectId() {
		if (this.relatedConfiguration instanceof AbstractTableConfiguration) {
			return ((AbstractTableConfiguration) this.relatedConfiguration).generateInsertValuesWithObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean hasParents() {
		if (utilities.arrayHasElement(this.relatedConfiguration.getParentRefInfo())) {
			for (RefInfo refInfo : this.relatedConfiguration.getParentRefInfo()) {
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
		if (utilities.arrayHasElement(this.relatedConfiguration.getParentRefInfo())) {
			for (RefInfo refInfo : this.relatedConfiguration.getParentRefInfo()) {
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
		if (this.relatedConfiguration == null) {
			throw new ForbiddenOperationException("The relatedConfiguration  is not set for record [" + this + "]");
		}
		
		return this.relatedConfiguration.getObjectName();
	}
	
	public static GenericDatabaseObject fastCreate(SyncImportInfoVO syncImportInfo, AbstractTableConfiguration tableConf) {
		GenericDatabaseObject obj = new GenericDatabaseObject();
		
		obj.setRelatedConfiguration(tableConf);
		
		obj.setObjectId(Oid.fastCreate("", syncImportInfo.getRecordOriginId()));
		
		return obj;
	}
}
