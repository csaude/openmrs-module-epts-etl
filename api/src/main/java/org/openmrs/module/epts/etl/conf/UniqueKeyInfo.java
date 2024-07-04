package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author jpboane
 */
public class UniqueKeyInfo {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String keyName;
	
	private List<Key> fields;
	
	private boolean fieldValuesLoaded;
	
	private TableConfiguration tabConf;
	
	public UniqueKeyInfo() {
	}
	
	public UniqueKeyInfo(TableConfiguration tabConf) {
		this.tabConf = tabConf;
	}
	
	public static UniqueKeyInfo init(TableConfiguration tabConf, String keyName) {
		UniqueKeyInfo uk = new UniqueKeyInfo(tabConf);
		uk.keyName = keyName;
		
		return uk;
	}
	
	public boolean isFieldValuesLoaded() {
		return fieldValuesLoaded;
	}
	
	public void setFieldValuesLoaded(boolean fieldValuesLoaded) {
		this.fieldValuesLoaded = fieldValuesLoaded;
	}
	
	public boolean isCompositeKey() {
		return utilities.arrayHasMoreThanOneElements(this.fields);
	}
	
	/**
	 * Retrieves this unique key as simple key
	 * 
	 * @return the simple key for this unique key
	 * @throws ForbiddenOperationException if this unique key is composite
	 */
	public Key retrieveSimpleKey() throws ForbiddenOperationException {
		if (isCompositeKey())
			throw new ForbiddenOperationException("The key is composite, you cannot retrive the SimpleKey");
		
		return this.fields.get(0);
	}
	
	public String retrieveSimpleKeyColumnName() {
		return retrieveSimpleKey().getName();
	}
	
