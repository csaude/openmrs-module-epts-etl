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
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.etl.model.LoadStatus;
import org.openmrs.module.epts.etl.etl.model.LoadingType;
import org.openmrs.module.epts.etl.etl.processor.transformer.TransformationType;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
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
				
				try {
					
					for (DstConf mappingInfo : getEtlItemConfiguration().getDstConf()) {
						EtlDatabaseObject dstObject = mappingInfo.getTransformerInstance().transform(this, srcRecord,
						    mappingInfo, null, TransformationType.PRINCIPAL, srcConn, dstConn);
						
						if (dstObject != null) {
							logTrace("dstRecord " + srcRecord + " transforming to " + dstObject);
							
							LoadRecord etlRec = LoadRecord.initEtlRecord(this, dstObject.getSrcRelatedObject(), dstObject,
							    mappingInfo);
							
							loadHelper.addRecord(etlRec);
							
						} else {
							logTrace("The dstRecord " + srcRecord + " could not be transformed");
						}
					}
				}
				catch (EtlTransformationException e) {
					if (getRelatedEtlConfiguration().getGeneralBehaviourOnEtlException().log()) {
						EtlLoadHelper.logEtlError(this, srcRecord, e, srcConn, dstConn);
					} else {
						throw e;
					}
				}
			}
			
			logDebug("Initializing the loading of " + etlObjects.size() + " " + getSrcConf().getFullTableName());
			
			loadHelper.load(srcConn, dstConn);
			
			tryToPerfomeEtlOnChild(this.getEtlItemConfiguration(), loadHelper, srcConn, dstConn);
			
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
	
	private void tryToPerfomeEtlOnChild(EtlItemConfiguration itemConf, EtlLoadHelper loadHelper, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		if (itemConf.hasChildItemConf()) {
			for (EtlItemConfiguration childItemConf : itemConf.getChildItemConf()) {
				childItemConf.fullLoad(this.getRelatedEtlOperationConfig());
				
				for (LoadRecord rec : loadHelper.getAllRecordsAsLoadRecord(childItemConf.getRelatedParentDstConf(),
				    LoadStatus.SUCCESS)) {
					performeEtlOnChildItem(childItemConf, rec, srcConn, dstConn);
				}
			}
		}
	}
	
	private void performeEtlOnChildItem(EtlItemConfiguration itemConf, LoadRecord parentLoadRecord, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		List<EtlDatabaseObject> etlObjects = itemConf.getSrcConf().searchRecords(this.getEngine(),
		    parentLoadRecord.getSrcRecord(), srcConn);
		
		EtlLoadHelper loadHelper = new EtlLoadHelper(this, itemConf.getDstConf(), etlObjects.size(), LoadingType.PRINCIPAL);
		
		for (EtlDatabaseObject srcRecord : etlObjects) {
			srcRecord.loadObjectIdData(itemConf.getSrcConf());
			
			try {
				
				for (DstConf mappingInfo : itemConf.getDstConf()) {
					
					try {
						if (srcRecord.getFieldValue("value_text") != null) {
							System.out.println();
						}
					}
					catch (Exception e) {}
					
					if (mappingInfo.checkIfSrcObjectCanBeLoaded(srcRecord)) {
						
						EtlDatabaseObject dstObject = mappingInfo.getTransformerInstance().transform(this, srcRecord,
						    mappingInfo, parentLoadRecord.getDstRecord(), TransformationType.PRINCIPAL, srcConn, dstConn);
						
						if (dstObject != null) {
							logTrace("dstRecord " + srcRecord + " transforming to " + dstObject);
							
							LoadRecord etlRec = LoadRecord.initEtlRecord(this, dstObject.getSrcRelatedObject(), dstObject,
							    mappingInfo);
							
							loadHelper.addRecord(etlRec);
							
						} else {
							logTrace("The dstRecord " + srcRecord + " could not be transformed");
						}
					}
				}
			}
			catch (EtlTransformationException e) {
				if (getRelatedEtlConfiguration().getGeneralBehaviourOnEtlException().log()) {
					EtlLoadHelper.logEtlError(this, srcRecord, e, srcConn, dstConn);
				} else {
					throw e;
				}
			}
		}
		
		logDebug("Initializing the loading of " + etlObjects.size() + " " + getSrcConf().getFullTableName());
		
		loadHelper.load(srcConn, dstConn);
		
		tryToPerfomeEtlOnChild(itemConf, loadHelper, srcConn, dstConn);
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		ReloadRecordsWithDefaultParentProcessor p = new ReloadRecordsWithDefaultParentProcessor(
		        (Engine<EtlDatabaseObject>) this.getEngine(), limits, false);
		
		p.setRelatedEtlProcessor(this);
		
		return p;
	}
}
