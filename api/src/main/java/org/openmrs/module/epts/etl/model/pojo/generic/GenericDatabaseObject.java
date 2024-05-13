package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.TableDataSourceConfig;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericDatabaseObject extends AbstractDatabaseObject {
	
	private DatabaseObjectConfiguration relatedConfiguration;
	
	private List<Field> fields;
	
	/*
	 * Objects from extraDataSource
	 */
	private List<GenericDatabaseObject> extraDataSourceObjects;
	
	private GenericDatabaseObject sharedPkObj;
	
	private boolean loadedFromDb;
	
	public GenericDatabaseObject() {
	}
	
	@Override
	public GenericDatabaseObject getSharedPkObj() {
		return sharedPkObj;
	}
	
	@Override
	public List<EtlDatabaseObject> getExtraDataSourceObjects() {
		return utilities.parseList(extraDataSourceObjects, EtlDatabaseObject.class);
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
	public void loadWithDefaultValues() {
		if (this.relatedConfiguration == null) {
			throw new ForbiddenOperationException("The relatedConfiguration  is not set");
		}
		
		for (Field f : this.fields) {
			if (!f.allowNull()) {
				f.loadWithDefaultValue();
			}
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
		
		if (relatedConfiguration instanceof SrcConf) {
			SrcConf srcConf = (SrcConf) relatedConfiguration;
			
			if (srcConf.getExtraTableDataSource() != null) {
				
				for (TableDataSourceConfig tsrc : srcConf.getExtraTableDataSource()) {
					
					if (extraDataSourceObjects == null) {
						extraDataSourceObjects = new ArrayList<>();
					}
					
					extraDataSourceObjects.add(new GenericDatabaseObject(tsrc));
				}
			}
		}
		
		if (relatedConfiguration instanceof TableConfiguration) {
			
			TableConfiguration tabConf = (TableConfiguration) relatedConfiguration;
			
			if (tabConf.getSharePkWith() != null) {
				
				if (extraDataSourceObjects != null) {
					for (GenericDatabaseObject obj : this.extraDataSourceObjects) {
						if (obj.getRelatedConfiguration().equals(tabConf.getSharedKeyRefInfo())) {
							this.sharedPkObj = obj;
						}
					}
				}
				
				if (this.sharedPkObj == null) {
					this.sharedPkObj = new GenericDatabaseObject(tabConf.getSharedKeyRefInfo());
				}
			}
			
		}
	}
	
	@Override
	public DatabaseObjectConfiguration getRelatedConfiguration() {
		return this.relatedConfiguration;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		try {
			super.load(rs);
			
			if (this.relatedConfiguration == null) {
				throw new ForbiddenOperationException("The relatedConfiguration  is not set");
			}
			
			for (Field field : this.fields) {
				try {
					field.setValue(retrieveFieldValue(
					    field.generateAliasedColumn((TableConfiguration) this.relatedConfiguration), field.getType(), rs));
					
					super.setFieldValue(field.getNameAsClassAtt(), field.getValue());
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			loadAdditionInfo(rs);
			
			loadedFromDb = true;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param rs
	 * @throws SQLException
	 */
	private void loadAdditionInfo(ResultSet rs) throws SQLException {
		if (this.relatedConfiguration instanceof SrcConf) {
			List<TableDataSourceConfig> allDs = ((SrcConf) this.relatedConfiguration).getExtraTableDataSource();
			
			if (utilities.arrayHasElement(allDs)) {
				for (TableDataSourceConfig ds : allDs) {
					GenericDatabaseObject dbo = findExtraObject(ds);
					
					dbo.load(rs);
				}
			}
		}
		
		if (this.sharedPkObj != null && !this.sharedPkObj.loadedFromDb) {
			this.sharedPkObj.load(rs);
		}
		
		if (this.relatedConfiguration instanceof TableConfiguration) {
			this.loadObjectIdData((TableConfiguration) this.relatedConfiguration);
		}
	}
	
	private GenericDatabaseObject findExtraObject(TableConfiguration tabConf) {
		for (GenericDatabaseObject dbo : this.extraDataSourceObjects) {
			if (dbo.relatedConfiguration.getAlias().equals(tabConf.getTableAlias())) {
				return dbo;
			}
		}
		
		throw new ForbiddenOperationException("No extra object found for " + tabConf.getTableName());
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateInsertParamsWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).getInsertSQLWithoutObjectId();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateInsertParamsWithObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).getInsertSQLWithObjectId();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getUpdateSQL() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).getUpdateSql();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public Object[] getUpdateParams() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateUpdateParams(this);
		}
		
		return null;
	}
	
	@Override
	public String generateInsertValuesWithoutObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateInsertValuesWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	public String generateInsertValuesWithObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateInsertValuesWithObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean hasParents() {
		if (utilities.arrayHasElement(this.relatedConfiguration.getParentRefInfo())) {
			for (ParentTable refInfo : this.relatedConfiguration.getParentRefInfo()) {
				for (RefMapping map : refInfo.getRefMapping()) {
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
			for (ParentTable refInfo : this.relatedConfiguration.getParentRefInfo()) {
				for (RefMapping map : refInfo.getRefMapping()) {
					
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
	
	@Override
	public String getObjectName() {
		return generateTableName();
	}
	
	public static GenericDatabaseObject fastCreate(SyncImportInfoVO syncImportInfo, TableConfiguration tableConf) {
		GenericDatabaseObject obj = new GenericDatabaseObject();
		
		obj.setRelatedConfiguration(tableConf);
		
		obj.setObjectId(Oid.fastCreate("", syncImportInfo.getRecordOriginId()));
		
		return obj;
	}
	
	@Override
	public void setParentToNull(ParentTableImpl refInfo) {
		for (RefMapping map : refInfo.getRefMapping()) {
			setFieldValue(map.getChildFieldName(), null);
		}
	}
	
	@Override
	public void changeParentValue(ParentTable refInfo, EtlDatabaseObject newParent) {
		for (RefMapping map : refInfo.getRefMapping()) {
			Object parentValue = newParent.getFieldValue(map.getChildFieldName());
			this.setFieldValue(map.getChildFieldName(), parentValue);
		}
		
	}
	
	@Override
	public String toString() {
		
		String tableName = this.relatedConfiguration != null ? this.relatedConfiguration.getObjectName() : "Aknown Object";
		
		return tableName + ": " + super.toString();
	}
}
