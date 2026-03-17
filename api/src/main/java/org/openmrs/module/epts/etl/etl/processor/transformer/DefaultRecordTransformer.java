package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.MissingRequiredTransformationObject;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DefaultRecordTransformer implements EtlRecordTransformer {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static final DefaultRecordTransformer INSTANCE = new DefaultRecordTransformer();
	
	private DefaultRecordTransformer() {
	}
	
	public static DefaultRecordTransformer getInstance() {
		return INSTANCE;
	}
	
	@Override
	public EtlDatabaseObject transform(EtlProcessor processor, EtlDatabaseObject srcObject, DstConf dstConf,
	        EtlDatabaseObject migratedDstParent, TransformationType transformationType, Connection srcConn,
	        Connection dstConn) throws DBException, EtlTransformationException {
		
		if (srcObject == null) {
			throw new EtlTransformationException("SrcObject cannot be null", null, ActionOnEtlException.ABORT);
		}
		
		processor.logTrace("Transforming dstRecord " + srcObject);
		
		List<EtlDatabaseObject> srcObjects = collectSourceObjects(processor, srcObject, migratedDstParent, dstConf,
		    transformationType, srcConn);
		
		if (srcObjects.isEmpty()) {
			return null;
		}
		
		EtlDatabaseObject transformedRec = dstConf.createRecordInstance();
		transformedRec.setSrcRelatedObject(srcObject);
		
		applyFieldTransformations(processor, transformedRec, srcObjects, srcConn, dstConn);
		
		resolvePrimaryKeyAndParent(processor, transformedRec, migratedDstParent, srcConn, dstConn);
		
		if (transformationType.onDemand()) {
			transformedRec.setUuid(UUID.randomUUID().toString());
		}
		
		transformedRec.setTransformationSrcObject(srcObjects);
		
		processor.logTrace("Record " + srcObject + " transformed to " + transformedRec);
		
		return transformedRec;
	}
	
	private void resolvePrimaryKeyAndParent(EtlProcessor processor, EtlDatabaseObject transformedRec,
	        EtlDatabaseObject migratedDstParent, Connection srcConn, Connection dstConn)
	        throws EtlTransformationException, DBException {
		
		DstConf dstConf = (DstConf) transformedRec.getRelatedConfiguration();
		
		transformedRec.loadObjectIdData(dstConf);
		transformedRec.loadUniqueKeyValues();
		
		if (!dstConf.useSharedPKKey()) {
			if (dstConf.useManualGeneratedObjectId() && !dstConf.getRelatedEtlConf().isDoNotTransformsPrimaryKeys()) {
				transformedRec.getObjectId().asSimpleKey().setValue(dstConf.retriveNextRecordId(processor));
			}
		}
		
		//Force the related child field to be mapped to the dstPK
		if (migratedDstParent != null) {
			for (ParentTable refInfo : dstConf.getParentRefInfo()) {
				if (refInfo.getTableName().equals(migratedDstParent.getRelatedConfiguration().getObjectName())) {
					Field fk = transformedRec.getField(refInfo);
					fk.getTransformingInfo()
					        .setTransformationDatasource((EtlDataSource) migratedDstParent.getRelatedConfiguration());
					fk.setValue(migratedDstParent.getObjectId().asSimpleNumericValue());
					
					break;
				}
			}
		}
	}
	
	private List<EtlDatabaseObject> collectSourceObjects(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject migratedDstParent, DstConf dstConf, TransformationType transformationType, Connection srcConn)
	        throws DBException {
		
		try {
			Set<EtlDatabaseObject> result = new LinkedHashSet<>();
			
			result.add(srcObject);
			
			addSharedObjects(result, srcObject);
			addParentObjects(result, srcObject, migratedDstParent);
			addAuxObjects(result, srcObject);
			addExtraDataSources(processor, result, srcObject, transformationType, srcConn);
			
			return new ArrayList<>(result);
		}
		catch (MissingRequiredTransformationObject e) {
			return null;
		}
	}
	
	private void applyFieldTransformations(EtlProcessor processor, EtlDatabaseObject transformedRec,
	        List<EtlDatabaseObject> srcObjects, Connection srcConn, Connection dstConn) throws DBException {
		
		DstConf dstConf = (DstConf) transformedRec.getRelatedConfiguration();
		
		for (FieldsMapping fieldsMapping : dstConf.getAllMapping()) {
			fieldsMapping.getTransformerInstance().performFieldTransformation(processor, srcObjects.get(0), transformedRec,
			    srcObjects, fieldsMapping, srcConn, dstConn);
		}
	}
	
	private void addAuxObjects(Set<EtlDatabaseObject> srcObjects, EtlDatabaseObject srcObject) {
		if (srcObject.hasAuxLoadObject()) {
			for (EtlDatabaseObject auxObject : srcObject.getAuxLoadObject()) {
				srcObjects.add(auxObject);
				
				if (auxObject.hasSharedPkObj()) {
					srcObjects.add(auxObject.getSharedPkObj());
				}
				
				if (auxObject.hasAuxLoadObject()) {
					for (EtlDatabaseObject innerObject : auxObject.getAuxLoadObject()) {
						srcObjects.add(innerObject);
						
						if (innerObject.hasSharedPkObj()) {
							srcObjects.add(innerObject.getSharedPkObj());
						}
					}
				}
			}
		}
	}
	
	private void addSharedObjects(Set<EtlDatabaseObject> srcObjects, EtlDatabaseObject srcObject) {
		if (srcObject.hasSharedPkObj()) {
			srcObjects.add(srcObject.getSharedPkObj());
		}
	}
	
	private void addParentObjects(Set<EtlDatabaseObject> srcObjects, EtlDatabaseObject srcObject,
	        EtlDatabaseObject migratedDstParent) {
		
		if (migratedDstParent != null) {
			srcObjects.add(migratedDstParent);
		}
		
		if (utilities.listHasElement(srcObject.getTransformationSrcObject())) {
			srcObjects.addAll(srcObject.getTransformationSrcObject());
		}
		
		if (migratedDstParent != null && utilities.listHasElement(migratedDstParent.getTransformationSrcObject())) {
			srcObjects.addAll(migratedDstParent.getTransformationSrcObject());
		}
	}
	
	private void addExtraDataSources(EtlProcessor processor, Set<EtlDatabaseObject> srcObjects, EtlDatabaseObject srcObject,
	        TransformationType transformationType, Connection srcConn) throws DBException {
		SrcConf srcConf = (SrcConf) srcObject.getRelatedConfiguration();
		
		for (EtlAdditionalDataSource mappingInfo : srcConf.getAvaliableExtraDataSource()) {
			
			List<EtlDatabaseObject> avaliableObjects = mappingInfo.allowMultipleSrcObjectsForLoading()
			        ? srcObjects.stream().toList()
			        : utilities.parseToList(srcObject);
			
			EtlDatabaseObject relatedSrcObject = mappingInfo.loadRelatedSrcObject(processor, srcObject, avaliableObjects,
			    srcConn);
			
			if (relatedSrcObject == null) {
				
				/*
				 * If the transformation is not principal, then mean the record is being transformed as parent of other record. So we force the tranformation
				 */
				if (mappingInfo.isRequired() && transformationType.isPrincipal()) {
					throw new MissingRequiredTransformationObject();
				} else if (!transformationType.isPrincipal()) {
					relatedSrcObject = mappingInfo.newInstance();
					relatedSrcObject.setRelatedConfiguration(mappingInfo);
				}
			}
			
			srcObjects.add(relatedSrcObject);
		}
	}
	
}
