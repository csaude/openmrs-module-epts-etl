package org.openmrs.module.epts.etl.conf;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TableAliasesGenerator;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.EtlOperationType;
import org.openmrs.module.epts.etl.conf.types.EtlProcessType;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.ProcessFinalizer;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class EtlConfiguration extends AbstractBaseConfiguration implements TableAliasesGenerator {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static final String SKIPPED_RECORD_TABLE_NAME = "skipped_record";
	
	public static final String DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME = "default_generated_object_key";
	
	public static final String ETL_RECORD_ERROR_TABLE_NAME = "etl_record_error";
	
	private String etlRootDirectory;
	
	private String originAppLocationCode;
	
	private List<EtlItemConfiguration> etlItemConfiguration;
	
	private DBConnectionInfo srcConnInfo;
	
	private DBConnectionInfo dstConnInfo;
	
	private EtlProcessType processType;
	
	private File relatedConfFile;
	
	private List<EtlOperationConfig> operations;
	
	private List<AbstractTableConfiguration> configuredTables;
	
	//If true, all operations defined within this conf won't run on start. But may run if this sync configuration is nested to another configuration
	private boolean automaticStart;
	
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
	
	/**
	 * The finalizer class
	 */
	private String finalizerFullClassName;
	
	private Class<? extends ProcessFinalizer> finalizerClazz;
	
	private Map<String, Integer> qtyLoadedTables;
	
	private Map<String, String> params;
	
	private boolean initialized;
	
	private String classPath;
	
	private EtlConfigurationTableConf defaultGeneratedObjectKeyTabConf;
	
	private EtlConfigurationTableConf etlRecordErrorTabCof;
	
	private EtlConfigurationTableConf skippedRecordTabConf;
	
	private List<TableConfiguration> fullLoadedTables;
	
	private List<String> busyTableAliasName;
	
	private List<String> generatedItemCodes;
	
	/*
	 * Indicates if in this process the primary keys are transformed or not. If yes, the transformed records are given a new pk, if no, the pk is src is the same in dst
	 */
	private boolean doNotTransformsPrimaryKeys;
	
	public EtlConfiguration() {
		this.allTables = new ArrayList<AbstractTableConfiguration>();
		
		this.initialized = false;
		
		this.qtyLoadedTables = new HashMap<>();
		
		this.configuredTables = new ArrayList<>();
		
		this.busyTableAliasName = new ArrayList<>();
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
		String startDate = getParamValue("startDate");
		
		if (utilities.stringHasValue(startDate)) {
			return DateAndTimeUtilities.createDate(startDate);
		}
		
		return null;
	}
	
	public Date getEndDate() {
		String endDate = getParamValue("endDate");
		
		if (utilities.stringHasValue(endDate)) {
			return DateAndTimeUtilities.createDate(endDate);
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
	
	public boolean isAutomaticStart() {
		return automaticStart;
	}
	
	public void setAutomaticStart(boolean automaticStart) {
		this.automaticStart = automaticStart;
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
	
	public Class<? extends ProcessFinalizer> getFinalizerClazz() {
		return finalizerClazz;
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
		
		if (utilities.arrayHasElement(getEtlItemConfiguration())) {
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
				config.setRelatedSyncConfiguration(this);
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
	
	public static <T extends EtlDatabaseObject> EtlConfiguration loadFromFile(File file) throws IOException {
		EtlConfiguration conf = EtlConfiguration.loadFromJSON(FileUtilities.realAllFileAsString(file));
		
		conf.setConfigFilePath(file.getAbsolutePath());
		
		conf.setRelatedConfFile(file);
		
		return conf;
	}
	
	static final String STRING_LOCK = new String("LOCK_STRING");
	
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
	 */
	public void init() {
		if (initialized) {
			return;
		}
		
		synchronized (STRING_LOCK) {
			
			this.defaultGeneratedObjectKeyTabConf = new EtlConfigurationTableConf(
			        EtlConfiguration.DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME, this);
			this.defaultGeneratedObjectKeyTabConf.setSchema(getSyncStageSchema());
			
			this.skippedRecordTabConf = new EtlConfigurationTableConf(EtlConfiguration.SKIPPED_RECORD_TABLE_NAME, this);
			this.skippedRecordTabConf.setSchema(getSyncStageSchema());
			
			this.etlRecordErrorTabCof = new EtlConfigurationTableConf(EtlConfiguration.ETL_RECORD_ERROR_TABLE_NAME, this);
			this.etlRecordErrorTabCof.setSchema(getSyncStageSchema());
			
			for (EtlOperationConfig operation : this.getOperations()) {
				if (operation.getMaxSupportedEngines() == 1) {
					operation.setUseSharedConnectionPerThread(false);
				}
			}
			
			for (EtlItemConfiguration tc : this.etlItemConfiguration) {
				tc.setRelatedSyncConfiguration(this);
				tc.getSrcConf().setParentConf(tc);
				
				if (tc.getSrcConf().hasAlias()) {
					tc.getSrcConf().setUsingManualDefinedAlias(true);
					tryToAddToBusyTableAliasName(tc.getSrcConf().getTableAlias());
				}
				
				addConfiguredTable(tc.getSrcConf());
				
				List<EtlAdditionalDataSource> allAvaliableDataSources = tc.getSrcConf().getAvaliableExtraDataSource();
				
				for (EtlAdditionalDataSource t : allAvaliableDataSources) {
					if (t instanceof AbstractTableConfiguration) {
						TableConfiguration tAsTabConf = (TableConfiguration) t;
						
						if (tAsTabConf.hasAlias()) {
							tAsTabConf.setUsingManualDefinedAlias(true);
							tryToAddToBusyTableAliasName(tAsTabConf.getTableAlias());
						}
						
						addConfiguredTable((AbstractTableConfiguration) t);
						t.setRelatedSrcConf(tc.getSrcConf());
					}
					
					t.setRelatedSrcConf(tc.getSrcConf());
				}
				
				if (tc.getSrcConf().hasSelfJoinTables()) {
					for (AuxExtractTable t : tc.getSrcConf().getSelfJoinTables()) {
						if (t.hasAlias()) {
							t.setUsingManualDefinedAlias(true);
							
							tryToAddToBusyTableAliasName(t.getTableAlias());
						}
					}
				}
				
				String code = "";
				
				if (utilities.arrayHasElement(tc.getDstConf())) {
					for (DstConf dst : tc.getDstConf()) {
						
						if (dst.hasAlias()) {
							dst.setUsingManualDefinedAlias(true);
							
							tryToAddToBusyTableAliasName(dst.getTableAlias());
						}
						
						addConfiguredTable(dst);
						
						dst.setParentConf(tc);
						
						code = utilities.stringHasValue(code) ? code + "_and_" + dst.getTableName() : dst.getTableName();
					}
				}
				
				code = utilities.stringHasValue(code) ? code : tc.getSrcConf().getTableName();
				
				code = tc.getSrcConf().getTableName() + "_to_" + code;
				
				tc.setConfigCode(finalizeItemCodeGeneration(code));
			}
		}
	}
	
	synchronized String finalizeItemCodeGeneration(String code) {
		if (this.generatedItemCodes == null)
			this.generatedItemCodes = new ArrayList<>();
		
		String newCode = code;
		
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
					conf.fullLoad();
				}
				
				logDebug("THE FULL CONFIGURATION LOAD HAS DONE ON ETL '" + conf.getConfigCode() + "'");
			}
			
			this.fullLoaded = true;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void loadAllTables() {
		if (UUID.randomUUID() != null)
			throw new ForbiddenOperationException("Please review this method");
		
		OpenConnection conn = null;
		
		try {
			conn = getSrcConnInfo().openConnection();
			
			DatabaseMetaData dbmd = conn.getMetaData();
			String[] types = { "TABLE" };
			
			ResultSet rs = dbmd.getTables(conn.getCatalog(), null, "%", types);
			
			while (rs.next()) {
				
				/*AbstractTableConfiguration tab = AbstractTableConfiguration.init(rs.getString("TABLE_NAME"), this);
				
				if (tab.getTableName().startsWith("_"))
					continue;
				
				this.allTables.add(tab);*/
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
		
	}
	
	public static <T extends EtlDatabaseObject> EtlConfiguration loadFromJSON(String json) {
		try {
			Class<?>[] types = new Class<?>[1];
			
			types[0] = ParentTableImpl.class;
			
			EtlConfiguration etlConfiguration = new ObjectMapperProvider(types).getContext(EtlConfiguration.class)
			        .readValue(json, EtlConfiguration.class);
			
			etlConfiguration.init();
			
			return etlConfiguration;
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
		
		for (EtlOperationConfig operation : this.getOperations()) {
			operation.validate();
		}
		
		if (utilities.stringHasValue(this.getFinalizerFullClassName())) {
			loadFinalizer();
			
			if (this.finalizerClazz == null) {
				errorMsg += ++errNum + ". The Finalizer class [" + this.getFinalizerFullClassName() + "] cannot be found\n";
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
			errorMsg = "There are errors on dstConf file " + this.relatedConfFile.getAbsolutePath() + "\n" + errorMsg;
			throw new ForbiddenOperationException(errorMsg);
		} else if (this.childConfig != null) {
			this.childConfig.validate();
		}
		
	}
	
	private boolean hasOperation() {
		return utilities.arrayHasElement(this.getOperations());
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
	
	public String getFinalizerFullClassName() {
		return finalizerFullClassName;
	}
	
	public void setFinalizerFullClassName(String finalizerFullClassName) {
		this.finalizerFullClassName = finalizerFullClassName;
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ProcessFinalizer> void loadFinalizer() {
		
		try {
			ClassLoader loader = ProcessFinalizer.class.getClassLoader();
			
			Class<S> c = (Class<S>) loader.loadClass(this.getFinalizerFullClassName());
			
			this.finalizerClazz = (Class<S>) c;
		}
		catch (ClassNotFoundException e) {}
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
	
	public String generateControllerId() {
		String controllerId = this.processType.name().toLowerCase();
		
		if (isSupposedToRunInOrigin() || isSupposedToHaveOriginAppCode()) {
			controllerId += "_from_" + getOriginAppLocationCode();
		}
		
		return controllerId;
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
	
	public void finalizeAllApps() {
		if (hasSrcConnInfo())
			getSrcConnInfo().finalize();
		
		if (hasDstConnInfo())
			getDstConnInfo().finalize();
		
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
	public String getParamValue(String paramName) {
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
		
		return paramObject.toString();
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
		
		for (TableConfiguration tab : getFullLoadedTables()) {
			if (tab.getSchema().equals(schema) && tab.getTableName().equals(tableName)) {
				return tab;
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
	
	public OpenConnection openDstConn() throws DBException, ForbiddenOperationException {
		OpenConnection dstConn = null;
		
		if (hasDstConnInfo()) {
			dstConn = getDstConnInfo().openConnection();
			
		} else {
			throw new ForbiddenOperationException("No dst conn config defined!");
		}
		
		return dstConn;
	}
	
	public OpenConnection openSrcConn() throws DBException, ForbiddenOperationException {
		return getSrcConnInfo().openConnection();
	}
}
