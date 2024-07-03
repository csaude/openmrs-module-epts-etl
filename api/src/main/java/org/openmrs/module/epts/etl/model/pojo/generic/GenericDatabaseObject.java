package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericDatabaseObject extends AbstractDatabaseObject {
	
	private DatabaseObjectConfiguration relatedConfiguration;
	
	private GenericDatabaseObject sharedPkObj;
	
	private boolean loadedFromDb;
	
	public GenericDatabaseObject() {
	}
	
	@Override
	@JsonIgnore
	public Oid getObjectId() {
		return super.getObjectId();
	}
	
	@Override
	@JsonIgnore
	public String getUuid() {
		return super.getUuid();
	}
	
	@Override
	@JsonIgnore
	public GenericDatabaseObject getSharedPkObj() {
		return sharedPkObj;
	}
	
	public GenericDatabaseObject(DatabaseObjectConfiguration relatedConfiguration) {
		setRelatedConfiguration(relatedConfiguration);
	}
	
	@Override
	public Object getFieldValue(String fieldName) {
		String fieldNameInSnakeCase = utilities.parsetoSnakeCase(fieldName);
		String fieldNameInCameCase = utilities.parsetoCamelCase(fieldName);
		
		try {
			return utilities.getFieldValueOnFieldList(utilities.parseList(this.fields, Field.class), fieldNameInSnakeCase);
		}
		catch (ForbiddenOperationException e) {
			
			try {
				return utilities.getFieldValueOnFieldList(utilities.parseList(this.fields, Field.class),
				    fieldNameInCameCase);
			}
			catch (ForbiddenOperationException e1) {
				
				return super.getFieldValue(fieldName);
			}
			
		}
		
	}
	
	@Override
	public void loadWithDefaultValues(Connection conn) throws DBException {
		if (this.relatedConfiguration == null) {
			throw new ForbiddenOperationException("The relatedConfiguration  is not set");
		}
		
		TableConfiguration conf = (TableConfiguration) getRelatedConfiguration();
		
		for (Field f : this.fields) {
			if (!f.allowNull()) {
				
				ParentTable p = conf.getFieldIsRelatedParent(f);
				
				if (p != null) {
					EtlDatabaseObject defaultParent = null;
					
					try {
						if (!p.hasAlias()) {
							p.tryToGenerateTableAlias(conf.getRelatedEtlConf());
						}
						
						defaultParent = p.getDefaultObject(conn);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					if (defaultParent == null) {
						try {
							defaultParent = conf.getSyncRecordClass().newInstance();
							defaultParent.setRelatedConfiguration(p);
							
							if (defaultParent.checkIfAllRelationshipCanBeresolved(conf, conn)) {
								defaultParent = p.generateAndSaveDefaultObject(conn);
							} else {
								throw new ForbiddenOperationException("There are recursive relationship between "
								        + conf.getTableName() + " and " + p.getTableName()
								        + " which cannot automatically resolved...! Please manual create default record for one of thise table using id '-1'");
							}
						}
						catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
					
					this.changeParentValue(p, defaultParent);
				} else {
					f.loadWithDefaultValue();
				}
			}
		}
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
		try {
			super.setFieldValue(fieldName, value);
		}
		catch (ForbiddenOperationException e) {}
		
		for (Field field : this.fields) {
			
			if (field.getName().equals(fieldName) || field.getNameAsClassAtt().equals(fieldName)) {
				field.setValue(value);
				
				return;
			}
		}
		
		String tableName = this.relatedConfiguration != null ? this.relatedConfiguration.getObjectName() : "Aknown Object";
		
		throw new ForbiddenOperationException("The field " + fieldName + " was not found on entity " + tableName);
	}
	
	@Override
	public void setRelatedConfiguration(DatabaseObjectConfiguration tableConfiguration) {
		this.relatedConfiguration = tableConfiguration;
		
		this.fields = this.getRelatedConfiguration().cloneFields(this);
		
		if (getRelatedConfiguration() instanceof TableConfiguration) {
			
			TableConfiguration tabConf = (TableConfiguration) getRelatedConfiguration();
			
			if (tabConf.getSharePkWith() != null) {
				this.sharedPkObj = new GenericDatabaseObject(tabConf.getSharedKeyRefInfo());
			}
			
		}
	}
	
	@Override
	@JsonIgnore
	public DatabaseObjectConfiguration getRelatedConfiguration() {
		return this.relatedConfiguration;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		try {
			super.load(rs);
			
			if (!this.hasRelatedConfiguration()) {
				throw new ForbiddenOperationException("The relatedConfiguration  is not set");
			}
			
			for (Field field : this.fields) {
				
				if (getRelatedConfiguration() instanceof TableConfiguration) {
					field.setValue(retrieveFieldValue(
					    field.generateAliasedColumn((TableConfiguration) this.relatedConfiguration), field.getType(), rs));
				} else {
					field.setValue(retrieveFieldValue(field.getName(), field.getType(), rs));
				}
				
				try {
					super.setFieldValue(field.getNameAsClassAtt(), field.getValue());
				}
				catch (IllegalArgumentException e) {
					//ignore if field is objectId as there is name clash with super.objectId
					if (!field.getNameAsClassAtt().equals("objectId")) {
						throw e;
					}
					
				}
				catch (ForbiddenOperationException e) {}
				
			}
			
			if (this.getSharedPkObj() != null && !this.getSharedPkObj().loadedFromDb) {
				this.getSharedPkObj().load(rs);
			}
			
			if (this.getRelatedConfiguration() instanceof TableConfiguration) {
				this.loadObjectIdData((TableConfiguration) this.getRelatedConfiguration());
			}
			
			loadedFromDb = true;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
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
	@JsonIgnore
	public String generateInsertValuesWithoutObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateInsertValuesWithoutObjectId(this);
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String generateInsertValuesWithObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateInsertValuesWithObjectId(this);
		}
		
		return null;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithObjectId(String insertQuestionMarks) {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			((TableConfiguration) this.relatedConfiguration).setInsertSQLQuestionMarksWithObjectId(insertQuestionMarks);
		}
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLQuestionMarksWithObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).getInsertSQLQuestionMarksWithObjectId();
		}
		
		return null;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithoutObjectId(String insertQuestionMarks) {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			((TableConfiguration) this.relatedConfiguration).setInsertSQLQuestionMarksWithoutObjectId(insertQuestionMarks);
		}
	}
	
	@Override
	@JsonIgnore
	public String getInsertSQLQuestionMarksWithoutObjectId() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).getInsertSQLQuestionMarksWithoutObjectId();
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String generateFullFilledUpdateSql() {
		if (this.relatedConfiguration instanceof TableConfiguration) {
			return ((TableConfiguration) this.relatedConfiguration).generateFullFilledUpdateSql(this);
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
	public Object getParentValue(ParentTable parent) {
		if (this.relatedConfiguration.hasParentRefInfo()) {
			for (ParentTable refInfo : this.relatedConfiguration.getParentRefInfo()) {
				for (RefMapping map : refInfo.getRefMapping()) {
					
					if (map.getChildFieldName().equals(map.getParentFieldName())) {
						return getFieldValue(map.getChildFieldName());
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	@JsonIgnore
	public String generateTableName() {
		if (this.relatedConfiguration == null) {
			throw new ForbiddenOperationException("The relatedConfiguration  is not set for record [" + this + "]");
		}
		
		return this.relatedConfiguration.getObjectName();
	}
	
	@Override
	@JsonIgnore
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
			String parentFieldName = map.getParentFieldName();
			String childFieldName = map.getChildFieldName();
			
			Object parentValue = newParent.getFieldValue(parentFieldName);
			this.setFieldValue(childFieldName, parentValue);
		}
		
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		
		String tableName = this.relatedConfiguration != null ? this.relatedConfiguration.getObjectName() : "Aknown Object";
		
		return tableName + ": " + super.toString();
	}
	
	@Override
	public void copyFrom(EtlDatabaseObject copyFrom) {
		
		if (!hasRelatedConfiguration())
			throw new ForbiddenOperationException("The relatedConfiguration  is not set for record [" + this + "]");
		
		if (!getRelatedConfiguration().isFullLoaded())
			throw new ForbiddenOperationException("The relatedConfiguration  is not full loaded");
		
		for (Field f : this.fields) {
			try {
				f.setValue(copyFrom.getFieldValue(f.getName()));
			}
			catch (ForbiddenOperationException e) {
				f.setValue(copyFrom.getFieldValue(f.getNameAsClassAtt()));
			}
		}
		
	}
	
	@Override
	public void generateFields() {
		if (this.relatedConfiguration == null) {
			throw new ForbiddenOperationException("The relatedConfiguration  is not set");
		}
		
		if (fields == null)
			throw new ForbiddenOperationException(
			        "Unkown object state, the field should not be empty as the relatedConfiguration is not empty");
	}
	
	@Override
	public void setObjectId(Oid objectId) {
		for (Key key : objectId.getFields()) {
			setFieldValue(key.getName(), key.getValue());
		}
		
		loadObjectIdData((TableConfiguration) getRelatedConfiguration());
	}
	
	@Override
	@JsonIgnore
	public Date getDateChanged() {
		return super.getDateChanged();
	}
	
	@Override
	@JsonIgnore
	public Date getDateCreated() {
		return super.getDateCreated();
	}
	
	@Override
	@JsonIgnore
	public Date getDateVoided() {
		return super.getDateVoided();
	}
}
