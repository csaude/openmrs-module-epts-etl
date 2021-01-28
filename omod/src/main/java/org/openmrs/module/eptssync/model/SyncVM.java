package org.openmrs.module.eptssync.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openmrs.module.eptssync.Main;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncVM {
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<SyncConfiguration> avaliableConfigurations;
	
	private SyncConfiguration activeConfiguration;
	private File configFile;

	private String activeTab;
	private String statusMessage;
	
	private SyncVM() throws IOException, DBException {
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory();
	
		File confDir = new File(rootDirectory + FileUtilities.getPathSeparator() + "sync" + FileUtilities.getPathSeparator() + "conf");
		
		if (!confDir.exists() || confDir.list().length == 0) {
			throw new ForbiddenOperationException("Nenhum dicheiro de configuracao foi encontrado!");
		}
		
		this.avaliableConfigurations = Main.loadSyncConfig(confDir.listFiles());
		
		for (SyncConfiguration conf : this.avaliableConfigurations) {
			if (conf.isAutomaticStart()) {
				this.activeConfiguration = conf;
				break;
			}
		}
		
		String configFileName = this.activeConfiguration.getInstallationType().equals("source") ? "source_sync_config.json" : "dest_sync_config.json";

		this.configFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "sync" + FileUtilities.getPathSeparator() + "conf" + FileUtilities.getPathSeparator() + configFileName);
	
		this.activeTab = this.activeConfiguration.getOperationsAsList().get(0).getOperationType();

		//ZipUtilities.copyModuleTagsToOpenMRS();	
	}
	
	
	public List<SyncOperationConfig> getOperations(){
		return this.activeConfiguration.getOperationsAsList();
	}
	
	@JsonIgnore
	public SyncConfiguration getActiveConfiguration() {
		return activeConfiguration;
	}
	
	@JsonIgnore
	public List<SyncConfiguration> getAvaliableConfigurations() {
		return avaliableConfigurations;
	}
	
	public static SyncVM getInstance() throws DBException, IOException {
		SyncVM vm = new SyncVM();
		
		return vm;
	}
	
	public String getActiveTab() {
		return activeTab;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	public void activateTab(String tab) {
		this.activeTab = tab;
	}
	
	public boolean isActivatedOperationTab(SyncOperationConfig operation) {
		return this.activeTab.equals(operation.getOperationType());
	}
	
	public ItemProgressInfo retrieveProgressInfo(SyncOperationConfig operation, SyncTableConfiguration item) {
		OperationController controller = operation.getRelatedController();
		
		if (controller == null) return null;
		
		return controller.retrieveProgressInfo(item);
	}
	
	public OperationController getActiveOperationController() {
		for (SyncOperationConfig syncConfig: this.getOperations()) {
			if (syncConfig.getOperationType().equals(this.activeTab)) {
				return syncConfig.getRelatedController();
			}
		}
		
		throw new ForbiddenOperationException("The application could not identify the active controller");
	}
	
	public void startSync(String selectedConfiguration) {
		for (SyncConfiguration conf : this.avaliableConfigurations) {
			if (conf.getDesignation().equals(selectedConfiguration)) {
				this.activeConfiguration = conf;
				break;
			};
		}
		
		this.activeConfiguration.setClassPath(ConfVM.retrieveClassPath());
		this.activeConfiguration.setModuleRootDirectory(ConfVM.retrieveModuleFolder());
		
		saveConfigFile(this.activeConfiguration);
		
		/*{
			OpenConnection conn = this.activeConfiguration.openConnetion();
			
			this.activeConfiguration.getRelatedController().initOperationsControllers(conn);
			
			conn.markAsSuccessifullyTerminected();
			conn.finalizeConnection();
			
			if (conn.getId() != null) return;
		}*/
		
		ProcessController.retrieveRunningThread(activeConfiguration);
		
		Main.runSync(this.activeConfiguration);
		
		while(this.activeConfiguration.getRelatedController() == null || !this.activeConfiguration.getRelatedController().isProgressInfoLoaded()) {
			TimeCountDown.sleep(10);
		}
		
		//tmpSync();
	}
	
	
	public void saveConfigFile(SyncConfiguration syncConfiguration) {
		FileUtilities.removeFile(this.configFile.getAbsolutePath());
		
		FileUtilities.write(this.configFile.getAbsolutePath(), syncConfiguration.parseToJSON());
	}
		
}
