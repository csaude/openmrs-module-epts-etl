package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.model.EtlLoadHelper;
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.etl.model.LoadStatus;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Transformer responsible for ensuring that a parent record exists in the destination table and
 * returning its primary key as the transformed value.
 * <p>
 * This transformer is typically used to enforce the creation of related parent records in the
 * destination database during the transformation of a child record. If the corresponding parent
 * record does not yet exist in the destination, the transformer will trigger its migration or
 * creation before returning the parent primary key.
 * </p>
 * <p>
 * Transformer syntax:
 * </p>
 * <pre>
 * ParentOnDemandLoadTransformer(
 *      parentTable,
 *      parentFieldOnDataSourceObject,
 *      dstField1:srcFieldOrValue1,
 *      dstField2:srcFieldOrValue2,
 *      ...
 * )
 * </pre>
 * <p>
 * Parameters:
 * </p>
 * <ul>
 * <li><b>parentTable</b> – destination parent table name</li>
 * <li><b>parentFieldOnDataSourceObject</b> – field used to locate the parent record in the source
 * data</li>
 * <li><b>dstField:srcFieldOrValue</b> – optional additional fields used to populate the parent
 * record when it is created</li>
 * </ul>
 * <p>
 * Behavior:
 * </p>
 * <ol>
 * <li>Resolve the parent identifier from the source data.</li>
 * <li>Locate the corresponding parent record in the destination database.</li>
 * <li>If the parent does not exist in the destination, load or create it using the configured ETL
 * mapping.</li>
 * <li>Return the parent record primary key as the transformed value.</li>
 * </ol>
 * <p>
 * This transformer is designed for high-performance migrations where parent objects must be loaded
 * on-demand during the transformation of child records.
 * </p>
 */
public class ParentOnDemandLoadTransformer implements EtlFieldTransformer {
	
	private final Object lock = new Object();
	
	private static final Map<String, ParentOnDemandLoadTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private String parentTable;
	
	private String parentField;
	
	private EtlItemConfiguration newParentRecordEtlItemConf;
	
	private EtlItemConfiguration existingParentRecordEtlItemConf;
	
	private FieldsMapping parentSourceFieldMapping;
	
	private DstConf dstConf;
	
	private List<String> parentFieldDefinitions;
	
	private List<FieldsMapping> parentFieldMappings;
	
	private MissingFastSrcParentBehaviour missingFastSrcParentBehaviour;
	
	public ParentOnDemandLoadTransformer(String parentTable, String parentField, List<String> parentFieldDefinitions,
	    DstConf dstConf) {
		
		this.parentFieldDefinitions = parentFieldDefinitions;
		this.parentField = parentField;
		this.parentTable = parentTable;
		this.dstConf = dstConf;
		
		this.missingFastSrcParentBehaviour = utilities.arrayHasElement(parentFieldDefinitions)
		        ? MissingFastSrcParentBehaviour.CREATE_ON_DST
		        : MissingFastSrcParentBehaviour.COMPLAIN;
		
		this.parentSourceFieldMapping = fastCreateFieldMap(parentField, null, dstConf);
		
		if (utilities.arrayHasElement(this.parentFieldDefinitions)) {
			this.parentFieldMappings = new ArrayList<>();
			
			for (String fieldData : this.parentFieldDefinitions) {
				String[] mapping = fieldData.split(":", 2);
				
				if (mapping.length != 2) {
					throw new EtlExceptionImpl("Wrong format for newObjectData within the " + getTransformerDsc() + "\n"
					        + "Each object param must be specified as filedName:srcFieldOrValue");
				}
				
				String dstField = mapping[0];
				String srcFieldOrValue = mapping[1];
				
				if (!utilities.stringHasValue(srcFieldOrValue) || srcFieldOrValue.toLowerCase().equals("null")) {
					srcFieldOrValue = null;
				}
				
				FieldsMapping fm = fastCreateFieldMap(srcFieldOrValue, dstField, dstConf);
				
				this.parentFieldMappings.add(fm);
			}
		}
	}
	
	private FieldsMapping fastCreateFieldMap(String parentFieldName, String dstField, DstConf dstConf) {
		FieldsMapping fieldMap = FieldsMapping.fastCreate(parentFieldName, dstField, dstConf);
		
		if (!fieldMap.hasDataSourceName() && !fieldMap.isMapToNullValue()) {
			
			if (utilities.isNumeric(parentFieldName)) {
				fieldMap.setSrcValue(parentFieldName);
			} else {
				throw new EtlExceptionImpl("The value '" + parentFieldName + "' on " + getTransformerDsc()
				        + " must be either a valid field datasource or number");
			}
		}
		
		fieldMap.tryToLoadTransformer(dstConf);
		
		return fieldMap;
	}
	
