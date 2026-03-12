package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DefaultRecordTransformer implements EtlRecordTransformer {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static DefaultRecordTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private DefaultRecordTransformer() {
	}
	
	public static DefaultRecordTransformer getInstance() {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			defaultTransformer = new DefaultRecordTransformer();
			
			return defaultTransformer;
		}
	}
	
	@Override
	public EtlDatabaseObject transform(EtlProcessor processor, EtlDatabaseObject srcObject, DstConf dstConf,
	        EtlDatabaseObject migratedDstParent, TransformationType transformationType, Connection srcConn,
	        Connection dstConn) throws DBException, EtlTransformationException {
		
		processor.logTrace("Transforming dstRecord " + srcObject);
		
		List<EtlDatabaseObject> srcObjects = new ArrayList<>();
		
		srcObjects.add(srcObject);
		
		if (srcObject.shasSharedPkObj()) {
			srcObjects.add(srcObject.getSharedPkObj());
		}
		
		if (migratedDstParent != null) {
			srcObjects.add(migratedDstParent);
		}
		
		if (srcObject.hasAuxLoadObject()) {
			for (EtlDatabaseObject auxObject : srcObject.getAuxLoadObject()) {
				srcObjects.add(auxObject);
				
				if (auxObject.shasSharedPkObj()) {
					srcObjects.add(auxObject.getSharedPkObj());
				}
				
				if (auxObject.hasAuxLoadObject()) {
					for (EtlDatabaseObject innerObject : auxObject.getAuxLoadObject()) {
						srcObjects.add(innerObject);
						
						if (innerObject.shasSharedPkObj()) {
							srcObjects.add(innerObject.getSharedPkObj());
						}
					}
				}
			}
		}
		
		for (EtlAdditionalDataSource mappingInfo : dstConf.getSrcConf().getAvaliableExtraDataSource()) {
			
			List<EtlDatabaseObject> avaliableObjects = mappingInfo.allowMultipleSrcObjectsForLoading() ? srcObjects
			        : utilities.parseToList(srcObject);
			
			EtlDatabaseObject relatedSrcObject = mappingInfo.loadRelatedSrcObject(processor, srcObject, avaliableObjects,
			    srcConn);
			
			if (relatedSrcObject == null) {
				
				/*
				 * If the transformation is not principal, then mean the record is being transformed as parent of other record. So we force the tranformation
				 */
				if (mappingInfo.isRequired() && transformationType.isPrincipal()) {
					return null;
				} else if (!transformationType.isPrincipal()) {
					relatedSrcObject = mappingInfo.newInstance();
					relatedSrcObject.setRelatedConfiguration(mappingInfo);
				}
			}
			
			srcObjects.add(relatedSrcObject);
		}
		
		EtlDatabaseObject transformedRec = dstConf.createRecordInstance();
		
		transformedRec.setRelatedConfiguration(dstConf);
		transformedRec.setSrcRelatedObject(srcObject);
		
		for (FieldsMapping fieldsMapping : dstConf.getAllMapping()) {
			fieldsMapping.getTransformerInstance().performeFieldTransformation(processor, srcObject, transformedRec,
			    srcObjects, fieldsMapping, srcConn, dstConn);
		}
		
		if (dstConf.useSharedPKKey()) {
			List<SrcConf> srcForSharedPk = dstConf.getSharedKeyRefInfo(srcConn)
			        .findRelatedSrcConfWhichAsAtLeastOnematchingDst(processor.getRelatedEtlOperationConfig());
			
			if (CommonUtilities.getInstance().arrayHasNoElement(srcForSharedPk)) {
				throw new ForbiddenOperationException(
				        "There are relashioship which cannot auto resolved as there is no configured etl for "
				                + dstConf.getSharedKeyRefInfo(srcConn).getTableName() + " as source and destination!");
			}
			
			EtlDatabaseObject dstParent = null;
			
			for (SrcConf src : srcForSharedPk) {
				DstConf sharedPkDstConf = ((EtlItemConfiguration) src.getParentConf()).findDstTable(
				    processor.getRelatedEtlOperationConfig(), dstConf.getSharedKeyRefInfo(dstConn).getTableName());
				
				EtlDatabaseObject recordAsSrc = src.createRecordInstance();
				recordAsSrc.setRelatedConfiguration(src);
				
				recordAsSrc.copyFrom(srcObject.getSharedPkObj());
				dstParent = sharedPkDstConf.getTransformerInstance().transform(processor, recordAsSrc, sharedPkDstConf,
				    migratedDstParent, TransformationType.INNER, srcConn, dstConn);
				
				if (dstParent != null) {
					
					dstParent.setSrcRelatedObject(srcObject.getSharedPkObj());
					break;
				}
			}
			
			if (dstParent == null) {
				throw new ForbiddenOperationException(
				        "The related shared pk object for record " + srcObject + " cannot be transformed");
			} else {
				transformedRec.setSharedPkObj(dstParent);
			}
		}
		
		transformedRec.loadObjectIdData(dstConf);
		
		if (dstConf.useManualGeneratedObjectId() && !dstConf.getRelatedEtlConf().isDoNotTransformsPrimaryKeys()) {
			transformedRec.getObjectId().asSimpleKey().setValue(dstConf.retriveNextRecordId(processor));
		}
		
		processor.logTrace("Record " + srcObject + " transformed to " + transformedRec);
		
		return transformedRec;
	}
	
}
