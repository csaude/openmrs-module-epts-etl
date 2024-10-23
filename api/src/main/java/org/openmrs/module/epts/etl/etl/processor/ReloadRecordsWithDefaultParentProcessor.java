package org.openmrs.module.epts.etl.etl.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.EtlLoadHelper;
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.etl.processor.transformer.TransformationType;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.RecordWithDefaultParentInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents a generic processor for ETL operation
 * 
 * @author jpboane
 */
public class ReloadRecordsWithDefaultParentProcessor extends TaskProcessor<EtlDatabaseObject> {
	
	private EtlProcessor relatedEtlProcessor;
	
	public ReloadRecordsWithDefaultParentProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	public EtlProcessor getRelatedEtlProcessor() {
		return relatedEtlProcessor;
	}
	
	public void setRelatedEtlProcessor(EtlProcessor relatedEtlProcessor) {
		this.relatedEtlProcessor = relatedEtlProcessor;
	}
	
	public EtlItemConfiguration getRelatedEtlItemConfiguration() {
		return relatedEtlProcessor.getEtlItemConfiguration();
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
			
			SrcConf mainSrc = getRelatedEtlItemConfiguration().getSrcConf();
			
			if (!mainSrc.isFullLoaded()) {
				mainSrc.fullLoad(srcConn);
			}
			
			for (EtlDatabaseObject etlObj : etlObjects) {
				
				List<RecordWithDefaultParentInfo> rcs = RecordWithDefaultParentInfo.getAllOfSrcRecord(mainSrc,
				    etlObj.getObjectId().asSimpleNumericValue(), srcConn);
				
				for (RecordWithDefaultParentInfo recWithDefaultParentInfo : rcs) {
					
					recWithDefaultParentInfo.fullLoad(this.getRelatedEtlItemConfiguration(), srcConn, dstConn);
					
					ParentTable parentRefInfo = recWithDefaultParentInfo.getParentRefInfo();
					
					List<SrcConf> avaliableSrcForCurrParent = parentRefInfo
					        .findRelatedSrcConfWhichAsAtLeastOnematchingDst(getRelatedEtlOperationConfig());
					
					if (utilities.arrayHasNoElement(avaliableSrcForCurrParent)) {
						throw new ForbiddenOperationException(
						        "There are relashioship which cannot auto resolved as there is no configured etl for "
						                + parentRefInfo.getTableName() + " as source and destination!");
					}
					
					EtlDatabaseObject dstParent = null;
					EtlDatabaseObject recordAsSrc = null;
					
					for (SrcConf src : avaliableSrcForCurrParent) {
						DstConf dst = ((EtlItemConfiguration) src.getParentConf())
						        .findDstTable(getRelatedEtlOperationConfig(), parentRefInfo.getTableName());
						
						recordAsSrc = src.createRecordInstance();
						recordAsSrc.setRelatedConfiguration(src);
						
						recordAsSrc.copyFrom(recWithDefaultParentInfo.getParentRecordInOrigin());
						
						dstParent = dst.getTransformerInstance().transform(this, recordAsSrc, dst, TransformationType.INNER,
						    srcConn, dstConn);
						
						if (dstParent != null) {
							
							LoadRecord parentData = new LoadRecord(recordAsSrc, dstParent, src, dst,
							        this.getRelatedEtlProcessor());
							
							DBException exception = null;
							
							try {
								EtlLoadHelper.performeParentLoading(parentData, srcConn, dstConn);
							}
							catch (DBException e) {
								throw new EtlExceptionImpl(e);
							}
							finally {
								
								if (parentData.getResultItem().hasInconsistences()
								        || exception != null && exception.isIntegrityConstraintViolationException()) {
									
									logDebug("The parent for default for parent ["
									        + recWithDefaultParentInfo.getParentRecordInOrigin()
									        + "] could not be loaded. The dstRecord [");
									/*
									InconsistenceInfo.generate(recWithDefaultParentInfo.getDstRecord().generateTableName(),
										recWithDefaultParentInfo.getDstRecord().getObjectId(),
									        parentRefInfo.getParentTableConfInDst().getTableName(),
									        parentRefInfo.getParentRecordInOrigin().getObjectId().getSimpleValueAsInt(),
									        null, this.getDstConf().getOriginAppLocationCode()));*/
									
								}
							}
							
							break;
						}
						
					}
					
					try {
						recWithDefaultParentInfo.getDstRecord()
						        .changeParentValue(recWithDefaultParentInfo.getParentRefInfo(), dstParent);
					}
					catch (NullPointerException e) {
						e.printStackTrace();
						
						throw e;
					}
				}
			}
		}
		catch (Exception e) {
			logWarn("Error ocurred on thread " + getProcessorId() + " On Records [" + getLimits() + "]... \n");
			logError(e.getLocalizedMessage());
			logError(e.getMessage());
			
			getTaskResultInfo().setFatalException(e);
		}
		
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		throw new ForbiddenOperationException("This method is forbidden");
	}
}
