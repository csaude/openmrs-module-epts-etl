package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.Field;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

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
		if (!utilities.stringHasValue(tableConfiguration.getPrimaryKey())) {
			throw new ForbiddenOperationException("The primary key is not defined!");
		}
		
		List<UniqueKeyInfo> uniqueKeysInfo = new ArrayList<UniqueKeyInfo>();
		
		try {
			String tableName = tableConfiguration.getTableName();
			
			ResultSet rs = conn.getMetaData().getIndexInfo(null, null, tableName, true, true);
			
			String prevIndexName = null;
			
			List<Field> keyElements = null;
			
			String indexName = "";
			
			while (rs.next()) {
				indexName = rs.getString("INDEX_NAME");
				
				if (!indexName.equals(prevIndexName)) {
					addUniqueKey(prevIndexName, keyElements, uniqueKeysInfo, tableConfiguration);
					
					prevIndexName = indexName;
					keyElements = new ArrayList<>();
				}
				
				keyElements.add(new Field(rs.getString("COLUMN_NAME")));
			}
			
			addUniqueKey(prevIndexName, keyElements, uniqueKeysInfo, tableConfiguration);
			
			if (tableConfiguration.useSharedPKKey()) {
				SyncTableConfiguration parentTableInfo = new SyncTableConfiguration();
				
				parentTableInfo = new SyncTableConfiguration();
				parentTableInfo.setTableName(tableConfiguration.getSharePkWith());
				parentTableInfo.setRelatedSyncTableInfoSource(tableConfiguration.getRelatedSynconfiguration());
				
				List<UniqueKeyInfo> parentUniqueKeys = loadUniqueKeysInfo(parentTableInfo, conn);
				
				if (utilities.arrayHasElement(parentUniqueKeys)) {
					uniqueKeysInfo.addAll(parentUniqueKeys);
				}
			}
			
			return uniqueKeysInfo;
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
		
	}
	
	private static boolean addUniqueKey(String keyName, List<Field> keyElements, List<UniqueKeyInfo> uniqueKeys,
	        SyncTableConfiguration config) {
		
		if (keyElements == null || keyElements.isEmpty())
			return false;
		
		//Don't add PK as uniqueKey
		if (keyElements.size() == 1 && keyElements.get(0).getName().equals(config.getPrimaryKey())) {
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
		for (Field field: this.getFields()) {
			if (!other.getFields().contains(field)) return false;
		}
		
		for (Field field: other.getFields()) {
			if (!this.getFields().contains(field)) return false;
		}
		
		return true;
	}
	
	@Override
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
	
	public static void main(String[] args) throws IOException, SQLException {
		SyncConfiguration syncConfig = SyncConfiguration
		        .loadFromFile(new File("D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\mozart\\detect_problematic_dbs.json"));
		
		SyncTableConfiguration config = syncConfig.find(SyncTableConfiguration.init("dsd", syncConfig));
		config.fullLoad();
		
		Object[] params = { "e2b37662-1d5f-11e0-b929-000c29ad1d07" };
		
		OpenConnection conn = syncConfig.getMainApp().openConnection();
		
		List<UniqueKeyInfo> uks = loadUniqueKeysInfo(config, conn);
		
		List<UniqueKeyInfo> knownKeys = generateKnownUk(config);
		UniqueKeyInfo keyInfo = knownKeys.get(0);	
		
		List<UniqueKeyInfo> uniqueKeys = DBUtilities.getUniqueKeys(config.getTableName(), conn.getCatalog(), conn);

			
		for (UniqueKeyInfo uk : uniqueKeys) {
			if (keyInfo.hasSameFields(uk)) {
				System.out.println("The key exists");
			}
		}
			
		
		/*for (UniqueKeyInfo uk : uks) {
			uk.loadValuesToFields(dbObject);
			
			System.out.println(uk.toString());
		}*/
		
		conn.finalizeConnection();
	}
	
	public void addField(Field field) {
		if (this.fields == null)
			this.fields = new ArrayList<Field>();
		
		if (this.fields.contains(field))
			throw new ForbiddenOperationException("The field " + field.getName() + " Alredy exists on the key");
		
		this.fields.add(field);
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
	
	
	private static List<UniqueKeyInfo> generateKnownUk(SyncTableConfiguration configuredTable) {
		try {
			List<UniqueKeyInfo> knownKeys_ = new ArrayList<UniqueKeyInfo>();
			
			Extension knownKeys = configuredTable.findExtension("knownKeys");
			
			for (Extension keyInfo: knownKeys.getExtension()) {
				UniqueKeyInfo uk = new UniqueKeyInfo();
				
				for (Extension keyPart : keyInfo.getExtension()) {
					uk.addField(new Field(keyPart.getValueString()));
				}
				
				knownKeys_.add(uk);
			}
			
			return knownKeys_;
		}
		catch (ForbiddenOperationException e1) {
			return  null;
		}
	}
	
}
