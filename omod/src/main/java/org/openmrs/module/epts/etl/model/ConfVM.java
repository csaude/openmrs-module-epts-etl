package org.openmrs.module.epts.etl.model;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.conf.EtlProcessType;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.ClassPathUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class ConfVM {
	private static ConfVM sourceConfVM;
	private static ConfVM destConfVM;
	
	private static final String INSTALATION_TAB = "1";
	private static final String OPERATIONS_TAB = "2";
	private static String TABLES_TAB = "3";
	
	private EtlConfiguration etlConfiguration;
	private EtlConfiguration otherSyncConfiguration;
	
	private String activeTab;
	
	private EtlOperationConfig selectedOperation;
	
	private AbstractTableConfiguration selectedTable;
	
	private File configFile;
	private String statusMessage;
	
	private ConfVM(String installationType) throws IOException, DBException {
		this.etlConfiguration = new EtlConfiguration();
		
		EtlProcessType processType = installationType.equals("source") ? EtlProcessType.SOURCE_SYNC : EtlProcessType.DATABASE_MERGE_FROM_JSON;
		
		this.etlConfiguration.setProcessType(processType);
		
		reset();
	}
	
	public EtlOperationConfig getSelectedOperation() {
		return selectedOperation;
	}

	public AbstractTableConfiguration getSelectedTable() {
		return selectedTable;
	}
	
	public void setSyncConfiguration(EtlConfiguration etlConfiguration) {
		this.etlConfiguration = etlConfiguration;
	}

	public void setSelectedOperation(EtlOperationConfig selectedOperation) {
		this.selectedOperation = selectedOperation;
	}

	public void setSelectedTable(AbstractTableConfiguration selectedTable) {
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
		
		String otherConfFile = this.etlConfiguration.getProcessType().isSourceSync() ? "dest_sync_config.json" : "source_sync_config.json";

		File otherConfigFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "resources" + FileUtilities.getPathSeparator() + otherConfFile);
		
		if (otherConfigFile.exists()) {
			try {
				this.otherSyncConfiguration = ConfVM.getInstance(this.etlConfiguration.getProcessType().isDataBaseMergeFromJSON() ? "destination" : "source").getSyncConfiguration();
			} catch (IOException e) {
				throw new ForbiddenOperationException(e);
			}
		}
	}

	private void reset() throws IOException, DBException {
		this.activeTab = ConfVM.INSTALATION_TAB;
		
		EtlConfiguration reloadedSyncConfiguration = null;
		
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory();
		
		String configFileName = this.etlConfiguration.getProcessType().isSourceSync() ? "source_sync_config.json" : "dest_sync_config.json";

		this.configFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "sync" + FileUtilities.getPathSeparator() + "conf" + FileUtilities.getPathSeparator() + configFileName);

		if (this.configFile.exists()) {
			reloadedSyncConfiguration = EtlConfiguration.loadFromFile(this.configFile);
		} else {
			String json = this.etlConfiguration.getProcessType().isSourceSync() ? ConfigData.generateDefaultSourcetConfig() : ConfigData.generateDefaultDestinationConfig();
		
			reloadedSyncConfiguration = EtlConfiguration.loadFromJSON(json);
			
			reloadedSyncConfiguration.setSyncRootDirectory(rootDirectory+ FileUtilities.getPathSeparator() + "sync" + FileUtilities.getPathSeparator() + "data");
			reloadedSyncConfiguration.setRelatedConfFile(this.configFile);
			
			Properties properties = new Properties();
			
			File openMrsRuntimePropertyFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "openmrs-runtime.properties");
			
			properties.load(FileUtilities.createStreamFromFile(openMrsRuntimePropertyFile));
			
			reloadedSyncConfiguration.getMainDBConnInfo().setConnectionURI(properties.getProperty("connection.url"));
			reloadedSyncConfiguration.getMainDBConnInfo().setDataBaseUserName(properties.getProperty("connection.username"));
			reloadedSyncConfiguration.getMainDBConnInfo().setDataBaseUserPassword(properties.getProperty("connection.password"));
		}
		
		reloadedSyncConfiguration.setRelatedController(this.etlConfiguration.getRelatedController() == null ? new ProcessController(null, reloadedSyncConfiguration) : this.etlConfiguration.getRelatedController());
		reloadedSyncConfiguration.getRelatedController().setConfiguration(reloadedSyncConfiguration);
		reloadedSyncConfiguration.loadAllTables();
		
		reloadedSyncConfiguration.setClassPath(retrieveClassPath());
		reloadedSyncConfiguration.setModuleRootDirectory(retrieveModuleFolder());
		
		this.etlConfiguration = reloadedSyncConfiguration;
		
		if (this.etlConfiguration.isSourceSyncProcess()) {
			if (this.etlConfiguration.getOriginAppLocationCode() == null) {
				//this.syncConfiguration.tryToDetermineOriginAppLocationCode();
			}
		}
		
	}

	public void selectOperation(EtlOperationType operationType) {
		if (operationType != null) {
			this.selectedOperation = etlConfiguration.findOperation(operationType);
		}
		else {
			this.selectedOperation = null;
		}
	}
	
	public void selectTable(String tableName) {
		this.selectedTable = etlConfiguration.findSyncTableConfigurationOnAllTables(tableName);
		
		if (etlConfiguration.find(this.selectedTable) == null || !this.selectedTable.isFullLoaded()) {
			try {
				this.selectedTable.fullLoad();
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public String getActiveTab() {
		return activeTab;
	}
	
	public void activateTab(String tab) {
		this.activeTab = tab;
	}
	
	public EtlConfiguration getSyncConfiguration() {
		return etlConfiguration;
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
		
		this.etlConfiguration.setAutomaticStart(true);
		FileUtilities.write(this.configFile.getAbsolutePath(), this.etlConfiguration.parseToJSON());
		
		//Make others not automcatic start
		
		if (this.otherSyncConfiguration != null && this.otherSyncConfiguration.getRelatedConfFile().exists()) {
			this.otherSyncConfiguration.setAutomaticStart(false);
			
			FileUtilities.removeFile(this.otherSyncConfiguration.getRelatedConfFile().getAbsolutePath());
			
			FileUtilities.write(this.otherSyncConfiguration.getRelatedConfFile().getAbsolutePath(), otherSyncConfiguration.parseToJSON());
		}
	}
}
