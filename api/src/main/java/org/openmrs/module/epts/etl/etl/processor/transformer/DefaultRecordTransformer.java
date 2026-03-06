package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
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
	
	private List<EtlDatabaseObject> loadAvaliableTransformationSrcObjects(EtlDatabaseObject srcObject, DstConf dstConf,
	        TransformationType transformationType, Connection srcConn, Connection dstConn) throws DBException {
		
		List<EtlDatabaseObject> srcObjects = new ArrayList<>();
		
		srcObjects.add(srcObject);
		
		if (srcObject.shasSharedPkObj()) {
			srcObjects.add(srcObject.getSharedPkObj());
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
			
			List<EtlDatabaseObject> relatedSrcObject = mappingInfo.loadRelatedSrcObjects(avaliableObjects, srcConn);
			
			if (relatedSrcObject == null) {
				
				/*
				 * If the transformation is not principal, then mean the record is being transformed as parent of other record. So we force the tranformation
				 */
				if (mappingInfo.isRequired() && transformationType.isPrincipal()) {
					return null;
				} else {
					relatedSrcObject = new ArrayList<>();
					relatedSrcObject.add(mappingInfo.newInstance());
					relatedSrcObject.get(0).setRelatedConfiguration(mappingInfo);
				}
			}
			
			srcObjects.addAll(relatedSrcObject);
		}
		
		return srcObjects;
	}
	
	@Override
	public List<EtlDatabaseObject> transform(TaskProcessor<EtlDatabaseObject> processor, EtlDatabaseObject srcObject,
	        DstConf dstConf, EtlDatabaseObject migratedDstParent, TransformationType transformationType, Connection srcConn,
	        Connection dstConn) throws DBException, EtlTransformationException {
		
		processor.logTrace("Transforming dstRecord for srcObject " + srcObject);
		
		List<EtlDatabaseObject> srcObjects;
		
		if (utilities.arrayHasElement(srcObject.getTransformationSrcObject())) {
			srcObjects = srcObject.getTransformationSrcObject();
		} else {
			srcObjects = loadAvaliableTransformationSrcObjects(srcObject, dstConf, transformationType, srcConn, dstConn);
			
			if (utilities.arrayHasNoElement(srcObjects)) {
				return null;
			}
		}
		
		List<EtlDatabaseObject> transformableSrcObjects = dstConf.loadRelatedSrcObject(srcObjects, dstConn);
		List<EtlDatabaseObject> transformedObjects = new ArrayList<>();
		
		for (EtlDatabaseObject tranformableObject : transformableSrcObjects) {
			EtlDatabaseObject transformedRec = dstConf.createRecordInstance();
			
			transformedRec.setRelatedConfiguration(dstConf);
			transformedRec.setSrcRelatedObject(tranformableObject);
			transformedRec.setTransformationSrcObject(srcObjects);
			
			for (FieldsMapping fieldsMapping : dstConf.getAllMapping()) {
				//We try to auto map the migratedDstParent PK with a related FK on this transformedRec
				if (migratedDstParent != null) {
					ParentTable parent = dstConf.findParentRefInfoByField(fieldsMapping.getDstField());
					
					if (parent != null) {
						//We only pass the migratedDstParent as source object to give it high priority
						fieldsMapping.getTransformerInstance().transform(transformedRec,
						    utilities.parseToList(migratedDstParent), fieldsMapping, srcConn, dstConn);
						
						continue;
					}
				}
				
				fieldsMapping.getTransformerInstance().transform(transformedRec, srcObjects, fieldsMapping, srcConn,
				    dstConn);
			}
			
			if (dstConf.useSharedPKKey()) {
				List<SrcConf> srcForSharedPk = dstConf.getSharedKeyRefInfo(dstConn)
				        .findRelatedSrcConfWhichAsAtLeastOnematchingDst(processor.getRelatedEtlOperationConfig());
				
				if (CommonUtilities.getInstance().arrayHasNoElement(srcForSharedPk)) {
					throw new EtlTransformationException(
					        "There are relashioship which cannot auto resolved as there is no configured etl for "
					                + dstConf.getSharedKeyRefInfo(dstConn).getTableName() + " as source and destination!",
					        tranformableObject, ActionOnEtlException.ABORT);
				}
				
				EtlDatabaseObject dstParent = null;
				
				for (SrcConf src : srcForSharedPk) {
					DstConf sharedPkDstConf = ((EtlItemConfiguration) src.getParentConf()).findDstTable(
					    processor.getRelatedEtlOperationConfig(), dstConf.getSharedKeyRefInfo(dstConn).getTableName());
					
					EtlDatabaseObject recordAsSrc = src.createRecordInstance();
					recordAsSrc.setRelatedConfiguration(src);
					
					recordAsSrc.copyFrom(tranformableObject.getSharedPkObj());
					
					List<EtlDatabaseObject> dstParents = sharedPkDstConf.getTransformerInstance().transform(processor,
					    recordAsSrc, sharedPkDstConf, migratedDstParent, TransformationType.INNER, srcConn, dstConn);
					
					if (utilities.arrayHasElement(dstParents)) {
						dstParent = dstParents.get(0);
						
						dstParent.setSrcRelatedObject(tranformableObject.getSharedPkObj());
						break;
					}
				}
				
				if (dstParent == null) {
					throw new EtlTransformationException(
					        "The related shared pk object for record " + tranformableObject + " cannot be transformed",
					        tranformableObject, ActionOnEtlException.ABORT);
				} else {
					transformedRec.setSharedPkObj(dstParent);
				}
			}
			
			transformedRec.loadObjectIdData(dstConf);
			
			if (dstConf.useManualGeneratedObjectId() && !dstConf.getRelatedEtlConf().isDoNotTransformsPrimaryKeys()) {
				transformedRec.getObjectId().asSimpleKey().setValue(dstConf.retriveNextRecordId(processor));
			}
			
			transformedObjects.add(transformedRec);
			
			processor.logTrace("Record " + tranformableObject + " transformed to " + transformedRec);
		}
		
		return transformedObjects;
	}
	
}
