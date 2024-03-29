package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Define the refencial information betwen a {@link SyncTableConfiguration} and its main parent;
 * 
 * @author jpboane
 */
public class UniqueKeyInfo {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String keyName;
	
	private List<Field> fields;
	
	public UniqueKeyInfo() {
	}
	
	public static UniqueKeyInfo init(String keyName) {
		UniqueKeyInfo uk = new UniqueKeyInfo();
		uk.keyName = keyName;
		
		return uk;
	}
	
	@JsonIgnore
	public List<String> generateListFromFieldsNames() {
		List<String> fieldsAsName = new ArrayList<String>();
		
		for (Field field : this.fields) {
			fieldsAsName.add(field.getName());
		}
		
		return fieldsAsName;
	}
	
	public String getKeyName() {
		return keyName;
	}
	
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	
	public void loadValuesToFields(DatabaseObject object) {
		for (Field field : this.fields) {
			String attClasse = AttDefinedElements.convertTableAttNameToClassAttName(field.getName());
			
			Object[] fieldValues = object.getFieldValues(attClasse);
			
			Object value = fieldValues != null ? fieldValues[0] : null;
			
			field.setValue(value);
		}
	}
	
	public static List<UniqueKeyInfo> loadUniqueKeysInfo(SyncTableConfiguration tableConfiguration, Connection conn)
	        throws DBException {
		if (!utilities.stringHasValue(tableConfiguration.getPrimaryKey(conn))) {
			throw new ForbiddenOperationException("The primary key is not defined!");
		}
		
		List<UniqueKeyInfo> uniqueKeysInfo = new ArrayList<UniqueKeyInfo>();
		
		ResultSet rs = null;
		
		try {
			String tableName = tableConfiguration.getTableName();
			
			rs = conn.getMetaData().getIndexInfo(conn.getCatalog(), conn.getSchema(), tableName, true, true);
			
			String prevIndexName = null;
			
			List<Field> keyElements = null;
			
			String indexName = "";
			
			while (rs.next()) {
				indexName = rs.getString("INDEX_NAME");
				
				if (!indexName.equals(prevIndexName)) {
					addUniqueKey(prevIndexName, keyElements, uniqueKeysInfo, tableConfiguration, conn);
					
					prevIndexName = indexName;
					keyElements = new ArrayList<>();
				}
				
				keyElements.add(new Field(rs.getString("COLUMN_NAME")));
			}
			
			addUniqueKey(prevIndexName, keyElements, uniqueKeysInfo, tableConfiguration, conn);
			
			if (tableConfiguration.useSharedPKKey()) {
				SyncTableConfiguration parentTableInfo = new SyncTableConfiguration();
				
				parentTableInfo = new SyncTableConfiguration();
				parentTableInfo.setTableName(tableConfiguration.getSharePkWith());
				parentTableInfo.setParent(tableConfiguration.getParent());
				
				List<UniqueKeyInfo> parentUniqueKeys = loadUniqueKeysInfo(parentTableInfo, conn);
				
				if (utilities.arrayHasElement(parentUniqueKeys)) {
					uniqueKeysInfo.addAll(parentUniqueKeys);
				}
			}
			
			return uniqueKeysInfo;
		}
		catch (SQLException e) {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (SQLException e1) {}
			}
			
			throw new DBException(e);
		}
		
	}
	
	private static boolean addUniqueKey(String keyName, List<Field> keyElements, List<UniqueKeyInfo> uniqueKeys,
	        SyncTableConfiguration config, Connection conn) {
		
		if (keyElements == null || keyElements.isEmpty())
			return false;
		
		//Don't add PK as uniqueKey
		if (keyElements.size() == 1 && keyElements.get(0).getName().equals(config.getPrimaryKey(conn))) {
			return false;
		}
		
		if (uniqueKeys == null)
			uniqueKeys = new ArrayList<>();
		
		UniqueKeyInfo uk = UniqueKeyInfo.init(keyName);
		uk.fields = keyElements;
		
		uniqueKeys.add(uk);
		
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof UniqueKeyInfo))
			return false;
		
		UniqueKeyInfo other = (UniqueKeyInfo) obj;
		
		if (utilities.stringHasValue(this.keyName) && this.keyName.equals(other.keyName)) {
			return true;
		}
		
		return this.hasSameFields(other);
	}
	
	public boolean hasSameFields(UniqueKeyInfo other) {
		for (Field field : this.getFields()) {
			if (!other.getFields().contains(field))
				return false;
		}
		
		for (Field field : other.getFields()) {
			if (!this.getFields().contains(field))
				return false;
		}
		
		return true;
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String toString = keyName + "[";
		int i = 0;
		
		for (Field field : this.fields) {
			String fieldInfo = field.getName() + ": " + field.getValue();
			
			if (i > 0) {
				toString += ",";
			}
			
			toString += fieldInfo;
			i++;
		}
		
		toString += "]";
		
		return toString;
	}
	
	public void addField(Field field) {
		if (this.fields == null)
			this.fields = new ArrayList<Field>();
		
		if (!this.fields.contains(field)) {
			this.fields.add(field);
		}
	}
	
	public static UniqueKeyInfo generateFromFieldList(List<String> fields) {
		if (!utilities.arrayHasElement(fields))
			throw new ForbiddenOperationException("The list cannot be empty");
		
		UniqueKeyInfo uk = new UniqueKeyInfo();
		
		for (String fieldName : fields) {
			uk.addField(new Field(fieldName));
		}
		
		return uk;
	}
	
	protected UniqueKeyInfo cloneMe() {
		UniqueKeyInfo uk = UniqueKeyInfo.init(keyName);
		
		for (Field field : fields) {
			uk.addField(new Field(field.getName()));
		}
		return uk;
	}
	
	public static List<UniqueKeyInfo> cloneAll(List<UniqueKeyInfo> uniqueKeysInfo) {
		
		if (uniqueKeysInfo == null) {
			return null;
		}
		
		List<UniqueKeyInfo> cloned = new ArrayList<>(uniqueKeysInfo.size());
		
		for (UniqueKeyInfo uk : uniqueKeysInfo) {
			cloned.add(uk.cloneMe());
		}
		
		return cloned;
	}
}
