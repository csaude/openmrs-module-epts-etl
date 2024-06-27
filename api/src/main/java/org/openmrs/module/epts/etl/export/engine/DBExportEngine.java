package org.openmrs.module.epts.etl.export.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.export.controller.DBExportController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DBExportEngine extends TaskProcessor<EtlDatabaseObject> {
	
	public DBExportEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DBExportController getRelatedOperationController() {
		return (DBExportController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> records, Connection srcConn, Connection dstConn) throws DBException {
		try {
			List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(records, EtlDatabaseObject.class);
			
			logDebug("GENERATING '" + records.size() + "' " + getMainSrcTableName() + " TO JSON FILE");
			
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(getMainSrcTableName(), syncRecordsAsOpenMRSObjects,
			    getEtlConfiguration().getOriginAppLocationCode(), true);
			
			File jsonFIle = generateJSONTempFile(jsonInfo,
			    syncRecordsAsOpenMRSObjects.get(0).getObjectId().getSimpleValueAsInt(),
			    syncRecordsAsOpenMRSObjects.get(records.size() - 1).getObjectId().getSimpleValueAsInt());
			
			logInfo("WRITING '" + records.size() + "' " + getMainSrcTableName() + " TO JSON FILE ["
			        + jsonFIle.getAbsolutePath() + ".json]");
			
			//Try to remove not terminate files
			{
				FileUtilities.removeFile(jsonFIle.getAbsolutePath());
				FileUtilities.removeFile(generateTmpMinimalJSONInfoFileName(jsonFIle, srcConn));
				FileUtilities.removeFile(jsonFIle.getAbsolutePath() + ".json");
				FileUtilities.removeFile(generateTmpMinimalJSONInfoFileName(jsonFIle, srcConn) + ".json");
			}
			
			FileUtilities.write(jsonFIle.getAbsolutePath(), jsonInfo.parseToJSON());
			
			FileUtilities.write(generateTmpMinimalJSONInfoFileName(jsonFIle, srcConn),
			    jsonInfo.generateMinimalInfo().parseToJSON());
			
			this.getEngine().logInfo("JSON [" + jsonFIle + ".json] CREATED!");
			
			logDebug("MARKING '" + records.size() + "' " + getMainSrcTableName() + " AS SYNCHRONIZED");
			
			logDebug("MARKING '" + records.size() + "' " + getMainSrcTableName() + " AS SYNCHRONIZED FINISHED");
			
			logDebug("MAKING FILES AVALIABLE");
			
			FileUtilities.renameTo(generateTmpMinimalJSONInfoFileName(jsonFIle, srcConn),
			    generateTmpMinimalJSONInfoFileName(jsonFIle, srcConn) + ".json");
			FileUtilities.renameTo(jsonFIle.getAbsolutePath(), jsonFIle.getAbsolutePath() + ".json");
			
			logInfo("WRITEN FILE " + jsonFIle.getPath() + ".json" + " WITH SIZE "
			        + new File(jsonFIle.getAbsolutePath() + ".json").length());
			
			if (new File(jsonFIle.getAbsolutePath() + ".json").length() == 0) {
				new File(jsonFIle.getAbsolutePath() + ".json").delete();
				new File(generateTmpMinimalJSONInfoFileName(jsonFIle, srcConn) + ".json").delete();
				
				throw new ForbiddenOperationException("EMPTY FILE WAS WROTE!!!!!");
			}
			
			markAllAsSynchronized(utilities.parseList(records, EtlDatabaseObject.class), srcConn);
			
			getTaskResultInfo().addAllToRecordsWithNoError(EtlOperationItemResult.parseFromEtlDatabaseObject(records));
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private String generateTmpMinimalJSONInfoFileName(File mainTempJSONInfoFile, Connection srcConn) {
		return mainTempJSONInfoFile.getAbsolutePath() + "_minimal";
	}
	
	private void markAllAsSynchronized(List<EtlDatabaseObject> syncRecords, Connection srcConn) throws DBException {
		
		DatabaseObjectDAO.refreshLastSyncDateOnOrigin(syncRecords, getSrcConf(),
		    getEtlConfiguration().getOriginAppLocationCode(), srcConn);
		
	}
	
	private File generateJSONTempFile(SyncJSONInfo jsonInfo, Integer startRecord, Integer lastRecord) throws IOException {
		return getRelatedOperationController().generateJSONTempFile(jsonInfo, getSrcConf(), startRecord, lastRecord);
	}
	
}
