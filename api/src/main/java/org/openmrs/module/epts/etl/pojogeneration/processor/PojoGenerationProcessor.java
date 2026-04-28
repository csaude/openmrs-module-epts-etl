package org.openmrs.module.epts.etl.pojogeneration.processor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.epts.etl.pojogeneration.model.PojoGenerationRecord;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * The processor responsible for transport synchronization files from origin to destination site
 * <p>
 * This is temporariy transportation method which suppose that the origin and destination are in the
 * same matchine, so the transport process consist on moving files from export directory to import
 * directory
 * <p>
 * In the future a propery transportation method should be implemented.
 * 
 * @author jpboane
 */
public class PojoGenerationProcessor extends TaskProcessor<PojoGenerationRecord> {
	
	private List<String> alreadyGeneratedClasses;
	
	private boolean pojoGenerated;
	
	public PojoGenerationProcessor(Engine<PojoGenerationRecord> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
		
		this.alreadyGeneratedClasses = new ArrayList<String>();
	}
	
	@Override
	public void performeEtl(List<PojoGenerationRecord> records, Connection srcConn, Connection dstConn) throws DBException {
		
		this.pojoGenerated = true;
		
		DBConnectionInfo mainApp = getEtlItemConfiguration().getSrcConnInfo();
		
		if (!getEtlItemConfiguration().isFullLoaded()) {
			getEtlItemConfiguration().fullLoad(this.getRelatedEtlOperationConfig());
		}
		
		generate(mainApp, getSrcConf());
		
		List<EtlAdditionalDataSource> allAvaliableDataSources = getEtlItemConfiguration().getSrcConf()
		        .getAvaliableExtraDataSource();
		
		for (EtlAdditionalDataSource t : allAvaliableDataSources) {
			generate(mainApp, t);
		}
		
		DBConnectionInfo mappingAppInfo = null;
		
		if (getRelatedEtlConfiguration().hasDstConnInfo()) {
			mappingAppInfo = getRelatedEtlConfiguration().getDstConnInfo();
			
			for (DstConf map : getEtlItemConfiguration().getDstConf()) {
				map.setRelatedConnInfo(mappingAppInfo);
				
				generate(mappingAppInfo, map);
				
			}
		}
		
		getTaskResultInfo().addAllToRecordsWithNoError(EtlOperationItemResult.parseFromEtlDatabaseObject(records));
		
	}
	
	private void generate(DBConnectionInfo app, DatabaseObjectConfiguration tableConfiguration) {
		if (!utilities.stringHasValue(app.getPojoPackageName())) {
			throw new ForbiddenOperationException("The app " + app + " has no package name!");
		}
		
		String fullClassName = tableConfiguration.generateFullClassName(app);
		
		if (!checkIfIsAlredyGenerated(fullClassName)) {
			OpenConnection appConn = null;
			
			try {
				appConn = app.openConnection();
				
				tableConfiguration.generateRecordClass(app, true);
				
				this.alreadyGeneratedClasses.add(fullClassName);
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
			finally {
				if (appConn != null)
					appConn.finalizeConnection();
			}
		}
	}
	
	private boolean checkIfIsAlredyGenerated(String fullClassPath) {
		return this.alreadyGeneratedClasses.contains(fullClassPath);
	}
	
	public boolean isPojoGenerated() {
		return pojoGenerated;
	}
	
	@Override
	public PojoGenerationController getRelatedOperationController() {
		return (PojoGenerationController) super.getRelatedOperationController();
	}
	
	@Override
	public TaskProcessor<PojoGenerationRecord> initReloadRecordsWithDefaultParentsTaskProcessor(
	        IntervalExtremeRecord limits) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
}