	public String retrieveSimpleKeyColumnNameAsClassAtt() {
		return retrieveSimpleKey().getNameAsClassAtt();
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
	
	public List<Key> getFields() {
		return fields;
	}
	
	public void setFields(List<Key> fields) {
		this.fields = fields;
	}
	
	public void loadValuesToFields(EtlDatabaseObject object) {
		for (Field field : this.fields) {
			Object value;
			
			try {
				try {
					value = object.getFieldValue(field.getName());
				}
				catch (ForbiddenOperationException e) {
					if (object.shasSharedPkObj()) {
						try {
							value = object.getSharedPkObj().getFieldValue(field.getName());
						}
						catch (ForbiddenOperationException e1) {
							value = object.getSharedPkObj()
							        .getFieldValue(AttDefinedElements.convertTableAttNameToClassAttName(field.getName()));
						}
					} else
						throw e;
				}
			}
			catch (ForbiddenOperationException e) {
				value = object.getFieldValue(AttDefinedElements.convertTableAttNameToClassAttName(field.getName()));
			}
			
			field.setValue(value);
		}
		
		this.fieldValuesLoaded = true;
	}
	
	public static List<UniqueKeyInfo> loadUniqueKeysInfo(TableConfiguration tabConf, Connection conn) throws DBException {
		
		List<UniqueKeyInfo> uniqueKeysInfo = new ArrayList<UniqueKeyInfo>();
		
		ResultSet rs = null;
		
		try {
			
			String tableName = DBUtilities.extractTableNameFromFullTableName(tabConf.getTableName());
			
			String catalog = conn.getCatalog();
			
			if (DBUtilities.isMySQLDB(conn)) {
				catalog = tabConf.getSchema();
			}
			
			rs = conn.getMetaData().getIndexInfo(catalog, tabConf.getSchema(), tableName, true, true);
			
			String prevIndexName = null;
			
			List<Key> keyElements = null;
			
			String indexName = "";
			
			while (rs.next()) {
				indexName = rs.getString("INDEX_NAME");
				
				if (!indexName.equals(prevIndexName)) {
					addUniqueKey(prevIndexName, keyElements, uniqueKeysInfo, tabConf, conn);
					
					prevIndexName = indexName;
					keyElements = new ArrayList<>();
				}
				
				if (!tabConf.isIgnorableField(Field.fastCreate(rs.getString("COLUMN_NAME")))) {
					Field f = tabConf.getField(rs.getString("COLUMN_NAME"));
					
					keyElements.add(Key.fastCreateTyped(f.getName(), f.getType()));
				}
			}
			
			addUniqueKey(prevIndexName, keyElements, uniqueKeysInfo, tabConf, conn);
			
			if (tabConf.useSharedPKKey()) {
				ParentTable p = tabConf.getSharedKeyRefInfo();
				
				if (!p.isFullLoaded()) {
					p.loadFields(conn);
					p.loadPrimaryKeyInfo(conn);
					p.loadUniqueKeys(conn);
				}
				
				if (p.hasUniqueKeys()) {
					uniqueKeysInfo.addAll(p.getUniqueKeys());
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
	
	private static boolean addUniqueKey(String keyName, List<Key> keyElements, List<UniqueKeyInfo> uniqueKeys,
	        TableConfiguration config, Connection conn) {
		
		if (keyElements == null || keyElements.isEmpty())
			return false;
		
		if (uniqueKeys == null)
			uniqueKeys = new ArrayList<>();
		
		UniqueKeyInfo uk = UniqueKeyInfo.init(config, keyName);
		uk.fields = keyElements;
		
		//Don't add PK as uniqueKey
		if (config.getPrimaryKey() != null && config.getPrimaryKey().equals(uk)) {
			return false;
		} else {
			
			if (!uk.contained(uniqueKeys)) {
				uniqueKeys.add(uk);
			}
			
			return true;
		}
	}
	
	private boolean contained(List<UniqueKeyInfo> uniqueKeys) {
		if (!utilities.arrayHasElement(uniqueKeys)) {
			return false;
		}
		
		for (UniqueKeyInfo uk : uniqueKeys) {
			if (this.equals(uk)) {
				return true;
			}
		}
		
		return false;
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
		
		if (this.hasSameFields(other)) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasSameValues(UniqueKeyInfo otherUniqueKey) {
		
		if (!this.hasSameFields(otherUniqueKey)) {
			throw new ForbiddenOperationException("The unique keys has diffents keys");
		}
		
		for (Key key : this.getFields()) {
			
			Key keyFromOtherUk = otherUniqueKey.getKey(key.getName());
			
			if (key.getValue() == null) {
				return false;
			}
			
			if (!key.getValue().equals(keyFromOtherUk.getValue())) {
				return false;
			}
		}
		
		return true;
		
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
	
	public boolean hasName() {
		return utilities.stringHasValue(getKeyName());
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		
		if (this.fields == null)
			return "";
		
		String toString = hasName() ? this.getKeyName() + " " : "";
		
		toString += "[";
		int i = 0;
		
		for (Field field : this.getFields()) {
			String fieldInfo = field.getName();
			
			fieldInfo += field.hasValue() ? ": " + field.getValue() : "";
			
			if (i > 0) {
				toString += ",";
			}
			
			toString += fieldInfo;
			i++;
		}
		
		toString += "]";
		
		return toString;
	}
	
	public void addKey(Key key) {
		if (this.fields == null)
			this.fields = new ArrayList<Key>();
		
		if (!this.fields.contains(key)) {
			this.fields.add(key);
		}
	}
	
	public static UniqueKeyInfo generateFromFieldList(AbstractTableConfiguration tabConf, List<String> fields) {
		if (!utilities.arrayHasElement(fields))
			throw new ForbiddenOperationException("The list cannot be empty");
		
		UniqueKeyInfo uk = new UniqueKeyInfo(tabConf);
		
		for (String fieldName : fields) {
			uk.addKey(new Key(fieldName));
		}
		
		return uk;
	}
	
	protected UniqueKeyInfo cloneMe() {
		UniqueKeyInfo uk = UniqueKeyInfo.init(this.tabConf, keyName);
		
		for (Field field : fields) {
			uk.addKey(Key.fastCreateTyped(field.getName(), field.getType()));
		}
		return uk;
	}
	
	public static List<UniqueKeyInfo> cloneAllAndLoadValues(List<UniqueKeyInfo> uniqueKeysInfo, EtlDatabaseObject obj) {
		//Since this is usually for an object, null the keyName to avoid "equals" 2 object because they have same keyName
		return cloneAllAndLoadValues(uniqueKeysInfo, obj, false);
		
	}
	
	public static List<UniqueKeyInfo> cloneAllWithKeyNameAndLoadValues(List<UniqueKeyInfo> uniqueKeysInfo,
	        EtlDatabaseObject obj) {
		return cloneAllAndLoadValues(uniqueKeysInfo, obj, true);
	}
	
	public static List<UniqueKeyInfo> cloneAllAndLoadValues(List<UniqueKeyInfo> uniqueKeysInfo, EtlDatabaseObject obj,
	        boolean keepUkName) {
		List<UniqueKeyInfo> uks = cloneAll_(uniqueKeysInfo);
		
		if (utilities.arrayHasElement(uks)) {
			
			for (UniqueKeyInfo uk : uks) {
				
				if (!keepUkName) {
					//Since this is usually for an object, null the keyName to avoid "equals" 2 object because they have same keyName
					uk.setKeyName(null);
				}
				
				uk.loadValuesToFields(obj);
			}
		}
		
		return uks;
	}
	
	public static List<UniqueKeyInfo> cloneAll_(List<UniqueKeyInfo> uniqueKeysInfo) {
		
		if (uniqueKeysInfo == null) {
			return null;
		}
		
		List<UniqueKeyInfo> cloned = new ArrayList<>(uniqueKeysInfo.size());
		
		for (UniqueKeyInfo uk : uniqueKeysInfo) {
			cloned.add(uk.cloneMe());
		}
		
		return cloned;
	}
	
	@JsonIgnore
	public String[] parseFieldNamesToArray() {
		String[] fields = new String[this.getFields().size()];
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			fields[i] = key.getName();
		}
		
		return fields;
	}
	
	@JsonIgnore
	public String[] parseFieldNamesToArray(String tableAlias) {
		String[] fields = new String[this.getFields().size()];
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			fields[i] = tableAlias + "." + key.getName();
		}
		
		return fields;
	}
	
	public String parseFieldNamesToCommaSeparatedString() {
		String fields = "";
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			if (utilities.stringHasValue(fields)) {
				fields += ", ";
			}
			
			fields += key.getName();
		}
		
		return fields;
	}
	
	public String generateInsertQuetionMark() {
		String fields = "";
		
		for (int i = 0; i < this.getFields().size(); i++) {
			if (utilities.stringHasValue(fields)) {
				fields += ", ";
			}
			
			fields += "?";
		}
		
		return fields;
	}
	
	public Object[] parseValuesToArray() {
		Object[] values = new Object[this.getFields().size()];
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			values[i] = key.getValue();
		}
		
		return values;
	}
	
	public String parseToFilledStringConditionWithAlias() {
		if (getTabConf() == null) {
			throw new ForbiddenOperationException("The tabConf is needed");
		}
		
		if (!getTabConf().hasAlias()) {
			throw new ForbiddenOperationException("The table " + getTabConf().getTableName() + " has no alias!");
		}
		
		return parseToFilledStringCondition(getTabConf().getAlias());
	}
	
	public String parseToFilledStringConditionWithoutAlias() {
		return parseToFilledStringCondition("");
	}
	
	public String parseToParametrizedStringConditionWithAlias() {
		if (getTabConf() == null) {
			throw new ForbiddenOperationException("The tabConf is needed");
		}
		
		if (!getTabConf().hasAlias()) {
			throw new ForbiddenOperationException("The table " + getTabConf().getTableName() + " has no alias!");
		}
		
		return parseToParametrizedStringCondition(getTabConf().getAlias());
	}
	
	public String parseToParametrizedStringConditionWithoutAlias() {
		return parseToParametrizedStringCondition("");
	}
	
	private String parseToFilledStringCondition(String alias) {
		
		String fields = "";
		
		String fullAlias;
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			if (utilities.stringHasValue(fields)) {
				fields += " AND ";
			}
			
			fullAlias = alias;
			
			if (utilities.stringHasValue(alias)) {
				fullAlias += ".";
			}
			
			fields += fullAlias + key.getName() + " = " + key.getValueAsSqlPart();
		}
		
		return fields;
	}
	
	private String parseToParametrizedStringCondition(String alias) {
		
		String fields = "";
		
		String fullAlias;
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			if (utilities.stringHasValue(fields)) {
				fields += " AND ";
			}
			
			fullAlias = alias;
			
			if (utilities.stringHasValue(alias)) {
				fullAlias += ".";
			}
			
			fields += fullAlias + key.getName() + " = ? ";
		}
		
		return fields;
	}
	
	public static String parseToFilledStringConditionToAllWithAlias(List<UniqueKeyInfo> uks) {
		return parseToFilledStringConditionToAll(uks, uks.get(0).getTabConf().getAlias());
	}
	
	public static String parseToFilledStringConditionToAllWithoutAlias(List<UniqueKeyInfo> uks) {
		return parseToFilledStringConditionToAll(uks, null);
	}
	
	private static String parseToFilledStringConditionToAll(List<UniqueKeyInfo> uks, String alias) {
		String fields = "";
		
		for (UniqueKeyInfo uk : uks) {
			if (utilities.stringHasValue(fields)) {
				fields += " AND ";
			}
			
			if (utilities.stringHasValue(alias)) {
				fields += uk.parseToFilledStringConditionWithAlias();
			} else {
				fields += uk.parseToFilledStringConditionWithoutAlias();
			}
		}
		
		return fields;
	}
	
	public static String parseToParametrizedStringConditionToAllWithAlias(List<UniqueKeyInfo> uks) {
		return parseToParametrizedStringConditionToAll(uks, uks.get(0).getTabConf().getAlias());
	}
	
	public static String parseToParametrizedStringConditionToAllWithoutAlias(List<UniqueKeyInfo> uks) {
		return parseToParametrizedStringConditionToAll(uks, null);
	}
	
	private static String parseToParametrizedStringConditionToAll(List<UniqueKeyInfo> uks, String alias) {
		String fields = "";
		
		for (UniqueKeyInfo uk : uks) {
			if (utilities.stringHasValue(fields)) {
				fields += " AND ";
			}
			
			if (utilities.stringHasValue(alias)) {
				fields += uk.parseToParametrizedStringConditionWithAlias();
			} else {
				fields += uk.parseToParametrizedStringConditionWithoutAlias();
			}
		}
		
		return fields;
	}
	
	public TableConfiguration getTabConf() {
		return tabConf;
	}
	
	public void setTabConf(TableConfiguration tabConf) {
		this.tabConf = tabConf;
	}
	
	public String generateSqlNullCheckWithDisjunction() {
		
		if (tabConf == null)
			throw new ForbiddenOperationException("The tabConf is not set");
		
		String fields = "";
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			if (utilities.stringHasValue(fields)) {
				fields += " OR ";
			}
			
			fields += this.tabConf.getTableAlias() + "." + key.getName() + " is null ";
		}
		
		return fields;
	}
	
