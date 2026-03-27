package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SqlConditionElement;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
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
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

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
 * <li><b>parentTable</b> – Name of the parent table in the destination database whose record must
 * exist before the child record is saved.</li>
 * <li><b>parentFieldOnDataSourceObject</b> – Field used to resolve the parent identifier from the
 * available source data objects.</li>
 * <li><b>dstField:srcFieldOrValue</b> – Optional additional field mappings used when creating the
 * parent record in the destination database. Each parameter defines how a field in the parent
 * record should be populated.
 * <p>
 * Supported forms:
 * </p>
 * <ul>
 * <li><b>dstField:srcField</b> – copy the value from a field available in the source data.</li>
 * <li><b>dstField:constantValue</b> – assign a constant value.</li>
 * <li><b>dstField:@parameter</b> – assign a dynamic ETL parameter value.</li>
 * <li><b>dstField:null</b> – explicitly set the destination field to <code>null</code>.</li>
 * <li><b>dstField:</b> – omit the value to implicitly assign <code>null</code>.</li>
 * </ul>
 * <p>
 * Examples:
 * </p>
 * <pre>
 *     visit_type_id:42
 *     date_started:encounter_datetime
 *     location_id:@migration_location_id
 *     date_stopped:null
 *     indication_concept_id:
 *     </pre>
 * <p>
 * Dynamic parameters start with <code>@</code> and are resolved using the ETL configuration
 * parameters during transformation.
 * </p>
 * </li>
 * </ul>
 * <p>
 * Behavior:
 * </p>
 * <ol>
 * <li>Resolve the parent identifier from the source data.</li>
 * <li>Locate the corresponding parent record in the destination database.</li>
 * <li>If the parent does not exist in the destination, create or migrate it using the provided
 * field mappings.</li>
 * <li>Return the parent record primary key as the transformed value.</li>
 * </ol>
 * <p>
 * Example:
 * </p>
 * <pre>
 * ParentOnDemandLoadTransformer(
 *     visit,
 *     visit_id,
 *     date_started:encounter_datetime,
 *     visit_type_id:42,
 *     location_id:@migration_location_id,
 *     date_stopped:null,
 *     indication_concept_id:
 * )
 * </pre>
 * <p>
 * In this example:
 * </p>
 * <ul>
 * <li>The parent record is created in the <b>visit</b> table.</li>
 * <li>The parent identifier is resolved using the field <b>visit_id</b>.</li>
 * <li>The field <b>date_started</b> is populated from <b>encounter_datetime</b>.</li>
 * <li>The field <b>visit_type_id</b> receives the constant value <b>42</b>.</li>
 * <li>The field <b>location_id</b> receives the dynamic ETL parameter
 * <b>@migration_location_id</b>.</li>
 * <li>The field <b>date_stopped</b> is explicitly set to <code>null</code>.</li>
 * <li>The field <b>indication_concept_id</b> is implicitly set to <code>null</code>.</li>
 * </ul>
 * <p>
 * This transformer is designed for high-performance migrations where parent objects must be loaded
 * on-demand during the transformation of child records.
 * </p>
 */
public class ParentOnDemandLoadTransformer extends AbstractEtlFieldTransformer {
	
	protected final Object lock = new Object();
	
	protected static final Map<String, ParentOnDemandLoadTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private String parentTable;
	
	private String parentField;
	
	private EtlItemConfiguration etlItemConfForNonExistingSrcParent;
	
	private EtlItemConfiguration etlItemConfForExistingSrcParent;
	
	private FieldsMapping parentSourceFieldMapping;
	
	private List<String> parentFieldDefinitions;
	
	private List<FieldsMapping> nonExistingParentFieldMappings;
	
	private List<SqlConditionElement> onDemandCheckParametersInfo;
	
	private String onDemandCheckCondition;
	
	private String questionMarkedOnDemandCheckCondition;
	
	private List<String> ignorableFields;
	
