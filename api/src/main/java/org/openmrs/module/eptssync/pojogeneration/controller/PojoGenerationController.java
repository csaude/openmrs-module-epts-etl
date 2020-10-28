package org.openmrs.module.eptssync.pojogeneration.controller;

import java.io.File;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.pojogeneration.engine.PojoGenerationEngine;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for data base preparation
 * 
 * @author jpboane
 *
 */
public class PojoGenerationController extends OperationController {
	
	public PojoGenerationController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new PojoGenerationEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}

	@Override
	public String getOperationType() {
		return SyncOperationConfig.SYNC_OPERATION_POJO_GENERATION;
	}	
	
	@Override
	public void changeStatusToFinished() {
		String operationId = this.getControllerId();
		
		String fileName = getConfiguration().getSyncRootDirectory() + "/process_status/"+operationId;
		
		//If the operation is real finishing now
		if (!new File(fileName).exists()) {
			OpenConnection conn = openConnection();
			
			try {
				validateAllPOJO(conn);
			} finally {
				conn.finalizeConnection();
			}
		}
		
		super.changeStatusToFinished();
	}

	private void validateAllPOJO(java.sql.Connection conn) {
		/*
		for (SyncTableConfiguration conf : getConfiguration().getTablesConfigurations()) {
			Class<OpenMRSObject> recordClass = null;
			
			try {
				recordClass = conf.getRecordClass();
				
				if (utilities().createInstance(recordClass).isGeneratedFromSkeletonClass()) {
					
					throw new ForbiddenOperationException("The class " + recordClass.getCanonicalName() + " was not full generated");
					//conf.generateRecordClass(true, conn);
				}
			} catch (Exception e) {
				e.printStackTrace();
				
				logInfo("THE POJO FOR TABLE " + conf.getTableName() + " WAS NOT ALREADY CREATED. CREATING NOW...");
	
				throw new ForbiddenOperationException("The class " + recordClass.getCanonicalName() + " was not full generated");
				//conf.generateRecordClass(true, conn);
			}
		}*/
		
		//Now validate all classes under the openmrs package
		/*
		String rootPackage = "org.openmrs.module.eptssync.model.openmrs." + getConfiguration().getClasspackage();
		
		File sourceFile = new File(getConfiguration().getPOJOSourceFilesDirectory().getAbsolutePath() + "/org/openmrs/module/eptssync/model/openmrs/" + getConfiguration().getClasspackage());
		File target = getConfiguration().getPOJOCompiledFilesDirectory();
		
	 	File[] files = sourceFile.listFiles();
	    
	 	for (File file : files) {
	 		String className = rootPackage + "." + FileUtilities.generateFileNameFromRealPathWithoutExtension(file.getAbsolutePath());
	 	
	 		Class<OpenMRSObject> recordClass = OpenMRSClassGenerator.tryToGetExistingCLass(target, className);
	 		
	 		OpenMRSObject obj = utilities().createInstance(recordClass);
	 		
			if (obj.isGeneratedFromSkeletonClass()) {
				logInfo("THE POJO FOR TABLE " + obj.generateTableName() + " WAS GENERATED FROM SKELETON... NOW REGENERATING FULL CLASS");
				SyncTableConfiguration tabConf = getConfiguration().findPulledTableConfiguration(obj.generateTableName());
				
				if (tabConf == null) {
					tabConf = SyncTableConfiguration.init(obj.generateTableName(), getConfiguration());
				}
				
				tabConf.generateRecordClass(true, conn);
				logInfo("POJO FOR TABLE " + obj.generateTableName() + " WAS FULL REGENERATED");
			}
	 	}*/
	}
	
	public SyncConfiguration getConfiguration() {
		return getProcessController().getConfiguration();
	}
}
