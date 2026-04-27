package org.openmrs.module.epts.etl.etl.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.EtlLoadHelper;
import org.openmrs.module.epts.etl.etl.model.LoadingType;
import org.openmrs.module.epts.etl.etl.processor.transformer.TransformationType;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.EtlInfo;
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
			perform(this.getEtlItemConfiguration(), etlObjects, null, LoadingType.PRINCIPAL, srcConn, dstConn);
		}
		catch (Exception e) {
			logWarn("Error ocurred on thread " + getProcessorId() + " On Records [" + getLimits() + "]... \n");
			logError(e.getLocalizedMessage());
			logError(this.getEngine().getEngineId() + ": " + e.getMessage());
			
			getTaskResultInfo().setFatalException(e);
		}
	}
	
	public EtlLoadHelper perform(EtlItemConfiguration etlItemConf, List<EtlDatabaseObject> etlObjects,
	        EtlDatabaseObject parentMigratedRec, LoadingType loadingType, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		for (EtlDatabaseObject record : etlObjects) {
			EtlDatabaseObject srcRecord = (EtlDatabaseObject) record;
			srcRecord.loadObjectIdData(etlItemConf.getSrcConf());
			
			for (DstConf mappingInfo : etlItemConf.getDstConf()) {
				if (mappingInfo.isDisabled()) {
					continue;
				}
				
				try {
					if (mappingInfo.checkIfSrcObjectCanBeLoaded(srcRecord)) {
						EtlDatabaseObject dstObject = mappingInfo.getTransformerInstance().transform(this, srcRecord,
						    mappingInfo, parentMigratedRec, TransformationType.PRINCIPAL, srcConn, dstConn);
						
						if (dstObject != null) {
							record.addDestinationRecord(dstObject);
							
							logTrace("dstRecord " + srcRecord + " transforming to " + dstObject);
						} else {
							logTrace("The dstRecord " + srcRecord + " could not be transformed");
						}
					}
				}
				catch (EtlTransformationException e) {
					if (getRelatedEtlConfiguration().getGeneralBehaviourOnEtlException().log()) {
						EtlDatabaseObject dstObject = mappingInfo.createRecordInstance();
						
						dstObject.setEtlInfo(EtlInfo.initEtlRecord(this, dstObject, dstObject));
						
						dstObject.getEtlInfo().setExceptionOnEtl(e);
						
						record.addDestinationRecord(dstObject);
					} else {
						throw e;
					}
				}
			}
		}
		
		logDebug("Initializing the loading of " + etlObjects.size() + " " + etlItemConf.getSrcConf().getFullTableName());
		
		EtlLoadHelper loadHelper = new EtlLoadHelper(this, etlObjects, loadingType);
		
		loadHelper.load(srcConn, dstConn);
		
		tryToPerfomeEtlOnChild(etlItemConf, loadHelper, srcConn, dstConn);
		
		logInfo("ETL OPERATION [" + etlItemConf.getConfigCode() + "] DONE ON " + etlObjects.size() + "' RECORDS");
		
		return loadHelper;
	}
	
	private void tryToPerfomeEtlOnChild(EtlItemConfiguration itemConf, EtlLoadHelper loadHelper, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		if (itemConf.hasChildItemConf()) {
			for (EtlItemConfiguration childItemConf : itemConf.getChildItemConf()) {
				childItemConf.fullLoad(this.getRelatedEtlOperationConfig());
				
				for (EtlDatabaseObject rec : loadHelper
				        .getAllSuccedTransformedObjects(childItemConf.getRelatedParentDstConf())) {
					performeEtlOnChildItem(childItemConf, rec, srcConn, dstConn);
				}
			}
		}
	}
	
	private void performeEtlOnChildItem(EtlItemConfiguration itemConf, EtlDatabaseObject transformedParent,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		List<EtlDatabaseObject> etlObjects = itemConf.getSrcConf().searchRecords(this.getEngine(),
		    transformedParent.getEtlInfo().getRelatedSrcObject(), transformedParent.getEtlInfo().getAvaliableSrcObjects(),
		    srcConn);
		
		if (!etlObjects.isEmpty()) {
			perform(itemConf, etlObjects, transformedParent, LoadingType.INNER, srcConn, dstConn);
		}
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		ReloadRecordsWithDefaultParentProcessor p = new ReloadRecordsWithDefaultParentProcessor(
		        (Engine<EtlDatabaseObject>) this.getEngine(), limits, false);
		
		p.setRelatedEtlProcessor(this);
		
		return p;
	}
}
