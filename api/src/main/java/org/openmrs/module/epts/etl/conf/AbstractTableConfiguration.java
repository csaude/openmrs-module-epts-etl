package org.openmrs.module.epts.etl.conf;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.DuplicateMappingException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractTableConfiguration extends EtlDataConfiguration implements Comparable<AbstractTableConfiguration>, DatabaseObjectConfiguration {
	
	private String tableName;
	
	private String tableAlias;
	
	private List<ParentTable> parents;
	
	private List<ParentTable> parentRefInfo;
	
	private List<ChildTable> childRefInfo;
	
	private Class<? extends DatabaseObject> syncRecordClass;
	
	private EtlDataConfiguration parentConf;
	
	private PrimaryKey primaryKey;
	
	private String sharePkWith;
	
	private boolean metadata;
	
	protected boolean fullLoaded;
	
	private boolean removeForbidden;
	
	/**
	 * List the field to observe when sync by date (ex: date_created, date_update, etc)
	 */
	private List<String> observationDateFields;
	
	private List<UniqueKeyInfo> uniqueKeys;
	
	private List<Field> fields;
	
	/**
	 * When merge existing records, the incoming record will win if the listed fields have the
	 * specified values. Note that, for the outer list the join condition will be "OR" and for the
	 * inner list the join condition will be "AND"
	 */
	private List<List<Field>> winningRecordFieldsInfo;
	
	private boolean autoIncrementId;
	
	private boolean disabled;
	
	private boolean mustLoadChildrenInfo;
	
	private String extraConditionForExtract;
	
	private String insertSQLWithObjectId;
	
	private String insertSQLWithoutObjectId;
	
	private String updateSQL;
	
	private DatabaseObjectLoaderHelper loadHealper;
	
	public AbstractTableConfiguration() {
		this.loadHealper = new DatabaseObjectLoaderHelper(this);
	}
	
	public AbstractTableConfiguration(String tableName) {
		this();
		
		this.tableName = tableName;
	}
	
	public void clone(AbstractTableConfiguration toCloneFrom) {
		this.tableName = toCloneFrom.tableName;
		this.tableAlias = toCloneFrom.tableAlias;
		this.parents = toCloneFrom.parents;
		this.childRefInfo = toCloneFrom.childRefInfo;
		this.parentRefInfo = toCloneFrom.parentRefInfo;
		this.syncRecordClass = toCloneFrom.syncRecordClass;
		this.parentConf = toCloneFrom.parentConf;
		this.primaryKey = toCloneFrom.primaryKey;
		this.sharePkWith = toCloneFrom.sharePkWith;
		this.metadata = toCloneFrom.metadata;
		this.fullLoaded = toCloneFrom.fullLoaded;
		this.removeForbidden = toCloneFrom.removeForbidden;
		this.observationDateFields = toCloneFrom.observationDateFields;
		this.uniqueKeys = toCloneFrom.uniqueKeys;
		this.fields = toCloneFrom.fields;
		this.winningRecordFieldsInfo = toCloneFrom.winningRecordFieldsInfo;
		this.fullLoaded = toCloneFrom.fullLoaded;
		this.extraConditionForExtract = toCloneFrom.extraConditionForExtract;
		this.insertSQLWithObjectId = toCloneFrom.insertSQLWithObjectId;
		this.insertSQLWithoutObjectId = toCloneFrom.insertSQLWithoutObjectId;
		this.updateSQL = toCloneFrom.updateSQL;
	}
	
	public String getTableAlias() {
		return tableAlias;
	}
	
	@Override
	public String getAlias() {
		return getTableAlias();
	}
	
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	
	@Override
	public DatabaseObjectLoaderHelper getLoadHealper() {
		return this.loadHealper;
	}
	
	public void setLoadHealper(DatabaseObjectLoaderHelper loadHealper) {
		this.loadHealper = loadHealper;
	}
	
	public String getInsertSQLWithObjectId() {
		return insertSQLWithObjectId;
	}
	
	public String getInsertSQLWithoutObjectId() {
		return insertSQLWithoutObjectId;
	}
	
	public String getUpdateSQL() {
		return updateSQL;
	}
	
	public String getExtraConditionForExtract() {
		return extraConditionForExtract;
	}
	
	public void setExtraConditionForExtract(String extraConditionForExtract) {
		this.extraConditionForExtract = extraConditionForExtract;
	}
	
	public boolean isMustLoadChildrenInfo() {
		return mustLoadChildrenInfo;
	}
	
	public void setMustLoadChildrenInfo(boolean mustLoadChildrenInfo) {
		this.mustLoadChildrenInfo = mustLoadChildrenInfo;
	}
	
	public boolean isAutoIncrementId() {
		return autoIncrementId;
	}
	
	public void setAutoIncrementId(boolean autoIncrementId) {
		this.autoIncrementId = autoIncrementId;
	}
	
	public List<List<Field>> getWinningRecordFieldsInfo() {
		return winningRecordFieldsInfo;
	}
	
	public void setWinningRecordFieldsInfo(List<List<Field>> winningRecordFieldsInfo) {
		this.winningRecordFieldsInfo = winningRecordFieldsInfo;
	}
	
	public boolean hasAlias() {
		return utilities.stringHasValue(this.tableAlias);
	}
	
	public boolean hasWinningRecordsInfo() {
		return this.winningRecordFieldsInfo != null;
	}
	
	@Override
	public List<ParentTable> getParentRefInfo() {
		return parentRefInfo;
	}
	
	public void setParentRefInfo(List<ParentTable> parentRefInfo) {
		this.parentRefInfo = parentRefInfo;
	}
	
	public void setChildRefInfo(List<ChildTable> childRefInfo) {
		this.childRefInfo = childRefInfo;
	}
	
	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	
	public List<UniqueKeyInfo> getUniqueKeys() {
		return uniqueKeys;
	}
	
	public void setUniqueKeys(List<UniqueKeyInfo> uniqueKeys) {
		this.uniqueKeys = uniqueKeys;
	}
	
	public List<String> getObservationDateFields() {
		return observationDateFields;
	}
	
	public void setObservationDateFields(List<String> observationDateFields) {
		this.observationDateFields = observationDateFields;
	}
	
	public boolean isRemoveForbidden() {
		return removeForbidden;
	}
	
	public void setRemoveForbidden(boolean removeForbidden) {
		this.removeForbidden = removeForbidden;
	}
	
	@Override
	public List<ChildTable> getChildRefInfo() {
		if (!this.mustLoadChildrenInfo) {
			throw new ForbiddenOperationException(
			        "The table configuration is set to not load Children. Please change configuration if you what to access Children ifo.");
		}
		
		return this.childRefInfo;
	}
	
	public AppInfo getMainApp() {
		return getRelatedSyncConfiguration().getMainApp();
	}
	
	@JsonIgnore
	public String getClasspackage(AppInfo application) {
		return application.getPojoPackageName();
	}
	
	public boolean isDoIntegrityCheckInTheEnd(EtlOperationType operationType) {
		return getRelatedSyncConfiguration().isDoIntegrityCheckInTheEnd(operationType);
	}
	
	@JsonIgnore
	public String getId() {
		return this.getRelatedSyncConfiguration().getDesignation() + "_" + this.tableName;
	}
	
	@JsonIgnore
	public String getParentsAsString() {
		String sourceFoldersAsString = "";
		
		if (utilities.arrayHasElement(this.getParents())) {
			for (int i = 0; i < this.getParents().size() - 1; i++) {
				sourceFoldersAsString += this.getParents().get(i).getTableName() + ",";
			}
			
			sourceFoldersAsString += this.getParents().get(this.getParents().size() - 1).getTableName();
		}
		
		return sourceFoldersAsString;
	}
	
	public List<ParentTable> getParents() {
		return parents;
	}
	
	public void setParents(List<ParentTable> parents) {
		this.parents = parents;
	}
	
	public String getSharePkWith() {
		return sharePkWith;
	}
	
	public void setSharePkWith(String sharePkWith) {
		this.sharePkWith = sharePkWith;
	}
	
	@Override
	public EtlDataConfiguration getParentConf() {
		return parentConf;
	}
	
	public void setParentConf(EtlDataConfiguration parentConf) {
		this.parentConf = parentConf;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@Override
	@JsonIgnore
	public String getObjectName() {
		return getTableName();
	}
	
	@JsonIgnore
	public boolean useSharedPKKey() {
		return utilities.stringHasValue(this.sharePkWith);
	}
	
	@JsonIgnore
	@Override
	public PrimaryKey getPrimaryKey() {
		OpenConnection conn = getRelatedAppInfo().openConnection();
		
		try {
			return getPrimaryKey(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@JsonIgnore
	public PrimaryKey getPrimaryKey(Connection conn) {
		if (this.primaryKey == null) {
			try {
				String tableName = DBUtilities.extractTableNameFromFullTableName(this.tableName);
				
				String schema = DBUtilities.determineSchemaFromFullTableName(this.tableName);
				
				schema = utilities.stringHasValue(schema) ? schema : conn.getSchema();
				
				String catalog = conn.getCatalog();
				
				if (DBUtilities.isMySQLDB(conn) && utilities.stringHasValue(schema)) {
					catalog = schema;
				}
				
				ResultSet rs = conn.getMetaData().getPrimaryKeys(catalog, schema, tableName);
				
				while (rs.next()) {
					
					if (this.primaryKey == null) {
						this.primaryKey = new PrimaryKey(this);
					}
					
					Key pk = new Key();
					pk.setName(rs.getString("COLUMN_NAME"));
					pk.setType(DBUtilities.determineColunType(this.tableName, pk.getName(), conn));
					pk.setType(AttDefinedElements.convertDatabaseTypeTOJavaType(pk.getName(), pk.getType()));
					
					this.primaryKey.addKey(pk);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			
		}
		
		return this.primaryKey;
	}
	
	public synchronized DatabaseObject generateAndSaveDefaultObject(Connection conn) throws DBException {
		try {
			DatabaseObject defaultObject = getDefaultObject(conn);
			
			if (defaultObject != null) {
				return defaultObject;
			} else {
				defaultObject = getSyncRecordClass().newInstance();
				defaultObject.setRelatedConfiguration(this);
				
				defaultObject.loadWithDefaultValues();
				
				for (ParentTable p : this.parentRefInfo) {
					
					DatabaseObject defaultParent = p.getDefaultObject(conn);
					
					if (defaultParent == null) {
						defaultParent = getSyncRecordClass().newInstance();
						defaultParent.setRelatedConfiguration(p);
						
						if (defaultParent.checkIfAllRelationshipCanBeresolved(this, conn)) {
							defaultParent = p.generateAndSaveDefaultObject(conn);
						} else {
							throw new ForbiddenOperationException("There are recursive relationship between "
							        + this.tableName + " and " + p.getTableName()
							        + " which cannot automatically resolved...! Please manual create default record for one of thise table using id '-1'");
						}
						
						defaultObject.changeParentValue(p, defaultParent);
					}
					
				}
				
				defaultObject.save(this, conn);
				
				defaultObject.loadObjectIdData(this);
				
				EtlConfigurationTableConf defaultGeneratedObjectKeyTabConf = getRelatedSyncConfiguration()
				        .getDefaultGeneratedObjectKeyTabConf();
				
				for (Key key : defaultObject.getObjectId().getFields()) {
					DatabaseObject keyInfo = defaultGeneratedObjectKeyTabConf.getSyncRecordClass().newInstance();
					
					keyInfo.setFieldValue("tableName", defaultGeneratedObjectKeyTabConf.getTableName());
					keyInfo.setFieldValue("column_name", key.getName());
					keyInfo.setFieldValue("key_value", key.getValue());
					
					keyInfo.save(defaultGeneratedObjectKeyTabConf, conn);
				}
				
				return defaultObject;
			}
		}
		catch (InstantiationException | IllegalAccessException | ForbiddenOperationException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public DatabaseObject getDefaultObject(Connection conn) throws DBException, ForbiddenOperationException {
		return DatabaseObjectDAO.getDefaultRecord(this, conn);
	}
	
	@JsonIgnore
	public void loadUniqueKeys() {
		OpenConnection conn = getRelatedSyncConfiguration().getMainApp().openConnection();
		
		try {
			loadUniqueKeys(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@JsonIgnore
	public void loadUniqueKeys(Connection conn) {
		if (this.uniqueKeys == null) {
			loadUniqueKeys(this, conn);
		}
	}
	
	@JsonIgnore
	private void loadUniqueKeys(AbstractTableConfiguration tableConfiguration, Connection conn) {
		if (tableConfiguration.uniqueKeys == null) {
			try {
				this.uniqueKeys = UniqueKeyInfo.loadUniqueKeysInfo(this, conn);
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
		}
	}
	
	@JsonIgnore
	public boolean hasPK() {
		return getPrimaryKey() != null;
	}
	
	@JsonIgnore
	public boolean hasPK(Connection conn) {
		return getPrimaryKey(conn) != null;
	}
	
	public boolean checkIfisIgnorableParentByClassAttName(String parentAttName, Connection conn) {
		for (ParentTable parent : this.parentRefInfo) {
			RefMapping map = parent.getRefMappingByChildClassAttName(parentAttName);
			
			return map.isIgnorable();
		}
		
		throw new ForbiddenOperationException("The att '" + parentAttName + "' doesn't represent any defined parent att");
	}
	
	private int countChildren(Connection conn) throws SQLException {
		String tableName = DBUtilities.extractTableNameFromFullTableName(this.tableName);
		
		String schema = DBUtilities.determineSchemaFromFullTableName(this.tableName);
		
		schema = utilities.stringHasValue(schema) ? schema : conn.getSchema();
		
		String catalog = conn.getCatalog();
		
		if (DBUtilities.isMySQLDB(conn) && utilities.stringHasValue(schema)) {
			catalog = schema;
		}
		
		ResultSet foreignKeyRS = conn.getMetaData().getExportedKeys(catalog, schema, tableName);
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				foreignKeyRS.last();
			} else {
				while (foreignKeyRS.next()) {}
				;
			}
			
			return foreignKeyRS.getRow();
		}
		finally {
			foreignKeyRS.close();
		}
		
	}
	
	public void logInfo(String msg) {
		getRelatedSyncConfiguration().logInfo(msg);
	}
	
	public void logDebug(String msg) {
		getRelatedSyncConfiguration().logDebug(msg);
	}
	
	public void logWarn(String msg) {
		getRelatedSyncConfiguration().logWarn(msg);
	}
	
	public void logErr(String msg) {
		getRelatedSyncConfiguration().logErr(msg);
	}
	
	private int countParents(Connection conn) throws SQLException {
		String tableName = DBUtilities.extractTableNameFromFullTableName(this.tableName);
		
		String schema = DBUtilities.determineSchemaFromFullTableName(this.tableName);
		
		schema = utilities.stringHasValue(schema) ? schema : conn.getSchema();
		
		String catalog = conn.getCatalog();
		
		if (DBUtilities.isMySQLDB(conn) && utilities.stringHasValue(schema)) {
			catalog = schema;
		}
		
		ResultSet foreignKeyRS = conn.getMetaData().getImportedKeys(catalog, schema, tableName);
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				foreignKeyRS.last();
			} else {
				while (foreignKeyRS.next()) {}
				;
			}
			
			return foreignKeyRS.getRow();
		}
		finally {
			foreignKeyRS.close();
		}
		
	}
	
	protected synchronized void loadParents(Connection conn) throws SQLException {
		logDebug("LOADING PARENTS FOR TABLE '" + getTableName() + "'");
		
		ResultSet foreignKeyRS = null;
		
		int count = countParents(conn);
		
		//First load all necessary info on configured parents
		
		if (utilities.arrayHasElement(this.parents)) {
			for (ParentTable p : this.parents) {
				
				p.setChildTableConf(this);
				
				if (p.getMapping() != null) {
					
					for (RefMapping map : p.getMapping()) {
						map.setParentTabConf(p);
						
						Field field = utilities.findOnArray(this.fields, new Field(map.getChildFieldName()));
						map.getChildField().setType(field.getType());
					}
				}
			}
		}
		
		if (count == 0) {
			logDebug("NO PARENT FOUND FOR TABLE '" + getTableName() + "'");
		} else
			try {
				logDebug("DISCOVERED '" + count + "' PARENTS FOR TABLE '" + getTableName() + "'");
				
				String tableName = DBUtilities.extractTableNameFromFullTableName(this.tableName);
				
				String schema = DBUtilities.determineSchemaFromFullTableName(this.tableName);
				
				schema = utilities.stringHasValue(schema) ? schema : conn.getSchema();
				
				String catalog = conn.getCatalog();
				
				if (DBUtilities.isMySQLDB(conn) && utilities.stringHasValue(schema)) {
					catalog = schema;
				}
				
				foreignKeyRS = conn.getMetaData().getImportedKeys(catalog, schema, tableName);
				
				while (foreignKeyRS.next()) {
					
					logDebug("CONFIGURING PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '"
					        + getTableName() + "'");
					
					String refCode = foreignKeyRS.getString("FK_NAME");
					
					String childFieldName = foreignKeyRS.getString("FKCOLUMN_NAME");
					
					String parentFieldName = foreignKeyRS.getString("PKCOLUMN_NAME");
					String parentTableName = foreignKeyRS.getString("PKTABLE_NAME");
					
					ParentTable parentTabConf = ParentTable.init(parentTableName, refCode);
					
					parentTabConf.setParentConf(this.parentConf);
					parentTabConf.setChildTableConf(this);
					
					addParentMappingInfo(refCode, childFieldName, parentTabConf, parentFieldName, conn);
					
					logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName()
					        + "' CONFIGURED");
				}
				
				//Copy additional configured Info
				if (this.parentRefInfo != null && this.parents != null) {
					
					for (ParentTable autoLoadedRefInfo : this.parentRefInfo) {
						
						for (ParentTable manualConfiguredRefInfo : this.parents) {
							
							ParentTable mixedConfiguredRef = manualConfiguredRefInfo;
							
							if (autoLoadedRefInfo.getTableName().equals(manualConfiguredRefInfo.getTableName())) {
								
								if (!manualConfiguredRefInfo.hasMapping()) {
									if (autoLoadedRefInfo.isCompositeMapping()) {
										throw new ForbiddenOperationException(
										        "You must manual configure the ref info for parent "
										                + manualConfiguredRefInfo.getTableName() + " on table "
										                + this.getTableName()
										                + ". Optionaly you can remove the manual parent specification");
									}
									
									//create default refInfo to force the copy of shared ref info
									
									mixedConfiguredRef = ParentTable.init(manualConfiguredRefInfo.getTableName(), "");
									
									mixedConfiguredRef.setConditionalFields(manualConfiguredRefInfo.getConditionalFields());
									
									mixedConfiguredRef.setChildTableConf(this);
									
									mixedConfiguredRef.setMapping(autoLoadedRefInfo.cloneAllMapping());
									
									mixedConfiguredRef.getSimpleRefMapping().setDefaultValueDueInconsistency(
									    manualConfiguredRefInfo.getDefaultValueDueInconsistency());
									mixedConfiguredRef.getSimpleRefMapping()
									        .setSetNullDueInconsistency(manualConfiguredRefInfo.isSetNullDueInconsistency());
								}
								
								if (autoLoadedRefInfo.equals(mixedConfiguredRef)) {
									autoLoadedRefInfo.setConditionalFields(mixedConfiguredRef.getConditionalFields());
									
									for (RefMapping map : autoLoadedRefInfo.getMapping()) {
										RefMapping configuredMap = mixedConfiguredRef.findRefMapping(map.getChildFieldName(),
										    map.getParentFieldName());
										
										if (configuredMap == null) {
											throw new ForbiddenOperationException("The mapping [" + map.getChildFieldName()
											        + " : " + map.getParentFieldName()
											        + "] was not found on configured mapping!");
										}
										
										map.setIgnorable(
										    map.isIgnorable() ? configuredMap.isIgnorable() : map.isIgnorable());
										map.setDefaultValueDueInconsistency(configuredMap.getDefaultValueDueInconsistency());
										map.setSetNullDueInconsistency(configuredMap.isSetNullDueInconsistency());
									}
								}
							}
						}
					}
				}
				//Check if there is a configured parent but not defined on the db schema
				
				if (utilities.arrayHasElement(this.parents)) {
					
					for (ParentTable configuredParent : this.parents) {
						
						if (configuredParent.hasMapping()) {
							if (!this.parentRefInfo.contains(configuredParent)) {
								this.parentRefInfo.add(configuredParent);
							}
						}
					}
				}
				
				logDebug("LOADED PARENTS FOR TABLE '" + getTableName() + "'");
			}
			finally
			
			{
				if (foreignKeyRS != null) {
					foreignKeyRS.close();
				}
			}
		
	}
	
	protected synchronized void loadChildren(Connection conn) throws SQLException {
		if (!this.mustLoadChildrenInfo)
			return;
		
		logDebug("LOADING CHILDREN FOR TABLE '" + getTableName() + "'");
		
		this.childRefInfo = new ArrayList<ChildTable>();
		
		int count = countChildren(conn);
		
		if (count == 0) {
			logDebug("NO CHILDREN FOUND FOR TABLE '" + getTableName() + "'");
		} else {
			ResultSet foreignKeyRS = null;
			
			try {
				logDebug("DISCOVERED '" + count + "' CHILDREN FOR TABLE '" + getTableName() + "'");
				
				String tableName = DBUtilities.extractTableNameFromFullTableName(this.tableName);
				
				String schema = DBUtilities.determineSchemaFromFullTableName(this.tableName);
				
				schema = utilities.stringHasValue(schema) ? schema : conn.getSchema();
				
				String catalog = conn.getCatalog();
				
				if (DBUtilities.isMySQLDB(conn) && utilities.stringHasValue(schema)) {
					catalog = schema;
				}
				
				foreignKeyRS = conn.getMetaData().getExportedKeys(catalog, schema, tableName);
				
				int i = 0;
				
				while (foreignKeyRS.next()) {
					logDebug("CONFIGURING CHILD " + ++i + " [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '"
					        + getTableName() + "'");
					
					String refCode = foreignKeyRS.getString("FK_NAME");
					
					String childTableName = foreignKeyRS.getString("FKTABLE_NAME");
					String childFieldName = foreignKeyRS.getString("FKCOLUMN_NAME");
					
					String parentFieldName = foreignKeyRS.getString("PKCOLUMN_NAME");
					
					ChildTable childTabConf = ChildTable.init(childTableName, refCode);
					
					childTabConf.setParentTableConf(this);
					
					childTabConf.setParentConf(this.parentConf);
					
					addChildMappingInfo(refCode, childTabConf, childFieldName, parentFieldName, conn);
					
					logDebug("CHILDREN " + i + " [" + foreignKeyRS.getString("FKTABLE_NAME") + "] FOR TABLE '"
					        + getTableName() + "' CONFIGURED");
				}
				
				logDebug("LOADED CHILDREN FOR TABLE '" + getTableName() + "'");
			}
			finally {
				if (foreignKeyRS != null) {
					foreignKeyRS.close();
				}
			}
		}
	}
	
	private void addChildMappingInfo(String refCode, ChildTable childTabConf, String childFieldName, String parentFieldName,
	        Connection conn) throws DBException {
		
		AbstractTableConfiguration parentTabConf = this;
		
		initRefInfo(RefType.EXPORTED, refCode, childTabConf, childFieldName, parentTabConf, parentFieldName, conn);
	}
	
	private void addParentMappingInfo(String refCode, String childFieldName, ParentTable parentTabConf,
	        String parentFieldName, Connection conn) throws DBException {
		
		AbstractTableConfiguration childTabConf = this;
		
		initRefInfo(RefType.IMPORTED, refCode, childTabConf, childFieldName, parentTabConf, parentFieldName, conn);
	}
	
	private void initRefInfo(RefType refType, String refCode, AbstractTableConfiguration childTabConf, String childFieldname,
	        AbstractTableConfiguration parentTabConf, String parentFieldName, Connection conn) throws DBException {
		
		String fieldName = null;
		
		if (refType.isImported()) {
			fieldName = childFieldname;
		} else {
			fieldName = parentFieldName;
		}
		
		Field field = utilities.findOnArray(this.fields, new Field(fieldName));
		
		if (field == null) {
			throw new ForbiddenOperationException(
			        "The field '" + fieldName + "' was not found on '" + this.tableName + "' fields!!!");
		}
		
		boolean ignorable = DBUtilities.isTableColumnAllowNull(this.tableName, fieldName, conn);
		
		RefMapping map = RefMapping.fastCreate(childFieldname, parentFieldName);
		
		map.getChildField().setType(field.getType());
		map.getParentField().setType(field.getType());
		map.setIgnorable(ignorable);
		
		if (this.parentRefInfo == null) {
			this.parentRefInfo = new ArrayList<>();
		}
		
		if (this.childRefInfo == null) {
			this.childRefInfo = new ArrayList<>();
		}
		
		RelatedTable ref = null;
		RelatedTable existingRef = null;
		
		if (refType.isImported()) {
			existingRef = utilities.findOnList(this.parentRefInfo, refCode);
			
			if (existingRef == null) {
				ref = (RelatedTable) parentTabConf;
				
				ref.setMetadata(!ref.isConfigured());
				this.parentRefInfo.add((ParentTable) ref);
			}
			
		} else {
			existingRef = utilities.findOnList(this.childRefInfo, refCode);
			
			if (existingRef == null) {
				ref = (RelatedTable) childTabConf;
				
				ref.setRelatedTabConf(parentTabConf);
				ref.setMetadata(!ref.isConfigured());
				
				this.childRefInfo.add((ChildTable) ref);
			}
			
		}
		
		try {
			ref.addMapping(map);
		}
		catch (DuplicateMappingException e) {}
		
	}
	
	@JsonIgnore
	public Class<? extends DatabaseObject> getSyncRecordClass() throws ForbiddenOperationException {
		return this.getSyncRecordClass(getRelatedAppInfo());
	}
	
	@JsonIgnore
	public Class<? extends DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		
		if (syncRecordClass == null)
			this.syncRecordClass = GenericDatabaseObject.class;
		
		return syncRecordClass;
	}
	
	private static String[] REMOVABLE_METADATA = {};
	
	/**
	 * By default an metadata cannot be removed, but there are situations where is needed to remove
	 * a metadata
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isRemovableMetadata() {
		return utilities.existOnArray(utilities.parseArrayToList(AbstractTableConfiguration.REMOVABLE_METADATA),
		    this.tableName);
	}
	
	@JsonIgnore
	public boolean existsSyncRecordClass(AppInfo application) {
		try {
			return getSyncRecordClass(application) != null;
		}
		catch (ForbiddenOperationException e) {
			
			return false;
		}
	}
	
	public void setSyncRecordClass(Class<DatabaseObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}
	
	@JsonIgnore
	public String generateFullClassName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return utilities.concatStringsWithSeparator(fullPackageName, generateClassName(), ".");
	}
	
	@JsonIgnore
	public String generateFullPackageName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return fullPackageName;
	}
	
	@JsonIgnore
	public String getOriginAppLocationCode() {
		return getRelatedSyncConfiguration().getOriginAppLocationCode();
	}
	
	public void generateRecordClass(AppInfo application, boolean fullClass) {
		try {
			if (fullClass) {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generate(this, application);
			} else {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, application);
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public void generateSkeletonRecordClass(AppInfo application) {
		try {
			this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, application);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@JsonIgnore
	public String generateClassName() {
		return generateClassName(this.tableName);
	}
	
	private String generateClassName(String tableName) {
		String[] nameParts = tableName.split("\\.");
		
		//Ignore the schema
		if (nameParts.length > 1) {
			tableName = nameParts[1];
		}
		
		nameParts = tableName.split("_");
		
		String className = utilities.capitalize(nameParts[0]);
		
		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}
		
		return className + "VO";
	}
	
	public boolean isMetadata() {
		return metadata;
	}
	
	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}
	
	@JsonIgnore
	public String generateRelatedStageTableName() {
		return this.getTableName() + "_stage";
	}
	
	@JsonIgnore
	public String generateRelatedStageUniqueKeysTableName() {
		return generateRelatedStageTableName() + "_unique_keys";
	}
	
	@JsonIgnore
	public String getSyncStageSchema() {
		return getRelatedSyncConfiguration().getSyncStageSchema();
	}
	
	@JsonIgnore
	public String generateFullStageTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageTableName();
	}
	
	public String generateFullTableName(Connection conn) throws DBException {
		return DBUtilities.tryToPutSchemaOnDatabaseObject(getTableName(), conn);
	}
	
	public String generateFullTableNameWithAlias(Connection conn) throws DBException, ForbiddenOperationException {
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.tableName);
		}
		
		return generateFullTableName(conn) + " " + this.tableAlias;
	}
	
	public String generateFullTableNameWithAlias(String schema) {
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.tableName);
		}
		
		return utilities.stringHasValue(schema) ? schema + "." + generateTableNameWithAlias() : generateTableNameWithAlias();
	}
	
	public String generateTableNameWithAlias() {
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.tableName);
		}
		
		return this.tableName + " " + this.tableAlias;
	}
	
	@JsonIgnore
	public String generateFullStageUniqueKeysTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageUniqueKeysTableName();
	}
	
	public boolean existRelatedExportStageTable(Connection conn) {
		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = generateRelatedStageTableName();
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public boolean existRelatedExportStageUniqueKeysTable(Connection conn) {
		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = generateRelatedStageUniqueKeysTableName();
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@JsonIgnore
	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@JsonIgnore
	public boolean isConfigured() {
		for (AbstractTableConfiguration tabConf : getRelatedSyncConfiguration().getConfiguredTables()) {
			if (tabConf.equals(this))
				return true;
		}
		
		return false;
	}
	
	public synchronized void fullLoad(Connection conn) {
		try {
			
			if (this.fullLoaded) {
				return;
			}
			
			boolean exists = DBUtilities.isTableExists(conn.getSchema(), getTableName(), conn);
			
			if (!exists)
				throw new ForbiddenOperationException("The table '" + generateFullTableName(conn) + "' does not exist!!!");
			
			setFields(DBUtilities.getTableFields(getTableName(), DBUtilities.determineSchemaName(conn), conn));
			
			getPrimaryKey(conn);
			
			loadParents(conn);
			loadChildren(conn);
			
			tryToDiscoverySharedKeyInfo(conn);
			
			loadUniqueKeys(conn);
			
			loadAttDefinition(conn);
			
			//If was not specifically set to true
			if (!this.autoIncrementId) {
				this.autoIncrementId = useAutoIncrementId(conn);
			}
			
			if (utilities.stringHasValue(this.extraConditionForExtract)) {
				this.extraConditionForExtract = this.extraConditionForExtract.replaceAll(tableName + "\\.",
				    tableAlias + "\\.");
			}
			
			this.fullLoaded = true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private void loadAttDefinition(Connection conn) {
		int qtyAttrs = this.fields.size();
		
		for (int i = 0; i < qtyAttrs - 1; i++) {
			Field field = this.fields.get(i);
			
			field.setAttDefinedElements(AttDefinedElements.define(field.getName(), field.getType(), false, this));
		}
		
		Field field = this.fields.get(qtyAttrs - 1);
		
		field.setAttDefinedElements(AttDefinedElements.define(field.getName(), field.getType(), true, this));
		
		generateSQLElemenets();
	}
	
	protected void tryToDiscoverySharedKeyInfo(Connection conn) throws DBException {
		//Discovery shared pk
		
		if (this.parentRefInfo != null) {
			for (ParentTable ref : this.parentRefInfo) {
				
				ref.getPrimaryKey(conn);
				
				PrimaryKey parentRefInfoAskey = new PrimaryKey(ref);
				parentRefInfoAskey.setFields(ref.extractParentFieldsFromRefMapping());
				
				PrimaryKey childRefInfoAskey = new PrimaryKey(ref.getChildTableConf());
				childRefInfoAskey.setFields(ref.extractChildFieldsFromRefMapping());
				
				if (ref.getPrimaryKey().equals(parentRefInfoAskey)
				        && ref.getChildTableConf().primaryKey.equals(childRefInfoAskey)) {
					this.sharePkWith = ref.getTableName();
					
					break;
				}
			}
		}
		
	}
	
	public synchronized void fullLoad() throws DBException {
		OpenConnection mainConn = getRelatedSyncConfiguration().getMainApp().openConnection();
		
		OpenConnection dstConn = null;
		
		try {
			fullLoad(mainConn);
		}
		finally {
			mainConn.finalizeConnection();
			
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
		}
	}
	
	public AbstractTableConfiguration getSharedTableConf() {
		if (getSharedKeyRefInfo() != null) {
			return getSharedKeyRefInfo();
		}
		
		return null;
	}
	
	public ParentTable getSharedKeyRefInfo() {
		if (sharePkWith == null) {
			return null;
		} else if (utilities.arrayHasElement(this.parentRefInfo)) {
			
			for (ParentTable parent : this.parentRefInfo) {
				if (parent.getTableName().equalsIgnoreCase(this.sharePkWith)) {
					
					PrimaryKey pk = parent.getPrimaryKey();
					
					PrimaryKey refInfoKey = new PrimaryKey(parent);
					refInfoKey.setFields(parent.extractParentFieldsFromRefMapping());
					
					if (pk.hasSameFields(refInfoKey)) {
						return parent;
					}
				}
			}
		}
		
		throw new ForbiddenOperationException("The related table of shared pk " + sharePkWith + " of table "
		        + this.getTableName() + " is not listed inparents!");
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		return "Table [name:" + this.tableName + ", Alias:" + this.tableAlias + ",   pk: " + this.primaryKey + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractTableConfiguration))
			return false;
		
		return this.getTableName().equalsIgnoreCase(((AbstractTableConfiguration) obj).getTableName());
	}
	
	@JsonIgnore
	public File getPOJOCopiledFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOCompiledFilesDirectory();
	}
	
	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOSourceFilesDirectory();
	}
	
	public AbstractTableConfiguration findParentOnChildRefInfo(String parentTableName) {
		for (ChildTable ref : this.childRefInfo) {
			if (parentTableName.equals(ref.getParentTableConf().getTableName())) {
				return ref.getParentTableConf();
			}
		}
		
		return null;
	}
	
	public List<ParentTable> findAllRefToParent(String parentTableName) {
		if (!utilities.arrayHasElement(this.parentRefInfo)) {
			return null;
		}
		
		List<ParentTable> references = new ArrayList<>();
		
		for (ParentTable parent : this.parentRefInfo) {
			if (parentTableName.equals(parent.getTableName())) {
				references.add(parent);
			}
		}
		
		return references;
	}
	
	@Override
	public int compareTo(AbstractTableConfiguration o) {
		if (this.equals(o))
			return 0;
		
		return this.tableName.compareTo(o.getTableName());
	}
	
	@JsonIgnore
	public File getClassPath() {
		return new File(this.parentConf.getRelatedSyncConfiguration().getClassPath());
	}
	
	@JsonIgnore
	public boolean isDestinationInstallationType() {
		return getRelatedSyncConfiguration().isDataBaseMergeFromJSONProcess();
	}
	
	@JsonIgnore
	public boolean isDataReconciliationProcess() {
		return getRelatedSyncConfiguration().isDataReconciliationProcess();
	}
	
	@JsonIgnore
	public boolean isDBQuickLoad() {
		return getRelatedSyncConfiguration().isDBQuickLoadProcess();
	}
	
	@JsonIgnore
	public boolean isDBQuickCopy() {
		return getRelatedSyncConfiguration().isDbCopy();
	}
	
	@JsonIgnore
	public boolean isDbCopy() {
		return getRelatedSyncConfiguration().isDBQuickCopyProcess();
	}
	
	@JsonIgnore
	public boolean isDataBasesMergeFromSourceDBProcess() {
		return getRelatedSyncConfiguration().isDataBaseMergeFromSourceDBProcess();
	}
	
	@JsonIgnore
	public boolean hasNoDateVoidedField() {
		return utilities.isStringIn(getTableName(), "note");
	}
	
	@JsonIgnore
	public boolean hasNotDateChangedField() {
		return utilities.isStringIn(getTableName(), "obs");
	}
	
	/**
	 * Generates SQL condition using the {@link #uniqueKeys} fulfilled with related values from
	 * especific object
	 * 
	 * @param dbObject the object from where the condition values will be retrieved from
	 * @return a SQL condition
	 */
	@JsonIgnore
	public String generateUniqueKeysParametrizedCondition(DatabaseObject dbObject) {
		if (this.getUniqueKeys() == null)
			return null;
		
		String joinCondition = "";
		
		for (int i = 0; i < this.getUniqueKeys().size(); i++) {
			
			String uniqueKeyJoinField = generateUniqueKeyConditionsFields(this.getUniqueKeys().get(i), dbObject);
			
			if (i > 0)
				joinCondition += " OR ";
			
			joinCondition += "(" + uniqueKeyJoinField + ")";
		}
		
		return joinCondition;
	}
	
	/**
	 * Generates SQL parametrized condition for {@link #uniqueKeys}
	 * 
	 * @return a parametrized SQL condition
	 */
	@JsonIgnore
	public String generateUniqueKeysParametrizedCondition() {
		String joinCondition = "";
		
		for (int i = 0; i < this.getUniqueKeys().size(); i++) {
			
			String uniqueKeyJoinField = generateUniqueKeyConditionsFields(this.getUniqueKeys().get(i));
			
			if (i > 0)
				joinCondition += " OR ";
			
			joinCondition += "(" + uniqueKeyJoinField + ")";
		}
		
		return joinCondition;
	}
	
	private String generateUniqueKeyConditionsFields(UniqueKeyInfo uniqueKey) {
		String joinFields = "";
		
		List<Key> uniqueKeyFields = uniqueKey.getFields();
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				joinFields += " AND ";
			
			joinFields += uniqueKeyFields.get(i) + " = ? ";
		}
		
		return joinFields;
	}
	
	private String generateUniqueKeyConditionsFields(UniqueKeyInfo uniqueKey, DatabaseObject dbObject) {
		String conditionFields = "";
		
		List<Key> uniqueKeyFields = uniqueKey.getFields();
		
		uniqueKey.loadValuesToFields(dbObject);
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			Field field = uniqueKeyFields.get(i);
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getName(), field.getValue());
		}
		
		return conditionFields;
	}
	
	public boolean hasUniqueKeys() {
		return utilities.arrayHasElement(this.getUniqueKeys());
	}
	
	public boolean useSimpleNumericPk() {
		return this.getPrimaryKey() != null && this.getPrimaryKey().isSimpleNumericKey();
	}
	
	public boolean useAutoIncrementId(Connection conn) throws DBException {
		
		if (this.getPrimaryKey() == null || this.getPrimaryKey().isCompositeKey()) {
			return false;
		}
		
		return DBUtilities.checkIfTableUseAutoIcrement(this.tableName, conn);
	}
	
	public EtlConfiguration getRelatedSyncConfiguration() {
		return this.parentConf.getRelatedSyncConfiguration();
	}
	
	@Override
	public boolean hasDateFields() {
		for (Field t : this.fields) {
			if (t.isDateField()) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<ParentTable> getConditionalParents() {
		List<ParentTable> conditionalParents = null;
		
		if (utilities.arrayHasElement(this.parents)) {
			
			conditionalParents = new ArrayList<>();
			
			for (ParentTable p : this.parents) {
				if (p.hasConditionalFields()) {
					conditionalParents.add(p);
				}
			}
		}
		
		return conditionalParents;
	}
	
	private void generateSQLElemenets() {
		generateInsertSQLWithObjectId();
		generateInsertSQLWithoutObjectId();
		generateUpdateSQL();
	}
	
	public Object[] generateInsertParamsWithObjectId(DatabaseObject obj) {
		int qtyAttrs = this.fields.size();
		
		Object[] params = new Object[qtyAttrs];
		
		for (int i = 0; i < this.fields.size(); i++) {
			Field field = this.fields.get(i);
			
			params[i] = obj.getFieldValue(field.getName());
		}
		
		return params;
	}
	
	public Object[] generateInsertParamsWithoutObjectId(DatabaseObject obj) {
		int qtyAttrs = this.fields.size();
		
		Object[] params = new Object[qtyAttrs];
		
		AttDefinedElements attElements;
		
		for (int i = 0; i < this.fields.size(); i++) {
			Field field = this.fields.get(i);
			
			attElements = field.getAttDefinedElements();
			
			if (!attElements.isPartOfObjectId()) {
				params[i] = obj.getFieldValue(field.getName());
			}
		}
		
		return params;
	}
	
	private void generateInsertSQLWithoutObjectId() {
		String insertSQLFieldsWithoutObjectId = "";
		String insertSQLQuestionMarksWithoutObjectId = "";
		
		for (Field field : this.fields) {
			AttDefinedElements attElements = field.getAttDefinedElements();
			
			if (!attElements.isPartOfObjectId()) {
				insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId,
				    attElements.getSqlInsertFirstPartDefinition());
				
				insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId,
				    attElements.getSqlInsertLastEndPartDefinition());
			}
		}
		
		this.insertSQLWithoutObjectId = "INSERT INTO " + this.getObjectName() + "(" + insertSQLFieldsWithoutObjectId
		        + ") VALUES( " + insertSQLQuestionMarksWithoutObjectId + ");";
	}
	
	private void generateInsertSQLWithObjectId() {
		String insertSQLFieldsWithObjectId = "";
		String insertSQLQuestionMarksWithObjectId = "";
		
		for (Field field : this.fields) {
			AttDefinedElements attElements = field.getAttDefinedElements();
			
			insertSQLFieldsWithObjectId = utilities.concatStrings(insertSQLFieldsWithObjectId,
			    attElements.getSqlInsertFirstPartDefinition());
			
			insertSQLQuestionMarksWithObjectId = utilities.concatStrings(insertSQLQuestionMarksWithObjectId,
			    attElements.getSqlInsertLastEndPartDefinition());
		}
		
		this.insertSQLWithObjectId = "INSERT INTO " + this.getObjectName() + "(" + insertSQLFieldsWithObjectId + ") VALUES( "
		        + insertSQLQuestionMarksWithObjectId + ");";
		
	}
	
	@JsonIgnore
	private String generateUpdateSQL() {
		if (this.getPrimaryKey() == null) {
			
			if (this.getUniqueKeys() == null) {
				throw new ForbiddenOperationException("Impossible to generate update params, there is no primary key");
			}
		}
		
		this.updateSQL = "UPDATE " + this.getObjectName() + " SET ";
		
		for (Field field : this.fields) {
			AttDefinedElements attElements = field.getAttDefinedElements();
			
			updateSQL = utilities.concatStrings(updateSQL, attElements.getSqlUpdateDefinition());
			
		}
		
		if (this.getPrimaryKey() != null) {
			updateSQL += " WHERE " + this.getPrimaryKey().parseToParametrizedStringCondition();
		} else {
			updateSQL += " WHERE " + UniqueKeyInfo.parseToParametrizedStringConditionToAll(this.getUniqueKeys());
		}
		
		return updateSQL;
	}
	
	@JsonIgnore
	public Object[] generateUpdateParams(DatabaseObject obj) {
		if (this.getPrimaryKey() == null && (this.getUniqueKeys() == null || this.getUniqueKeys().isEmpty()))
			throw new ForbiddenOperationException("Impossible to generate update params, there is unique key defied");
		
		int qtyAttrs = this.fields.size();
		
		Object[] params = new Object[qtyAttrs + this.getPrimaryKey().getFields().size()];
		
		int i = 0;
		
		for (i = 0; i < this.fields.size(); i++) {
			Field field = this.fields.get(i);
			
			params[i] = obj.getFieldValue(field.getName());
		}
		
		List<Key> keys = null;
		
		if (this.getPrimaryKey() != null) {
			keys = this.getPrimaryKey().getFields();
		} else {
			keys = new ArrayList<>();
			
			for (UniqueKeyInfo key : this.getUniqueKeys()) {
				keys.addAll(key.getFields());
			}
		}
		
		for (Key key : this.getPrimaryKey().getFields()) {
			params[i++] = obj.getFieldValue(key.getName());
		}
		
		return params;
		
	}
	
	public String generateInsertValuesWithoutObjectId(DatabaseObject obj) {
		String insertValuesWithObjectIdDefinition = "";
		
		AttDefinedElements attElements;
		
		for (Field field : this.fields) {
			attElements = field.getAttDefinedElements();
			
			if (!attElements.isPartOfObjectId()) {
				insertValuesWithObjectIdDefinition = utilities.concatStrings(insertValuesWithObjectIdDefinition,
				    attElements.defineSqlInsertValue(obj));
			}
		}
		
		return insertValuesWithObjectIdDefinition;
	}
	
	public String generateInsertValuesWithObjectId(DatabaseObject obj) {
		String insertValuesWithObjectIdDefinition = "";
		
		AttDefinedElements attElements;
		
		for (Field field : this.fields) {
			attElements = field.getAttDefinedElements();
			
			insertValuesWithObjectIdDefinition = utilities.concatStrings(insertValuesWithObjectIdDefinition,
			    attElements.defineSqlInsertValue(obj));
		}
		
		return insertValuesWithObjectIdDefinition;
	}
	
	public abstract AppInfo getRelatedAppInfo();
	
	public abstract boolean isGeneric();
	
	@Override
	public List<Field> cloneFields() {
		List<Field> clonedFields = new ArrayList<>();
		
		if (utilities.arrayHasElement(this.fields)) {
			for (Field field : this.fields) {
				clonedFields.add(field.createACopy());
			}
		}
		
		return clonedFields;
	}
	
	public boolean hasCompositeKey() {
		return this.getPrimaryKey() != null && this.getPrimaryKey().isCompositeKey();
	}
	
	/**
	 * Generate a select columns content using the alias {@link #tableAlias}
	 * 
	 * @return
	 * @throws ForbiddenOperationException if the table does not have alias
	 */
	@JsonIgnore
	public String generateFullAliasedSelectColumns() throws ForbiddenOperationException {
		
		if (!hasAlias()) {
			throw new ForbiddenOperationException("No alias is defined for table " + this.tableName);
		}
		
		String fullSelectColumns = "";
		
		for (Field f : this.getFields()) {
			fullSelectColumns = utilities.concatStringsWithSeparator(fullSelectColumns, f.generateAliasedSelectColumn(this),
			    ",");
		}
		
		if (this.useSharedPKKey()) {
			fullSelectColumns += "," + this.getSharedTableConf().generateFullAliasedSelectColumns();
		}
		
		return fullSelectColumns;
	}
	
	/**
	 * Generates the content for SELECT FROM clause content.
	 * 
	 * @return
	 */
	public String generateSelectFromClauseContent() {
		return generateSelectFromClauseContentOnSpecificSchema("");
	}
	
	/**
	 * Generates the content for SELECT FROM clause content.
	 * 
	 * @return
	 * @throws DBException
	 */
	public String generateSelectFromClauseContentOnSpecificSchema(Connection conn) throws DBException {
		return generateSelectFromClauseContentOnSpecificSchema(DBUtilities.determineSchemaName(conn));
	}
	
	/**
	 * Generates the content for SELECT FROM clause content.
	 * 
	 * @return
	 */
	public String generateSelectFromClauseContentOnSpecificSchema(String schema) {
		String fromClause = generateFullTableNameWithAlias(schema);
		
		if (useSharedPKKey()) {
			fromClause += " INNER JOIN " + this.getSharedTableConf().generateFullTableNameWithAlias(schema) + " ON "
			        + this.getSharedKeyRefInfo().generateJoinCondition() + "\n";
		}
		
		return fromClause;
	}
	
	public boolean hasParentRefInfo() {
		return utilities.arrayHasElement(this.parentRefInfo);
	}
	
	public boolean hasChildRefInfo() {
		return utilities.arrayHasElement(this.childRefInfo);
	}
	
	public List<FieldsMapping> tryToLoadJoinFields(AbstractTableConfiguration relatedTabConf) {
		
		List<FieldsMapping> joinFields = new ArrayList<>();
		
		//Assuming that this datasource is parent
		List<ParentTable> pInfo = relatedTabConf.findAllRefToParent(this.getTableName());
		
		if (utilities.arrayHasElement(pInfo)) {
			
			if (utilities.arrayHasExactlyOneElement(pInfo)) {
				
				ParentTable ref = pInfo.get(0);
				
				for (RefMapping map : ref.getMapping()) {
					joinFields.add(new FieldsMapping(map.getChildField().getName(), "", map.getParentField().getName()));
				}
			} else {
				throw new ForbiddenOperationException(
				        "The mapping cannot be auto generated! Multiple references were found between " + this.getTableName()
				                + " And " + relatedTabConf.getTableName());
				
			}
		} else {
			
			//Assuning that the this data src is child
			pInfo = this.findAllRefToParent(relatedTabConf.getTableName());
			
			if (utilities.arrayHasExactlyOneElement(pInfo)) {
				
				ParentTable ref = pInfo.get(0);
				
				for (RefMapping map : ref.getMapping()) {
					joinFields.add(new FieldsMapping(map.getParentField().getName(), "", map.getChildField().getName()));
				}
			} else {
				throw new ForbiddenOperationException(
				        "The mapping cannot be auto generated! Multiple references were found between " + this.getTableName()
				                + " And " + relatedTabConf.getTableName());
				
			}
		}
		
		if (!utilities.arrayHasElement(joinFields)) {
			throw new ForbiddenOperationException(
			        "No join fields were difined between " + this.getTableName() + " And " + relatedTabConf.getTableName());
		}
		
		return joinFields;
	}
	
	protected String generateConditionsFields(DatabaseObject dbObject, List<FieldsMapping> joinFields,
	        String joinExtraCondition) {
		String conditionFields = "";
		
		for (int i = 0; i < joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = joinFields.get(i);
			
			Object value;
			
			try {
				value = dbObject.getFieldValue(field.getSrcField());
			}
			catch (ForbiddenOperationException e) {
				value = dbObject.getFieldValue(field.getSrcFieldAsClassField());
			}
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getDstField(), value);
		}
		
		if (utilities.stringHasValue(joinExtraCondition)) {
			conditionFields += " AND (" + joinExtraCondition + ")";
		}
		
		return conditionFields;
	}
	
	public String generateJoinCondition(AbstractTableConfiguration joiningTable, List<FieldsMapping> joinFields,
	        String joinExtraCondition) {
		String conditionFields = "";
		
		for (int i = 0; i < joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = joinFields.get(i);
			
			conditionFields += joiningTable.getTableAlias() + "." + field.getSrcField() + " = " + getTableAlias() + "."
			        + field.getDstField();
		}
		
		if (utilities.stringHasValue(joinExtraCondition)) {
			conditionFields += " AND (" + joinExtraCondition + ")";
		}
		
		return conditionFields;
	}
	
	public AbstractTableConfiguration findFullConfiguredConfInAllRelatedTable(String tableName) {
		if (this.getTableName().equals(tableName) && this.isFullLoaded()) {
			return this;
		}
		
		if (this.hasParentRefInfo()) {
			for (ParentTable p : this.getParentRefInfo()) {
				AbstractTableConfiguration fullLoaded = p.findFullConfiguredConfInAllRelatedTable(tableName);
				
				if (fullLoaded != null) {
					return fullLoaded;
				}
			}
		}
		
		if (this.hasChildRefInfo()) {
			for (ChildTable p : this.getChildRefInfo()) {
				AbstractTableConfiguration fullLoaded = p.findFullConfiguredConfInAllRelatedTable(tableName);
				
				if (fullLoaded != null) {
					return fullLoaded;
				}
			}
		}
		
		return null;
	}
	
	private boolean allRelatedTablesFullLoaded;
	
	public void fullLoadAllRelatedTables(TableAliasesGenerator aliasGenerator, AbstractTableConfiguration related,
	        Connection conn) {
		if (allRelatedTablesFullLoaded == true) {
			return;
		}
		
		if (this.hasParentRefInfo()) {
			for (ParentTable ref : this.getParentRefInfo()) {
				
				if (!ref.isMetadata()) {
					AbstractTableConfiguration existingConf = null;
					
					if (related != null) {
						existingConf = related.findFullConfiguredConfInAllRelatedTable(ref.getTableName());
					}
					
					if (existingConf == null) {
						existingConf = this.findFullConfiguredConfInAllRelatedTable(ref.getTableName());
					}
					
					if (existingConf != null) {
						ref.clone(ref);
					} else {
						ref.fullLoad(conn);
					}
					
					ref.fullLoadAllRelatedTables(aliasGenerator, this, conn);
				}
				
				ref.setTableAlias(aliasGenerator.generateAlias(ref));
			}
			
			this.allRelatedTablesFullLoaded = true;
		}
	}
}
