package org.openmrs.module.epts.etl.pojogeneration.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.AdditionlExtractionSrcTable;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.DstConf;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.PojobleDatabaseObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.epts.etl.pojogeneration.model.PojoGenerationRecord;
import org.openmrs.module.epts.etl.pojogeneration.model.PojoGenerationSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * The engine responsible for transport synchronization files from origin to destination site
 * <p>
 * This is temporariy transportation method which suppose that the origin and destination are in the
 * same matchine, so the transport process consist on moving files from export directory to import
 * directory
 * <p>
 * In the future a propery transportation method should be implemented.
 * 
 * @author jpboane
 */
public class PojoGenerationEngine extends Engine {
	
	private List<String> alreadyGeneratedClasses;
	
	private boolean pojoGenerated;
	
	public PojoGenerationEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		this.alreadyGeneratedClasses = new ArrayList<String>();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> migrationRecords, Connection conn) throws DBException {
		this.pojoGenerated = true;
		
		AppInfo mainApp = getEtlConfiguration().getMainApp();
		
		if (!getEtlConfiguration().isFullLoaded()) {
			getEtlConfiguration().fullLoad();
		}
		
		generate(mainApp, getMainSrcTableConf());
		
		if (getEtlConfiguration().getSrcConf().getAdditionalExtractionInfo() != null) {
			for (AdditionlExtractionSrcTable t : getEtlConfiguration().getSrcConf().getAdditionalExtractionInfo()
			        .getAdditionalExtractionTables()) {
				generate(mainApp, t);
			}
		}
		
		if (getEtlConfiguration().getSrcConf().getExtraDataSource() != null) {
			for (EtlExtraDataSource src : getEtlConfiguration().getSrcConf().getExtraDataSource()) {
				generate(mainApp, src.getAvaliableSrc());
			}
		}
		
		List<AppInfo> otherApps = getEtlConfiguration().getRelatedSyncConfiguration().exposeAllAppsNotMain();
		
		AppInfo mappingAppInfo = null;
		
		if (utilities.arrayHasElement(otherApps)) {
			mappingAppInfo = otherApps.get(0);
			
			for (DstConf map : getEtlConfiguration().getDstConf()) {
				map.setRelatedAppInfo(mappingAppInfo);
				
				generate(mappingAppInfo, map.getDstTableConf());
				
			}
		}
	}
	
	private void generate(AppInfo app, PojobleDatabaseObject tableConfiguration) {
		if (!utilities.stringHasValue(app.getPojoPackageName())) {
			throw new ForbiddenOperationException("The app " + app.getApplicationCode() + " has no package name!");
		}
		
		String fullClassName = tableConfiguration.generateFullClassName(app);
		
		if (!checkIfIsAlredyGenerated(fullClassName)) {
			OpenConnection appConn = app.openConnection();
			
			try {
				tableConfiguration.generateRecordClass(app, true);
				
				this.alreadyGeneratedClasses.add(fullClassName);
			}
			finally {
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
	protected List<SyncRecord> searchNextRecords(Connection conn) {
		if (pojoGenerated)
			return null;
		
		List<SyncRecord> records = new ArrayList<SyncRecord>();
		
		records.add(new PojoGenerationRecord(getMainSrcTableConf()));
		
		return records;
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		return new PojoGenerationSearchParams(this, limits, conn);
	}
	
	@Override
	public PojoGenerationController getRelatedOperationController() {
		return (PojoGenerationController) super.getRelatedOperationController();
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
}
