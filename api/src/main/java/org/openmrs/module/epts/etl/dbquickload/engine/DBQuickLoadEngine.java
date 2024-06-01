package org.openmrs.module.epts.etl.dbquickload.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.epts.etl.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DBQuickLoadEngine extends EtlEngine {


	public DBQuickLoadEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public DBQuickLoadSearchParams getSearchParams() {
		return (DBQuickLoadSearchParams) super.getSearchParams();
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> migrationRecords, Connection conn) throws DBException {
		List<SyncImportInfoVO> migrationRecordAsSyncInfo = utilities.parseList(migrationRecords, SyncImportInfoVO.class);
		
		for (SyncImportInfoVO rec : migrationRecordAsSyncInfo)
			rec.setConsistent(1);
		
		this.logInfo("WRITING  '" + migrationRecords.size() + "' " + getMainSrcTableName() + " TO STAGING TABLE");
		
		SyncImportInfoDAO.insertAll(migrationRecordAsSyncInfo, getSrcConf(), conn);
		
		this.logInfo("'" + migrationRecords.size() + "' " + getMainSrcTableName() + " WROTE TO STAGING TABLE");
		
		this.logDebug("MOVING SOURCE JSON [" + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + "] TO BACKUP AREA.");
		
		BaseDAO.commit(conn);
		
		moveSoureJSONFileToBackup();
		
		logDebug("SOURCE JSON [" + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + "] MOVED TO BACKUP AREA.");
	}
	
	private void moveSoureJSONFileToBackup() {
		try {
			
			String pathToBkpFile = "";
			
			pathToBkpFile += getSyncBkpDirectory().getAbsolutePath();
			pathToBkpFile += FileUtilities.getPathSeparator();
			pathToBkpFile += FileUtilities.generateFileNameFromRealPath(this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath());
			
			if (new File(pathToBkpFile).exists()) {
				FileUtilities.removeFile(pathToBkpFile);
			}
			
			FileUtilities.renameTo(this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath(), pathToBkpFile);
			
			//FileUtilities.removeFile(this.currJSONSourceFile.getAbsolutePath());
			
			if (this.getSearchParams().getCurrJSONSourceFile().exists()) {
				throw new ForbiddenOperationException(
				        "The file " + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + " Could not removed");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}

	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadLimitsManager limits, Connection conn) {
		QuickLoadLimits loadLimits = new QuickLoadLimits();
		loadLimits.copy(limits);
		
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new DBQuickLoadSearchParams(this,
		        this.getEtlConfiguration(), loadLimits);
		
		searchParams.setQtdRecordPerSelected(1);
		
		loadLimits.setRelatedSearchParams((DBQuickLoadSearchParams) searchParams);
		
		return searchParams;
	}
	
	private File getSyncBkpDirectory() throws IOException {
		String baseDirectory = getRelatedOperationController().getSyncBkpDirectory(getSrcConf())
		        .getAbsolutePath();
		
		return new File(baseDirectory);
	}
	
	@Override
	public DBQuickLoadController getRelatedOperationController() {
		return (DBQuickLoadController) super.getRelatedOperationController();
	}
	

}
