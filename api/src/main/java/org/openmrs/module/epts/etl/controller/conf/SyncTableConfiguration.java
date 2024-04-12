package org.openmrs.module.epts.etl.controller.conf;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.PojobleDatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableConfiguration extends SyncDataConfiguration implements Comparable<SyncTableConfiguration>, PojobleDatabaseObject {
	
	private String tableName;
	
	private List<TableParent> parents;
	
	private List<TableParent> conditionalParents;
	
	private List<RefInfo> parentRefInfo;
	
	private List<RefInfo> childRefInfo;
	
	private Class<DatabaseObject> syncRecordClass;
	
	private SyncDataConfiguration parent;
	
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
	
	private boolean manualIdGeneration;
	
	private boolean disabled;
	
	private boolean mustLoadChildrenInfo;
	
	private String extraConditionForExtract;
	
	public SyncTableConfiguration() {
	}
	
	public void clone(SyncTableConfiguration toCloneFrom) {
		this.tableName = toCloneFrom.tableName;
		this.parents = toCloneFrom.parents;
		this.childRefInfo = toCloneFrom.childRefInfo;
		this.parentRefInfo = toCloneFrom.parentRefInfo;
		this.conditionalParents = toCloneFrom.conditionalParents;
		this.syncRecordClass = toCloneFrom.syncRecordClass;
		this.parent = toCloneFrom.parent;
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
	
	public boolean isManualIdGeneration() {
		return manualIdGeneration;
	}
	
	public List<List<Field>> getWinningRecordFieldsInfo() {
		return winningRecordFieldsInfo;
	}
	
	public void setWinningRecordFieldsInfo(List<List<Field>> winningRecordFieldsInfo) {
		this.winningRecordFieldsInfo = winningRecordFieldsInfo;
	}
	
	public boolean hasWinningRecordsInfo() {
		return this.winningRecordFieldsInfo != null;
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
	
	@JsonIgnore
	public boolean isFromOpenMRSModel() {
		return this.getRelatedSyncConfiguration().isOpenMRSModel();
	}
	
	public boolean isRemoveForbidden() {
		return removeForbidden;
	}
	
	public void setRemoveForbidden(boolean removeForbidden) {
		this.removeForbidden = removeForbidden;
	}
	
	public List<TableParent> getConditionalParents() {
		return conditionalParents;
	}
	
	public void setConditionalParents(List<TableParent> conditionalParents) {
		this.conditionalParents = conditionalParents;
	}
	
	@Override
	public List<RefInfo> getChildRefInfo() {
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
	
	public boolean isDoIntegrityCheckInTheEnd(SyncOperationType operationType) {
		return getRelatedSyncConfiguration().isDoIntegrityCheckInTheEnd(operationType);
	}
	
	@JsonIgnore
	public String getId() {
		return this.getRelatedSyncConfiguration().getDesignation() + "_" + this.tableName;
	}
	
	@JsonIgnore
	public boolean isUuidColumnNotExists() {
		return this.isFromOpenMRSModel() && this.tableName.equals("patient") ? true : false;
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
	
	public List<TableParent> getParents() {
		return parents;
	}
	
	public void setParents(List<TableParent> parents) {
		this.parents = parents;
	}
	
	public String getSharePkWith() {
		return sharePkWith;
	}
	
	public void setSharePkWith(String sharePkWith) {
		this.sharePkWith = sharePkWith;
	}
	
	@Override
	public SyncDataConfiguration getParent() {
		return parent;
	}
	
	public void setParent(SyncDataConfiguration parent) {
		this.parent = parent;
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
		OpenConnection conn = getParent().getMainApp().openConnection();
		
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
						this.primaryKey = new PrimaryKey();
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
	private void loadUniqueKeys(SyncTableConfiguration tableConfiguration, Connection conn) {
		if (tableConfiguration.uniqueKeys == null) {
			try {
				this.uniqueKeys = UniqueKeyInfo.loadUniqueKeysInfo(this, conn);
				
				if (useSharedPKKey()) {
					SyncTableConfiguration parentConf = getSharedKeyRefInfo().getParentTableCof();
					
					parentConf.loadUniqueKeys(conn);
					
					if (utilities.arrayHasElement(parentConf.getUniqueKeys())) {
						
						for (UniqueKeyInfo uk : parentConf.getUniqueKeys()) {
							if (!utilities.existOnArray(this.uniqueKeys, uk)) {
								this.uniqueKeys.add(uk);
							}
						}
					}
				}
				
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
		for (RefInfo parent : this.parentRefInfo) {
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
			for (TableParent p : this.parents) {
				for (RefInfo r : p.getRefInfo()) {
					r.setChildTableConf(this);
					r.setParentTableCof(p);
					
					for (RefMapping map : r.getFieldsMapping()) {
						map.setRefInfo(r);
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
					
					SyncTableConfiguration referencedTabConf = SyncTableConfiguration.init(parentTableName, this.parent);
					
					addParentRefInfo(refCode, childFieldName, referencedTabConf, parentFieldName, conn);
					
					logDebug("PARENT [" + foreignKeyRS.getString("PKTABLE_NAME") + "] FOR TABLE '" + getTableName()
					        + "' CONFIGURED");
				}
				
				//Check if there is a configured parent but not defined on the db schema
				
				if (utilities.arrayHasElement(this.parents)) {
					
					for (TableParent configuredParent : this.parents) {
						
						for (RefInfo r : configuredParent.getRefInfo()) {
							if (!this.parentRefInfo.contains(r)) {
								this.parentRefInfo.add(r);
							}
						}
					}
					
				}
				
				logDebug("LOADED PARENTS FOR TABLE '" + getTableName() + "'");
			}
			finally {
				if (foreignKeyRS != null) {
					foreignKeyRS.close();
				}
			}
		
	}
	
	protected synchronized void loadChildren(Connection conn) throws SQLException {
		if (!this.mustLoadChildrenInfo)
			return;
		
		logDebug("LOADING CHILDREN FOR TABLE '" + getTableName() + "'");
		
		this.childRefInfo = new ArrayList<RefInfo>();
		
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
					String parentTableName = foreignKeyRS.getString("PKTABLE_NAME");
					
					SyncTableConfiguration childTabConf = SyncTableConfiguration.init(childTableName, this.parent);
					
					addChildRefInfo(refCode, childTabConf, childFieldName, parentFieldName, conn);
					
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
	
	protected void loadConditionalParents(Connection conn) throws DBException {
		if (!utilities.arrayHasElement(this.conditionalParents))
			return;
		
		for (TableParent p : this.conditionalParents) {
			for (RefInfo r : p.getRefInfo()) {
				r.setChildTableConf(this);
				r.setParentTableCof(p);
			}
		}
	}
	
	private void addChildRefInfo(String refCode, SyncTableConfiguration childTabConf, String childFieldName,
	        String parentFieldName, Connection conn) throws DBException {
		
		SyncTableConfiguration parentTabConf = this;
		
		initRefInfo(RefType.PARENT, refCode, childTabConf, childFieldName, parentTabConf, parentFieldName, conn);
	}
	
	private void addParentRefInfo(String refCode, String childFieldName, SyncTableConfiguration parentTabConf,
	        String parentFieldName, Connection conn) throws DBException {
		
		SyncTableConfiguration childTabConf = this;
		
		initRefInfo(RefType.CHILD, refCode, childTabConf, childFieldName, parentTabConf, parentFieldName, conn);
		
	}
	
	private RefInfo initRefInfo(RefType refType, String refCode, SyncTableConfiguration childTabConf, String childFieldname,
	        SyncTableConfiguration parentTabConf, String parentFieldName, Connection conn) throws DBException {
		
		String fieldName = null;
		
		if (refType.isChild()) {
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
		
		List<RefInfo> allRef = null;
		RefInfo ref = null;
		
		if (refType.isChild()) {
			allRef = this.childRefInfo;
		} else {
			allRef = this.parentRefInfo;
		}
		
		ref = utilities.findOnList(allRef, RefInfo.init(refCode));
		
		if (ref == null) {
			ref = RefInfo.init(refCode);
			
			ref.setParentTableCof(parentTabConf);
			ref.setChildTableConf(childTabConf);
			
			//Mark as metadata if is not specificaly mapped as parent in conf file
			if (!ref.getParentTableCof().isConfigured()) {
				ref.getParentTableCof().setMetadata(true);
			}
			
			allRef.add(ref);
		}
		
		RefMapping configuredMapping = findConfiguredRefMapping(parentTabConf.getTableName(), map.getParentField().getName(),
		    map.getChildField().getName());
		
		if (configuredMapping != null) {
			map.setDefaultValueDueInconsistency(configuredMapping.getDefaultValueDueInconsistency());
			map.setSetNullDueInconsistency(configuredMapping.getDefaultValueDueInconsistency() == null);
			
			ref.setConditionField(map.getRefInfo().getConditionField());
			ref.setConditionValue(map.getRefInfo().getConditionValue());
		}
		
		ref.addMapping(map);
		
		return ref;
	}
	
	public static SyncTableConfiguration init(String tableName, SyncDataConfiguration parent) {
		SyncTableConfiguration tableInfo = parent.getRelatedSyncConfiguration().findPulledTableConfiguration(tableName);
		
		if (tableInfo == null) {
			tableInfo = new SyncTableConfiguration();
			tableInfo.setTableName(tableName);
			tableInfo.setParent(parent);
			
			parent.getRelatedSyncConfiguration().addToTableConfigurationPull(tableInfo);
		}
		
		return tableInfo;
	}
	
	@JsonIgnore
	public Class<DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		if (syncRecordClass == null)
			this.syncRecordClass = DatabaseEntityPOJOGenerator.tryToGetExistingCLass(generateFullClassName(application),
			    getRelatedSyncConfiguration());
		
		if (syncRecordClass == null) {
			OpenConnection conn = application.openConnection();
			
			try {
				generateRecordClass(application, true);
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		if (syncRecordClass == null)
			throw new ForbiddenOperationException("The related pojo of table " + getTableName() + " was not found!!!!");
		
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
		return utilities.existOnArray(utilities.parseArrayToList(SyncTableConfiguration.REMOVABLE_METADATA), this.tableName);
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
		for (SyncTableConfiguration tabConf : getRelatedSyncConfiguration().getConfiguredTables()) {
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
				throw new ForbiddenOperationException("The table '" + getTableName() + "' does not exist!!!");
			
			getPrimaryKey(conn);
			
			loadParents(conn);
			loadChildren(conn);
			
			loadConditionalParents(conn);
			
			loadUniqueKeys(conn);
			
			setFields(DBUtilities.getTableFields(getTableName(), DBUtilities.determineSchemaName(conn), conn));
			
			this.fullLoaded = true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
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
	
	public RefInfo getSharedKeyRefInfo() {
		if (sharePkWith == null) {
			return null;
		} else if (utilities.arrayHasElement(this.getParents())) {
			
			for (RefInfo refInfo : this.parentRefInfo) {
				if (refInfo.getParentTableCof().getTableName().equalsIgnoreCase(this.sharePkWith)) {
					
					PrimaryKey pk = refInfo.getParentTableCof().getPrimaryKey();
					
					PrimaryKey refInfoKey = new PrimaryKey();
					refInfoKey.setFields(refInfo.extractParentFieldsFromRefMapping());
					
					if (pk.hasSameFields(refInfoKey)) {
						return refInfo;
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
		return "Table [name:" + this.tableName + ", pk: " + this.primaryKey + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof SyncTableConfiguration))
			return false;
		
		return this.getTableName().equalsIgnoreCase(((SyncTableConfiguration) obj).getTableName());
	}
	
	@JsonIgnore
	public File getPOJOCopiledFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOCompiledFilesDirectory();
	}
	
	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOSourceFilesDirectory();
	}
	
	/**
	 * Find and returns the RefMapping info on configured parents.
	 * 
	 * @param parentTableName
	 * @return the parents with a given name #parentTableName
	 * @throws ForbiddenOperationException if there are more that one parents with a given name
	 */
	public RefMapping findConfiguredRefMapping(String parentTableName, String parentField, String childField)
	        throws ForbiddenOperationException {
		if (!utilities.arrayHasElement(this.parents))
			return null;
		
		for (TableParent info : this.parents) {
			
			if (info.getTableName().equalsIgnoreCase(parentTableName)) {
				for (RefInfo r : info.getRefInfo()) {
					RefMapping rm = r.findRefMapping(childField, parentField);
					
					if (rm != null)
						return rm;
				}
			}
		}
		
		return null;
	}
	
	public SyncTableConfiguration findParentOnChildRefInfo(String parentTableName) {
		for (RefInfo ref : this.childRefInfo) {
			if (parentTableName.equals(ref.getParentTableName())) {
				return ref.getParentTableCof();
			}
		}
		
		return null;
	}
	
	@Override
	public int compareTo(SyncTableConfiguration o) {
		if (this.equals(o))
			return 0;
		
		return this.tableName.compareTo(o.getTableName());
	}
	
	@JsonIgnore
	public File getClassPath() {
		return new File(this.parent.getRelatedSyncConfiguration().getClassPath());
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
	 * Generates SQL join condition between two tables (some table) one from source another from
	 * destination database based on the {@link #uniqueKeys}
	 * 
	 * @param sourceTableAlias alias name for source table
	 * @param destinationTableAlias alias name for destination table
	 * @return the generated join condition based on {@link #uniqueKeys}
	 */
	@JsonIgnore
	public String generateUniqueKeysJoinCondition(String sourceTableAlias, String destinationTableAlias) {
		String joinCondition = "";
		
		for (int i = 0; i < this.getUniqueKeys().size(); i++) {
			
			String uniqueKeyJoinField = generateUniqueKeyJoinField(this.getUniqueKeys().get(i));
			
			if (i > 0)
				joinCondition += " OR ";
			
			joinCondition += "(" + uniqueKeyJoinField + ")";
		}
		
		if (!utilities.stringHasValue(joinCondition) && this.isMetadata()) {
			joinCondition = "dest_." + getPrimaryKey() + " = src_." + getPrimaryKey();
		}
		
		return joinCondition;
	}
	
	private String generateUniqueKeyJoinField(UniqueKeyInfo uk) {
		List<Key> uniqueKeyFields = uk.getFields();
		
		String joinFields = "";
		
		for (int i = 0; i < uniqueKeyFields.size(); i++) {
			if (i > 0)
				joinFields += " AND ";
			
			joinFields += "dest_." + uniqueKeyFields.get(i).getName() + " = src_." + uniqueKeyFields.get(i).getName();
		}
		
		return joinFields;
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
	
	public boolean useManualIdGeneration(Connection conn) throws DBException {
		return DBUtilities.checkIfTableUseAutoIcrement(this.tableName, conn);
	}
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return this.parent.getRelatedSyncConfiguration();
	}
	
}
