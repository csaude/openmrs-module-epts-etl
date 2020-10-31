package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ForbiddenException;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SyncConfiguration {
	private String syncRootDirectory;
	private String syncStageSchema;
	
	private String originAppLocationCode;
	
	private Map<String, SyncTableConfiguration> syncTableConfigurationPull;
	
	private List<SyncTableConfiguration> tablesConfigurations;
	
	private boolean firstExport;
	private DBConnectionInfo connInfo;
	
	private boolean mustCreateStageSchemaElements;
	
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
	
	private SyncConfiguration() {
		syncTableConfigurationPull = new HashMap<String, SyncTableConfiguration>();
	}
	
	public String getClassPath() {
		return classPath;
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
	
	public boolean isDestinationInstallationType() {
		return this.installationType.equals(supportedInstallationTypes[1]);
	}
	
	public boolean isSourceInstallationType() {
		return this.installationType.equals(supportedInstallationTypes[0]);
	}
	
	
	public boolean mustCreateStageSchemaElements() {
		return mustCreateStageSchemaElements;
	}
	
	public boolean isMustCreateStageSchemaElements() {
		return mustCreateStageSchemaElements;
	}
	
	public void setMustCreateStageSchemaElements(boolean mustCreateStageSchemaElements) {
		this.mustCreateStageSchemaElements = mustCreateStageSchemaElements;
	}
	
	public String getPojoPackage() {
		return isDestinationInstallationType() ? this.installationType : this.originAppLocationCode;
	}
	
	public DBConnectionInfo getConnInfo() {
		return connInfo;
	}
	
	public void setConnInfo(DBConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}
	
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
		return syncStageSchema;
	}
	
	public void setSyncStageSchema(String syncStageSchema) {
		this.syncStageSchema = syncStageSchema;
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}

	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}	
	
	static Logger logger = Logger.getLogger(SyncConfiguration.class);
	
	
	/*public SyncTableConfiguration retrieveTableInfoByTableName(String tableName, Connection conn) {
		for (SyncTableConfiguration info : this.tablesConfigurations) {
			logger.info("RETRIEVING TABLE INFO OF TABLE '" + tableName + "' ON CONFIGURATION [" + info + "]");
			
			if (info.getTableName().equals(tableName)) return info;
		}
		
		for (SyncTableConfiguration info : this.tablesConfigurations) {
			
			for (ParentRefInfo child : info.getChildRefInfo(conn)) {
				logger.info("RETRIEVING TABLE INFO OF TABLE '" + tableName + "' ON CHILD [" + child.getReferenceTableInfo().getTableName() + "] OF CONFIGURATION [" + info + "]");
				
				if (child.getReferenceTableInfo().getTableName().equals(tableName)) {
					if (child.getReferenceTableInfo().isFullLoaded()) {
						return child.getReferenceTableInfo();
					}
				}
			}
		}
		
		return null;
	}*/

	public void setRelatedConfFile(File relatedConfFile) {
		this.relatedConfFile = relatedConfFile;
	}
	
	public File getRelatedConfFile() {
		return relatedConfFile;
	}
	
	private boolean fullLoaded;
	public static SyncConfiguration loadFromFile(File file) throws IOException {
		SyncConfiguration conf = SyncConfiguration.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		
		conf.setRelatedConfFile(file);
		
		//addToClasspath(conf.getPOJOCompiledFilesDirectory());
		
		return conf;
	}

	public void fullLoad(Connection conn) {
		if (fullLoaded) return;
		
		for (SyncTableConfiguration conf : this.getTablesConfigurations()) {
			conf.fullLoad(conn);
		} 
		
		this.fullLoaded = true;
	}

	private static SyncConfiguration loadFromJSON (String json) {
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
	
	public SyncTableConfiguration find(SyncTableConfiguration tableConfiguration) {
		return utilities.findOnList(this.tablesConfigurations, tableConfiguration);
	}

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
	
	private SyncOperationConfig findOperation(String operationType) {
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
	
	public void validate() throws ForbiddenOperationException{
		String errorMsg = "";
		int errNum = 0;
		
		if (this.isSourceInstallationType()) {
			if (utilities.stringHasValue(getOriginAppLocationCode())) errorMsg += ++errNum + ". You must specify value for 'originAppLocationCode' parameter";
			if (utilities.stringHasValue(getSyncRootDirectory())) errorMsg += ++errNum + ". You must specify value for 'syncRootDirectory' parameter";
				
		
		}
		
		for (SyncOperationConfig operation : this.operations) {
			if (this.isDestinationInstallationType()) {
				if (!operation.canBeRunInDestinationInstallation()) errorMsg += ++errNum + ". This operation ["+ operation.getOperationType() + "] Cannot be configured in destination installation";
			}
			else {
				if (!operation.canBeRunInSourceInstallation()) errorMsg += ++errNum + ". This operation ["+ operation.getOperationType() + "] Cannot be configured in source installation";
			}
		}
		
		for (SyncTableConfiguration tableConf : this.tablesConfigurations) {
			if (tableConf.getParents() != null) {
				for (String parent : tableConf.getParents()) {
					if (findSyncTableConfiguration(parent) == null) errorMsg += ++errNum + ". The parent '" + parent + " of table " + tableConf.getTableName() + " is not configured\n";
				}
			}
		}
		
		if (utilities.stringHasValue(errorMsg)) throw new ForbiddenOperationException(errorMsg);
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

	public File getPOJOCompiledFilesDirectory() {
		return new File(getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo" + FileUtilities.getPathSeparator() + "bin");
	}

	public File getPOJOSourceFilesDirectory() {
		return new File(getSyncRootDirectory() + FileUtilities.getPathSeparator() + "pojo" + FileUtilities.getPathSeparator() + "src");
	}
}
