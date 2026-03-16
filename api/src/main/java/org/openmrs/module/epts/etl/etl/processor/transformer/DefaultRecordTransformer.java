package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
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
		
		if (utilities.arrayHasElement(srcObject.getTransformationSrcObject())) {
			srcObjects.addAll(srcObject.getTransformationSrcObject());
		}
		
		if (migratedDstParent != null && utilities.arrayHasElement(migratedDstParent.getTransformationSrcObject())) {
			srcObjects.addAll(migratedDstParent.getTransformationSrcObject());
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
		
		boolean skipMigratedDstParent = false;
		
		Field pk = transformedRec.getField(dstConf.getPrimaryKey().asSimpleKey().getName());
		EtlDatabaseObject parentInDst = null;
		DstConf sharedPkDstConf = null;
		
		if (pk.getTransformingInfo().isLoadedWithDstValue()) {
			skipMigratedDstParent = true;
			
			if (dstConf.useSharedPKKey()) {
				if (migratedDstParent != null && migratedDstParent.getRelatedConfiguration().getObjectName()
				        .equals(dstConf.getSharedTableConf(dstConn).getTableName())) {
					
					parentInDst = migratedDstParent;
				} else {
					Oid oid = Oid.fastCreate(dstConf.getSharedTableConf(dstConn), pk.getValue());
					parentInDst = DatabaseObjectDAO.getByOid(dstConf.getSharedTableConf(dstConn), oid, dstConn);
				}
			}
			
		} else {
			if (dstConf.useSharedPKKey()) {
				List<SrcConf> srcForSharedPk = dstConf.getSharedKeyRefInfo(srcConn)
				        .findRelatedSrcConfWhichAsAtLeastOnematchingDst(processor.getRelatedEtlOperationConfig());
				
				if (CommonUtilities.getInstance().arrayHasNoElement(srcForSharedPk)) {
					throw new ForbiddenOperationException(
					        "There are relashioship which cannot auto resolved as there is no configured etl for "
					                + dstConf.getSharedKeyRefInfo(srcConn).getTableName() + " as source and destination!");
				}
				
				for (SrcConf src : srcForSharedPk) {
					sharedPkDstConf = ((EtlItemConfiguration) src.getParentConf()).findDstTable(
					    processor.getRelatedEtlOperationConfig(), dstConf.getSharedKeyRefInfo(dstConn).getTableName());
					
					EtlDatabaseObject recordAsSrc = src.createRecordInstance();
					recordAsSrc.setRelatedConfiguration(src);
					
					recordAsSrc.copyFrom(srcObject.getSharedPkObj());
					parentInDst = sharedPkDstConf.getTransformerInstance().transform(processor, recordAsSrc, sharedPkDstConf,
					    migratedDstParent, TransformationType.INNER, srcConn, dstConn);
					
					if (parentInDst != null) {
						
						parentInDst.setSrcRelatedObject(srcObject.getSharedPkObj());
						
						break;
					}
				}
			}
			
			if (dstConf.useSharedPKKey()) {
				if (parentInDst == null) {
					throw new ForbiddenOperationException(
					        "The related shared pk object for record " + srcObject + " cannot be found or transformed");
				} else {
					transformedRec.setSharedPkObj(parentInDst);
				}
			}
		}

		if (parentInDst != null) {
			pk = transformedRec.getField(dstConf.getSharedKeyRefInfo(dstConn));
			
			parentInDst.loadObjectIdData();
			
			pk.setValue(parentInDst.getObjectId().asSimpleValue());
			pk.getTransformingInfo().setTransformationDatasource(sharedPkDstConf);
			
		} else {
			transformedRec.loadObjectIdData(dstConf);
			
			if (dstConf.useManualGeneratedObjectId() && !dstConf.getRelatedEtlConf().isDoNotTransformsPrimaryKeys()) {
				transformedRec.getObjectId().asSimpleKey().setValue(dstConf.retriveNextRecordId(processor));
			}
		}
		
		if (transformationType.onDemand()) {
			transformedRec.setUuid(UUID.randomUUID().toString());
		}
		
		//Force the related child field to be mapped to the dstPK
		if (migratedDstParent != null && !skipMigratedDstParent) {
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
		
		processor.logTrace("Record " + srcObject + " transformed to " + transformedRec);
		
		transformedRec.setTransformationSrcObject(srcObjects);
		
		return transformedRec;
	}
	
}
