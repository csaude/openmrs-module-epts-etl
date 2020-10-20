package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.List;

import javax.ws.rs.ForbiddenException;

import org.openmrs.module.eptssync.Main;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SyncConfiguration {
	private String syncRootDirectory;
	private String syncStageSchema;
	
	private String originAppLocationCode;
	
	private List<SyncTableConfiguration> tablesConfigurations;
	
	private boolean firstExport;
	private boolean mustCreateClasses;
	private boolean mustRecompileTable;
	private DBConnectionInfo connInfo;
	
	private String classpackage;
	private boolean mustCreateStageSchemaElements;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String installationType;
	private File relatedConfFile;

	private List<SyncOperationConfig> operations;
	
	//If true, all operations defined within this conf won't run on start. But may run if this sync configuration is nested to another configuration
	private boolean automaticStart;
	
	private String childConfigFilePath;
	private SyncConfiguration childConfig;
	
	public static String PROCESSING_MODE_SEQUENCIAL="sequencial";
	public static String PROCESSING_MODE_PARALLEL="parallel";
	
	private static final String[] supportedInstallationTypes = {"source", "destination"};
	
	private SyncConfiguration() {
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
	
	public boolean mustCreateStageSchemaElements() {
		return mustCreateStageSchemaElements;
	}
	
	public boolean isMustCreateStageSchemaElements() {
		return mustCreateStageSchemaElements;
	}
	
	public void setMustCreateStageSchemaElements(boolean mustCreateStageSchemaElements) {
		this.mustCreateStageSchemaElements = mustCreateStageSchemaElements;
	}
	
	public String getClasspackage() {
		return classpackage;
	}
	
	public void setClasspackage(String classpackage) {
		this.classpackage = classpackage;
	}
	public boolean isMustCreateClasses() {
		return mustCreateClasses;
	}

	public void setMustCreateClasses(boolean mustCreateClasses) {
		this.mustCreateClasses = mustCreateClasses;
	}

	public DBConnectionInfo getConnInfo() {
		return connInfo;
	}
	
	public void setConnInfo(DBConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}
	
	public boolean isMustRecompileTable() {
		return mustRecompileTable;
	}
	
	public void setMustRecompileTable(boolean mustRecompileTable) {
		this.mustRecompileTable = mustRecompileTable;
	}
	
	/*
	public int getDefaultQtyRecordsPerEngine(String operationType) {
		try {
			return  findOperation(operationType).getDefaultQtyRecordsPerEngine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return  findOperation(operationType).getDefaultQtyRecordsPerEngine();
	}
	*/
	
	public boolean isDoIntegrityCheckInTheEnd(String operationType) {
		return  findOperation(operationType).isDoIntegrityCheckInTheEnd();
	}
	
	/*
	public int getDefaultQtyRecordsPerSelect(String operationType) {
		return findOperation(operationType).getDefaultQtyRecordsPerSelect();
	}
	*/
	
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
		this.tablesConfigurations = tablesConfigurations;
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
	
	public SyncTableConfiguration retrieveTableInfoByTableName(String tableName, Connection conn) {
		for (SyncTableConfiguration info : this.tablesConfigurations) {
			if (info.getTableName().equals(tableName)) return info;
		}
		
		for (SyncTableConfiguration info : this.tablesConfigurations) {
			
			for (ParentRefInfo child : info.getChildRefInfo(conn)) {
				if (child.getReferenceTableInfo().getTableName().equals(tableName)) {
					if (child.getReferenceTableInfo().isFullLoaded()) {
						return child.getReferenceTableInfo();
					}
				}
			}
		}
		
		return null;
	}

	public void setRelatedConfFile(File relatedConfFile) {
		this.relatedConfFile = relatedConfFile;
	}
	
	public File getRelatedConfFile() {
		return relatedConfFile;
	}
	
	public static SyncConfiguration loadFromFile(File file) throws IOException {
		SyncConfiguration conf = SyncConfiguration.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		
		conf.setRelatedConfFile(file);
		
		return conf;
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

	public SyncTableConfiguration find(SyncTableConfiguration tablesConfigurations) {
		return utilities.findOnList(this.tablesConfigurations, tablesConfigurations);
	}

	public String getDesignation() {
		return this.installationType + "_" + this.originAppLocationCode;
	}
	
	public List<SyncOperationConfig> getOperations() {
		return operations;
	}
	
	public void setOperations(List<SyncOperationConfig> operations) {
		for (SyncOperationConfig operation : operations) {
			operation.setRelatedSyncConfig(this);
			
			if (operation.getChild() != null) {
				operation.getChild().setRelatedSyncConfig(this);
			}
		}
		
		this.operations = operations;
	}
	
	private SyncOperationConfig findOperation(String operationType) {
		SyncOperationConfig toFind = SyncOperationConfig.fastCreate(operationType);
		
		for (SyncOperationConfig op : this.operations) {
			if (op.equals(toFind)) return op;
			
			if (op.getChild() != null) {
				
				if (op.getChild().equals(toFind)) {
					return op.getChild();
				}
				else
				if (op.getChild().getChild() != null && op.getChild().getChild().equals(toFind)) {
					return op.getChild().getChild();
				}
			}
		}
		
		return null;
	}
	
	public void validate() throws ForbiddenOperationException{
		for (SyncOperationConfig operation : this.operations) {
			if (this.isDestinationInstallationType()) {
				if (!operation.canBeRunInDestinationInstallation()) throw new ForbiddenOperationException("This operation ["+ operation.getOperationType() + "] Cannot be configured in destination installation");
			}
			else {
				if (!operation.canBeRunInSourceInstallation()) throw new ForbiddenOperationException("This operation ["+ operation.getOperationType() + "] Cannot be configured in source installation");
			}
		}
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

	public File getPojoProjectLocation() {
		return Main.getProjectPOJODirectory();
	}
}
