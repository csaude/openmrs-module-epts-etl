package org.openmrs.module.epts.etl.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.module.epts.etl.Main;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class SyncVM {
	
	@SuppressWarnings("unused")
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<EtlConfiguration> avaliableConfigurations;
	
	private EtlConfiguration activeConfiguration;
	
	private File configFile;
	
	private String activeTab;
	
	private String statusMessage;
	
	private SyncVM() throws IOException, DBException {
		String rootDirectory = OpenmrsUtil.getApplicationDataDirectory();
		
		File confDir = new File(
		        rootDirectory + FileUtilities.getPathSeparator() + "sync" + FileUtilities.getPathSeparator() + "conf");
		
		if (!confDir.exists() || confDir.list().length == 0) {
			throw new ForbiddenOperationException("Nenhum dicheiro de configuracao foi encontrado!");
		}
		
		this.avaliableConfigurations = null;//Main.loadSyncConfig(confDir.listFiles());
		
		for (EtlConfiguration conf : this.avaliableConfigurations) {
			if (conf.isAutomaticStart()) {
				this.activeConfiguration = conf;
				break;
			}
		}
		
		String configFileName = this.activeConfiguration.getProcessType().isSourceSync() ? "source_sync_config.json"
		        : "dest_sync_config.json";
		
		this.configFile = new File(rootDirectory + FileUtilities.getPathSeparator() + "sync"
		        + FileUtilities.getPathSeparator() + "conf" + FileUtilities.getPathSeparator() + configFileName);
		
		this.activeTab = this.activeConfiguration.getOperationsAsList().get(0).getOperationType().toString();
		
		//ZipUtilities.copyModuleTagsToOpenMRS();	
	}
	
	public List<EtlOperationConfig> getOperations() {
		return this.activeConfiguration.getOperationsAsList();
	}
	
	@JsonIgnore
	public EtlConfiguration getActiveConfiguration() {
		return activeConfiguration;
	}
	
	@JsonIgnore
	public List<EtlConfiguration> getAvaliableConfigurations() {
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
	
	public boolean isActivatedOperationTab(EtlOperationConfig operation) {
		return this.activeTab.equals(operation.getOperationType());
	}
	
	public TableOperationProgressInfo retrieveProgressInfo(EtlOperationConfig operation, EtlItemConfiguration item,
	        String appOriginCode) {
		OperationController controller = operation.getRelatedController(appOriginCode);
		
		if (controller == null)
			return null;
		
		return controller.retrieveProgressInfo(item);
	}
	
	public TableOperationProgressInfo retrieveProgressInfo(EtlOperationConfig operation, EtlItemConfiguration item) {
		return retrieveProgressInfo(operation, item, null);
	}
	
	public OperationController getActiveOperationController() {
		for (EtlOperationConfig syncConfig : this.getOperations()) {
			if (syncConfig.getOperationType().equals(this.activeTab)) {
				return syncConfig.getRelatedController(null);
			}
		}
		
		throw new ForbiddenOperationException("The application could not identify the active controller");
	}
	
	public void startSync(String selectedConfiguration) {
		for (EtlConfiguration conf : this.avaliableConfigurations) {
			if (conf.getDesignation().equals(selectedConfiguration)) {
				this.activeConfiguration = conf;
				break;
			}
			;
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
		
		try {
			Main.runSync(this.activeConfiguration);
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		
		while (this.activeConfiguration.getRelatedController() == null
		        || !this.activeConfiguration.getRelatedController().isProgressInfoLoaded()) {
			TimeCountDown.sleep(10);
		}
		
		//tmpSync();
	}
	
	public void saveConfigFile(EtlConfiguration etlConfiguration) {
		FileUtilities.removeFile(this.configFile.getAbsolutePath());
		
		FileUtilities.write(this.configFile.getAbsolutePath(), etlConfiguration.parseToJSON());
	}
	
}
