package org.openmrs.module.epts.etl.dbquickexport.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.dbquickexport.controller.DBQuickExportController;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DBQuickExportEngine extends TaskProcessor<EtlDatabaseObject> {
	
	public DBQuickExportEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DBQuickExportController getRelatedOperationController() {
		return (DBQuickExportController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> records, Connection srcConn, Connection dstConn) throws DBException {
		try {
			List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(records, EtlDatabaseObject.class);
			
			this.getEngine().logInfo("GENERATING '" + records.size() + "' " + getMainSrcTableName() + " TO JSON FILE");
			
			for (EtlDatabaseObject rec : syncRecordsAsOpenMRSObjects) {
				rec.setUniqueKeysInfo(UniqueKeyInfo.cloneAllAndLoadValues(getSrcConf().getUniqueKeys(), rec));
			}
			
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(getMainSrcTableName(), syncRecordsAsOpenMRSObjects,
			    getEtlConfiguration().getOriginAppLocationCode(), false);
			
			jsonInfo.clearOriginApplicationCodeForAllChildren();
			
			//Generates the File to store the tmp json file
			File jsonFIle = generateJSONTempFile(jsonInfo,
			    syncRecordsAsOpenMRSObjects.get(0).getObjectId().getSimpleValueAsInt(),
			    syncRecordsAsOpenMRSObjects.get(records.size() - 1).getObjectId().getSimpleValueAsInt());
			
			this.getEngine().logInfo("WRITING '" + records.size() + "' " + getMainSrcTableName() + " TO JSON FILE ["
			        + jsonFIle.getAbsolutePath() + ".json]");
			
			//Try to remove not terminate files
			{
				FileUtilities.removeFile(jsonFIle.getAbsolutePath());
				FileUtilities.removeFile(jsonFIle.getAbsolutePath() + ".json");
			}
			
			FileUtilities.write(jsonFIle.getAbsolutePath(), jsonInfo.parseToJSON());
			
			this.logDebug("JSON [" + jsonFIle + ".json] CREATED!");
			
			this.logDebug("MARKING '" + records.size() + "' " + getMainSrcTableName() + " AS SYNCHRONIZED");
			
			this.logDebug("MARKING '" + records.size() + "' " + getMainSrcTableName() + " AS SYNCHRONIZED FINISHED");
			
			this.logDebug("MAKING FILES AVALIABLE");
			
			FileUtilities.renameTo(jsonFIle.getAbsolutePath(), jsonFIle.getAbsolutePath() + ".json");
			
			logInfo("WRITEN FILE " + jsonFIle.getPath() + ".json" + " WITH SIZE "
			        + new File(jsonFIle.getAbsolutePath() + ".json").length());
			
			if (new File(jsonFIle.getAbsolutePath() + ".json").length() == 0) {
				new File(jsonFIle.getAbsolutePath() + ".json").delete();
				
				throw new ForbiddenOperationException("EMPTY FILE WAS WROTE!!!!!");
			}
			
			getTaskResultInfo().addAllToRecordsWithNoError(
			    EtlOperationItemResult.parseFromEtlDatabaseObject(syncRecordsAsOpenMRSObjects));
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
	
	private File generateJSONTempFile(SyncJSONInfo jsonInfo, Integer startRecord, Integer lastRecord) throws IOException {
		return getRelatedOperationController().generateJSONTempFile(jsonInfo, getSrcConf(), startRecord, lastRecord);
	}
}
