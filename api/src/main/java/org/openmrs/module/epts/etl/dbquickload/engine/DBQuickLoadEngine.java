package org.openmrs.module.epts.etl.dbquickload.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.epts.etl.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DBQuickLoadEngine extends EtlEngine {
	
	public DBQuickLoadEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DBQuickLoadSearchParams getSearchParams() {
		return (DBQuickLoadSearchParams) super.getSearchParams();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn) throws DBException {
		List<SyncImportInfoVO> migrationRecordAsSyncInfo = utilities.parseList(etlObjects, SyncImportInfoVO.class);
		
		for (SyncImportInfoVO rec : migrationRecordAsSyncInfo)
			rec.setConsistent(1);
		
		this.logInfo("WRITING  '" + etlObjects.size() + "' " + getMainSrcTableName() + " TO STAGING TABLE");
		
		SyncImportInfoDAO.insertAll(migrationRecordAsSyncInfo, getSrcConf(), srcConn);
		
		this.logInfo("'" + etlObjects.size() + "' " + getMainSrcTableName() + " WROTE TO STAGING TABLE");
		
		this.logDebug(
		    "MOVING SOURCE JSON [" + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + "] TO BACKUP AREA.");
		
		moveSoureJSONFileToBackup();
		
		logDebug(
		    "SOURCE JSON [" + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + "] MOVED TO BACKUP AREA.");
		
		getTaskResultInfo().addAllToRecordsWithNoError(EtlOperationItemResult.parseFromEtlDatabaseObject(etlObjects));
	}
	
	private void moveSoureJSONFileToBackup() {
		try {
			
			String pathToBkpFile = "";
			
			pathToBkpFile += getSyncBkpDirectory().getAbsolutePath();
			pathToBkpFile += FileUtilities.getPathSeparator();
			pathToBkpFile += FileUtilities
			        .generateFileNameFromRealPath(this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath());
			
			if (new File(pathToBkpFile).exists()) {
				FileUtilities.removeFile(pathToBkpFile);
			}
			
			FileUtilities.renameTo(this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath(), pathToBkpFile);
			
			//FileUtilities.removeFile(this.currJSONSourceFile.getAbsolutePath());
			
			if (this.getSearchParams().getCurrJSONSourceFile().exists()) {
				throw new ForbiddenOperationException("The file "
				        + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + " Could not removed");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private File getSyncBkpDirectory() throws IOException {
		String baseDirectory = getRelatedOperationController().getSyncBkpDirectory(getSrcConf()).getAbsolutePath();
		
		return new File(baseDirectory);
	}
	
	@Override
	public DBQuickLoadController getRelatedOperationController() {
		return (DBQuickLoadController) super.getRelatedOperationController();
	}
	
}