	String getTransformerDsc() {
		String sql = "ParentOnDemandLoadTransformer: (" + parentTable + ", " + parentField;
		
		if (utilities.arrayHasElement(this.parentFieldDefinitions)) {
			sql += ", " + this.parentFieldDefinitions.toString();
		}
		
		return sql + ")";
	}
	
	private static String buildCacheKey(String parentTableName, String parentField, List<String> fields) {
		return parentTableName + "|" + parentField + "|" + fields;
	}
	
	public static ParentOnDemandLoadTransformer getInstance(List<Object> parameters, DstConf dstConf,
	        TransformableField field) {
		
		if (parameters == null || parameters.size() < 2) {
			throw new ForbiddenOperationException("A ParentOnDemandLoadTransformer needs at least 2 parameters.\n"
			        + "Eg: ParentOnDemandLoadTransformer(parentTableName, parentFieldName)");
		}
		
		String parentTable = parameters.get(0).toString();
		String parentTableField = parameters.get(1).toString();
		
		List<String> defaultObjectData = parameters.size() > 2
		        ? parameters.subList(2, parameters.size()).stream().map(Object::toString).toList()
		        : null;
		
		String key = buildCacheKey(parameters.get(0).toString(), parameters.get(1).toString(), defaultObjectData);
		
		return INSTANCES.computeIfAbsent(key,
		    k -> new ParentOnDemandLoadTransformer(parentTable, parentTableField, defaultObjectData, dstConf));
	}
	
	void tryToInitNewParentEtlItemConf(Connection srcConn, Connection dstConn) throws DBException {
		
		if (this.newParentRecordEtlItemConf == null) {
			synchronized (lock) {
				if (newParentRecordEtlItemConf == null) {
					
					AbstractTableConfiguration parentConf = new GenericTableConfiguration(parentTable);
					parentConf.setRelatedEtlConfig(dstConf.getRelatedEtlConf());
					
					EtlItemConfiguration conf = EtlItemConfiguration.fastCreate(parentConf, srcConn);
					
					conf.setDoNotFullLoadDstConf(true);
					
					conf.fullLoad(dstConf.getRelatedEtlConf().getOperations().get(0));
					
					conf.getSrcConf().fullLoad(srcConn);
					
					this.newParentRecordEtlItemConf = conf;
				}
			}
		}
	}
	
	void tryToInitExistingParentEtlItemConf(Connection srcConn, Connection dstConn) throws DBException {
		
		if (this.existingParentRecordEtlItemConf == null) {
			synchronized (lock) {
				if (existingParentRecordEtlItemConf == null) {
					
					AbstractTableConfiguration parentConf = new GenericTableConfiguration(parentTable);
					parentConf.setRelatedEtlConfig(dstConf.getRelatedEtlConf());
					
					EtlItemConfiguration conf = EtlItemConfiguration.fastCreate(parentConf, srcConn);
					
					conf.setDoNotFullLoadDstConf(true);
					conf.fullLoad(dstConf.getRelatedEtlConf().getOperations().get(0));
					
					conf.getSrcConf().fullLoad(srcConn);
					
					this.existingParentRecordEtlItemConf = conf;
				}
			}
		}
	}
	
	void tryToInitFastNewParentDstConf(Connection srcConn, Connection dstConn) throws DBException {
		DstConf fastParentDstConf = getFastNewParentDstConf(dstConn, dstConn);
		
		if (!fastParentDstConf.isFullLoaded()) {
			synchronized (lock) {
				if (!fastParentDstConf.isFullLoaded()) {
					
					fastParentDstConf.setDoNotUseSrcConfAsDataSource(true);
					
					fastParentDstConf.addAllToAvaliableDataSource(this.dstConf.getAllAvaliableDataSource());
					
					fastParentDstConf.setMapping(this.parentFieldMappings);
					
					fastParentDstConf.fullLoad(dstConn);
				}
			}
		}
	}
	
	void tryToInitFastExistingParentDstConf(Connection dstConn) throws DBException {
		if (this.existingParentRecordEtlItemConf == null) {
			return;
		}
		
		DstConf fastParentDstConf = getFastExistingParentDstConf(dstConn, dstConn);
		
		if (!fastParentDstConf.isFullLoaded()) {
			synchronized (lock) {
				if (!fastParentDstConf.isFullLoaded()) {
					
					fastParentDstConf.setDoNotUseSrcConfAsDataSource(true);
					
					fastParentDstConf.addAllToAvaliableDataSource(this.dstConf.getAllAvaliableDataSource());
					
					fastParentDstConf.setMapping(this.parentFieldMappings);
					
					fastParentDstConf.fullLoad(dstConn);
				}
			}
		}
	}
	
