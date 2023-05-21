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
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.model.pojo.mozart.LocationVO;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * Define the refencial information betwen a {@link SyncTableConfiguration} and its main parent;
 * 
 * @author jpboane
 *
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
	
	public List<String> generateListFromFieldsNames(){
		List<String> fieldsAsName = new ArrayList<String>();
		
		for (Field field: this.fields) {
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

	public void loadValuesToFields(DatabaseObject object){
		for (Field field : this.fields) {
			String attClasse = AttDefinedElements.convertTableAttNameToClassAttName(field.getName());
			
			Object value = object.getFieldValues(attClasse)[0];
		
			field.setValue(value);
		}
	}
	
	public static List<UniqueKeyInfo> loadUniqueKeysInfo(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
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
			
			addUniqueKey(indexName, keyElements, uniqueKeysInfo, tableConfiguration);
			
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
	
	private static boolean addUniqueKey(String keyName, List<Field> keyElements, List<UniqueKeyInfo> uniqueKeys, SyncTableConfiguration config) {
		
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
		if (obj == null) return false;
		if (!(obj instanceof UniqueKeyInfo)) return false;
		
		UniqueKeyInfo other = (UniqueKeyInfo)obj;
		
		return this.keyName.equals(other.keyName);
	}
	
	
	@Override
	public String toString() {
		String toString = keyName + "[";
		int i=0;
		
		for (Field field : this.fields) {
			String fieldInfo = field.getName() + ": " + field.getValue();
			
			if (i>0) {
				toString += ",";
			}
			
			toString += fieldInfo;
			i++;
		}
		
		toString += "]";
		
		return toString;
	}
	
	public static void main(String[] args) throws DBException, IOException {
		SyncConfiguration syncConfig = SyncConfiguration.loadFromFile(new File("D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\mozart\\detect_problematic_dbs.json"));
		
		SyncTableConfiguration config = syncConfig.find(SyncTableConfiguration.init("location", syncConfig));
		config.fullLoad();
		
		Object[] params = {"e2b37662-1d5f-11e0-b929-000c29ad1d07"};
		
		OpenConnection conn = syncConfig.getMainApp().openConnection();
		
		LocationVO dbObject = DatabaseObjectDAO.find(LocationVO.class, "select * from location where location_uuid = ? ", params, conn);
		
		
		List<UniqueKeyInfo> uks = loadUniqueKeysInfo(config, conn);
		
		for (UniqueKeyInfo uk : uks) {
			uk.loadValuesToFields(dbObject);
			
			System.out.println(uk.toString());
		}
		
		conn.finalizeConnection();
	}
}
