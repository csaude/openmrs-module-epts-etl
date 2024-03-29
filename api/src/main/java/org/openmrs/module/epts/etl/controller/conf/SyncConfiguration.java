package org.openmrs.module.epts.etl.controller.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
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

import javax.ws.rs.ForbiddenException;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.ProcessFinalizer;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SyncConfiguration extends BaseConfiguration {
	
	private String syncRootDirectory;
	
	private String originAppLocationCode;
	
	private Map<String, SyncTableConfiguration> syncTableConfigurationPull;
	
	private List<EtlConfiguration> etlConfiguration;
	
	private List<AppInfo> appsInfo;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private SyncProcessType processType;
	
	private File relatedConfFile;
	
	private List<SyncOperationConfig> operations;
	
	private List<SyncTableConfiguration> configuredTables;
	
	//If true, all operations defined within this conf won't run on start. But may run if this sync configuration is nested to another configuration
	private boolean automaticStart;
	
	private String childConfigFilePath;
	
	private SyncConfiguration childConfig;
	
	private boolean disabled;
	
	public static String PROCESSING_MODE_SEQUENCIAL = "sequencial";
	
	public static String PROCESSING_MODE_PARALLEL = "parallel";
	
	private String classPath;
	
	private File moduleRootDirectory;
	
	private boolean fullLoaded;
	
	private ProcessController relatedController;
	
	private List<SyncTableConfiguration> allTables;
	
	private EptsEtlLogger logger;
	
	private ModelType modelType;
	
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
	
	public SyncConfiguration() {
		syncTableConfigurationPull = new HashMap<String, SyncTableConfiguration>();
		this.allTables = new ArrayList<SyncTableConfiguration>();
		
		this.initialized = false;
		
		this.qtyLoadedTables = new HashMap<>();
		
		this.configuredTables = new ArrayList<>();
	}
	
	public Map<String, String> getParams() {
		return params;
	}
	
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	public List<SyncTableConfiguration> getConfiguredTables() {
		return configuredTables;
	}
	
	public void setConfiguredTables(List<SyncTableConfiguration> configuredTables) {
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
	
	public ModelType getModelType() {
		return modelType;
	}
	
	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}
	
	@JsonIgnore
	public List<SyncTableConfiguration> getAllTables() {
		return allTables;
	}
	
	public void setAllTables(List<SyncTableConfiguration> allTables) {
		this.allTables = allTables;
	}
	
	public void setRelatedController(ProcessController relatedController) {
		this.relatedController = relatedController;
	}
	
	@JsonIgnore
	public ProcessController getRelatedController() {
		return relatedController;
	}
	
	@JsonIgnore
	public DBConnectionInfo getMainDBConnInfo() {
		return find(AppInfo.init(AppInfo.MAIN_APP_CODE)).getConnInfo();
	}
	
	@JsonIgnore
	public AppInfo getMainApp() throws ForbiddenOperationException {
		AppInfo mainApp = find(AppInfo.init(AppInfo.MAIN_APP_CODE));
		
		if (mainApp == null)
			throw new ForbiddenOperationException("No main app found on configurations!");
		
		return mainApp;
	}
	
	@JsonIgnore
	public boolean isOpenMRSModel() {
		return this.modelType.isOpenMRS();
	}
	
	public String getClassPath() {
		return classPath;
	}
	
	@JsonIgnore
	public File getClassPathAsFile() {
		return new File(classPath);
	}
	
	public void setClassPath(String classPath) {
		this.classPath = classPath;
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
	public SyncConfiguration getChildConfig() {
		return childConfig;
	}
	
	public void setChildConfig(SyncConfiguration childConfig) {
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
	
	public SyncProcessType getProcessType() {
		return processType;
	}
	
	public void setProcessType(SyncProcessType processType) {
		
		if (processType != null && !processType.isSupportedProcessType()) {
			throw new ForbiddenException("The 'processType' of syncConf file must be in " + SyncProcessType.values());
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
	public boolean isEtl() {
		return processType.isEtl();
	}
	
	@JsonIgnore
	public boolean isDBReSyncProcess() {
		return processType.isDBResync();
	}
	
	@JsonIgnore
	public boolean isDbCopy() {
		return this.processType.isDbCopy();
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
	public boolean isDBQuickMergeProcess() {
		return processType.isDBQuickMerge();
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
	public boolean isDBQuickCopyProcess() {
		return processType.isDBQuickCopy();
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
	public String getPojoPackage(AppInfo app) {
		return app.getPojoPackageName();
	}
	
	public List<AppInfo> getAppsInfo() {
		return appsInfo;
	}
	
	public void setAppsInfo(List<AppInfo> appsInfo) {
		this.appsInfo = appsInfo;
	}
	
	@JsonIgnore
	public boolean isDoIntegrityCheckInTheEnd(SyncOperationType operationType) {
		SyncOperationConfig op = findOperation(operationType);
		
		return op.isDoIntegrityCheckInTheEnd();
	}
	
	public List<EtlConfiguration> getEtlConfiguration() {
		return etlConfiguration;
	}
	
	public List<String> parseEtlConfigurationsToString_() {
		List<String> tableConfigurationsAsString = new ArrayList<>();
		
		if (utilities.arrayHasElement(getEtlConfiguration())) {
			for (EtlConfiguration tc : getEtlConfiguration()) {
				tableConfigurationsAsString.add(tc.getConfigCode());
			}
		}
		
		return tableConfigurationsAsString;
	}
	
	public String getSyncRootDirectory() {
		return syncRootDirectory;
	}
	
	public void setSyncRootDirectory(String syncRootDirectory) {
		this.syncRootDirectory = syncRootDirectory;
	}
	
	public String generateProcessStatusFolder() {
		String subFolder = "";
		
		if (this.isSupposedToRunInOrigin()) {
			subFolder = "source";
		} else if (this.isSupposedToRunInDestination()) {
			subFolder = "destination";
		}
		
		return this.getSyncRootDirectory() + FileUtilities.getPathSeparator() + "process_status"
		        + FileUtilities.getPathSeparator() + subFolder + FileUtilities.getPathSeparator() + this.getDesignation();
	}
	
	public void setEtlConfigurations(List<EtlConfiguration> etlConfiguration) {
		if (etlConfiguration != null) {
			for (EtlConfiguration config : etlConfiguration) {
				config.setRelatedSyncConfiguration(this);
				
				addToTableConfigurationPull(config.getMainSrcTableConf());
				
				if (config.getSrcConf().getAdditionalExtractionInfo() != null
				        && config.getSrcConf().getAdditionalExtractionInfo().getAdditionalExtractionTables() != null) {
					
					for (AdditionlExtractionSrcTable t : config.getSrcConf().getAdditionalExtractionInfo()
					        .getAdditionalExtractionTables()) {
						addToTableConfigurationPull(t);
					}
					
				}
			}
		}
		
		this.etlConfiguration = etlConfiguration;
	}
	
	public void addToTableConfigurationPull(SyncTableConfiguration tableConfiguration) {
		syncTableConfigurationPull.put(tableConfiguration.getTableName(), tableConfiguration);
	}
	
	public SyncTableConfiguration findPulledTableConfiguration(String tableName) {
		return syncTableConfigurationPull.get(tableName);
	}
	
	public String getSyncStageSchema() {
		String schema;
		
		if (utilities.stringHasValue(this.syncStageSchema)) {
			schema = this.syncStageSchema;
		} else if (isSupposedToRunInOrigin()) {
			schema = this.originAppLocationCode + "_sync_stage_area";
		} else if (isDBQuickLoadProcess() || isDataReconciliationProcess() || isDBQuickCopyProcess()
		        || isDataBaseMergeFromSourceDBProcess()) {
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
	
	public static SyncConfiguration loadFromFile(File file) throws IOException {
		
		SyncConfiguration conf;
		InputStream b = null;
		
		try {
			
			b = Files.newInputStream(file.toPath());
			
			conf = SyncConfiguration.loadFromJSON(new String(IOUtils.toByteArray(b)));
			
			conf.setRelatedConfFile(file);
		}
		finally {
			if (b != null) {
				try {
					b.close();
				}
				catch (IOException e) {}
			}
		}
		
		return conf;
	}
	
	static final String STRING_LOCK = new String("LOCK_STRING");
	
	void initLogger() {
		if (this.logger != null)
			return;
		
		synchronized (STRING_LOCK) {
			
			if (this.logger != null)
				return;
			
			this.logger = new EptsEtlLogger(SyncConfiguration.class);
		}
		
	}
	
	public void logDebug(String msg) {
		if (logger == null)
			initLogger();
		
		this.logger.debug(msg);
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
			for (EtlConfiguration tc : this.etlConfiguration) {
				tc.setRelatedSyncConfiguration(this);
				tc.getMainSrcTableConf().setParent(tc.getSrcConf());
				
				addConfiguredTable(tc.getMainSrcTableConf());
				addToTableConfigurationPull(tc.getMainSrcTableConf());
				
				if (tc.getSrcConf().getAdditionalExtractionInfo() != null) {
					
					tc.getSrcConf().getAdditionalExtractionInfo().setParent(tc.getSrcConf());
					
					for (AdditionlExtractionSrcTable t : tc.getSrcConf().getAdditionalExtractionInfo()
					        .getAdditionalExtractionTables()) {
						
						addConfiguredTable(t);
						addToTableConfigurationPull(t);
						t.setParent(tc.getSrcConf());
					}
				}
				
				String code = "";
				
				if (utilities.arrayHasElement(tc.getDstConf())) {
					for (DstConf dst : tc.getDstConf()) {
						addConfiguredTable(dst.getDstTableConf());
						
						addToTableConfigurationPull(dst.getDstTableConf());
						
						dst.getDstTableConf().setParent(dst);
						
						code = utilities.stringHasValue(code) ? "_and_" + dst.getDstTableConf().getTableName()
						        : dst.getDstTableConf().getTableName();
					}
				}
				
				code = utilities.stringHasValue(code) ? code : tc.getMainSrcTableConf().getTableName();
				
				code = tc.getMainSrcTableConf().getTableName() + "_to_" + code;
				
				tc.setConfigCode(code);
			}
		}
	}
	
	public boolean supportMultipleDestination() {
		return this.isEtl();
	}
	
	private void addConfiguredTable(SyncTableConfiguration tableConfiguration) {
		if (!this.configuredTables.contains(tableConfiguration)) {
			this.configuredTables.add(tableConfiguration);
		}
	}
	
	public void fullLoad() {
		if (this.fullLoaded)
			return;
		
		initLogger();
		
		try {
			for (EtlConfiguration conf : this.getEtlConfiguration()) {
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
		
		OpenConnection conn = getMainApp().openConnection();
		
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			String[] types = { "TABLE" };
			
			ResultSet rs = dbmd.getTables(conn.getCatalog(), null, "%", types);
			
			while (rs.next()) {
				
				/*SyncTableConfiguration tab = SyncTableConfiguration.init(rs.getString("TABLE_NAME"), this);
				
				if (tab.getTableName().startsWith("_"))
					continue;
				
				this.allTables.add(tab);*/
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
		
	}
	
	public static SyncConfiguration loadFromJSON(String json) {
		try {
			SyncConfiguration syncConfiguration = new ObjectMapperProvider().getContext(SyncConfiguration.class)
			        .readValue(json, SyncConfiguration.class);
			
			syncConfiguration.init();
			
			return syncConfiguration;
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
	
	public EtlConfiguration findSyncEtlConfiguration(String configCode) {
		EtlConfiguration tableConfiguration = new EtlConfiguration();
		tableConfiguration.setConfigCode(configCode);
		
		return find(tableConfiguration);
	}
	
	public SyncTableConfiguration findSyncTableConfigurationOnAllTables(String tableName) {
		SyncTableConfiguration tableConfiguration = new SyncTableConfiguration();
		tableConfiguration.setTableName(tableName);
		
		return utilities.findOnList(this.allTables, tableConfiguration);
	}
	
	public AppInfo find(AppInfo appToFind) throws ForbiddenOperationException {
		AppInfo app = utilities.findOnArray(this.appsInfo, appToFind);
		
		if (app == null)
			throw new ForbiddenOperationException(
			        "No configured app found with code [" + appToFind.getApplicationCode() + "]");
		
		return app;
	}
	
	public EtlConfiguration find(EtlConfiguration config) {
		return utilities.findOnList(this.etlConfiguration, config);
	}
	
	public SyncTableConfiguration find(SyncTableConfiguration config) {
		return utilities.findOnList(this.configuredTables, config);
	}
	
	@JsonIgnore
	public String getDesignation() {
		return this.processType.name().toLowerCase();
	}
	
	public List<SyncOperationConfig> getOperations() {
		return operations;
	}
	
	public void setOperations(List<SyncOperationConfig> operations) {
		for (SyncOperationConfig operation : operations) {
			operation.setRelatedSyncConfig(this);
			
			if (operation.getChild() != null) {
				SyncOperationConfig child = operation.getChild();
				
				while (child != null) {
					child.setRelatedSyncConfig(this);
					
					child = child.getChild();
				}
			}
		}
		
		this.operations = operations;
	}
	
	public SyncOperationConfig findOperation(SyncOperationType operationType) {
		SyncOperationConfig toFind = SyncOperationConfig.fastCreate(operationType);
		
		for (SyncOperationConfig op : this.operations) {
			if (op.equals(toFind))
				return op;
			
			SyncOperationConfig child = op.getChild();
			
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
	public List<SyncOperationConfig> getOperationsAsList() {
		List<SyncOperationConfig> operationsAsList = new ArrayList<SyncOperationConfig>();
		
		for (SyncOperationConfig op : this.operations) {
			operationsAsList.add(op);
			
			SyncOperationConfig child = op.getChild();
			
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
		
		if (!utilities.stringHasValue(getSyncRootDirectory()))
			errorMsg += ++errNum + ". You must specify value for 'syncRootDirectory' parameter\n";
		
		if (!this.isSupposedToHaveOriginAppCode()) {
			if (utilities.stringHasValue(getOriginAppLocationCode()))
				errorMsg += ++errNum + ". You cannot configure 'originAppLocationCode' parameter in [" + getProcessType()
				        + " configuration\n";
		}
		
		if (getProcessType() == null || !utilities.stringHasValue(getProcessType().name()))
			errorMsg += ++errNum + ". You must specify value for 'processType' parameter\n";
		
		if (getModelType() == null || !utilities.stringHasValue(getModelType().name()))
			errorMsg += ++errNum + ". You must specify value for 'modelType' parameter\n";
		
		for (SyncOperationConfig operation : this.operations) {
			operation.validate();
		}
		
		if (utilities.stringHasValue(this.getFinalizerFullClassName())) {
			loadFinalizer();
			
			if (this.finalizerClazz == null) {
				errorMsg += ++errNum + ". The Finalizer class [" + this.getFinalizerFullClassName() + "] cannot be found\n";
			}
		}
		
		if (!supportMultipleDestination()) {
			for (EtlConfiguration config : this.getEtlConfiguration()) {
				if (utilities.arrayHasMoreThanOneElements(config.getDstConf())) {
					errorMsg += ++errNum + ". The config for source " + config.getMainSrcTableConf().getTableName()
					        + " has multiple destination \n";
				}
			}
		}
		
		List<SyncOperationType> supportedOperations = null;
		
		if (isEtl()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInEtlProcess();
		} else if (isPojoGeneration()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInPojoGenerationProcess();
		} else if (isSourceSyncProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInSourceSyncProcess();
		} else if (isDataBaseMergeFromJSONProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDestinationSyncProcess();
		} else if (isDBReSyncProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBReSyncProcess();
		} else if (isDBQuickExportProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickExportProcess();
		} else if (isDBQuickLoadProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickLoadProcess();
		} else if (isDataReconciliationProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDataReconciliationProcess();
		} else if (isDBQuickCopyProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickCopyProcess();
		} else if (isDataBaseMergeFromSourceDBProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDataBasesMergeFromSourceDBProcess();
		} else if (isDBQuickMergeProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickMergeProcess();
		} else if (isDBQuickMergeWithEntityGenerationDBProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickMergeWithEntityGenerationProcess();
		} else if (isDBQuickMergeWithDatabaseGenerationDBProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickMergeWithDatabaseGenerationProcess();
		} else if (isDBInconsistencyCheckProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBInconsistencyCheckProcess();
		} else if (isResolveProblems()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInResolveProblemsProcess();
		} else if (isDbCopy()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDbCopyProcess();
		} else if (isDetectGapesOnDbTables()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDetectGapesOnDbTables();
		}
		
		if (supportedOperations != null) {
			for (SyncOperationType operationType : supportedOperations) {
				if (!isOperationConfigured(operationType))
					errorMsg += ++errNum + ". The operation '" + operationType + " is not configured\n";
			}
		}
		
		try {
			getMainApp();
		}
		catch (ForbiddenOperationException e) {
			errorMsg += ++errNum + ". No main app were configured!";
		}
		
		if (utilities.stringHasValue(errorMsg)) {
			errorMsg = "There are errors on config file " + this.relatedConfFile.getAbsolutePath() + "\n" + errorMsg;
			throw new ForbiddenOperationException(errorMsg);
		} else if (this.childConfig != null) {
			this.childConfig.validate();
		}
		
	}
	
	public String getFinalizerFullClassName() {
		return finalizerFullClassName;
	}
	
	public void setFinalizerFullClassName(String finalizerFullClassName) {
		this.finalizerFullClassName = finalizerFullClassName;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ProcessFinalizer> void loadFinalizer() {
		
		try {
			URL[] classPaths = new URL[] { getClassPathAsFile().toURI().toURL() };
			
			URLClassLoader loader = URLClassLoader.newInstance(classPaths);
			
			Class<T> c = (Class<T>) loader.loadClass(this.getFinalizerFullClassName());
			
			loader.close();
			
			this.finalizerClazz = (Class<T>) c;
		}
		catch (ClassNotFoundException e) {}
		catch (IOException e) {}
	}
	
	private boolean isOperationConfigured(SyncOperationType operationType) {
		SyncOperationConfig operation = new SyncOperationConfig();
		operation.setOperationType(operationType);
		
		for (SyncOperationConfig op : this.getOperations()) {
			if (operation.equals(op))
				return true;
			
			SyncOperationConfig child = op.getChild();
			
			while (child != null) {
				if (operation.equals(child))
					return true;
				
				child = child.getChild();
			}
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!(obj instanceof SyncConfiguration))
			return false;
		
		SyncConfiguration otherObj = (SyncConfiguration) obj;
		
		return this.getDesignation().equalsIgnoreCase(otherObj.getDesignation());
	}
	
	public boolean existsOnArray(List<SyncConfiguration> syncConfigs) {
		return utilities.findOnArray(syncConfigs, this) != null;
	}
	
	@JsonIgnore
	public File getPOJOCompiledFilesDirectory() {
		String packageDir = getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo"
		        + FileUtilities.getPathSeparator();
		
		return new File(packageDir + "bin");
	}
	
	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		String packageDir = getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo"
		        + FileUtilities.getPathSeparator();
		
		return new File(packageDir + FileUtilities.getPathSeparator() + "src");
	}
	
	@JsonIgnore
	public File getSqlScriptsDirectory() {
		String scriptsDir = getSyncRootDirectory() + FileUtilities.getPathSeparator() + "sql-scripts";
		
		return new File(scriptsDir);
	}
	
	public void refreshTables() {
		
		if (UUID.randomUUID() != null) {
			throw new ForbiddenOperationException("Please revier this mathod");
		}
		
		List<SyncTableConfiguration> tablesConfigurations = new ArrayList<SyncTableConfiguration>();
		
		for (SyncTableConfiguration conf : this.allTables) {
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
	
	public void tryToDetermineOriginAppL_ocationCode() throws DBException {
		OpenConnection conn = getMainApp().openConnection();
		
		String sql = " SELECT location.name as designacao, count(*) as value "
		        + " FROM visit INNER JOIN location on location.location_id = visit.location_id "
		        + " GROUP BY location.name ";
		
		List<SimpleValue> locations = BaseDAO.search(SimpleValue.class, sql, null, conn);
		
		SimpleValue locationWithMoreRecords = !locations.isEmpty() ? locations.get(0) : null;
		
		for (SimpleValue location : locations) {
			if (location.intValue() > locationWithMoreRecords.intValue()) {
				locationWithMoreRecords = location;
			}
		}
		
		if (locationWithMoreRecords != null) {
			this.setOriginAppLocationCode(
			    utilities.replaceAllEmptySpace(locationWithMoreRecords.getDesignacao(), '_').toLowerCase());
		}
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
	public File getPojoPackageAsDirectory(AppInfo app) {
		String pojoPackageDir = "";
		pojoPackageDir += getPOJOCompiledFilesDirectory().getAbsolutePath() + FileUtilities.getPathSeparator();
		
		pojoPackageDir += getPojoPackageRelativePath(app).replaceAll("/",
		    Matcher.quoteReplacement(FileUtilities.getPathSeparator()));
		
		return new File(pojoPackageDir);
	}
	
	@JsonIgnore
	public String getPojoPackageRelativePath(AppInfo app) {
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
	
	public List<AppInfo> exposeAllAppsNotMain() {
		List<AppInfo> apps = new ArrayList<AppInfo>();
		
		AppInfo mainApp = AppInfo.init(AppInfo.MAIN_APP_CODE);
		
		for (AppInfo app : this.appsInfo) {
			if (!app.equals(mainApp)) {
				apps.add(app);
			}
		}
		
		return apps;
	}
	
	public boolean isSupposedToHaveOriginAppCode() {
		return this.isSupposedToRunInOrigin() || this.isDBQuickCopyProcess() || this.isDBQuickMergeProcess()
		        || this.isDBQuickMergeWithEntityGenerationDBProcess() || this.isDBInconsistencyCheckProcess()
		        || this.isDBQuickMergeWithDatabaseGenerationDBProcess();
	}
	
	public boolean isSupposedToRunInDestination() {
		return this.isDataBaseMergeFromJSONProcess() || this.isDBQuickLoadProcess() || this.isDataReconciliationProcess()
		        || this.isDBQuickCopyProcess() || this.isDataBaseMergeFromSourceDBProcess() || this.isDBQuickMergeProcess()
		        || this.isResolveProblems() || this.isDBQuickMergeWithEntityGenerationDBProcess()
		        || this.isDBQuickMergeWithDatabaseGenerationDBProcess();
	}
	
	public boolean isSupposedToRunInOrigin() {
		return this.isSourceSyncProcess() || this.isDBReSyncProcess() || this.isDBQuickExportProcess()
		        || this.isDBInconsistencyCheckProcess();
	}
	
	public boolean isPerformedInTheSameDatabase() {
		return this.isResolveProblems() || this.isDBInconsistencyCheckProcess();
	}
	
	public void finalizeAllApps() {
		for (AppInfo app : getAppsInfo()) {
			app.finalize();
		}
	}
	
	public String getParamValue(String paramName) {
		if (this.params != null) {
			return this.params.get(paramName);
		}
		
		return null;
	}
}
