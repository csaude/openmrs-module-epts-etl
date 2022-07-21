package org.openmrs.module.eptssync.model;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.ForbiddenException;

import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncOperationType;
import org.openmrs.module.eptssync.controller.conf.SyncProcessType;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.utilities.ClassPathUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class ConfVM {
	private static ConfVM sourceConfVM;
	private static ConfVM destConfVM;
	
	private static final String INSTALATION_TAB = "1";
	private static final String OPERATIONS_TAB = "2";
	private static String TABLES_TAB = "3";
	
	private SyncConfiguration syncConfiguration;
	private SyncConfiguration otherSyncConfiguration;
	
	private String activeTab;
	
	private SyncOperationConfig selectedOperation;
	
	private SyncTableConfiguration selectedTable;
	
	private File configFile;
	private String statusMessage;
	
	private ConfVM(String installationType) throws IOException, DBException {
		this.syncConfiguration = new SyncConfiguration();
		
		SyncProcessType processType = installationType.equals("source") ? SyncProcessType.SOURCE_SYNC : SyncProcessType.DATABASE_MERGE_FROM_JSON;
		
		this.syncConfiguration.setProcessType(processType);
		
		reset();
	}
	
	public SyncOperationConfig getSelectedOperation() {
		return selectedOperation;
	}

	public SyncTableConfiguration getSelectedTable() {
		return selectedTable;
	}
	
	public void setSyncConfiguration(SyncConfiguration syncConfiguration) {
		this.syncConfiguration = syncConfiguration;
	}

	public void setSelectedOperation(SyncOperationConfig selectedOperation) {
		this.selectedOperation = selectedOperation;
	}

	public void setSelectedTable(SyncTableConfiguration selectedTable) {
		this.selectedTable = selectedTable;
	}

	public String getStatusMessage() {
		return statusMessage;
	}
	
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	protected static String retrieveClassPath() {
		return ClassPathUtilities.retrieveModuleJar().getAbsolutePath();
	}
	
	protected static File retrieveModuleFolder() {
		return ClassPathUtilities.retrieveModuleFolder();
	}
	
	public static ConfVM getInstance(String installationType) throws IOException, DBException {
		
		ConfVM vm = null;
		
		if (installationType.equals("source")) {
			if (sourceConfVM != null) {
				vm = sourceConfVM;
				
				vm.reset();
			}
			else {
				vm = new ConfVM(installationType);
			}
			
			sourceConfVM = vm;
			
			if (destConfVM != null) {
				vm.otherSyncConfiguration = destConfVM.getSyncConfiguration();
			}
			
		}
		else {
			if (destConfVM != null) {
				vm = destConfVM;
				
				vm.reset();
			}
			else {
				vm = new ConfVM(installationType);
				
			}

			if (sourceConfVM != null) {
				vm.otherSyncConfiguration = sourceConfVM.getSyncConfiguration();
			}
			
			destConfVM = vm;
		}
		
		if (vm.otherSyncConfiguration == null) {
			vm.determineOtherSyncConfiguration();
		}
		
		return vm;
	}
	
	private void determineOtherSyncConfiguration() throws DBException {
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory();
		
		String otherConfFile = this.syncConfiguration.getProcessType().isSourceSync() ? "dest_sync_config.json" : "source_sync_config.json";

		File otherConfigFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "resources" + FileUtilities.getPathSeparator() + otherConfFile);
		
		if (otherConfigFile.exists()) {
			try {
				this.otherSyncConfiguration = ConfVM.getInstance(this.syncConfiguration.getProcessType().isDataBaseMergeFromJSON() ? "destination" : "source").getSyncConfiguration();
			} catch (IOException e) {
				throw new ForbiddenException(e);
			}
		}
	}

	private void reset() throws IOException, DBException {
		this.activeTab = ConfVM.INSTALATION_TAB;
		
		SyncConfiguration reloadedSyncConfiguration = null;
		
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory();
		
		String configFileName = this.syncConfiguration.getProcessType().isSourceSync() ? "source_sync_config.json" : "dest_sync_config.json";

		this.configFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "sync" + FileUtilities.getPathSeparator() + "conf" + FileUtilities.getPathSeparator() + configFileName);

		if (this.configFile.exists()) {
			reloadedSyncConfiguration = SyncConfiguration.loadFromFile(this.configFile);
		} else {
			String json = this.syncConfiguration.getProcessType().isSourceSync() ? ConfigData.generateDefaultSourcetConfig() : ConfigData.generateDefaultDestinationConfig();
		
			reloadedSyncConfiguration = SyncConfiguration.loadFromJSON(null, json);
			
			reloadedSyncConfiguration.setSyncRootDirectory(rootDirectory+ FileUtilities.getPathSeparator() + "sync" + FileUtilities.getPathSeparator() + "data");
			reloadedSyncConfiguration.setRelatedConfFile(this.configFile);
			
			Properties properties = new Properties();
			
			File openMrsRuntimePropertyFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "openmrs-runtime.properties");
			
			properties.load(FileUtilities.createStreamFromFile(openMrsRuntimePropertyFile));
			
			reloadedSyncConfiguration.getMainDBConnInfo().setConnectionURI(properties.getProperty("connection.url"));
			reloadedSyncConfiguration.getMainDBConnInfo().setDataBaseUserName(properties.getProperty("connection.username"));
			reloadedSyncConfiguration.getMainDBConnInfo().setDataBaseUserPassword(properties.getProperty("connection.password"));
		}
		
		reloadedSyncConfiguration.setRelatedController(this.syncConfiguration.getRelatedController() == null ? new ProcessController(reloadedSyncConfiguration) : this.syncConfiguration.getRelatedController());
		reloadedSyncConfiguration.getRelatedController().setConfiguration(reloadedSyncConfiguration);
		reloadedSyncConfiguration.loadAllTables();
		
		reloadedSyncConfiguration.setClassPath(retrieveClassPath());
		reloadedSyncConfiguration.setModuleRootDirectory(retrieveModuleFolder());
		
		this.syncConfiguration = reloadedSyncConfiguration;
		
		if (this.syncConfiguration.isSourceSyncProcess()) {
			if (this.syncConfiguration.getOriginAppLocationCode() == null) {
				//this.syncConfiguration.tryToDetermineOriginAppLocationCode();
			}
		}
		
	}

	public void selectOperation(SyncOperationType operationType) {
		if (operationType != null) {
			this.selectedOperation = syncConfiguration.findOperation(operationType);
		}
		else {
			this.selectedOperation = null;
		}
	}
	
	public void selectTable(String tableName) {
		this.selectedTable = syncConfiguration.findSyncTableConfigurationOnAllTables(tableName);
		
		if (syncConfiguration.find(this.selectedTable) == null || !this.selectedTable.isFullLoaded()) {
			this.selectedTable.fullLoad();
		}
	}
	
	public String getActiveTab() {
		return activeTab;
	}
	
	public void activateTab(String tab) {
		this.activeTab = tab;
	}
	
	public SyncConfiguration getSyncConfiguration() {
		return syncConfiguration;
	}
	
	public boolean isInstallationTabActive() {
		return this.activeTab.equals(ConfVM.INSTALATION_TAB);
	}
	
	public boolean isOperationsTabActive() {
		return this.activeTab.equals(ConfVM.OPERATIONS_TAB);
	}
	
	public boolean isTablesTabActive() {
		return this.activeTab.equals(ConfVM.TABLES_TAB);
	}

	public void save() {
		FileUtilities.removeFile(this.configFile.getAbsolutePath());
		
		this.syncConfiguration.setAutomaticStart(true);
		FileUtilities.write(this.configFile.getAbsolutePath(), this.syncConfiguration.parseToJSON());
		
		//Make others not automcatic start
		
		if (this.otherSyncConfiguration != null && this.otherSyncConfiguration.getRelatedConfFile().exists()) {
			this.otherSyncConfiguration.setAutomaticStart(false);
			
			FileUtilities.removeFile(this.otherSyncConfiguration.getRelatedConfFile().getAbsolutePath());
			
			FileUtilities.write(this.otherSyncConfiguration.getRelatedConfFile().getAbsolutePath(), otherSyncConfiguration.parseToJSON());
		}
	}
}
