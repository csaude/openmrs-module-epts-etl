package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SyncConfiguration {
	private String syncRootDirectory;
	
	private String originAppLocationCode;
	
	private Map<String, SyncTableConfiguration> syncTableConfigurationPull;
	
	private List<SyncTableConfiguration> tablesConfigurations;
	
	private boolean firstExport;
	private DBConnectionInfo connInfo;

	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String installationType;
	private File relatedConfFile;
	
	private List<SyncOperationConfig> operations;
	
	//If true, all operations defined within this conf won't run on start. But may run if this sync configuration is nested to another configuration
	private boolean automaticStart;
	
	private String childConfigFilePath;
	private SyncConfiguration childConfig;
	
	private boolean disabled;
	
	public static String PROCESSING_MODE_SEQUENCIAL="sequencial";
	public static String PROCESSING_MODE_PARALLEL="parallel";
	
	private static final String[] supportedInstallationTypes = {"source", "destination"};
	
	private String classPath;
	private File moduleRootDirectory;
	
	private boolean fullLoaded;
	private ProcessController relatedController;
	
	private List<SyncTableConfiguration> allTables;
	private DBConnectionService connService;

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
	public OpenConnection openConnetion() {
		if (connService == null) connService = DBConnectionService.init(this.getConnInfo());
		
		return connService.openConnection();
	}
	
	public String getClassPath() {
		return classPath;
	}
	
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
	
	public String getInstallationType() {
		return installationType;
	}
	
	public void setInstallationType(String installationType) {
		if (!utilities.isStringIn(installationType, supportedInstallationTypes)) {
			throw new ForbiddenException("The 'installationType' of syncConf file must be in "+supportedInstallationTypes);
		}
		this.installationType = installationType;
	}
	
	@JsonIgnore
	public boolean isDestinationInstallationType() {
		return this.installationType.equals(supportedInstallationTypes[1]);
	}
	
	@JsonIgnore
	public boolean isSourceInstallationType() {
		return this.installationType.equals(supportedInstallationTypes[0]);
	}
	
	@JsonIgnore
	public String getPojoPackage() {
		return isDestinationInstallationType() ? this.installationType : this.originAppLocationCode;
	}
	
	public DBConnectionInfo getConnInfo() {
		return connInfo;
	}
	
	public void setConnInfo(DBConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}
	
	@JsonIgnore
	public boolean isDoIntegrityCheckInTheEnd(String operationType) {
		SyncOperationConfig op = findOperation(operationType);
		
		return op.isDoIntegrityCheckInTheEnd();
	}
	
	public boolean isFirstExport() {
		return firstExport;
	}

	public void setFirstExport(boolean firstExport) {
		this.firstExport = firstExport;
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
		if (isSourceInstallationType()) {
			return this.originAppLocationCode + "_sync_stage_area";
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
		SyncConfiguration conf = SyncConfiguration.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		
		conf.setRelatedConfFile(file);
		
		//addToClasspath(conf.getPOJOCompiledFilesDirectory());
		
		return conf;
	}

	public void fullLoad() {
		if (this.fullLoaded) return;
		
		try {
			for (SyncTableConfiguration conf : this.getTablesConfigurations()) {
				if (!conf.isFullLoaded()) {
					logInfo("PERFORMING FULL CONFIGURATION LOAD ON TABLE '"  + conf.getTableName() + "'");
					conf.fullLoad();
				}
			
				logInfo("THE FULL CONFIGURATION LOAD HAS DONE ON TABLE '"  + conf.getTableName() + "'");
			} 

			this.fullLoaded = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void loadAllTables() {
		OpenConnection conn = openConnetion();
		
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
	
	public void logInfo(String msg) {
		utilities.logInfo(msg, logger);
	}
	
	public static SyncConfiguration loadFromJSON (String json) {
		try {
			SyncConfiguration config = new ObjectMapperProvider().getContext(SyncConfiguration.class).readValue(json, SyncConfiguration.class);
			
			if (config.getChildConfigFilePath() != null) {
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
	
	public SyncTableConfiguration find(SyncTableConfiguration tableConfiguration) {
		return utilities.findOnList(this.tablesConfigurations, tableConfiguration);
	}

	@JsonIgnore
	public String getDesignation() {
		return this.installationType + (utilities.stringHasValue(this.originAppLocationCode) ?  "_" + this.originAppLocationCode : "");
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
	
	public SyncOperationConfig findOperation(String operationType) {
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
		
		throw new ForbiddenOperationException("THE OPERATION '" + operationType.toUpperCase() + "' WAS NOT FOUND!!!!");
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
		
		if (this.isSourceInstallationType()) {
			if (!utilities.stringHasValue(getOriginAppLocationCode())) errorMsg += ++errNum + ". You must specify value for 'originAppLocationCode' parameter \n" ;
			if (!utilities.stringHasValue(getSyncRootDirectory())) errorMsg += ++errNum + ". You must specify value for 'syncRootDirectory' parameter\n";
		}
		
		if (this.isDestinationInstallationType()) {
			if (utilities.stringHasValue(getOriginAppLocationCode())) errorMsg += ++errNum + ". You cannot configure for 'originAppLocationCode' parameter in destination configuration\n" ;
		}
		
		for (SyncOperationConfig operation : this.operations) {
			operation.validate(); 
		}
		
		for (SyncTableConfiguration tableConf : this.tablesConfigurations) {
			if (tableConf.getParents() != null) {
				for (RefInfo parent : tableConf.getParents()) {
					//if (findSyncTableConfiguration(parent.getTableName()) == null) errorMsg += ++errNum + ". The parent '" + parent + " of table " + tableConf.getTableName() + " is not configured\n";
				}
			}
		}
		
		List<String> supportedOperations = isSourceInstallationType() ? SyncOperationConfig.getSupportedOperationsInSourceInstallation() : SyncOperationConfig.getSupportedOperationsInDestinationInstallation();
		
		for (String operationType : supportedOperations) {
			if (!isOperationConfigured(operationType)) errorMsg += ++errNum + ". The operation '" + operationType + " is not configured\n";
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
	
	private boolean isOperationConfigured(String operationType) {
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
		return new File(getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo" + FileUtilities.getPathSeparator() + "bin");
	}

	@JsonIgnore
	public File getPOJOSourceFilesDirectory() {
		return new File(getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo" + FileUtilities.getPathSeparator() + "src");
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

	public void tryToDetermineOriginAppLocationCode() throws DBException {
		OpenConnection conn = openConnetion();
		
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
	
	@JsonIgnore
	public File getModuleRootDirectory() {
		return moduleRootDirectory;
	}
	
	public void setModuleRootDirectory(File moduleRootDirectory) {
		this.moduleRootDirectory = moduleRootDirectory;
	}
	
	public File getPojoPackageAsDirectory() {
		String pojoPackageDir = "";
		pojoPackageDir += getPOJOCompiledFilesDirectory().getAbsolutePath() + FileUtilities.getPathSeparator();
		pojoPackageDir += getPojoPackageRelativePath();
		
		return new File(pojoPackageDir);
	}
	
	public String getPojoPackageRelativePath() {
		String pojoPackageDir = "";
		
		pojoPackageDir += "org" + FileUtilities.getPathSeparator();
		pojoPackageDir += "openmrs" + FileUtilities.getPathSeparator();
		pojoPackageDir += "module" + FileUtilities.getPathSeparator();
		pojoPackageDir += "eptssync" + FileUtilities.getPathSeparator();
		pojoPackageDir += "model" + FileUtilities.getPathSeparator();
		pojoPackageDir += "pojo" + FileUtilities.getPathSeparator();
		pojoPackageDir += this.getPojoPackage() + FileUtilities.getPathSeparator();
		
		return pojoPackageDir;
	}
}