	SrcConf getFastNewParentSrcConf(Connection srcConn, Connection dstConn) throws DBException {
		tryToInitNewParentEtlItemConf(srcConn, dstConn);
		
		return this.newParentRecordEtlItemConf.getSrcConf();
	}
	
	SrcConf getFastExistingParentSrcConf(Connection srcConn, Connection dstConn) throws DBException {
		tryToInitExistingParentEtlItemConf(srcConn, dstConn);
		
		return this.existingParentRecordEtlItemConf.getSrcConf();
	}
	
	DstConf getFastExistingParentDstConf(Connection srcConn, Connection dstConn) throws DBException {
		
		if (this.existingParentRecordEtlItemConf == null) {
			tryToInitFastExistingParentDstConf(dstConn);
		}
		
		return this.existingParentRecordEtlItemConf.getDstConf().get(0);
	}
	
	DstConf getFastNewParentDstConf(Connection srcConn, Connection dstConn) throws DBException {
		tryToInitFastNewParentDstConf(srcConn, dstConn);
		
		return this.newParentRecordEtlItemConf.getDstConf().get(0);
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		EtlDatabaseObject dstParent = resolveParent(processor, srcObject, transformedRecord, additionalSrcObjects, srcConn,
		    dstConn);
		
		if (dstParent == null) {
			dstParent = createParent(processor, srcObject, transformedRecord, additionalSrcObjects, field, srcConn, dstConn);
		}
		
		if (dstParent == null) {
			throw new EtlTransformationException("Error on transforming the parentDstRecord on " + getTransformerDsc(),
			        srcObject, ActionOnEtlException.ABORT);
		}
		
		return new FieldTransformingInfo(field, dstParent.getObjectId().asSimpleValue(),
		        (EtlDataSource) dstParent.getRelatedConfiguration());
	}
	
	EtlDatabaseObject resolveSrcParent(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn,
	        Connection dstConn) throws EtlTransformationException, DBException {
		
		EtlDatabaseObject srcParent = null;
		
		FieldTransformingInfo fieldInfo = parentSourceFieldMapping.getTransformerInstance().transform(processor, srcObject,
		    transformedRecord, additionalSrcObjects, parentSourceFieldMapping, srcConn, dstConn);
		
		if (fieldInfo != null && fieldInfo.getTransformedValue() != null) {
			srcParent = DatabaseObjectDAO.getByOid(getFastExistingParentSrcConf(srcConn, dstConn),
			    Oid.fastCreate(getFastExistingParentSrcConf(srcConn, dstConn), fieldInfo.getTransformedValue()), srcConn);
			
			if (srcParent == null && this.missingFastSrcParentBehaviour.complainOnMissingSrcParent()) {
				throw new EtlTransformationException("The related srcValue (" + srcParent
				        + ") does not represent a valid Src Object within " + getTransformerDsc(), srcObject,
				        ActionOnEtlException.ABORT);
			}
		}
		
		return srcParent;
	}
	
	EtlDatabaseObject resolveParent(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject transformedRecord,
	        List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn, Connection dstConn)
	        throws EtlTransformationException, DBException {
		
		EtlDatabaseObject srcParent = resolveSrcParent(processor, srcObject, transformedRecord, additionalSrcObjects,
		    srcConn, dstConn);
		
		srcParent.setRelatedConfiguration(getFastExistingParentDstConf(srcConn, dstConn));
		
		return DatabaseObjectDAO.getByUniqueKeys(srcParent, dstConn);
	}
	
	EtlDatabaseObject createParent(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject transformedRecord,
	        List<EtlDatabaseObject> additionalSrcObjects, TransformableField field, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		EtlDatabaseObject srcParent = resolveSrcParent(processor, srcObject, transformedRecord, additionalSrcObjects,
		    srcConn, dstConn);
		
		DstConf dstConf = null;
		
		if (srcParent != null) {
			tryToInitExistingParentEtlItemConf(srcConn, dstConn);
			
			dstConf = this.existingParentRecordEtlItemConf.getDstConf().get(0);
		} else {
			tryToInitNewParentEtlItemConf(srcConn, dstConn);
			
			dstConf = this.newParentRecordEtlItemConf.getDstConf().get(0);
			
			srcParent = this.newParentRecordEtlItemConf.getSrcConf().createRecordInstance();
		}
		
		EtlLoadHelper loadHelper = EtlLoadHelper.fastLoadRecord(processor, srcParent, dstConf, srcConn, dstConn);
		
		List<LoadRecord> migratedRecs = loadHelper.getAllRecordsAsLoadRecord(dstConf, LoadStatus.SUCCESS);
		
		if (utilities.arrayHasElement(migratedRecs)) {
			return migratedRecs.get(0).getDstRecord();
		}
		
		return null;
		
	}
}
