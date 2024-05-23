package org.openmrs.module.epts.etl.dbquickload.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.epts.etl.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DBQuickLoadEngine extends Engine {
	
	private File currJSONSourceFile;
	
	/*
	 * The current json info which is being processed
	 */
	private SyncJSONInfo currJSONInfo;
	
	public DBQuickLoadEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<EtlObject> migrationRecords, Connection conn) throws DBException {
		List<SyncImportInfoVO> migrationRecordAsSyncInfo = utilities.parseList(migrationRecords, SyncImportInfoVO.class);
		
		for (SyncImportInfoVO rec : migrationRecordAsSyncInfo)
			rec.setConsistent(1);
		
		this.logInfo("WRITING  '" + migrationRecords.size() + "' " + getMainSrcTableName() + " TO STAGING TABLE");
		
		SyncImportInfoDAO.insertAll(migrationRecordAsSyncInfo, getSrcConf(), conn);
		
		this.logInfo("'" + migrationRecords.size() + "' " + getMainSrcTableName() + " WROTE TO STAGING TABLE");
		
		this.logDebug("MOVING SOURCE JSON [" + this.currJSONSourceFile.getAbsolutePath() + "] TO BACKUP AREA.");
		
		BaseDAO.commit(conn);
		
		moveSoureJSONFileToBackup();
		
		logDebug("SOURCE JSON [" + this.currJSONSourceFile.getAbsolutePath() + "] MOVED TO BACKUP AREA.");
	}
	
	private void moveSoureJSONFileToBackup() {
		try {
			
			String pathToBkpFile = "";
			
			pathToBkpFile += getSyncBkpDirectory().getAbsolutePath();
			pathToBkpFile += FileUtilities.getPathSeparator();
			pathToBkpFile += FileUtilities.generateFileNameFromRealPath(this.currJSONSourceFile.getAbsolutePath());
			
			if (new File(pathToBkpFile).exists()) {
				FileUtilities.removeFile(pathToBkpFile);
			}
			
			FileUtilities.renameTo(this.currJSONSourceFile.getAbsolutePath(), pathToBkpFile);
			
			//FileUtilities.removeFile(this.currJSONSourceFile.getAbsolutePath());
			
			if (this.currJSONSourceFile.exists()) {
				throw new ForbiddenOperationException(
				        "The file " + this.currJSONSourceFile.getAbsolutePath() + " Could not removed");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<EtlObject> searchNextRecords(Connection conn) {
		this.currJSONSourceFile = getNextJSONFileToLoad();
		
		if (this.currJSONSourceFile == null)
			return null;
		
		getRelatedOperationController().logInfo("Loading content on JSON File " + this.currJSONSourceFile.getAbsolutePath());
		
		try {
			String json = new String(Files.readAllBytes(Paths.get(currJSONSourceFile.getAbsolutePath())));
			
			this.currJSONInfo = SyncJSONInfo.loadFromJSON(json);
			this.currJSONInfo.setFileName(currJSONSourceFile.getAbsolutePath());
			
			for (SyncImportInfoVO rec : this.currJSONInfo.getSyncInfo()) {
				rec.setRecordOriginLocationCode(this.currJSONInfo.getOriginAppLocationCode());
			}
			
			return utilities.parseList(this.currJSONInfo.getSyncInfo(), EtlObject.class);
			
		}
		catch (Exception e) {
			getRelatedOperationController().logInfo("Error performing " + this.currJSONSourceFile.getAbsolutePath());
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private File getNextJSONFileToLoad() {
		File[] files = getSyncDirectory().listFiles(this.getSearchParams());
		
		if (files != null && files.length > 0) {
			return files[0];
		}
		
		return null;
	}
	
	@Override
	public DBQuickLoadSearchParams getSearchParams() {
		return (DBQuickLoadSearchParams) super.getSearchParams();
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(RecordLimits limits, Connection conn) {
		QuickLoadLimits loadLimits = new QuickLoadLimits();
		loadLimits.copy(limits);
		
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new DBQuickLoadSearchParams(getRelatedOperationController(),
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
	
	private File getSyncDirectory() {
		String baseDirectory = getRelatedOperationController().getSyncDirectory(getSrcConf())
		        .getAbsolutePath();
		
		return new File(baseDirectory);
	}
}
