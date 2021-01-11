package org.openmrs.module.eptssync.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openmrs.module.eptssync.Main;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.ClassPathUtilities;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.OpenMRSPOJOGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class SyncVM {
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<SyncConfiguration> avaliableConfigurations;
	
	private SyncConfiguration activeConfiguration;
	private File configFile;

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
	}
	
	public SyncConfiguration getActiveConfiguration() {
		return activeConfiguration;
	}
	
	public List<SyncConfiguration> getAvaliableConfigurations() {
		return avaliableConfigurations;
	}
	
	public static SyncVM getInstance() throws DBException, IOException {
		SyncVM vm = new SyncVM();
		
		return vm;
	}

	public void startSync(String selectedConfiguration) {
		for (SyncConfiguration conf : this.avaliableConfigurations) {
			if (conf.getDesignation().equals(selectedConfiguration)) {
				this.activeConfiguration = conf;
				break;
			};
		}
		
		this.activeConfiguration.setClassPath(ConfVM.retrieveClassPath());
		this.activeConfiguration.setModuleRootDirectory(ConfVM.retrieveModuleFolder(this.activeConfiguration));
		
		saveConfigFile(this.activeConfiguration);
		
		//Main.runSync(this.activeConfiguration);
		
		tmpSync();
	}
	
	public void saveConfigFile(SyncConfiguration syncConfiguration) {
		FileUtilities.removeFile(this.configFile.getAbsolutePath());
		
		FileUtilities.write(this.configFile.getAbsolutePath(), syncConfiguration.parseToJSON());
	}
	
	
	private void tmpSync() {
		DBConnectionService connService = DBConnectionService.init(this.activeConfiguration.getConnInfo());
		
		OpenConnection conn = connService.openConnection();
		
		try {
			List<SyncTableConfiguration> allSync = this.activeConfiguration.getTablesConfigurations();
			
			//OpenMRSPOJOGenerator.copyClassPathContentToFolder(this.activeConfiguration.getClassPathAsFile(), this.activeConfiguration.getPOJOCompiledFilesDirectory());
			
			//System.out.println(OpenMRSObject.class.getClassLoader().getResource("org/openmrs/module/eptssync/model/pojo/generic/OpenMRSObject.class"));
			
			//OpenMRSPOJOGenerator.tryToGetExistingCLass("org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject", this.activeConfiguration);
			
			
			//copyJarContent(this.activeConfiguration);
			
			//allSync.get(0).generateRecordClass(true, conn);
			
			//Class<OpenMRSObject> obj = OpenMRSPOJOGenerator.tryToGetExistingCLass(allSync.get(0).generateFullClassName(), this.activeConfiguration);
			
			//OpenMRSObjectDAO.getFirstRecord(allSync.get(0), null, conn);
	
			//System.out.println(obj);
			
			for (SyncTableConfiguration syncInfo: allSync) {
				syncInfo.generateRecordClass(true, conn);
			}
			
			
			OpenMRSPOJOGenerator.tryToGetExistingCLass("org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject", this.activeConfiguration);
			
			for (SyncTableConfiguration syncInfo: allSync) {
				Class<OpenMRSObject> obj = OpenMRSPOJOGenerator.tryToGetExistingCLass(syncInfo.generateFullClassName(), this.activeConfiguration);
				
				OpenMRSObjectDAO.getFirstRecord(syncInfo, null, conn);
				
			}
			
			conn.finalizeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void copyJarContent(SyncConfiguration syncConfiguration) throws IOException {
		File jarTmpFolder = new File (syncConfiguration.getSyncRootDirectory() + FileUtilities.getPathSeparator() + "temp");
		
		FileUtilities.tryToCreateDirectoryStructure(jarTmpFolder.getAbsolutePath());
		
		File classPathContentTempDir = new File(jarTmpFolder + FileUtilities.getPathSeparator() + "classPathContent");
		
		FileUtilities.tryToCreateDirectoryStructure(classPathContentTempDir.getAbsolutePath());
		
		ClassPathUtilities.copyJarContentToFolder(syncConfiguration.getClassPathAsFile(), classPathContentTempDir);
	}	
}
