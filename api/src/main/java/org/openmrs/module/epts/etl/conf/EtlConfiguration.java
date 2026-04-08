package org.openmrs.module.epts.etl.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;

import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.datasource.EtlConfigurationSrcConf;
import org.openmrs.module.epts.etl.conf.datasource.EtlItemSrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TableAliasesGenerator;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.AutoIncrementHandlingType;
import org.openmrs.module.epts.etl.conf.types.EtlInconsistencyBehavior;
import org.openmrs.module.epts.etl.conf.types.EtlOperationType;
import org.openmrs.module.epts.etl.conf.types.EtlProcessType;
import org.openmrs.module.epts.etl.conf.types.EtlTotalRecordsCountStrategy;
import org.openmrs.module.epts.etl.conf.types.RelationshipResolutionStrategy;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class EtlConfiguration extends AbstractBaseConfiguration implements TableAliasesGenerator, EtlDataConfiguration {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	static final String STRING_LOCK = new String("LOCK_STRING");
	
	public static final String SKIPPED_RECORD_TABLE_NAME = "skipped_record";
	
	public static final String DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME = "default_generated_object_key";
	
	public static final String ETL_RECORD_ERROR_TABLE_NAME = "etl_record_error";
	
	private static final String DEFAULT_ETL_ELEMENTS_TEMPLATE_FILE = "etl_elements_templates.json";
	
	private String etlRootDirectory;
	
	private String etlTemplatesFilePath;
	
	private String originAppLocationCode;
	
	private EtlConfigurationSrcConf dynamicSrcConf;
	
	private List<EtlItemConfiguration> etlItemConfiguration;
	
	private EtlItemConfiguration testingEtlItemConfiguration;
	
	private DBConnectionInfo mainConnInfo;
	
	private DBConnectionInfo srcConnInfo;
	
	private DBConnectionInfo dstConnInfo;
	
	private EtlProcessType processType;
	
	private File relatedConfFile;
	
	private List<EtlOperationConfig> operations;
	
	private List<AbstractTableConfiguration> configuredTables;
	
	private boolean manualStart;
	
	private String childConfigFilePath;
	
	private String configFilePath;
	
	private EtlConfiguration childConfig;
	
	private boolean disabled;
	
	private File moduleRootDirectory;
	
	private boolean fullLoaded;
	
	private ProcessController relatedController;
	
	private List<AbstractTableConfiguration> allTables;
	
	private EptsEtlLogger logger;
	
	private String syncStageSchema;
	
	private final String stringLock = new String("LOCK_STRING");
	
	private ProcessFinalizerConf finalizer;
	
	private Map<String, Integer> qtyLoadedTables;
	
	private Map<String, String> params;
	
	private boolean initialized;
	
	private String classPath;
	
	private EtlConfigurationTableConf defaultGeneratedObjectKeyTabConf;
	
	private EtlConfigurationTableConf recordWithDefaultParentsInfoTabConf;
	
	private EtlConfigurationTableConf etlRecordErrorTabCof;
	
	private EtlConfigurationTableConf skippedRecordTabConf;
	
	private List<TableConfiguration> fullLoadedTables;
	
	private List<String> busyTableAliasName;
	
	private List<String> generatedItemCodes;
	
	/*
	 * Indicates if in this process the primary keys are transformed or not. If yes, the transformed records are given a new pk, if no, the pk is src is the same in dst
	 */
	private boolean doNotTransformsPrimaryKeys;
	
	/**
	 * If present, the value from this field will be mapped as a primary key for all tables that
	 * don't have a primary key but have a field with name matching this field. <br>
	 * This value will be overridden by the correspondent value on {@link EtlItemConfiguration} if
	 * present there
	 */
	private String manualMapPrimaryKeyOnField;
	
	/**
	 * The time in seconds to wait before check the process status again
	 */
	private int waitTimeToCheckStatus;
	
	private EtlConfiguration parentEtlConf;
	
	private AutoIncrementHandlingType autoIncrementHandlingType;
	
	/**
	 * A numeric value added to the primary key of the very first destination record for all tables
	 * defined in the ETL Item Configuration. This property cannot be used when
	 * autoIncrementHandlingType is explicitly set to AS_SCHEMA_DEFINED. If this property is
	 * provided and autoIncrementHandlingType is not specified, it will automatically be set to
	 * IGNORE_SCHEMA_DEFINITION.
	 */
	private Integer primaryKeyInitialIncrementValue;
	
	private boolean reRunable;
	
	private ActionOnEtlException defaultExceptionBehavior;
	
	private EtlInconsistencyBehavior defaultInconsistencyBehavior;
	
	/**
	 * Defines additional source tables that are not explicitly configured as ETL sources but are
	 * related to the configured source tables.
	 * <p>
	 * These tables must be declared so they are treated as part of the ETL data domain rather than
	 * as metadata tables. This ensures that relationships such as joins, foreign keys, and
	 * dependent records are correctly resolved during the transformation process.
	 * </p>
	 */
	private List<String> relatedEtlSrcTables;
	
	private RelationshipResolutionStrategy relationshipResolutionStrategy;
	
	public EtlConfiguration() {
		this.allTables = new ArrayList<AbstractTableConfiguration>();
		
		this.initialized = false;
		
		this.qtyLoadedTables = new HashMap<>();
		
		this.configuredTables = new ArrayList<>();
		
		this.busyTableAliasName = new ArrayList<>();
		
		this.waitTimeToCheckStatus = 5;
		
		this.defaultExceptionBehavior = ActionOnEtlException.ABORT_PROCESS;
		this.relationshipResolutionStrategy = RelationshipResolutionStrategy.RESOLVE;
		this.defaultInconsistencyBehavior = EtlInconsistencyBehavior.ABORT_PROCESS;
	}
	
	public RelationshipResolutionStrategy getRelationshipResolutionStrategy() {
		return relationshipResolutionStrategy;
	}
	
	public void setRelationshipResolutionStrategy(RelationshipResolutionStrategy relationshipResolutionStrategy) {
		this.relationshipResolutionStrategy = relationshipResolutionStrategy;
	}
	
	public List<String> getRelatedEtlSrcTables() {
		return relatedEtlSrcTables;
	}
	
	public void setRelatedEtlSrcTables(List<String> relatedEtlSrcTables) {
		this.relatedEtlSrcTables = relatedEtlSrcTables;
	}
	
	public String getEtlTemplatesFilePath() {
		return etlTemplatesFilePath;
	}
	
	public void setEtlTemplatesFilePath(String etlTemplatesFilePath) {
		this.etlTemplatesFilePath = etlTemplatesFilePath;
	}
	
	public Integer getPrimaryKeyInitialIncrementValue() {
		return primaryKeyInitialIncrementValue;
	}
	
	public void setPrimaryKeyInitialIncrementValue(Integer primaryKeyInitialIncrementValue) {
		this.primaryKeyInitialIncrementValue = primaryKeyInitialIncrementValue;
	}
	
	public AutoIncrementHandlingType getAutoIncrementHandlingType() {
		return autoIncrementHandlingType;
	}
	
	public void setAutoIncrementHandlingType(AutoIncrementHandlingType autoIncrementHandlingType) {
		this.autoIncrementHandlingType = autoIncrementHandlingType;
	}
	
	public EtlConfiguration getParentEtlConf() {
		return parentEtlConf;
	}
	
	public void setParentEtlConf(EtlConfiguration parentEtlConf) {
		this.parentEtlConf = parentEtlConf;
	}
	
	public boolean hasParentEtlConf() {
		return this.parentEtlConf != null;
	}
	
	public EtlConfigurationSrcConf getDynamicSrcConf() {
		return dynamicSrcConf;
	}
	
	public void setDynamicSrcConf(EtlConfigurationSrcConf dynamicSrcConf) {
		this.dynamicSrcConf = dynamicSrcConf;
	}
	
	public boolean isDynamic() {
		return this.getDynamicSrcConf() != null;
	}
	
	public void setTestingEtlItemConfiguration(EtlItemConfiguration testingEtlItemConfiguration) {
		this.testingEtlItemConfiguration = testingEtlItemConfiguration;
	}
	
	public EtlItemConfiguration getTestingEtlItemConfiguration() {
		return testingEtlItemConfiguration;
	}
	
	public int getWaitTimeToCheckStatus() {
		return waitTimeToCheckStatus;
	}
	
	public void setWaitTimeToCheckStatus(int waitTimeToCheckStatus) {
		this.waitTimeToCheckStatus = waitTimeToCheckStatus;
	}
	
	public String getManualMapPrimaryKeyOnField() {
		return manualMapPrimaryKeyOnField;
	}
	
	public void setManualMapPrimaryKeyOnField(String manualMapPrimaryKeyOnField) {
		this.manualMapPrimaryKeyOnField = manualMapPrimaryKeyOnField;
	}
	
	public boolean isManualStart() {
		return manualStart;
	}
	
	public void setManualStart(boolean manualStart) {
		this.manualStart = manualStart;
	}
	
	public String getConfigFilePath() {
		return configFilePath;
	}
	
	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}
	
	public EtlConfigurationTableConf getSkippedRecordTabConf() {
		return skippedRecordTabConf;
	}
	
	public void setSkippedRecordTabConf(EtlConfigurationTableConf skippedRecordTabConf) {
		this.skippedRecordTabConf = skippedRecordTabConf;
	}
	
	public EtlConfigurationTableConf getRecordWithDefaultParentsInfoTabConf() {
		return recordWithDefaultParentsInfoTabConf;
	}
	
	public void setRecordWithDefaultParentsInfoTabConf(EtlConfigurationTableConf recordWithDefaultParentsInfoTabConf) {
		this.recordWithDefaultParentsInfoTabConf = recordWithDefaultParentsInfoTabConf;
	}
	
	public boolean isDoTransformsPrimaryKeys() {
		return !isDoNotTransformsPrimaryKeys();
	}
	
	public boolean isDoNotTransformsPrimaryKeys() {
		return doNotTransformsPrimaryKeys;
	}
	
	public void setDoNotTransformsPrimaryKeys(boolean doNotTransformsPrimaryKeys) {
		this.doNotTransformsPrimaryKeys = doNotTransformsPrimaryKeys;
	}
	
	public EtlConfigurationTableConf getEtlRecordErrorTabCof() {
		return etlRecordErrorTabCof;
	}
	
	public EtlConfigurationTableConf getDefaultGeneratedObjectKeyTabConf() {
		return defaultGeneratedObjectKeyTabConf;
	}
	
	public Map<String, String> getParams() {
		return params;
	}
	
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	public List<TableConfiguration> getConfiguredTables() {
		return utilities.parseList(configuredTables, TableConfiguration.class);
	}
	
	public void setConfiguredTables(List<AbstractTableConfiguration> configuredTables) {
		this.configuredTables = configuredTables;
	}
	
	public int increaseQtyLoadedTables(String tableName) {
		synchronized (stringLock) {
			
			if (this.qtyLoadedTables.containsKey(tableName)) {
				int currentValue = this.qtyLoadedTables.get(tableName);
				
				this.qtyLoadedTables.put(tableName, currentValue + 1);
			} else {
				this.qtyLoadedTables.put(tableName, 1);
			}
			
			return this.qtyLoadedTables.get(tableName);
		}
	}
	
	public void setSyncStageSchema(String syncStageSchema) {
		this.syncStageSchema = syncStageSchema;
	}
	
	@JsonIgnore
	public List<AbstractTableConfiguration> getAllTables() {
		return allTables;
	}
	
	public void setAllTables(List<AbstractTableConfiguration> allTables) {
		this.allTables = allTables;
	}
	
	public void setRelatedController(ProcessController relatedController) {
		this.relatedController = relatedController;
	}
	
	@JsonIgnore
	public ProcessController getRelatedController() {
		return relatedController;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public Date getStartDate() {
		Object startDate = getParamValue("startDate");
		
		if (utilities.objectHasValue(startDate)) {
			return DateAndTimeUtilities.createDate(startDate.toString());
		}
		
		return null;
	}
	
	public Date getEndDate() {
		Object endDate = getParamValue("endDate");
		
		if (utilities.objectHasValue(endDate)) {
			return DateAndTimeUtilities.createDate(endDate.toString());
		}
		
		return null;
	}
	
	@JsonIgnore
	public EtlConfiguration getChildConfig() {
		return childConfig;
	}
	
	public void setChildConfig(EtlConfiguration childConfig) {
		this.childConfig = childConfig;
	}
	
	public String getChildConfigFilePath() {
		return childConfigFilePath;
	}
	
	public void setChildConfigFilePath(String childConfigFilePath) {
		this.childConfigFilePath = childConfigFilePath;
	}
	
	public EtlProcessType getProcessType() {
		return processType;
	}
	
	public void setProcessType(EtlProcessType processType) {
		
		if (processType != null && !processType.isSupportedProcessType()) {
			throw new ForbiddenOperationException(
			        "The 'processType' of syncConf file must be in " + EtlProcessType.values());
		}
		
		this.processType = processType;
	}
	
	@JsonIgnore
	public boolean isDataBaseMergeFromJSONProcess() {
		return processType.isDataBaseMergeFromJSON();
	}
	
	@JsonIgnore
	public boolean isSourceSyncProcess() {
		return processType.isSourceSync();
	}
	
	@JsonIgnore
	public boolean isPojoGeneration() {
		return processType.isPojoGeneration();
	}
	
	@JsonIgnore
	public boolean isEtlProcess() {
		return processType.isEtl();
	}
	
	@JsonIgnore
	public boolean isReEtlProcess() {
		return processType.isReEtl();
	}
	
	@JsonIgnore
	public boolean isDBReSyncProcess() {
		return processType.isDBResync();
	}
	
	@JsonIgnore
	public boolean isDetectMissingRecords() {
		return this.processType.isDetectMissingRecords();
	}
	
	@JsonIgnore
	public boolean isDetectGapesOnDbTables() {
		return this.processType.isDetectGapesOnDbTables();
	}
	
	@JsonIgnore
	public boolean isDBQuickExportProcess() {
		return processType.isDBQuickExport();
	}
	
	@JsonIgnore
	public boolean isDBQuickMergeWithEntityGenerationDBProcess() {
		return processType.isQuickMergeWithEntityGeneration();
	}
	
	@JsonIgnore
	public boolean isDBQuickMergeWithDatabaseGenerationDBProcess() {
		return processType.isQuickMergeWithDatabaseGeneration();
	}
	
	@JsonIgnore
	public boolean isDataBaseMergeFromSourceDBProcess() {
		return processType.isDataBaseMergeFromSourceDB();
	}
	
	@JsonIgnore
	public boolean isDBQuickLoadProcess() {
		return processType.isDBQuickLoad();
	}
	
	@JsonIgnore
	public boolean isDataReconciliationProcess() {
		return processType.isDataReconciliation();
	}
	
	@JsonIgnore
	public boolean isDBInconsistencyCheckProcess() {
		return processType.isdDBInconsistencyCheck();
	}
	
	@JsonIgnore
	public boolean isResolveProblems() {
		return processType.isGenericProcess();
	}
	
	@JsonIgnore
	public String getPojoPackage(DBConnectionInfo connInfo) {
		return connInfo.getPojoPackageName();
	}
	
	public DBConnectionInfo getMainConnInfo() {
		return mainConnInfo;
	}
	
	public void setMainConnInfo(DBConnectionInfo mainConnInfo) {
		this.mainConnInfo = mainConnInfo;
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return srcConnInfo;
	}
	
	public void setSrcConnInfo(DBConnectionInfo srcConnInfo) {
		this.srcConnInfo = srcConnInfo;
	}
	
	public DBConnectionInfo getDstConnInfo() {
		return dstConnInfo;
	}
	
	public void setDstConnInfo(DBConnectionInfo dstConnInfo) {
		this.dstConnInfo = dstConnInfo;
	}
	
	public List<EtlItemConfiguration> getEtlItemConfiguration() {
		return etlItemConfiguration;
	}
	
	public List<String> parseEtlConfigurationsToString_() {
		List<String> tableConfigurationsAsString = new ArrayList<>();
		
		if (utilities.listHasElement(getEtlItemConfiguration())) {
			for (EtlItemConfiguration tc : getEtlItemConfiguration()) {
				tableConfigurationsAsString.add(tc.getConfigCode());
			}
		}
		
		return tableConfigurationsAsString;
	}
	
	public String getEtlRootDirectory() {
		return etlRootDirectory;
	}
	
	public void setEtlRootDirectory(String etlRootDirectory) {
		this.etlRootDirectory = etlRootDirectory;
	}
	
	public String generateProcessStatusFolder() {
		String subFolder = "";
		
		if (this.isSupposedToRunInOrigin()) {
			subFolder = "source";
		} else if (this.isSupposedToRunInDestination()) {
			subFolder = "destination";
		}
		
		return this.getEtlRootDirectory() + FileUtilities.getPathSeparator() + "process_status"
		        + FileUtilities.getPathSeparator() + subFolder + FileUtilities.getPathSeparator() + this.getDesignation();
	}
	
	public void setEtlItemConfiguration(List<EtlItemConfiguration> etlItemConfiguration) {
		if (etlItemConfiguration != null) {
			for (EtlItemConfiguration config : etlItemConfiguration) {
				config.setRelatedEtlConfig(this);
			}
		}
		
		this.etlItemConfiguration = etlItemConfiguration;
	}
	
	public String getSyncStageSchema() {
		String schema;
		
		if (utilities.stringHasValue(this.syncStageSchema)) {
			schema = this.syncStageSchema;
		} else if (isSupposedToRunInOrigin()) {
			schema = this.originAppLocationCode + "_sync_stage_area";
		} else if (isDBQuickLoadProcess() || isDataReconciliationProcess() || isDataBaseMergeFromSourceDBProcess()) {
			schema = "minimal_db_info";
		} else {
			schema = "sync_stage_area";
		}
		
		return schema.toLowerCase();
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}
	
	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}
	
	public void setRelatedConfFile(File relatedConfFile) {
		this.relatedConfFile = relatedConfFile;
	}
	
	@JsonIgnore
	public File getRelatedConfFile() {
		return relatedConfFile;
	}
	
	public static <T extends EtlDatabaseObject> EtlConfiguration loadFromFile(File file)
	        throws IOException, ForbiddenOperationException {
		
		String json = FileUtilities.realAllFileAsString(file);
		
		Properties fileProps = loadProperties(System.getProperty("etl.env.file"));
		
		EtlConfiguration conf = EtlConfiguration.loadFromJSON(
		    EtlDataConfiguration.resolvePlaceholders(json, fileProps, System.getProperties(), System.getenv()), file);
		
		conf.setConfigFilePath(file.getAbsolutePath());
		
		conf.setRelatedConfFile(file);
		
		return conf;
	}
	
	public static Properties loadProperties(String path) {
		if (path == null)
			return null;
		
		Properties props = new Properties();
		
		try (InputStream is = new FileInputStream(path)) {
			props.load(is);
		}
		catch (IOException e) {
			throw new RuntimeException("Error loading properties file: " + path, e);
		}
		
		return props;
	}
	
	void initLogger() {
		if (this.logger != null)
			return;
		
		synchronized (STRING_LOCK) {
			
			if (this.logger != null)
				return;
			
			this.logger = new EptsEtlLogger(EtlConfiguration.class);
		}
		
	}
	
	public void logDebug(String msg) {
		if (logger == null)
			initLogger();
		
		this.logger.debug(msg);
	}
	
	public void logTrace(String msg) {
		if (logger == null)
			initLogger();
		
		this.logger.trace(msg);
	}
	
	public void logInfo(String msg) {
		if (logger == null)
			initLogger();
		
		logger.info(msg);
	}
	
	public void logWarn(String msg) {
		if (logger == null)
			initLogger();
		
		logger.warn(msg);
	}
	
	public void logErr(String msg) {
		if (logger == null)
			initLogger();
		
		logger.error(msg);
	}
	
	/**
	 * Loads the code for each
	 * 
	 * @throws ForbiddenOperationException
	 * @throws DBException
	 */
	public void init(OpenConnection srcConn, OpenConnection dstConn, OpenConnection mainConn)
	        throws ForbiddenOperationException, DBException {
		if (initialized) {
			return;
		}
		
		synchronized (STRING_LOCK) {
			this.defaultGeneratedObjectKeyTabConf = new EtlConfigurationTableConf(
			        EtlConfiguration.DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME, this);
			
			this.skippedRecordTabConf = new EtlConfigurationTableConf(EtlConfiguration.SKIPPED_RECORD_TABLE_NAME, this);
			
			this.etlRecordErrorTabCof = new EtlConfigurationTableConf(EtlConfiguration.ETL_RECORD_ERROR_TABLE_NAME, this);
			
			this.recordWithDefaultParentsInfoTabConf = new EtlConfigurationTableConf(
			        this.getRecordWithDefaultParentInfoTableName(), this);
			
			if (this.hasMainConnInfo()) {
				this.getMainConnInfo().setRelatedEtlConf(this);
				this.getMainConnInfo().tryToLoadPlaceHolders(this);
			}
			if (this.hasSrcConnInfo()) {
				this.getSrcConnInfo().setRelatedEtlConf(this);
				this.getSrcConnInfo().tryToLoadPlaceHolders(this);
			}
			
			if (this.hasDstConnInfo()) {
				this.getDstConnInfo().setRelatedEtlConf(this);
				this.getDstConnInfo().tryToLoadPlaceHolders(this);
			}
			
			if (this.getAutoIncrementHandlingType() == null) {
				if (this.getPrimaryKeyInitialIncrementValue() != null && this.getPrimaryKeyInitialIncrementValue() > 0) {
					this.autoIncrementHandlingType = AutoIncrementHandlingType.IGNORE_SCHEMA_DEFINITION;
				} else {
					this.autoIncrementHandlingType = AutoIncrementHandlingType.AS_SCHEMA_DEFINED;
				}
			}
			
			if (this.getPrimaryKeyInitialIncrementValue() == null) {
				this.setPrimaryKeyInitialIncrementValue(0);
			}
			
			if (this.etlTemplatesFilePath == null) {
				etlTemplatesFilePath = this.getRelatedConfFile().getParent() + File.separator
				        + DEFAULT_ETL_ELEMENTS_TEMPLATE_FILE;
			}
			
			for (EtlOperationConfig operation : this.getOperations()) {
				if (operation.getMaxSupportedProcessors() == 1) {
					operation.setUseSharedConnectionPerThread(false);
				}
				
				if (operation.isConsoleDst()) {
					operation.setDoNotSaveOperationProgress(true);
				}
				
				if (operation.getTotalAvaliableRecordsToProcess() != null) {
					operation.setTotalCountStrategy(EtlTotalRecordsCountStrategy.USE_PROVIDED_COUNT);
				}
			}
			
			List<EtlItemConfiguration> allItem = new ArrayList<>();
			
			tryToExecuteStartupScripts(srcConn.getDbConnInfo(), dstConn.getDbConnInfo());
			
			int pos = 0;
			
			for (EtlItemConfiguration item : this.getEtlItemConfiguration()) {
				pos++;
				
				item.setRelatedEtlConfig(this);
				
				if (item.isDynamic()) {
					List<EtlItemConfiguration> dynamicItems = item.generateDynamicItems(this, srcConn);
					
					if (utilities.listHasElement(dynamicItems)) {
						logDebug(
						    "Found Dynamic Item on position [" + pos + "] whith " + dynamicItems.size() + " returned item!");
						
						for (EtlItemConfiguration dItem : dynamicItems) {
							allItem.add(dItem);
							
							initItem(dItem, false);
						}
						
					} else {
						logWarn("No Item was returned on dynamic item [" + pos + "]");
					}
					
				} else {
					if (item.getAutoIncrementHandlingType() == null) {
						item.setAutoIncrementHandlingType(this.getAutoIncrementHandlingType());
					}
					
					if (item.getPrimaryKeyInitialIncrementValue() == null) {
						item.setPrimaryKeyInitialIncrementValue(this.getPrimaryKeyInitialIncrementValue());
					}
					
					allItem.add(item);
					
					initItem(item, false);
				}
			}
			
			this.setEtlItemConfiguration(allItem);
			
			if (this.hasTestingItem()) {
				initItem(this.getTestingEtlItemConfiguration(), true);
			}
			
			if (this.relatedEtlSrcTables != null) {
				for (String tableName : this.relatedEtlSrcTables) {
					addConfiguredTable(new GenericTableConfiguration(tableName));
				}
			}
			
			ensureEtlSchemaTablesExists();
			
			DefaultEtlValidator.tryToValidate(this, srcConn, dstConn);
			
		}
	}
	
	private OpenConnection ensureEtlSchemaTablesExists() throws DBException {
		if (!this.isImportStageSchemaExists()) {
			this.createStageSchema();
		}
		
		if (!existInconsistenceInfoTable()) {
			createInconsistenceInfoTable();
		}
		
		if (!existOperationProgressInfoTable()) {
			createTableOperationProgressInfo();
		}
		
		if (!existsDefaultGeneratedObjectKeyTable()) {
			createDefaultGeneratedObjectKeyTable();
		}
		
		if (!existEtlRecordErrorTable()) {
			createEtlRecordErrorTable();
		}
		
		if (!existsSkippedRecordsTable()) {
			createSkippedRecordsTable();
		}
		
		if (!existRelatedRecursiveRecordInfoTable()) {
			logDebug("GENERATING RELATED RECURSIVE TABLE");
			
			createRecordWithDefaultParentInfoTable();
			
			logDebug("RELATEDRECURSIVE TABLE GENERATED");
		}
		
		OpenConnection conn = openSrcConn();
		
		if (this.hasDstConnInfo()) {
			
			//Try to openConnection to determine if db schama exists
			boolean dstDbExists = false;
			
			try {
				OpenConnection dstConn = getDstConnInfo().openConnection();
				dstConn.finalizeConnection();
				
				dstDbExists = true;
			}
			catch (DBException e) {
				if (DBUtilities.determineDataBaseFromException(e).equals(DBUtilities.MYSQL_DATABASE)) {
					if (!DBException.checkIfExceptionContainsMessage(e, "Unknown database")) {
						throw e;
					}
				} else
					throw e;
			}
			
			if (!dstDbExists) {
				this.getDstConnInfo().restoreDump(this);
			}
		}
		return conn;
	}
	
	private void tryToExecuteStartupScripts(DBConnectionInfo srcConnInfo, DBConnectionInfo dstConnInfo) throws DBException {
		
		if (!this.hasParentEtlConf()) {
			File srcScriptsDir = this.getSrcSqlStartupScriptsDirectory();
			
			if (srcScriptsDir.listFiles() != null) {
				for (File script : srcScriptsDir.listFiles()) {
					DBUtilities.runScriptOnDbServer(srcConnInfo, script.getAbsolutePath());
				}
			}
			
			File dstScriptsDir = this.getDstSqlStartupScriptsDirectory();
			
			if (dstScriptsDir.listFiles() != null) {
				for (File script : dstScriptsDir.listFiles()) {
					DBUtilities.runScriptOnDbServer(dstConnInfo, script.getAbsolutePath());
				}
			}
			
		}
	}
	
	public boolean hasTestingItem() {
		return this.getTestingEtlItemConfiguration() != null && !this.getTestingEtlItemConfiguration().isDisabled();
	}
	
	public void initItem(EtlItemConfiguration item, boolean testing) {
		item.tryToLoadFromTemplate();
		
		item.setRelatedEtlConfig(this);
		item.getSrcConf().setParentConf(item);
		
		item.setTesting(testing);
		
		if (!item.getSrcConf().hasDstType()) {
			//We start with the first operation dst type. Eventual this should be changed if the nested operation has different dstType
			item.getSrcConf().setDstType(this.getOperations().get(0).getDstType());
		}
		
		if (item.getSrcConf().hasAlias()) {
			item.getSrcConf().setUsingManualDefinedAlias(true);
			tryToAddToBusyTableAliasName(item.getSrcConf().getTableAlias());
		}
		
		addConfiguredTable(item.getSrcConf());
		
		item.getSrcConf().tryToLoadSchemaInfo(item.getRelatedEtlSchemaObject());
		
		List<EtlAdditionalDataSource> allAvaliableDataSources = item.getSrcConf().getAvaliableExtraDataSource();
		
		for (EtlAdditionalDataSource t : allAvaliableDataSources) {
			t.tryToLoadFromTemplate();
			
			if (t instanceof AbstractTableConfiguration) {
				TableConfiguration tAsTabConf = (TableConfiguration) t;
				
				if (tAsTabConf.hasAlias()) {
					tAsTabConf.setUsingManualDefinedAlias(true);
					tryToAddToBusyTableAliasName(tAsTabConf.getTableAlias());
				}
				
				addConfiguredTable((AbstractTableConfiguration) t);
				t.setRelatedSrcConf(item.getSrcConf());
			}
			
			t.setRelatedSrcConf(item.getSrcConf());
		}
		
		if (item.getSrcConf().hasAuxExtractTable()) {
			for (AuxExtractTable t : item.getSrcConf().getAuxExtractTable()) {
				t.tryToLoadFromTemplate();
				
				if (t.hasAlias()) {
					t.setUsingManualDefinedAlias(true);
					
					tryToAddToBusyTableAliasName(t.getTableAlias());
				}
			}
		}
		
		String code = "";
		
		List<String> alreadyIncludedTables = new ArrayList<>();
		
		if (utilities.listHasElement(item.getDstConf())) {
			for (DstConf dst : item.getDstConf()) {
				dst.tryToLoadFromTemplate();
				
				dst.tryToLoadSchemaInfo(item.getRelatedEtlSchemaObject());
				
				if (dst.hasAlias()) {
					dst.setUsingManualDefinedAlias(true);
					
					tryToAddToBusyTableAliasName(dst.getTableAlias());
				}
				
				addConfiguredTable(dst);
				
				dst.setParentConf(item);
				
				if (!alreadyIncludedTables.contains(dst.getTableName())) {
					alreadyIncludedTables.add(dst.getTableName());
					
					code = utilities.stringHasValue(code) ? code + "_and_" + dst.getTableName() : dst.getTableName();
				}
			}
		}
		
		code = utilities.stringHasValue(code) ? code : item.getSrcConf().getTableName();
		
		code = item.getSrcConf().getTableName() + "_to_" + code;
		
		item.setShortCode(code);
		
		code += "_on_" + this.generateProcessId()
		        + (item.hasParentItemConf() ? "_within_" + item.getParentItemConf().getShortCode() : "");
		
		item.setConfigCode(finalizeItemCodeGeneration(code));
		
		tryToLoadChildItemConf(item, testing);
	}
	
	private void tryToLoadChildItemConf(EtlItemConfiguration item, boolean testing) {
		if (item.hasChildItemConf()) {
			for (EtlItemConfiguration childItem : item.getChildItemConf()) {
				childItem.setParentItemConf(item);
				childItem.setRelatedEtlConfig(this);
				
				if (!utilities.stringHasValue(childItem.getRelatedParentDstConfName())) {
					if (utilities.arrayHasExactlyOneElement(item.getDstConf())) {
						childItem.setRelatedParentDstConfName(item.getDstConf().get(0).getName());
					} else {
						throw new ForbiddenOperationException(
						        "The relatedParentDstConfName was not defined for the conf " + item.getConfigCode());
					}
				}
				
				childItem.setRelatedParentDstConf(item.findDstConf(childItem.getRelatedParentDstConfName()));
				
				initItem(childItem, testing);
			}
		}
	}
	
	synchronized String finalizeItemCodeGeneration(String code) {
		if (this.generatedItemCodes == null)
			this.generatedItemCodes = new ArrayList<>();
		
		String newCode = code + "_001";
		
		int i = 1;
		
		while (this.generatedItemCodes.contains(newCode)) {
			newCode = code + "_" + utilities.garantirXCaracterOnNumber(++i, 3);
		}
		
		this.generatedItemCodes.add(newCode);
		
		return newCode;
	}
	
	private void addConfiguredTable(AbstractTableConfiguration tableConfiguration) {
		if (!this.configuredTables.contains(tableConfiguration)) {
			this.configuredTables.add(tableConfiguration);
		}
	}
	
	public void fullLoad() {
		if (this.fullLoaded)
			return;
		
		initLogger();
		
		try {
			for (EtlItemConfiguration conf : this.getEtlItemConfiguration()) {
				if (!conf.isFullLoaded()) {
					logDebug("PERFORMING FULL CONFIGURATION LOAD ON ETL '" + conf.getConfigCode() + "'");
					conf.fullLoad(null);
				}
				
				logDebug("THE FULL CONFIGURATION LOAD HAS DONE ON ETL '" + conf.getConfigCode() + "'");
			}
			
			this.fullLoaded = true;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T extends EtlDatabaseObject> EtlConfiguration loadFromJSON(String json, File srcFile)
	        throws ForbiddenOperationException {
		
		OpenConnection srcConn = null;
		OpenConnection dstConn = null;
		OpenConnection mainConn = null;
		
		try {
			Class<?>[] types = new Class<?>[1];
			
			types[0] = ParentTableImpl.class;
			
			EtlConfiguration etlConfiguration = new ObjectMapperProvider(types).getContext(EtlConfiguration.class)
			        .readValue(json, EtlConfiguration.class);
			
			if (!etlConfiguration.isDynamic()) {
				srcConn = etlConfiguration.openSrcConn();
				dstConn = etlConfiguration.tryOpenDstConn();
				mainConn = etlConfiguration.tryOpenMainConn();
				
				etlConfiguration.setConfigFilePath(srcFile.getAbsolutePath());
				
				etlConfiguration.setRelatedConfFile(srcFile);
				
				etlConfiguration.init(srcConn, dstConn, mainConn);
				
				srcConn.markAsSuccessifullyTerminated();
			}
			
			return etlConfiguration;
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		catch (JsonParseException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (JsonMappingException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (srcConn != null) {
				srcConn.finalizeConnection();
			}
			
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
			
			if (mainConn != null) {
				mainConn.finalizeConnection();
			}
		}
	}
	
	public EtlItemConfiguration findSyncEtlConfiguration(String configCode) {
		EtlItemConfiguration tableConfiguration = new EtlItemConfiguration();
		tableConfiguration.setConfigCode(configCode);
		
		return find(tableConfiguration);
	}
	
	public AbstractTableConfiguration findSyncTableConfigurationOnAllTables(String tableName) {
		AbstractTableConfiguration tableConfiguration = new GenericTableConfiguration();
		tableConfiguration.setTableName(tableName);
		
		return utilities.findOnList(this.allTables, tableConfiguration);
	}
	
	public EtlItemConfiguration find(EtlItemConfiguration config) {
		return utilities.findOnList(this.etlItemConfiguration, config);
	}
	
	public AbstractTableConfiguration find(AbstractTableConfiguration config) {
		return utilities.findOnList(this.configuredTables, config);
	}
	
	@JsonIgnore
	public String getDesignation() {
		return this.processType.name().toLowerCase();
	}
	
	public List<EtlOperationConfig> getOperations() {
		return operations;
	}
	
	public void setOperations(List<EtlOperationConfig> operations) {
		for (EtlOperationConfig operation : operations) {
			operation.setRelatedEtlConfig(this);
			
			if (operation.getChild() != null) {
				EtlOperationConfig child = operation.getChild();
				
				while (child != null) {
					child.setRelatedEtlConfig(this);
					
					child = child.getChild();
				}
			}
		}
		
		this.operations = operations;
	}
	
	public EtlOperationConfig findOperation(EtlOperationType operationType) {
		EtlOperationConfig toFind = EtlOperationConfig.fastCreate(operationType, this);
		
		for (EtlOperationConfig op : this.operations) {
			if (op.equals(toFind))
				return op;
			
			EtlOperationConfig child = op.getChild();
			
			while (child != null) {
				if (child.equals(toFind)) {
					return child;
				}
				
				child = child.getChild();
			}
		}
		
		throw new ForbiddenOperationException("THE OPERATION '" + operationType + "' WAS NOT FOUND!!!!");
	}
	
	@JsonIgnore
	public List<EtlOperationConfig> getOperationsAsList() {
		List<EtlOperationConfig> operationsAsList = new ArrayList<>();
		
		for (EtlOperationConfig op : this.operations) {
			operationsAsList.add(op);
			
			EtlOperationConfig child = op.getChild();
			
			while (child != null) {
				operationsAsList.add(child);
				
				child = child.getChild();
			}
		}
		
		return operationsAsList;
	}
	
	public void validate() throws ForbiddenOperationException {
		String errorMsg = "";
		int errNum = 0;
		
		if (this.isSupposedToHaveOriginAppCode()) {
			if (!utilities.stringHasValue(getOriginAppLocationCode()))
				errorMsg += ++errNum + ". You must specify value for 'originAppLocationCode' parameter \n";
		}
		
		if (!utilities.stringHasValue(getEtlRootDirectory()))
			errorMsg += ++errNum + ". You must specify value for 'etlRootDirectory' parameter\n";
		
		if (!this.isSupposedToHaveOriginAppCode()) {
			if (utilities.stringHasValue(getOriginAppLocationCode()))
				errorMsg += ++errNum + ". You cannot configure 'originAppLocationCode' parameter in [" + getProcessType()
				        + " configuration\n";
		}
		
		if (getProcessType() == null || !utilities.stringHasValue(getProcessType().name()))
			errorMsg += ++errNum + ". You must specify value for 'processType' parameter\n";
		
		if (!hasOperation()) {
			this.setOperations(utilities.parseToList(EtlOperationConfig.createDefaultOperation(this)));
		}
		
		if (this.getAutoIncrementHandlingType() != null && this.getAutoIncrementHandlingType().isAsSchemaDefined()) {
			if (this.getPrimaryKeyInitialIncrementValue() != null && this.getPrimaryKeyInitialIncrementValue() > 0) {
				errorMsg += ++errNum
				        + ". The 'autoIncrementHandlingType' is set to 'AS_SCHEMA_DEFINED' you must ommit the 'primaryKeyInitialIncrementValue' property or change it to 'IGNORE_SCHEMA_DEFINITION'. \n";
			}
		}
		
		for (EtlOperationConfig operation : this.getOperations()) {
			operation.validate();
			
			if (this.hasTestingItem()) {
				operation.setDoNotSaveOperationProgress(true);
			}
		}
		
		if (this.hasFinalizer()) {
			this.getFinalizer().loadFinalizer();
			
			if (this.getFinalizer().getFinalizerClazz() == null) {
				errorMsg += ++errNum + ". The Finalizer class [" + this.getFinalizer().getFinalizerFullClassName()
				        + "] cannot be found\n";
			}
			
			if (this.getFinalizer().getConnectionToUse().isMain() && !this.hasMainConnInfo()) {
				errorMsg += ++errNum
				        + ". The Finalizer 'connectionToUse' is set to 'mainConnInfo' but there is no mainConnInfo  \n";
			}
			
		}
		
		List<EtlOperationType> supportedOperations = null;
		
		if (isDetectMissingRecords()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDetectMissingRecordsProcess();
		} else if (isEtlProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInEtlProcess();
		} else if (isPojoGeneration()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInPojoGenerationProcess();
		} else if (isSourceSyncProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInSourceSyncProcess();
		} else if (isDataBaseMergeFromJSONProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDestinationSyncProcess();
		} else if (isDBReSyncProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDBReSyncProcess();
		} else if (isDBQuickExportProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDBQuickExportProcess();
		} else if (isDBQuickLoadProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDBQuickLoadProcess();
		} else if (isDataReconciliationProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDataReconciliationProcess();
		} else if (isDataBaseMergeFromSourceDBProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDataBasesMergeFromSourceDBProcess();
		} else if (isDBInconsistencyCheckProcess()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDBInconsistencyCheckProcess();
		} else if (isResolveProblems()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInResolveProblemsProcess();
		} else if (isDetectGapesOnDbTables()) {
			supportedOperations = EtlOperationConfig.getSupportedOperationsInDetectGapesOnDbTables();
		}
		
		if (supportedOperations != null) {
			for (EtlOperationType operationType : supportedOperations) {
				if (!isOperationConfigured(operationType) && !operationCanBeOmitted(supportedOperations, operationType))
					errorMsg += ++errNum + ". The operation '" + operationType + " is not configured\n";
			}
		}
		
		if (!hasSrcConnInfo()) {
			errorMsg += ++errNum + ". No Src conn were configured!";
		}
		
		if (utilities.stringHasValue(errorMsg)) {
			errorMsg = "There are errors on Configuration file " + this.relatedConfFile.getAbsolutePath() + "\n" + errorMsg;
			throw new ForbiddenOperationException(errorMsg);
		} else if (this.childConfig != null) {
			this.childConfig.validate();
		}
		
	}
	
	public boolean hasFinalizer() {
		return this.getFinalizer() != null;
	}
	
	public ProcessFinalizerConf getFinalizer() {
		return finalizer;
	}
	
	public void setFinalizer(ProcessFinalizerConf finalizer) {
		this.finalizer = finalizer;
	}
	
	private boolean hasOperation() {
		return utilities.listHasElement(this.getOperations());
	}
	
	private boolean isOperationConfigured(EtlOperationType operationType) {
		EtlOperationConfig operation = new EtlOperationConfig();
		operation.setOperationType(operationType);
		
		for (EtlOperationConfig op : this.getOperations()) {
			if (operation.equals(op))
				return true;
			
			EtlOperationConfig child = op.getChild();
			
			while (child != null) {
				if (operation.equals(child))
					return true;
				
				child = child.getChild();
			}
		}
		
		return false;
	}
	
	private boolean operationCanBeOmitted(List<EtlOperationType> supportedOperations, EtlOperationType operationType) {
		boolean ok = false;
		
		if (operationType.isEtl()) {
			for (EtlOperationType type : supportedOperations) {
				
				if (isOperationConfigured(type)) {
					ok = true;
					
					break;
				}
			}
		}
		
		return ok;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!(obj instanceof EtlConfiguration))
			return false;
		
		EtlConfiguration otherObj = (EtlConfiguration) obj;
		
		return this.getDesignation().equalsIgnoreCase(otherObj.getDesignation());
	}
	
	public boolean existsOnArray(List<EtlConfiguration> syncConfigs) {
		return utilities.findOnArray(syncConfigs, this) != null;
	}
	
	@JsonIgnore
	public File getPOJOCompiledFilesDirectory() {
		String packageDir = getEtlRootDirectory() + FileUtilities.getPathSeparator() + "pojo"
		        + FileUtilities.getPathSeparator();
		
		return new File(packageDir + "bin");
	}
	
	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		String packageDir = getEtlRootDirectory() + FileUtilities.getPathSeparator() + "pojo"
		        + FileUtilities.getPathSeparator();
		
		return new File(packageDir + FileUtilities.getPathSeparator() + "src");
	}
	
	@JsonIgnore
	public File getSqlScriptsDirectory() {
		String scriptsDir = getEtlRootDirectory() + FileUtilities.getPathSeparator() + "dump-scripts";
		
		return new File(scriptsDir);
	}
	
	public File getSrcSqlStartupScriptsDirectory() {
		String scriptsDir = getSqlScriptsDirectory() + FileUtilities.getPathSeparator() + "startup"
		        + FileUtilities.getPathSeparator() + "src";
		
		return new File(scriptsDir);
	}
	
	public File getDstSqlStartupScriptsDirectory() {
		String scriptsDir = getSqlScriptsDirectory() + FileUtilities.getPathSeparator() + "startup"
		        + FileUtilities.getPathSeparator() + "dst";
		
		return new File(scriptsDir);
	}
	
	public void refreshTables() {
		
		if (UUID.randomUUID() != null) {
			throw new ForbiddenOperationException("Please revier this mathod");
		}
		
		List<AbstractTableConfiguration> tablesConfigurations = new ArrayList<AbstractTableConfiguration>();
		
		for (AbstractTableConfiguration conf : this.allTables) {
			if (!conf.isDisabled()) {
				tablesConfigurations.add(conf);
				
				//Newly activated table
				if (this.find(conf) == null) {
					try {
						conf.fullLoad();
					}
					catch (DBException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		//this.etlConfiguration = tablesConfigurations;
	}
	
	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
	
	public String generateProcessId() {
		String controllerId = this.processType.name().toLowerCase();
		
		if (isSupposedToRunInOrigin() || isSupposedToHaveOriginAppCode()) {
			controllerId += "_on_" + getOriginAppLocationCode();
		}
		
		return controllerId + "_using_" + this.getConfigFileName();
	}
	
	@JsonIgnore
	public File getModuleRootDirectory() {
		return moduleRootDirectory;
	}
	
	public void setModuleRootDirectory(File moduleRootDirectory) {
		this.moduleRootDirectory = moduleRootDirectory;
	}
	
	@JsonIgnore
	public File getPojoPackageAsDirectory(DBConnectionInfo connInfo) {
		String pojoPackageDir = "";
		pojoPackageDir += getPOJOCompiledFilesDirectory().getAbsolutePath() + FileUtilities.getPathSeparator();
		
		pojoPackageDir += getPojoPackageRelativePath(connInfo).replaceAll("/",
		    Matcher.quoteReplacement(FileUtilities.getPathSeparator()));
		
		return new File(pojoPackageDir);
	}
	
	@JsonIgnore
	public String getPojoPackageRelativePath(DBConnectionInfo app) {
		String relativePathSeparator = "/";
		
		String pojoPackageDir = "";
		
		pojoPackageDir += "org" + relativePathSeparator;
		pojoPackageDir += "openmrs" + relativePathSeparator;
		pojoPackageDir += "module" + relativePathSeparator;
		pojoPackageDir += "epts" + relativePathSeparator;
		pojoPackageDir += "etl" + relativePathSeparator;
		pojoPackageDir += "model" + relativePathSeparator;
		pojoPackageDir += "pojo" + relativePathSeparator;
		
		pojoPackageDir += this.getPojoPackage(app) + relativePathSeparator;
		
		return pojoPackageDir;
	}
	
	public boolean isSupposedToHaveOriginAppCode() {
		return this.isSupposedToRunInOrigin() || this.isDBQuickMergeWithEntityGenerationDBProcess()
		        || this.isDBInconsistencyCheckProcess() || this.isDBQuickMergeWithDatabaseGenerationDBProcess()
		        || this.isEtlProcess() || this.isDetectMissingRecords() || this.isReEtlProcess();
	}
	
	public boolean isSupposedToRunInDestination() {
		return this.isDataBaseMergeFromJSONProcess() || this.isDBQuickLoadProcess() || this.isDataReconciliationProcess()
		        || this.isDataBaseMergeFromSourceDBProcess() || this.isResolveProblems()
		        || this.isDBQuickMergeWithEntityGenerationDBProcess() || this.isDBQuickMergeWithDatabaseGenerationDBProcess()
		        || this.isEtlProcess() || this.isReEtlProcess();
	}
	
	public boolean isSupposedToRunInOrigin() {
		return this.isSourceSyncProcess() || this.isDBReSyncProcess() || this.isDBQuickExportProcess()
		        || this.isDBInconsistencyCheckProcess();
	}
	
	public boolean isPerformedInTheSameDatabase() {
		return this.isResolveProblems() || this.isDBInconsistencyCheckProcess();
	}
	
	public boolean hasSrcConnInfo() {
		return getSrcConnInfo() != null;
	}
	
	public boolean hasDstConnInfo() {
		return getDstConnInfo() != null;
	}
	
	public boolean hasMainConnInfo() {
		return getMainConnInfo() != null;
	}
	
	public void finalizeAllApps() {
		if (hasSrcConnInfo())
			getSrcConnInfo().finalize();
		
		if (hasDstConnInfo())
			getDstConnInfo().finalize();
		
		if (hasMainConnInfo())
			getMainConnInfo().finalize();
	}
	
	@SuppressWarnings("rawtypes")
	public boolean hasDefinedParameter(String paramName) {
		if (this.hasConfiguredParams()) {
			if (this.getParams().containsKey(paramName)) {
				return true;
			}
		}
		
		String[] paramElements = paramName.split("\\.");
		
		Object paramObject = this;
		
		//Try to lookup for parameter inside the configuration fields
		for (String paramElement : paramElements) {
			
			String[] arrayParamElements = paramElement.split("\\[");
			
			String simpleParamName = arrayParamElements[0];
			
			try {
				if (paramObject instanceof List) {
					
					int pos = Integer.parseInt((arrayParamElements[1]).split("\\]")[0]);
					
					paramObject = ((List) paramObject).get(pos);
				} else {
					paramObject = utilities.getFieldValue(paramObject, simpleParamName);
				}
				
				return true;
				
			}
			catch (ForbiddenOperationException e) {
				return false;
			}
		}
		
		return false;
	}
	
	public boolean hasConfiguredParams() {
		return this.params != null && !this.params.isEmpty();
	}
	
	@SuppressWarnings("rawtypes")
	public Object getParamValue(String paramName) {
		if (hasConfiguredParams()) {
			if (this.getParams().containsKey(paramName)) {
				return this.getParams().get(paramName);
			}
		}
		
		String[] paramElements = paramName.split("\\.");
		
		Object paramObject = this;
		
		//Try to lookup for parameter inside the configuration fields
		for (String paramElement : paramElements) {
			
			String[] arrayParamElements = paramElement.split("\\[");
			
			String simpleParamName = arrayParamElements[0];
			
			try {
				if (paramObject instanceof List) {
					
					int pos = Integer.parseInt((arrayParamElements[1]).split("\\]")[0]);
					
					paramObject = ((List) paramObject).get(pos);
				} else {
					paramObject = utilities.getFieldValue(paramObject, simpleParamName);
				}
				
			}
			catch (ForbiddenOperationException e) {
				return null;
			}
		}
		
		return paramObject;
	}
	
	public String getClassPath() {
		return this.classPath;
	}
	
	public File getClassPathAsFile() {
		return null;
	}
	
	public void setClassPath(String retrieveClassPath) {
	}
	
	public TableConfiguration findTableInSrc(TableConfiguration tableConf, Connection srcConn) throws DBException {
		String srcSchema = getSrcConnInfo().determineSchema();
		
		if (!DBUtilities.isTableExists(srcSchema, tableConf.getTableName(), srcConn)) {
			throw new ForbiddenOperationException(
			        "The table " + tableConf.getTableName() + " does not exists on src schema " + srcSchema);
		}
		
		TableConfiguration fullLoadedTable = findOnFullLoadedTables(tableConf.getTableName(), srcSchema);
		
		if (fullLoadedTable != null) {
			return fullLoadedTable;
		} else {
			GenericTableConfiguration gt = new GenericTableConfiguration();
			
			gt.tryToGenerateTableAlias(this);
			
			gt.fullLoad(srcConn);
			
			return gt;
		}
	}
	
	@JsonIgnore
	public List<TableConfiguration> getFullLoadedTables() {
		return fullLoadedTables;
	}
	
	public void addToFullLoadedTables(TableConfiguration tableConfiguration) {
		if (getFullLoadedTables() == null)
			fullLoadedTables = new ArrayList<>();
		
		fullLoadedTables.add(tableConfiguration);
	}
	
	public TableConfiguration findOnFullLoadedTables(String tableName, String schema) {
		if (getFullLoadedTables() == null)
			return null;
		
		boolean done = false;
		
		//To avoid failure on ConcurrentModificationException
		while (!done) {
			
			try {
				for (TableConfiguration tab : this.getFullLoadedTables()) {
					if (tab.getSchema().equals(schema) && tab.getTableName().equals(tableName)) {
						return tab;
					}
				}
				
				done = true;
			}
			catch (ConcurrentModificationException e) {
				logWarn("ConcurrentModificationException found when finding on loaded table. The aplication will retry");
				TimeCountDown.sleep(2);
			}
		}
		return null;
	}
	
	private void tryToAddToBusyTableAliasName(String tableAlias) {
		if (this.busyTableAliasName == null) {
			this.busyTableAliasName = new ArrayList<>();
		}
		
		if (!this.busyTableAliasName.contains(tableAlias)) {
			this.busyTableAliasName.add(tableAlias);
		}
	}
	
	@Override
	public synchronized void generateAliasForTable(TableConfiguration tabConfig) {
		if (tabConfig.hasAlias())
			return;
		
		if (this.busyTableAliasName == null) {
			this.busyTableAliasName = new ArrayList<>();
		}
		
		int i = 1;
		
		String tableName = DBUtilities.extractTableNameFromFullTableName(tabConfig.getTableName());
		
		String generatedTableAlias = tableName + "_" + i;
		
		while (this.busyTableAliasName.contains(generatedTableAlias)) {
			generatedTableAlias = tableName + "_" + ++i;
		}
		
		this.busyTableAliasName.add(generatedTableAlias);
		
		tabConfig.setTableAlias(generatedTableAlias);
	}
	
	public OpenConnection tryOpenDstConn() throws DBException {
		try {
			return openDstConn();
		}
		catch (ForbiddenOperationException e) {
			return null;
		}
	}
	
	public OpenConnection tryOpenMainConn() throws DBException {
		try {
			return openMainConn();
		}
		catch (ForbiddenOperationException e) {
			return null;
		}
	}
	
	public OpenConnection openDstConn() throws DBException, ForbiddenOperationException {
		OpenConnection dstConn = null;
		
		if (hasDstConnInfo()) {
			dstConn = getDstConnInfo().openConnection();
			
			if (this.doNotResolveRelationship()) {
				DBUtilities.disableForegnKeyChecks(dstConn);
			}
			
		} else {
			throw new ForbiddenOperationException("No dst conn config defined!");
		}
		
		return dstConn;
	}
	
	public boolean doNotResolveRelationship() {
		return this.getRelationshipResolutionStrategy().skip();
	}
	
	public OpenConnection openMainConn() throws DBException, ForbiddenOperationException {
		OpenConnection mainConn = null;
		
		if (hasMainConnInfo()) {
			mainConn = getMainConnInfo().openConnection();
		} else {
			throw new ForbiddenOperationException("No main conn config defined!");
		}
		
		return mainConn;
	}
	
	public OpenConnection openSrcConn() throws DBException, ForbiddenOperationException {
		OpenConnection conn = getSrcConnInfo().openConnection();
		
		if (this.doNotResolveRelationship()) {
			DBUtilities.disableForegnKeyChecks(conn);
		}
		
		return conn;
	}
	
	@Override
	public EtlConfiguration getRelatedEtlConf() {
		return this.getParentEtlConf();
	}
	
	@Override
	public EtlDataConfiguration getParentConf() {
		return this.getParentEtlConf();
	}
	
	@Override
	public void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration) {
	}
	
	public EtlConfiguration cloneDynamic(EtlDatabaseObject schemaInfoSrc) {
		EtlConfiguration clonedEtlConf = new EtlConfiguration();
		
		clonedEtlConf.setEtlRootDirectory(tryToLoadPlaceHolders(this.getEtlRootDirectory(), schemaInfoSrc));
		
		clonedEtlConf.setOriginAppLocationCode(tryToLoadPlaceHolders(this.getOriginAppLocationCode(), schemaInfoSrc));
		
		clonedEtlConf.setEtlItemConfiguration(new ArrayList<>());
		
		if (this.hasTestingItem()) {
			clonedEtlConf.setTestingEtlItemConfiguration(this.getTestingEtlItemConfiguration());
		}
		
		clonedEtlConf.setMainConnInfo(this.getMainConnInfo());
		
		clonedEtlConf.setSrcConnInfo(new DBConnectionInfo());
		clonedEtlConf.getSrcConnInfo().copyFromOther(this.getSrcConnInfo());
		clonedEtlConf.getSrcConnInfo().tryToLoadPlaceHolders(schemaInfoSrc);
		
		clonedEtlConf.setDstConnInfo(new DBConnectionInfo());
		clonedEtlConf.getDstConnInfo().copyFromOther(this.getDstConnInfo());
		clonedEtlConf.getDstConnInfo().tryToLoadPlaceHolders(schemaInfoSrc);
		
		clonedEtlConf.setProcessType(this.getProcessType());
		clonedEtlConf.setRelatedConfFile(this.getRelatedConfFile());
		clonedEtlConf.setOperations(this.getOperations());
		clonedEtlConf.setManualStart(this.isManualStart());
		clonedEtlConf.setChildConfigFilePath(tryToLoadPlaceHolders(this.getChildConfigFilePath(), schemaInfoSrc));
		clonedEtlConf.setConfigFilePath(this.getChildConfigFilePath());
		clonedEtlConf.setDisabled(this.isDisabled());
		clonedEtlConf.setModuleRootDirectory(this.getModuleRootDirectory());
		clonedEtlConf.setSyncStageSchema(tryToLoadPlaceHolders(this.getSyncStageSchema(), schemaInfoSrc));
		clonedEtlConf.setFinalizer(this.getFinalizer());
		clonedEtlConf.setParams(tryToLoadPlaceHolders(getParams(), schemaInfoSrc));
		clonedEtlConf.setClassPath(this.getClassPath());
		clonedEtlConf.setDoNotTransformsPrimaryKeys(this.isDoNotTransformsPrimaryKeys());
		clonedEtlConf.setManualMapPrimaryKeyOnField(this.getManualMapPrimaryKeyOnField());
		clonedEtlConf.setWaitTimeToCheckStatus(this.getWaitTimeToCheckStatus());
		clonedEtlConf.setRelationshipResolutionStrategy(this.getRelationshipResolutionStrategy());
		clonedEtlConf.setPrimaryKeyInitialIncrementValue(this.getPrimaryKeyInitialIncrementValue());
		clonedEtlConf.setAutoIncrementHandlingType(this.getAutoIncrementHandlingType());
		
		OpenConnection srcConn = null;
		OpenConnection dstConn = null;
		OpenConnection mainConn = null;
		
		try {
			srcConn = clonedEtlConf.openSrcConn();
			dstConn = clonedEtlConf.tryOpenDstConn();
			mainConn = clonedEtlConf.tryOpenMainConn();
			
			for (EtlItemConfiguration item : this.getEtlItemConfiguration()) {
				
				EtlItemConfiguration cloned = new EtlItemConfiguration();
				
				cloned.copyFromOther(item, clonedEtlConf, true, srcConn);
				cloned.tryToReplacePlaceholders(schemaInfoSrc);
				
				if (item.getEtlItemSrcConf() != null) {
					cloned.setEtlItemSrcConf(new EtlItemSrcConf());
					cloned.getEtlItemSrcConf().copyFromOther(item.getEtlItemSrcConf(), null, cloned, srcConn);
				}
				
				clonedEtlConf.getEtlItemConfiguration().add(cloned);
			}
			
			clonedEtlConf.init(srcConn, dstConn, mainConn);
			
		}
		catch (DBException e) {
			throw new EtlExceptionImpl(e);
		}
		finally {
			if (srcConn != null) {
				srcConn.finalizeConnection();
			}
			
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
			
			if (mainConn != null) {
				mainConn.finalizeConnection();
			}
		}
		
		return clonedEtlConf;
	}
	
	private Map<String, String> tryToLoadPlaceHolders(Map<String, String> params, EtlDatabaseObject schemaInfoSrc) {
		if (params != null) {
			
			Map<String, String> newMap = new LinkedHashMap<>();
			
			for (Map.Entry<String, String> entry : params.entrySet()) {
				newMap.put(entry.getKey(), DBUtilities.tryToReplaceParamsInQuery(entry.getValue(), schemaInfoSrc));
			}
			
			return newMap;
		} else {
			return null;
		}
	}
	
	private String tryToLoadPlaceHolders(String str, EtlDatabaseObject schemaInfoSrc) {
		return DBUtilities.tryToReplaceParamsInQuery(str, schemaInfoSrc);
	}
	
	public String getRecordWithDefaultParentInfoTableName() {
		return "record_with_default_parents";
	}
	
	public String generateFullRecursiveInfoTableName() {
		return getSyncStageSchema() + "." + getRecordWithDefaultParentInfoTableName();
	}
	
	@Override
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
	}
	
	public boolean isReRunable() {
		return reRunable;
	}
	
	public void setReRunable(boolean reRunable) {
		this.reRunable = reRunable;
	}
	
	public boolean reRunable() {
		return isReRunable();
	}
	
	@Override
	public ActionOnEtlException getGeneralBehaviourOnEtlException() {
		return this.defaultExceptionBehavior;
	}
	
	public ActionOnEtlException getDefaultExceptionBehavior() {
		return defaultExceptionBehavior;
	}
	
	public void setDefaultExceptionBehavior(ActionOnEtlException defaultExceptionBehavior) {
		this.defaultExceptionBehavior = defaultExceptionBehavior;
	}
	
	public EtlInconsistencyBehavior getDefaultInconsistencyBehavior() {
		return defaultInconsistencyBehavior;
	}
	
	public void setDefaultInconsistencyBehavior(EtlInconsistencyBehavior defaultInconsistencyBehavior) {
		this.defaultInconsistencyBehavior = defaultInconsistencyBehavior;
	}
	
	@Override
	public void setTemplate(EtlTemplateInfo template) {
	}
	
	@Override
	public void copyFromTemplate(EtlDataConfiguration template) {
	}
	
	@Override
	public EtlTemplateInfo getTemplate() {
		return null;
	}
	
	public String getConfigFileName() {
		return FileUtilities.generateFileNameFromRealPathWithoutExtension(this.getConfigFilePath());
	}
	
	public String generateDatabaseSchemaFullPath(DBConnectionInfo dbConnConf) {
		if (!dbConnConf.hasDatabaseSchemaPath())
			throw new EtlExceptionImpl("No databaseSchemaPath was defined for thos dbConnConf");
		
		return getSqlScriptsDirectory() + FileUtilities.getPathSeparator() + dbConnConf.getDatabaseSchemaPath();
	}
	
	private void createStageSchema() throws DBException {
		OpenConnection conn = getSrcConnInfo().openConnection();
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				DBUtilities.createDb(getSrcConnInfo(), this.getSyncStageSchema());
			} else {
				BaseDAO.executeBatch(conn, "CREATE SCHEMA " + this.getSyncStageSchema());
			}
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private boolean isImportStageSchemaExists() throws DBException {
		OpenConnection conn = openSrcConn();
		
		try {
			return DBUtilities.isResourceExist(null, null, DBUtilities.RESOURCE_TYPE_SCHEMA, this.getSyncStageSchema(),
			    conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private boolean existRelatedRecursiveRecordInfoTable() {
		OpenConnection conn = null;
		
		try {
			String schema = this.getSyncStageSchema();
			String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
			String tabName = this.getRecordWithDefaultParentInfoTableName();
			
			conn = openSrcConn();
			
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null) {
				conn.finalizeConnection();
			}
			
		}
	}
	
	public boolean existInconsistenceInfoTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String schema = this.getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "inconsistence_info";
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	public boolean existsDefaultGeneratedObjectKeyTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String schema = this.getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = EtlConfiguration.DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME;
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	public boolean existsSkippedRecordsTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String schema = this.getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = EtlConfiguration.SKIPPED_RECORD_TABLE_NAME;
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	public boolean existOperationProgressInfoTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String schema = this.getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "table_operation_progress_info";
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	public boolean existEtlRecordErrorTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String schema = this.getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = EtlConfiguration.ETL_RECORD_ERROR_TABLE_NAME;
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	private void createTableOperationProgressInfo() throws DBException {
		
		EtlConfiguration config = this;
		
		OpenConnection conn = openSrcConn();
		
		try {
			String sql = "";
			
			sql += "CREATE TABLE " + config.getSyncStageSchema() + ".table_operation_progress_info (\n";
			sql += DBUtilities.generateTableAutoIncrementField("id", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("operation_id", 250, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("operation_name", 250, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("table_name", 100, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableDateTimeField("started_at", "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableDateTimeField("last_refresh_at", "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("min_record_id", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("max_record_id", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("total_records", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("total_processed_records", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("status", 50, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableTimeStampField("creation_date", conn) + ",\n";
			sql += DBUtilities.generateTableUniqueKeyDefinition(
			    config.getSyncStageSchema() + "_UNQ_OPERATION_ID".toLowerCase(), "operation_id", conn) + ",\n";
			sql += DBUtilities.generateTablePrimaryKeyDefinition("id", "table_operation_progress_info_pk", conn) + "\n";
			
			sql += ");\n";
			
			BaseDAO.executeBatch(conn, sql);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createSkippedRecordsTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String schema = this.getSyncStageSchema();
		
		String tableName = EtlConfiguration.SKIPPED_RECORD_TABLE_NAME;
		
		sql += "CREATE TABLE " + schema + "." + tableName + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 30, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("object_id", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_key".toLowerCase(), "table_name, object_id",
		    conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		try {
			BaseDAO.executeBatch(conn, sql);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createRecordWithDefaultParentInfoTable() {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConn();
			
			String tableName = this.getRecordWithDefaultParentInfoTableName();
			
			String sql = "";
			String notNullConstraint = "NOT NULL";
			String endLineMarker = ",\n";
			
			sql += "CREATE TABLE " + this.generateFullRecursiveInfoTableName() + "(\n";
			sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
			sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, notNullConstraint, conn)
			        + endLineMarker;
			sql += DBUtilities.generateTableVarcharField("src_table_name", 100, notNullConstraint, conn) + endLineMarker;
			sql += DBUtilities.generateTableVarcharField("dst_table_name", 100, notNullConstraint, conn) + endLineMarker;
			sql += DBUtilities.generateTableBigIntField("src_rec_id", notNullConstraint, conn) + endLineMarker;
			sql += DBUtilities.generateTableBigIntField("dst_rec_id", notNullConstraint, conn) + endLineMarker;
			sql += DBUtilities.generateTableVarcharField("parent_table", 50, notNullConstraint, conn) + endLineMarker;
			sql += DBUtilities.generateTableVarcharField("parent_field", 50, notNullConstraint, conn) + endLineMarker;
			sql += DBUtilities.generateTableBigIntField("src_parent_id", notNullConstraint, conn) + endLineMarker;
			sql += DBUtilities.generateTableNumericField("inconsistent_parent", 1, notNullConstraint, -1, conn)
			        + endLineMarker;
			sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
			
			sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_record_key".toLowerCase(),
			    "src_rec_id, parent_table, parent_field", conn) + endLineMarker;
			
			sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn);
			sql += ")";
			
			String indexName = tableName + "location_idx";
			String indexFields = "record_origin_location_code";
			
			String idxDefinition = DBUtilities.generateIndexDefinition(this.generateFullRecursiveInfoTableName(), indexName,
			    indexFields, conn);
			
			BaseDAO.executeBatch(conn, sql, idxDefinition);
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (DBException e) {
			throw new EtlExceptionImpl(e);
		}
		finally {
			if (conn != null) {
				conn.finalizeConnection();
			}
		}
	}
	
	private void createDefaultGeneratedObjectKeyTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String schema = this.getSyncStageSchema();
		
		String tableName = EtlConfiguration.DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME;
		
		sql += "CREATE TABLE " + schema + "." + tableName + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 30, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("column_name", 30, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("key_value", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_key".toLowerCase(), "table_name, column_name",
		    conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		try {
			BaseDAO.executeBatch(conn, sql);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createEtlRecordErrorTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String schema = this.getSyncStageSchema();
		
		String tableName = EtlConfiguration.ETL_RECORD_ERROR_TABLE_NAME;
		
		sql += "CREATE TABLE " + schema + "." + tableName + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("record_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 50, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("origin_location_code", 50, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("exception", 200, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("exception_description", 1000, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		String idxDefinition = DBUtilities.generateIndexDefinition(schema + "." + tableName,
		    tableName + "_idx".toLowerCase(), "table_name, origin_location_code", conn) + ";";
		
		try {
			BaseDAO.executeBatch(conn, sql, idxDefinition);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createInconsistenceInfoTable() throws DBException {
		OpenConnection conn = openSrcConn();
		
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String sql = "";
		
		sql += "CREATE TABLE " + this.getSyncStageSchema() + ".inconsistence_info (\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableIntegerField("record_id", 11, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("parent_table_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("parent_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("default_parent_id", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, notNullConstraint, conn)
		        + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", "inconsistence_info_pk", conn);
		sql += ");";
		
		try {
			BaseDAO.executeBatch(conn, sql);
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
}
