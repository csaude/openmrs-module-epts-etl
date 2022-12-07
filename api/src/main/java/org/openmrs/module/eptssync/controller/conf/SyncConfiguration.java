package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;

import javax.ws.rs.ForbiddenException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SyncConfiguration {
	private String syncRootDirectory;
	
	private String originAppLocationCode;
	private Date observationDate;
	private Map<String, SyncTableConfiguration> syncTableConfigurationPull;
	
	private List<SyncTableConfiguration> tablesConfigurations;
	
	private List<AppInfo> appsInfo;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private SyncProcessType processType;
	private File relatedConfFile;
	
	private List<SyncOperationConfig> operations;
	
	//If true, all operations defined within this conf won't run on start. But may run if this sync configuration is nested to another configuration
	private boolean automaticStart;
	
	private String childConfigFilePath;
	private SyncConfiguration childConfig;
	
	private boolean disabled;
	
	public static String PROCESSING_MODE_SEQUENCIAL="sequencial";
	public static String PROCESSING_MODE_PARALLEL="parallel";
	
	private String classPath;
	private File moduleRootDirectory;
	
	private boolean fullLoaded;
	private ProcessController relatedController;
	
	private List<SyncTableConfiguration> allTables;

	private static Log logger = LogFactory.getLog(SyncConfiguration.class);
	
	public SyncConfiguration() {
		syncTableConfigurationPull = new HashMap<String, SyncTableConfiguration>();
		this.allTables = new ArrayList<SyncTableConfiguration>();
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
	public AppInfo getMainApp() throws ForbiddenOperationException{
		AppInfo mainApp = find(AppInfo.init(AppInfo.MAIN_APP_CODE));
		
		if (mainApp == null) throw new ForbiddenOperationException("No main app found on configurations!");
	
		return mainApp;
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
	
	public Date getObservationDate() {
		return observationDate;
	}

	public void setObservationDate(Date observationDate) {
		this.observationDate = observationDate;
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
			throw new ForbiddenException("The 'processType' of syncConf file must be in "+SyncProcessType.values());
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
	public boolean isDBReSyncProcess() {
		return processType.isDBResync();
	}
	
	@JsonIgnore
	public boolean isDBQuickExportProcess() {
		return processType.isDBQuickExport();
	}

	@JsonIgnore
	public boolean isQuickMergeUniformeDBProcess() {
		return processType.isQuickMergeWithoutEntityGeneration();
	}
	
	@JsonIgnore
	public boolean isQuickMergeNonUniformeDBProcess() {
		return processType.isQuickMergeWithEntityGeneration();
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
		return processType.isResolveProblems();
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
	
	public List<SyncTableConfiguration> getTablesConfigurations() {
		return tablesConfigurations;
	}
	
	public String getSyncRootDirectory() {
		return syncRootDirectory;
	}

	public void setSyncRootDirectory(String syncRootDirectory) {
		this.syncRootDirectory = syncRootDirectory;
	}

	public void setTablesConfigurations(List<SyncTableConfiguration> tablesConfigurations) {
		if (tablesConfigurations != null) {
			for (SyncTableConfiguration config : tablesConfigurations) {
				config.setRelatedSyncTableInfoSource(this);
				
				addToTableConfigurationPull(config);
			}
		}
		
		this.tablesConfigurations = tablesConfigurations;
	}
	
	public void addToTableConfigurationPull(SyncTableConfiguration tableConfiguration) {
		syncTableConfigurationPull.put(tableConfiguration.getTableName(), tableConfiguration);
	}

	public SyncTableConfiguration findPulledTableConfiguration(String tableName) {
		return syncTableConfigurationPull.get(tableName);
	}
	
	public String getSyncStageSchema() {
		if (isSupposedToRunInOrigin()) {
			return this.originAppLocationCode + "_sync_stage_area";
		}
		if (isDBQuickLoadProcess() || isDataReconciliationProcess() || isDBQuickCopyProcess() || isDataBaseMergeFromSourceDBProcess()) {
			return "minimal_db_info";
		}
		else {
			return "sync_stage_area";
		}
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
		SyncConfiguration conf = SyncConfiguration.loadFromJSON(file, new String(Files.readAllBytes(file.toPath())));
		
		conf.setRelatedConfFile(file);
		
		return conf;
	}

	public void logDebug(String msg) {
		utilities.logDebug(msg, logger, determineLogLevel());
	}

	public void logInfo(String msg) {
		utilities.logInfo(msg, logger, determineLogLevel());
	}
	
	public void logWarn(String msg) {
		utilities.logWarn(msg, logger, determineLogLevel());
	}
		
	public void logErr(String msg) {
		utilities.logErr(msg, logger, determineLogLevel());
	}
	
	public void fullLoad() {
		if (this.fullLoaded) return;
		
		try {
			for (SyncTableConfiguration conf : this.getTablesConfigurations()) {
				if (!conf.isFullLoaded()) {
					logDebug("PERFORMING FULL CONFIGURATION LOAD ON TABLE '"  + conf.getTableName() + "'");
					conf.fullLoad();
				}
			
				logDebug("THE FULL CONFIGURATION LOAD HAS DONE ON TABLE '"  + conf.getTableName() + "'");
			} 

			this.fullLoaded = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void loadAllTables() {
		OpenConnection conn = getMainApp().openConnection();
		
        try {
			DatabaseMetaData dbmd = conn.getMetaData();
			String[] types = {"TABLE"};
			
			ResultSet rs = dbmd.getTables(conn.getCatalog(), null, "%", types);
			
			while (rs.next()) {
				SyncTableConfiguration tab = SyncTableConfiguration.init(rs.getString("TABLE_NAME"), this);
			
				if (tab.getTableName().startsWith("_")) continue;
				
				if (find(tab) == null) {
					tab.setDisabled(true);
				}
				
				this.allTables.add(tab);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
        finally {
			conn.finalizeConnection();
		}
		
	}
	
	public static SyncConfiguration loadFromJSON (File file, String json) {
		try {
			SyncConfiguration config = new ObjectMapperProvider().getContext(SyncConfiguration.class).readValue(json, SyncConfiguration.class);
			
			if (config.getChildConfigFilePath() != null) {
				config.logDebug("FOUND THE CHILD [" + config.getChildConfigFilePath()   + "] FOR [" + file.getAbsolutePath() + "]");
							
				config.setChildConfig(loadFromFile(new File(config.getChildConfigFilePath())));
			}
			
			return config;
		} catch (JsonParseException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} 
	}

	public SyncTableConfiguration findSyncTableConfiguration(String tableName) {
		SyncTableConfiguration tableConfiguration = new SyncTableConfiguration();
		tableConfiguration.setTableName(tableName);
		
		return find(tableConfiguration);
	}
	
	public SyncTableConfiguration findSyncTableConfigurationOnAllTables(String tableName) {
		SyncTableConfiguration tableConfiguration = new SyncTableConfiguration();
		tableConfiguration.setTableName(tableName);
		
		return utilities.findOnList(this.allTables, tableConfiguration);
	}
	
	public AppInfo find(AppInfo appToFind) {
		AppInfo app = utilities.findOnArray(this.appsInfo, appToFind);
		
		if (app == null) throw new ForbiddenOperationException("No configured app found with code [" +appToFind.getApplicationCode() + "]");
		
		return app;
	}
	
	public SyncTableConfiguration find(SyncTableConfiguration tableConfiguration) {
		return utilities.findOnList(this.tablesConfigurations, tableConfiguration);
	}

	@JsonIgnore
	public String getDesignation() {
		return this.processType.name().toLowerCase();
		//+ (utilities.stringHasValue(this.originAppLocationCode) ?  "_" + this.originAppLocationCode : "");
	}
	
	public List<SyncOperationConfig> getOperations() {
		return operations;
	}
	
	public void setOperations(List<SyncOperationConfig> operations) {
		for (SyncOperationConfig operation : operations) {
			operation.setRelatedSyncConfig(this);
			
			if (operation.getChild() != null) {
				SyncOperationConfig child = operation.getChild();
				
				while(child != null) {
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
			if (op.equals(toFind)) return op;
			
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
	public List<SyncOperationConfig> getOperationsAsList(){
		List<SyncOperationConfig>  operationsAsList = new ArrayList<SyncOperationConfig>();
		
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
	
	public void validate() throws ForbiddenOperationException{
		String errorMsg = "";
		int errNum = 0;
		
		if (this.isSupposedToHaveOriginAppCode()) {
			if (!utilities.stringHasValue(getOriginAppLocationCode())) errorMsg += ++errNum + ". You must specify value for 'originAppLocationCode' parameter \n" ;
		}
		
		if (!utilities.stringHasValue(getSyncRootDirectory())) errorMsg += ++errNum + ". You must specify value for 'syncRootDirectory' parameter\n";
			
		if (!this.isSupposedToHaveOriginAppCode()) {
			if (utilities.stringHasValue(getOriginAppLocationCode())) errorMsg += ++errNum + ". You cannot configure for 'originAppLocationCode' parameter in [" + getProcessType() + " configuration\n" ;
		}
		
		for (SyncOperationConfig operation : this.operations) {
			operation.validate(); 
		}
			
		List<SyncOperationType> supportedOperations = null;
		
		if (isSourceSyncProcess() ) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInSourceSyncProcess();
		}
		else
		if (isDataBaseMergeFromJSONProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDestinationSyncProcess();
		}
		else
		if (isDBReSyncProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBReSyncProcess();
		}
		else
		if (isDBQuickExportProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickExportProcess();
		}				
		else
		if (isDBQuickLoadProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickLoadProcess();
		}		
		else
		if (isDataReconciliationProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDataReconciliationProcess();
		}		
		else
		if (isDBQuickCopyProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBQuickCopyProcess();
		}
		else
		if (isDataBaseMergeFromSourceDBProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDataBasesMergeFromSourceDBProcess();
		}
		else
		if (isQuickMergeUniformeDBProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInQuickMergeUniformeDBProcess();
		}
		else
		if (isQuickMergeNonUniformeDBProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInQuickMergeNonUniformeDBProcess();
		}
		else
		if (isDBInconsistencyCheckProcess()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInDBInconsistencyCheckProcess();
		}
		else
		if (isResolveProblems()) {
			supportedOperations = SyncOperationConfig.getSupportedOperationsInResolveProblemsProcess();
		}
		
		if (supportedOperations != null) {
			for (SyncOperationType operationType : supportedOperations) {
				if (!isOperationConfigured(operationType)) errorMsg += ++errNum + ". The operation '" + operationType + " is not configured\n";
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
		}
		else
		if (this.childConfig != null){
			this.childConfig.validate();
		}
		
	}
	
	private boolean isOperationConfigured(SyncOperationType operationType) {
		SyncOperationConfig operation = new SyncOperationConfig();
		operation.setOperationType(operationType);
		
		
		for (SyncOperationConfig op : this.getOperations()) {
			if (operation.equals(op)) return true;
			
			SyncOperationConfig child = op.getChild();
			
			while(child != null) {
				if (operation.equals(child)) return true;
			
				child = child.getChild();
			}
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (!(obj instanceof SyncConfiguration)) return false;
		
		SyncConfiguration otherObj = (SyncConfiguration)obj;
		
		return this.getDesignation().equalsIgnoreCase(otherObj.getDesignation());
	}
	
	public boolean existsOnArray(List<SyncConfiguration> syncConfigs) {
		return utilities.findOnArray(syncConfigs, this) != null;
	}

	@JsonIgnore
	public File getPOJOCompiledFilesDirectory() {
		String packageDir = getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo" + FileUtilities.getPathSeparator() ;
		
		return new File(packageDir+ "bin");
	}

	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		String packageDir = getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo" + FileUtilities.getPathSeparator() ;
		
		return new File(packageDir + FileUtilities.getPathSeparator() + "src");
	}

	public void refreshTables() {
		List<SyncTableConfiguration> tablesConfigurations = new ArrayList<SyncTableConfiguration>();
		
		for (SyncTableConfiguration conf : this.allTables) {
			if (!conf.isDisabled()) {
				tablesConfigurations.add(conf);
				
				//Newly activated table
				if (this.find(conf) == null) {
					conf.fullLoad();
				}
			}
		}
		
		this.tablesConfigurations = tablesConfigurations;
	}
	
	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}

	public void tryToDetermineOriginAppL_ocationCode() throws DBException {
		OpenConnection conn = getMainApp().openConnection();
		
		String sql = " SELECT location.name as designacao, count(*) as value " +
					 " FROM visit INNER JOIN location on location.location_id = visit.location_id " +
					 " GROUP BY location.name ";
		
		List<SimpleValue> locations = BaseDAO.search(SimpleValue.class, sql, null, conn);
		
		SimpleValue locationWithMoreRecords = !locations.isEmpty() ? locations.get(0) : null;
		
		for (SimpleValue location : locations) {
			if (location.intValue() > locationWithMoreRecords.intValue()) {
				locationWithMoreRecords = location;
			}
		}
		
		if (locationWithMoreRecords != null) {
			this.setOriginAppLocationCode(utilities.replaceAllEmptySpace(locationWithMoreRecords.getDesignacao(), '_').toLowerCase());
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
		
		pojoPackageDir +=  getPojoPackageRelativePath(app).replaceAll("/", Matcher.quoteReplacement(FileUtilities.getPathSeparator()) );
		
		return new File(pojoPackageDir);
	}
	
	@JsonIgnore
	public String getPojoPackageRelativePath(AppInfo app) {
		String relativePathSeparator = "/";
		
		String pojoPackageDir = "";
		
		pojoPackageDir += "org" + relativePathSeparator;
		pojoPackageDir += "openmrs" + relativePathSeparator;
		pojoPackageDir += "module" + relativePathSeparator;
		pojoPackageDir += "eptssync" + relativePathSeparator;
		pojoPackageDir += "model" + relativePathSeparator;
		pojoPackageDir += "pojo" + relativePathSeparator;
	
		pojoPackageDir += this.getPojoPackage(app) + relativePathSeparator;
		
		return pojoPackageDir;
	}
	
	public List<AppInfo> exposeAllAppsNotMain(){
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
		return this.isSupposedToRunInOrigin() || this.isDBQuickCopyProcess() || this.isQuickMergeUniformeDBProcess() || this.isQuickMergeNonUniformeDBProcess() || this.isDBInconsistencyCheckProcess();
	}
	
	public boolean isSupposedToRunInDestination() {
		return this.isDataBaseMergeFromJSONProcess() || 
					this.isDBQuickLoadProcess() || 
						this.isDataReconciliationProcess() ||
							this.isDBQuickCopyProcess() ||
							this.isDataBaseMergeFromSourceDBProcess() ||
								this.isQuickMergeUniformeDBProcess() ||
									this.isResolveProblems() ||
										this.isQuickMergeNonUniformeDBProcess();
	}
	
	public boolean isSupposedToRunInOrigin() {
		return this.isSourceSyncProcess() || 
					this.isDBReSyncProcess() || 
						this.isDBQuickExportProcess() ||
							this.isDBInconsistencyCheckProcess();
	}
	
	
	public static Level determineLogLevel() {
		String log = System.getProperty("log.level");
		
		if (!utilities.stringHasValue(log)) return Level.INFO;
		
		if (log.equals("DEBUG")) return Level.FINE;
		if (log.equals("INFO")) return Level.INFO;
		if (log.equals("WARN")) return Level.WARNING;
		if (log.equals("ERROR")) return Level.SEVERE;
		
		throw new ForbiddenOperationException("Unsupported Log Level [" + log + "]");
		
	}

	public boolean isPerformedInTheSameDatabase() {
		return this.isResolveProblems() || 
					this.isDBInconsistencyCheckProcess();
	}
}