	public String generateSqlNotNullCheckWithDisjunction() {
		
		if (tabConf == null)
			throw new ForbiddenOperationException("The tabConf is not set");
		
		String fields = "";
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			if (utilities.stringHasValue(fields)) {
				fields += " OR ";
			}
			
			fields += this.tabConf.getTableAlias() + "." + key.getName() + " is not null ";
		}
		
		return fields;
	}
	
	public String generateSqlNotNullCheckWithIntersetion() {
		String fields = "";
		
		for (int i = 0; i < this.getFields().size(); i++) {
			Key key = this.getFields().get(i);
			
			if (utilities.stringHasValue(fields)) {
				fields += " AND ";
			}
			
			fields += this.tabConf.getTableAlias() + "." + key.getName() + " is not null ";
		}
		
		return fields;
	}
	
	public Key getKey(String name) {
		if (!utilities.arrayHasElement(this.fields)) {
			return null;
		}
		
		for (Key key : this.fields) {
			if (key.getName().equals(name)) {
				return key;
			}
		}
		
		return null;
	}
	
	public boolean containsKey(Key key) {
		return getKey(key.getName()) != null;
	}
	
	public boolean hasFields() {
		return utilities.arrayHasElement(this.fields);
	}
	
	public boolean hasNullFields() {
		if (!isFieldValuesLoaded())
			throw new ForbiddenOperationException(
			        "The values for the fields was not loaded. Please call loadValuesToFields(EtlDatabaseObject object) before you try to call this method");
		
		for (Key k : this.fields) {
			if (k.getValue() == null) {
				return true;
			}
		}
		
		return false;
	}
}
