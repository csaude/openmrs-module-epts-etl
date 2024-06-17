package org.openmrs.module.epts.etl.load.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.load.controller.DataLoadController;
import org.openmrs.module.epts.etl.load.model.LoadSyncDataSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DataLoadEngine extends TaskProcessor<EtlDatabaseObject> {
	
	public DataLoadEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits) {
		super(monitor, limits);
	}
	
	@Override
	protected EtlOperationResultHeader<EtlDatabaseObject> performeSync(List<EtlDatabaseObject> records, Connection srcConn,
	        Connection dstConn) throws DBException {
		List<SyncImportInfoVO> migrationRecordAsSyncInfo = utilities.parseList(records, SyncImportInfoVO.class);
		
		logInfo("WRITING  '" + records.size() + "' " + getMainSrcTableName() + " TO STAGING TABLE");
		
		SyncImportInfoDAO.insertAll(migrationRecordAsSyncInfo, getSrcConf(), srcConn);
		
		logInfo("'" + records.size() + "' " + getMainSrcTableName() + " WROTE TO STAGING TABLE");
		
		logDebug(
		    "MOVING SOURCE JSON [" + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + "] TO BACKUP AREA.");
		
		moveSoureJSONFileToBackup();
		
		logDebug(
		    "SOURCE JSON [" + this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath() + "] MOVED TO BACKUP AREA.");
	
	
		return new EtlOperationResultHeader<>(records);
	}
	
	private void moveSoureJSONFileToBackup() {
		try {
			
			String pathToBkpFile = "";
			
			pathToBkpFile += getSyncBkpDirectory().getAbsolutePath();
			pathToBkpFile += FileUtilities.getPathSeparator();
			pathToBkpFile += FileUtilities
			        .generateFileNameFromRealPath(this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath());
			
			FileUtilities.renameTo(this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath(), pathToBkpFile);
			
			//NOW, MOVE MINIMAL FILE
			
			String[] parts = this.getSearchParams().getCurrJSONSourceFile().getAbsolutePath().split(".json");
			String minimalFile = parts[0] + "_minimal.json";
			
			String pathToBkpMinimalFile = "";
			pathToBkpMinimalFile += getSyncBkpDirectory().getAbsolutePath();
			pathToBkpMinimalFile += FileUtilities.getPathSeparator();
			pathToBkpMinimalFile += FileUtilities.generateFileNameFromRealPath(minimalFile);
			
			FileUtilities.renameTo(minimalFile, pathToBkpMinimalFile);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	boolean printed;
	
	@Override
	public LoadSyncDataSearchParams getSearchParams() {
		return (LoadSyncDataSearchParams) super.getSearchParams();
	}
	
	private File getSyncBkpDirectory() throws IOException {
		String baseDirectory = getRelatedOperationController().getSyncBkpDirectory(getSrcConf()).getAbsolutePath();
		
		return new File(baseDirectory);
	}
	
	@Override
	public DataLoadController getRelatedOperationController() {
		return (DataLoadController) super.getRelatedOperationController();
	}
}
