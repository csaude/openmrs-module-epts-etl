package org.openmrs.module.eptssync.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class ConfVM {
	private static final String INSTALATION_TAB = "1";
	private static final String OPERATIONS_TAB = "2";
	private static String TABLES_TAB = "3";
	
	private SyncConfiguration syncConfiguration;
	private String activeTab;
	
	private SyncOperationConfig selectedOperation;
	
	private SyncTableConfiguration selectedTable;
	
	public ConfVM(HttpServletRequest request, String installationType) throws IOException {
		this.activeTab = ConfVM.INSTALATION_TAB;
		
		
		//ModelAndView modelAndView = new ModelAndView();
		
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory();
		
		String configFileName = installationType.equals("source") ? "source_sync_config.json" : "dest_sync_config.json";

		File config = new File(rootDirectory + FileUtilities.getPathSeparator() + "syncConf" + FileUtilities.getPathSeparator() + configFileName);
	
		if (config.exists()) {
			syncConfiguration = SyncConfiguration.loadFromFile(config);
		} else {
						
			String json = installationType.equals("source") ? ConfigData.generateDefaultSourcetConfig() : ConfigData.generateDefaultDestinationConfig();
			
			config = new File(rootDirectory + FileUtilities.getPathSeparator() + "resources" + FileUtilities.getPathSeparator() + configFileName);

			syncConfiguration = SyncConfiguration.loadFromJSON(json);
			syncConfiguration.setSyncRootDirectory(rootDirectory+ FileUtilities.getPathSeparator() + "syncConf");
		
			Properties properties = new Properties();
			
			File openMrsRuntimePropertyFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "openmrs-runtime.properties");
			
			properties.load(FileUtilities.createStreamFromFile(openMrsRuntimePropertyFile));
			
			syncConfiguration.getConnInfo().setConnectionURI(properties.getProperty("connection.url"));
			syncConfiguration.getConnInfo().setDataBaseUserName(properties.getProperty("connection.username"));
			syncConfiguration.getConnInfo().setDataBaseUserPassword(properties.getProperty("connection.password"));
		}
		
		syncConfiguration.setClassPath(retrieveClassPath());
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

	private String retrieveClassPath() {
		String rootDirectory = Paths.get(".").normalize().toAbsolutePath().toString();
		
		File[] allFiles = new File(rootDirectory + FileUtilities.getPathSeparator() + "temp").listFiles();
		
		Arrays.sort(allFiles);
		
		for (int i = allFiles.length - 1; i >= 0; i--) {
			if (allFiles[i].isDirectory() && allFiles[i].getAbsolutePath().contains("openmrs-lib-cache")) {
				File classPath = new File(allFiles[i].getAbsoluteFile() + FileUtilities.getPathSeparator() + "eptssync" + FileUtilities.getPathSeparator() + "lib");
				
				return classPath.getAbsolutePath();
			}
		}
		
		return null;
	}
	
	public void selectOperation(String operationType) {
		if (!operationType.isEmpty()) {
			this.selectedOperation = syncConfiguration.findOperation(operationType);
		}
		else {
			this.selectedOperation = null;
		}
	}
	
	public void selectTable(String tableName) {
		this.selectedTable = syncConfiguration.findSyncTableConfiguration(tableName);
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
}