	public ParentOnDemandLoadTransformer(List<Object> parameters, DstConf dstConf, TransformableField field) {
		super(parameters, dstConf, field);
		
		if (parameters == null || parameters.size() < 3) {
			throw new ForbiddenOperationException("A ParentOnDemandLoadTransformer needs at least 3 parameters.\n"
			        + "ParentOnDemandLoadTransformer(parentTableName, parentFieldName, onDemandCheckCondition)");
		}
		
		this.parentTable = parameters.get(0).toString();
		this.parentField = parameters.get(1).toString();
		this.onDemandCheckCondition = parameters.get(2).toString();
		
		this.parentFieldDefinitions = parameters.size() > 3
		        ? parameters.subList(3, parameters.size()).stream().map(Object::toString).toList()
		        : null;
		
		this.parentSourceFieldMapping = FieldsMapping.fastCreate(parentField, field.getDstField(), relatedDstConf);
		
		if (this.parentTable.equals("encounter") ) {
			System.err.println();
		}
		
		if (utilities.listHasElement(this.parentFieldDefinitions)) {
			this.nonExistingParentFieldMappings = new ArrayList<>();
			
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
				
				FieldsMapping fm = fastCreateFieldMap(srcFieldOrValue, dstField, relatedDstConf);
				
				this.nonExistingParentFieldMappings.add(fm);
			}
		}
	}
	
	public void setIgnorableFields(List<String> ignorableFields) {
		this.ignorableFields = ignorableFields;
	}
	
	public List<String> getIgnorableFields() {
		return ignorableFields;
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
		
		return fieldMap;
	}
	
	public String getTransformerDsc() {
		String sql = "ParentOnDemandLoadTransformer: (" + parentTable + ", " + parentField;
		
		if (utilities.listHasElement(this.parentFieldDefinitions)) {
			sql += ", " + this.parentFieldDefinitions.toString();
		}
		
		return sql + ")";
	}
	
	public static String buildCacheKey(String parentTableName, String parentField, String onDemandCheckCondition,
	        List<String> fields) {
		return parentTableName + "|" + parentField + "|" + onDemandCheckCondition + "|" + fields;
	}
	
	public DstConf getRelatedDstConf() {
		return relatedDstConf;
	}
	
	public static ParentOnDemandLoadTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		
		if (parameters == null || parameters.size() < 3) {
			throw new ForbiddenOperationException("A ParentOnDemandLoadTransformer needs at least 3 parameters.\n"
			        + "ParentOnDemandLoadTransformer(parentTableName, parentFieldName, onDemandCheckCondition)");
		}
		
		String parentTable = parameters.get(0).toString();
		String parentTableField = parameters.get(1).toString();
		String onDemandCheckCondition = parameters.get(2).toString();
		
		List<String> defaultObjectData = parameters.size() > 3
		        ? parameters.subList(3, parameters.size()).stream().map(Object::toString).toList()
		        : null;
		
		String key = buildCacheKey(parentTable, parentTableField, onDemandCheckCondition, defaultObjectData);
		
		return INSTANCES.computeIfAbsent(key, k -> new ParentOnDemandLoadTransformer(parameters, relatedDstConf, field));
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		EtlDatabaseObject dstParent = null;
		
		try {
			dstParent = resolveParent(processor, srcObject, transformedRecord, additionalSrcObjects, srcConn, dstConn);
		}
		catch (InconsistentStateException e) {
			srcObject.setFieldValue(this.parentField, null);
		}
		
		if (dstParent == null) {
			dstParent = retrieveExistingOnDemandParent(processor, srcObject, additionalSrcObjects, srcConn, dstConn);
			
			if (dstParent == null) {
				dstParent = createParent(processor, srcObject, transformedRecord, additionalSrcObjects, field, srcConn,
				    dstConn);
			}
			
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
		
		FieldTransformingInfo fieldInfo = null;
		
		try {
			fieldInfo = parentSourceFieldMapping.getTransformerInstance().transform(processor, srcObject, transformedRecord,
			    additionalSrcObjects, parentSourceFieldMapping, srcConn, dstConn);
		}
		catch (EtlExceptionImpl e) {
			e.printStackTrace();
		}
		
		if (fieldInfo != null && fieldInfo.getTransformedValue() != null) {
			srcParent = DatabaseObjectDAO.getByOid(getSrcConfForExistingSrcParent(srcConn, dstConn),
			    Oid.fastCreate(getSrcConfForExistingSrcParent(srcConn, dstConn), fieldInfo.getTransformedValue()), srcConn);
			
			if (srcParent == null) {
				throw new InconsistentStateException("The related srcValue (" + fieldInfo.getTransformedValue()
				        + ") does not represent a valid Src Object within " + getTransformerDsc());
			}
		}
		
		return srcParent;
	}
	
	EtlDatabaseObject resolveParent(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject transformedRecord,
	        List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn, Connection dstConn)
	        throws EtlTransformationException, DBException {
		
		EtlDatabaseObject srcParent = resolveSrcParent(processor, srcObject, transformedRecord, additionalSrcObjects,
		    srcConn, dstConn);
		
		if (srcParent != null) {
			tryToInitDstConfForExistingSrcParent(dstConn);
			
			srcParent.setRelatedConfiguration(getDstConfForExistingSrcParent(srcConn, dstConn));
			
			return DatabaseObjectDAO.getByUniqueKeys(srcParent, dstConn);
		} else {
			return null;
		}
	}
	
	EtlDatabaseObject createParent(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject transformedRecord,
	        List<EtlDatabaseObject> additionalSrcObjects, TransformableField field, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		processor.logDebug(
		    "Performing on-demand creation of " + this.getSrcConfForExistingSrcParent(srcConn, dstConn).getTableName()
		            + " For " + srcObject.getRelatedConfiguration().getObjectName());
		
		EtlDatabaseObject srcParent = resolveSrcParent(processor, srcObject, transformedRecord, additionalSrcObjects,
		    srcConn, dstConn);
		
		DstConf dstConf = null;
		
		TransformationType transformationType = TransformationType.PRINCIPAL;
		
		if (srcParent != null) {
			tryToInitDstConfForExistingSrcParent(dstConn);
			
			dstConf = getDstConfForExistingSrcParent(srcConn, dstConn);
		} else {
			tryToInitDstConfForNonExistingSrcParent(srcConn, dstConn);
			
			dstConf = getDstConfForNonExistingSrcParent(srcConn, dstConn);
			
			srcParent = getSrcConfForNonExistingSrcParent(srcConn, dstConn).createRecordInstance();
			
			transformationType = TransformationType.ON_DEMAND;
		}
		
		srcParent.setTransformationSrcObject(additionalSrcObjects);
		
		EtlLoadHelper loadHelper = EtlLoadHelper.fastLoadRecord(processor, srcParent, dstConf, transformationType, srcConn,
		    dstConn);
		
		List<LoadRecord> migratedRecs = loadHelper.getAllRecordsAsLoadRecord(dstConf, LoadStatus.SUCCESS);
		
		if (utilities.listHasElement(migratedRecs)) {
			return migratedRecs.get(0).getDstRecord();
		}
		
		return null;
		
	}
	
	private EtlDatabaseObject retrieveExistingOnDemandParent(EtlProcessor processor, EtlDatabaseObject srcObject,
	        List<EtlDatabaseObject> srcObjects, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		tryToInitDstConfForNonExistingSrcParent(srcConn, dstConn);
		tryToInitOnDemandCheckConditionElements(srcConn, dstConn);
		
		SrcConf srcConf = getSrcConfForNonExistingSrcParent(srcConn, dstConn);
		DstConf dstConf = getDstConfForExistingSrcParent(srcConn, dstConn);
		
		String condition = this.questionMarkedOnDemandCheckCondition;
		
		Object[] params = new Object[this.onDemandCheckParametersInfo.size()];
		
		EtlDatabaseObject auxObject = dstConf.createRecordInstance();
		
		for (int i = 0; i < params.length; i++) {
			FieldsMapping mapping = this.onDemandCheckParametersInfo.get(i).getMappig();
			
			mapping.getTransformerInstance().performFieldTransformation(processor, srcObjects.get(0), auxObject, srcObjects,
			    mapping, srcConn, dstConn);
			
			FieldTransformingInfo paramValueInfo = auxObject.getField(mapping.getDstField()).getTransformingInfo();
			
			params[i] = paramValueInfo.getTransformedValue();
			
			if (!paramValueInfo.isLoadedWithDstValue()) {
				ParentTable refInfo = dstConf.findParentRefInfoByField(paramValueInfo.getSrcField().getName());
				
				if (refInfo != null) {
					EtlDatabaseObject parentInSrc = auxObject.retrieveParentInSrcUsingDstParentInfo(refInfo, srcConf,
					    srcConn);
					
					EtlDatabaseObject parentInDst = null;
					
					if (parentInSrc != null) {
						parentInDst = auxObject.retrieveParentInDestination(refInfo, parentInSrc, dstConn);
					}
					
					if (parentInDst == null) {
						throw new EtlTransformationException(
						        "The " + refInfo.getTableName() + "(" + params[i] + ") of " + dstConf.getTableName() + "("
						                + srcObject.getObjectId().asSimpleNumericValue() + ") cannot be found on src db",
						        srcObject, ActionOnEtlException.ABORT);
					}
					
					params[i] = parentInDst.getObjectId().asSimpleNumericValue();
				}
			}
		}
		
		return getDstConfForNonExistingSrcParent(srcConn, dstConn).find(condition, params, dstConn);
	}
	
	DstConf getDstConfForExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		return this.etlItemConfForExistingSrcParent.getDstConf().get(0);
	}
	
	protected DstConf getDstConfForNonExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		return this.etlItemConfForNonExistingSrcParent.getDstConf().get(0);
	}
	
	SrcConf getSrcConfForNonExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		tryToInitEtlItemConfForNonExistingSrcParent(srcConn, dstConn);
		
		return this.etlItemConfForNonExistingSrcParent.getSrcConf();
	}
	
	SrcConf getSrcConfForExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		tryToInitEtlItemConfForExistingSrcParent(srcConn, dstConn);
		
		return this.etlItemConfForExistingSrcParent.getSrcConf();
	}
	
	protected void tryToInitDstConfForNonExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		tryToInitEtlItemConfForNonExistingSrcParent(srcConn, dstConn);
		
		DstConf dstConf = getDstConfForNonExistingSrcParent(dstConn, dstConn);
		
		if (!dstConf.isFullLoaded()) {
			synchronized (lock) {
				if (!dstConf.isFullLoaded()) {
					
					dstConf.setDoNotUseSrcConfAsDataSource(true);
					dstConf.setIgnorableFields(this.getIgnorableFields());
					
					dstConf.addAllToAvaliableDataSource(this.relatedDstConf.getAllAvaliableDataSource());
					dstConf.addAllToPreferredDataSource(this.relatedDstConf.getAllPrefferredDataSource());
					
					dstConf.setMapping(this.nonExistingParentFieldMappings);
					
					dstConf.fullLoad(dstConn);
				}
			}
		}
	}
	
	void tryToInitOnDemandCheckConditionElements(Connection srcConn, Connection dstConn) throws DBException {
		if (this.onDemandCheckParametersInfo == null) {
			synchronized (lock) {
				
				tryToInitDstConfForNonExistingSrcParent(srcConn, dstConn);
				
				this.questionMarkedOnDemandCheckCondition = this.onDemandCheckCondition;
				
				if (utilities.stringHasValue(this.onDemandCheckCondition)) {
					List<SqlConditionElement> elements = SQLUtilities
					        .extractSqlConditionElements(this.onDemandCheckCondition);
					
					for (SqlConditionElement field : elements) {
						
						field.fullLoad(relatedDstConf);
						
						String regex = "\\b" + Pattern.quote(field.getField()) + "\\s*" + Pattern.quote(field.getOperator())
						        + "\\s*" + Pattern.quote(field.getValue()) + "\\b";
						
						this.questionMarkedOnDemandCheckCondition = this.questionMarkedOnDemandCheckCondition
						        .replaceAll(regex, field.getField() + " " + field.getOperator() + " ?");
					}
					
					this.onDemandCheckParametersInfo = elements;
				}
			}
		}
	}
	
	void tryToInitDstConfForExistingSrcParent(Connection dstConn) throws DBException {
		tryToInitEtlItemConfForExistingSrcParent(dstConn, dstConn);
		
		DstConf dstConf = getDstConfForExistingSrcParent(dstConn, dstConn);
		
		if (!dstConf.isFullLoaded()) {
			synchronized (lock) {
				if (!dstConf.isFullLoaded()) {
					
					List<EtlDataSource> avaliableDataSource = null;
					List<EtlDataSource> preferredDataSource = null;
					
					if (this.relatedDstConf.useSharedPKKey()
					        && dstConf.getTableName().equals(this.relatedDstConf.getSharePkWith())) {
						
						preferredDataSource = new ArrayList<>();
						avaliableDataSource = new ArrayList<>();
						
						for (EtlDataSource p : this.relatedDstConf.getAllAvaliableDataSource()) {
							if (p != this.relatedDstConf.getSrcConf().getSharedKeyRefInfo(dstConn)) {
								avaliableDataSource.add(p);
							}
						}
						
						for (EtlDataSource p : this.relatedDstConf.getAllPrefferredDataSource()) {
							if (p != this.relatedDstConf.getSrcConf().getSharedKeyRefInfo(dstConn)) {
								preferredDataSource.add(p);
							}
						}
						
					} else {
						avaliableDataSource = this.relatedDstConf.getAllAvaliableDataSource();
						preferredDataSource = this.relatedDstConf.getAllPrefferredDataSource();
						
					}
					dstConf.addAllToAvaliableDataSource(avaliableDataSource);
					dstConf.addAllToPreferredDataSource(preferredDataSource);
					
					dstConf.fullLoad(dstConn);
				}
			}
		}
	}
	
	protected void tryToInitEtlItemConfForNonExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		
		if (this.etlItemConfForNonExistingSrcParent == null) {
			synchronized (lock) {
				if (etlItemConfForNonExistingSrcParent == null) {
					
					AbstractTableConfiguration parentConf = new GenericTableConfiguration(parentTable);
					parentConf.setRelatedEtlConfig(relatedDstConf.getRelatedEtlConf());
					
					EtlItemConfiguration conf = EtlItemConfiguration.fastCreate(parentConf, srcConn);
					conf.setParentItemConf(relatedDstConf.getParentConf());
					conf.setRelatedParentDstConfName(relatedDstConf.getTableAlias());
					
					conf.setDoNotFullLoadDstConf(true);
					
					conf.fullLoad(relatedDstConf.getRelatedEtlConf().getOperations().get(0));
					
					conf.getSrcConf().fullLoad(srcConn);
					
					this.etlItemConfForNonExistingSrcParent = conf;
				}
			}
		}
	}
	
	void tryToInitEtlItemConfForExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		
		if (this.etlItemConfForExistingSrcParent == null) {
			synchronized (lock) {
				if (etlItemConfForExistingSrcParent == null) {
					
					AbstractTableConfiguration parentConf = new GenericTableConfiguration(parentTable);
					parentConf.setRelatedEtlConfig(relatedDstConf.getRelatedEtlConf());
					
					EtlItemConfiguration conf = EtlItemConfiguration.fastCreate(parentConf, srcConn);
					conf.setParentItemConf(relatedDstConf.getParentConf());
					conf.setRelatedParentDstConfName(relatedDstConf.getTableAlias());
					
					conf.setDoNotFullLoadDstConf(true);
					conf.fullLoad(relatedDstConf.getRelatedEtlConf().getOperations().get(0));
					
					conf.getSrcConf().fullLoad(srcConn);
					
					this.etlItemConfForExistingSrcParent = conf;
				}
			}
		}
	}
	
}
