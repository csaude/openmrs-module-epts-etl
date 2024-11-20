package org.openmrs.module.epts.etl.etl.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.EtlLoadHelper;
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.etl.model.LoadingType;
import org.openmrs.module.epts.etl.etl.processor.transformer.TransformationType;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents a generic processor for ETL operation
 * 
 * @author jpboane
 */
public class EtlProcessor extends TaskProcessor<EtlDatabaseObject> {
	
	public EtlProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> getSearchParams() {
		return (EtlDatabaseObjectSearchParams) super.getSearchParams();
	}
	
	public DBConnectionInfo getDstConnInfo() {
		return this.getRelatedOperationController().getDstConnInfo();
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return this.getRelatedOperationController().getSrcConnInfo();
	}
	
	@Override
	public EtlController getRelatedOperationController() {
		return (EtlController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn) throws DBException {
		try {
			EtlLoadHelper loadHelper = new EtlLoadHelper(this, this.getEtlItemConfiguration().getDstConf(),
			        etlObjects.size(), LoadingType.PRINCIPAL);
			
			for (EtlObject record : etlObjects) {
				
				EtlDatabaseObject srcRecord = (EtlDatabaseObject) record;
				srcRecord.loadObjectIdData(getSrcConf());
				
				for (DstConf mappingInfo : getEtlItemConfiguration().getDstConf()) {
					EtlDatabaseObject dstObject = mappingInfo.getTransformerInstance().transform(this, srcRecord,
					    mappingInfo, TransformationType.PRINCIPAL, srcConn, dstConn);
					
					if (dstObject != null) {
						logTrace("dstRecord " + srcRecord + " transforming to " + dstObject);
						
						LoadRecord etlRec = initEtlRecord(srcRecord, dstObject, mappingInfo);
						
						loadHelper.addRecord(etlRec);
						
					} else {
						logTrace("The dstRecord " + srcRecord + " could not be transformed");
					}
				}
			}
			
			logDebug("Initializing the loading of " + etlObjects.size() + " " + getSrcConf().getFullTableName());
			
			loadHelper.load(srcConn, dstConn);
			
			logInfo("ETL OPERATION [" + getEtlItemConfiguration().getConfigCode() + "] DONE ON " + etlObjects.size()
			        + "' RECORDS");
		}
		catch (Exception e) {
			logWarn("Error ocurred on thread " + getProcessorId() + " On Records [" + getLimits() + "]... \n");
			logError(e.getLocalizedMessage());
			logError(e.getMessage());
			
			getTaskResultInfo().setFatalException(e);
		}
		
	}
	
	public LoadRecord initEtlRecord(EtlDatabaseObject srcObject, EtlDatabaseObject destObject, DstConf mappingInfo) {
		return new LoadRecord(srcObject, destObject, getSrcConf(), mappingInfo, this);
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		ReloadRecordsWithDefaultParentProcessor p = new ReloadRecordsWithDefaultParentProcessor(
		        (Engine<EtlDatabaseObject>) this.getEngine(), limits, false);
		
		p.setRelatedEtlProcessor(this);
		
		return p;
	}
}
