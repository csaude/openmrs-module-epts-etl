package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlTemplateInfo;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SqlConditionElement;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.model.EtlLoadHelper;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingFieldException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

/**
 * Transformer responsible for resolving, reusing, or creating a parent record on demand in the
 * destination table and returning its primary key as the transformed value.
 * <p>
 * This transformer is used when the current record being transformed depends on a parent record
 * that may or may not already exist in the destination database. The transformer first attempts to
 * resolve an existing parent from the source data, then tries to locate an already created
 * on-demand parent in the destination using a configurable condition, and finally creates the
 * parent record on demand when necessary.
 * </p>
 * <p>
 * Transformer syntax:
 * </p>
 * <pre>
 * ParentOnDemandLoadTransformer(
 *      parentTable,
 *      parent_field_in_datasource_object:srcField,
 *      on_demand_check_condition:sqlCondition,
 *      template:templateName,
 *      template_param_paramName:srcFieldOrValue,
 *      dstField1:srcFieldOrValue1,
 *      dstField2:srcFieldOrValue2,
 *      ...
 *      override_fields:field1,field2,...
 * )
 * </pre>
 * <p>
 * Parameters:
 * </p>
 * <ul>
 * <li><b>parentTable</b> – Name of the parent table in the destination database whose record must
 * exist before the child record is saved.</li>
 * <li><b>parent_field_in_datasource_object:srcField</b> – Defines the field from the available
 * source data objects that should be used to resolve the parent record from the source database.
 * This parameter replaces the old positional <code>parentFieldOnDataSourceObject</code>
 * argument.</li>
 * <li><b>on_demand_check_condition:sqlCondition</b> – Optional condition used to search for an
 * already existing parent record previously created on demand in the destination database. If a
 * record matching this condition is found, it will be reused as the parent instead of creating a
 * new one.</li>
 * <li><b>template:templateName</b> – Optional template name used to initialize the
 * {@link org.openmrs.module.epts.etl.conf.EtlItemConfiguration} responsible for creating or loading
 * the parent on demand. Templates allow reuse of predefined ETL configurations.</li>
 * <li><b>template_param_paramName:srcFieldOrValue</b> – Optional parameters used to dynamically
 * inject values into the specified template. These parameters follow a flat prefix-based approach
 * and are resolved at runtime before the template is applied.
 * <p>
 * Supported forms:
 * </p>
 * <ul>
 * <li><b>template_param_x:srcField</b> – copy value from a source field</li>
 * <li><b>template_param_x:constantValue</b> – assign constant value</li>
 * <li><b>template_param_x:@parameter</b> – assign dynamic ETL parameter</li>
 * <li><b>template_param_x:null</b> – explicitly assign <code>null</code></li>
 * <li><b>template_param_x:</b> – implicitly assign <code>null</code></li>
 * </ul>
 * </li>
 * <li><b>dstField:srcFieldOrValue</b> – Optional additional field mappings used when creating the
 * parent record in the destination database. Each parameter defines how a field in the parent
 * record should be populated.
 * <p>
 * Supported forms:
 * </p>
 * <ul>
 * <li><b>dstField:srcField</b> – copy the value from a field available in the source data</li>
 * <li><b>dstField:constantValue</b> – assign a constant value</li>
 * <li><b>dstField:@parameter</b> – assign a dynamic ETL parameter value</li>
 * <li><b>dstField:null</b> – explicitly set the destination field to <code>null</code></li>
 * <li><b>dstField:</b> – omit the value to implicitly assign <code>null</code></li>
 * </ul>
 * </li>
 * <li><b>override_fields:field1,field2,...</b> – Defines which fields should be recomputed and
 * updated when an existing parent record is reused. By default, existing parent records are reused
 * as-is. When this parameter is defined, only the specified fields are recalculated and overwritten
 * using the same transformation logic defined for parent creation.</li>
 * </ul>
 * <p>
 * Behavior:
 * </p>
 * <ol>
 * <li>Attempt to resolve the parent from the source database using
 * <code>parent_field_in_datasource_object</code>.</li>
 * <li>If the source parent exists, attempt to locate the corresponding record in the
 * destination.</li>
 * <li>If no destination parent is found and <code>on_demand_check_condition</code> is defined,
 * search for an already existing parent previously created on demand.</li>
 * <li>If no existing parent is found, initialize the ETL item configuration (optionally using a
 * template and injected template parameters) and create or migrate the parent on demand.</li>
 * <li>If an existing parent is reused and <code>override_fields</code> is defined, recompute and
 * update only the specified fields.</li>
 * <li>Return the parent primary key as the transformed value.</li>
 * </ol>
 * <p>
 * Example:
 * </p>
 * <pre>
 * ParentOnDemandLoadTransformer(
 *     visit,
 *     parent_field_in_datasource_object:visit_id,
 *     on_demand_check_condition:patient_id=patient_id and date_started=encounter_datetime,
 *     template:visit_on_demand_template,
 *     template_param_patient_id:@patient_id,
 *     template_param_visit_date:@encounter_datetime,
 *     visit_type_id:42,
 *     date_started:encounter_datetime,
 *     location_id:@migration_location_id,
 *     date_stopped:null,
 *     indication_concept_id:
 * )
 * </pre>
 * <p>
 * In this example:
 * </p>
 * <ul>
 * <li>The parent record belongs to the <b>visit</b> table.</li>
 * <li>The source parent is resolved using the field <b>visit_id</b>.</li>
 * <li>If no mapped destination parent is found, the transformer checks for an existing visit
 * matching the provided condition.</li>
 * <li>The ETL configuration is initialized using the template <b>visit_on_demand_template</b>.</li>
 * <li>The template parameters <b>patient_id</b> and <b>visit_date</b> are dynamically
 * injected.</li>
 * <li>The field <b>visit_type_id</b> receives the constant value <b>42</b>.</li>
 * <li>The field <b>date_started</b> is populated from <b>encounter_datetime</b>.</li>
 * <li>The field <b>location_id</b> is populated from the ETL parameter
 * <b>@migration_location_id</b>.</li>
 * <li>The field <b>date_stopped</b> is explicitly set to <code>null</code>.</li>
 * <li>The field <b>indication_concept_id</b> is implicitly set to <code>null</code>.</li>
 * </ul>
 * <p>
 * This transformer is intended for high-performance ETL scenarios where parent records must be
 * resolved or created dynamically during child record transformation.
 * </p>
 */
public class ParentOnDemandLoadTransformer extends AbstractEtlFieldTransformer {
	
	protected final Object lock = new Object();
	
	protected static final Map<String, ParentOnDemandLoadTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private String parentTableName;
	
	private String parentSourceField;
	
	private EtlItemConfiguration onDemandCreateParentItemConf;
	
	private EtlItemConfiguration existingParentItemConf;
	
	private FieldsMapping parentSourceIdMapping;
	
	private List<String> rawParameterDefinitions;
	
	private List<FieldsMapping> onDemandParentFieldMappings;
	
	private List<FieldsMapping> overrideFields;
	
	private String overrideFieldsStr;
	
	private List<SqlConditionElement> onDemandCondtionElements;
	
	private String onDemandCheckCondition;
	
	private String parametrizedOnDemandCheckCondition;
	
	private List<String> ignorableFields;
	
	private String templateName;
	
	private Map<String, Object> templateParams;
	
	public ParentOnDemandLoadTransformer(List<Object> parameters, DstConf dstConf, TransformableField field) {
		super(parameters, dstConf, field);
		
		if (parameters == null || parameters.size() < 1) {
			throw new ForbiddenOperationException("A ParentOnDemandLoadTransformer needs at least 1 parameters.\n"
			        + "ParentOnDemandLoadTransformer(parentTableName)");
		}
		
		this.templateParams = new HashMap<>();
		
		this.parentTableName = parameters.get(0).toString();
		
		this.rawParameterDefinitions = parameters.size() > 1
		        ? parameters.subList(1, parameters.size()).stream().map(Object::toString).toList()
		        : null;
		
		if (utilities.listHasElement(this.rawParameterDefinitions)) {
			this.onDemandParentFieldMappings = new ArrayList<>();
			
			for (String fieldData : this.rawParameterDefinitions) {
				String[] mapping = fieldData.split(":", 2);
				
				if (mapping.length != 2) {
					throw new EtlExceptionImpl("Wrong format for newObjectData within the " + getTransformerDsc() + "\n"
					        + "Each object param must be specified as filedName:srcFieldOrValue");
				}
				
				String dstField = mapping[0];
				String srcFieldOrValue = mapping[1];
				
				if (dstField.equals("parent_field_in_datasource_object")) {
					if (!utilities.stringHasValue(srcFieldOrValue)) {
						throw new ForbiddenOperationException("The parent_field_in_datasource_object has no value");
					}
					
					this.parentSourceField = srcFieldOrValue;
					
					this.parentSourceIdMapping = utilities.stringHasValue(this.parentSourceField)
					        ? new FieldsMapping(this.parentSourceField, dstConf.getSrcConf().getTableAlias(),
					                field.getDstField())
					        : null;
					
				} else if (dstField.equals("on_demand_check_condition")) {
					if (!utilities.stringHasValue(srcFieldOrValue)) {
						throw new ForbiddenOperationException("The on_demand_check_condition has no value");
					}
					
					if (srcFieldOrValue.equals("on_demand_order_group_check_condition.sql")) {
						logTrace("Loading on_demand_check_condition...");
					}
					
					if (dstConf.getRelatedEtlConf().checkIfIsValidDumpScript(srcFieldOrValue)) {
						this.onDemandCheckCondition = dstConf.getRelatedEtlConf().readDumpScriptContent(srcFieldOrValue);
						
						if (dstConf.hasTemplate()) {
							this.onDemandCheckCondition = EtlDataConfiguration.resolvePlaceholders(
							    this.onDemandCheckCondition, null, null, null, dstConf.getTemplate().getParameters());
						}
						
					} else {
						this.onDemandCheckCondition = srcFieldOrValue;
					}
					
					if (!SQLUtilities.isValidSqlCondition(this.onDemandCheckCondition)) {
						throw new EtlExceptionImpl(
						        "Wrong format for on_demand_condition within the transformer " + field.getTransformer());
					}
				} else if (dstField.equals("template")) {
					if (!utilities.stringHasValue(srcFieldOrValue)) {
						throw new ForbiddenOperationException("The template has no value");
					}
					
					this.templateName = srcFieldOrValue;
				} else if (dstField.startsWith("template_param_")) {
					
					String paramName = dstField.substring("template_param_".length());
					
					if (!utilities.stringHasValue(paramName)) {
						throw new ForbiddenOperationException("Invalid template_param key: " + dstField);
					}
					
					templateParams.put(paramName, srcFieldOrValue);
				} else if (dstField.equals("override_fields")) {
					this.overrideFieldsStr = srcFieldOrValue;
				} else {
					if (!utilities.stringHasValue(srcFieldOrValue) || srcFieldOrValue.toLowerCase().equals("null")) {
						srcFieldOrValue = null;
					}
					
					FieldsMapping fm = fastCreateFieldMap(srcFieldOrValue, dstField, relatedDstConf);
					
					this.onDemandParentFieldMappings.add(fm);
				}
			}
		}
		
		if (utilities.stringHasValue(this.overrideFieldsStr)) {
			String[] toOverride = this.overrideFieldsStr.split(",");
			
			this.overrideFields = new ArrayList<>(toOverride.length);
			
			for (String to : toOverride) {
				FieldsMapping f = new FieldsMapping();
				f.setDstField(to);
				
				int i = onDemandParentFieldMappings.indexOf(f);
				
				if (i < 0) {
					throw new EtlExceptionImpl("The field to override '" + f.getDstField()
					        + "' is not listed on onDemandFields on transformer \n" + getTransformerDsc());
				}
				
				overrideFields.add(onDemandParentFieldMappings.get(i));
				
			}
		}
		
		if (!utilities.stringHasValue(this.onDemandCheckCondition) && !utilities.stringHasValue(this.parentSourceField)) {
			throw new ForbiddenOperationException(
			        "At least on_demand_check_condition or parent_field_in_datasource_object must be specified");
		}
		
		if (!this.templateParams.isEmpty() && !utilities.stringHasValue(this.templateName)) {
			throw new ForbiddenOperationException(
			        "Template parameters specified but no templated was defined with transformer: \n" + getTransformerDsc());
		}
		
		logTrace("Transformer Initialized \n" + this);
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
		String sql = "PARENT_ON_DEMAND_TRANSFORMER: (" + parentTableName;
		
		if (utilities.listHasElement(this.rawParameterDefinitions)) {
			sql += ", " + this.rawParameterDefinitions.toString();
		}
		
		return sql + ")";
	}
	
	public static String buildCacheKey(String parentTable, List<String> fields, DstConf relatedDstConf) {
		return parentTable + "|" + relatedDstConf.getTableName() + "|" + fields;
	}
	
	public DstConf getRelatedDstConf() {
		return relatedDstConf;
	}
	
	public static ParentOnDemandLoadTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		
		if (parameters == null || parameters.size() < 1) {
			throw new ForbiddenOperationException("A ParentOnDemandLoadTransformer needs at least 1 parameters.\n"
			        + "ParentOnDemandLoadTransformer(parentTableName)");
		}
		
		String parentTable = parameters.get(0).toString();
		
		List<String> defaultObjectData = parameters.size() > 1
		        ? parameters.subList(1, parameters.size()).stream().map(Object::toString).toList()
		        : null;
		
		String key = buildCacheKey(parentTable, defaultObjectData, relatedDstConf);
		
		return INSTANCES.computeIfAbsent(key, k -> new ParentOnDemandLoadTransformer(parameters, relatedDstConf, field));
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (transformedRecord.getRelatedConfiguration().hasTemplate() && transformedRecord.getRelatedConfiguration()
		        .getTemplateName().equals("closest_previous_lab_order_request")) {
			logTrace("Starting order on demand!");
		}
		
		EtlDatabaseObject dstParent = null;
		EtlDatabaseObject srcParent = null;
		
		if (existingSrcParentIsApplicable()) {
			
			try {
				srcParent = resolveSrcParent(processor, srcObject, transformedRecord, additionalSrcObjects, srcConn,
				    dstConn);
				
				dstParent = resolveParent(processor, srcParent, srcObject, transformedRecord, additionalSrcObjects, srcConn,
				    dstConn);
			}
			catch (InconsistentStateException e) {
				
				ParentTable parentInfo = ((TableConfiguration) srcObject.getRelatedConfiguration())
				        .findParentRefInfoByParentTable(parentTableName);
				
				parentInfo.setTableName(parentTableName);
				
				InconsistenceInfo i = InconsistenceInfo.generate(srcObject, parentInfo,
				    processor.getRelatedEtlConfiguration().getOriginAppLocationCode());
				
				srcObject.setFieldValue(this.parentSourceField, null);
				
				i.save(relatedDstConf, srcConn);
			}
		}
		
		if (dstParent == null) {
			dstParent = retrieveExistingOnDemandParent(processor, srcObject, additionalSrcObjects, srcConn, dstConn);
			
			if (dstParent == null) {
				if (usesTemplate()) {
					SrcConf srcConf = loadSrcConfForNonExistingSrcParentIfNeeded(srcConn, dstConn);
					
					List<EtlDatabaseObject> recs = srcConf.searchRecords(null, srcObject, srcConn);
					
					if (recs.isEmpty()) {
						throw new EtlExceptionImpl("No src record was returned with " + this);
					}
					
					srcParent = recs.get(0);
				}
				dstParent = createParent(processor, srcParent, srcObject, transformedRecord, additionalSrcObjects, field,
				    srcConn, dstConn);
			}
			
		}
		
		if (dstParent == null) {
			throw new EtlTransformationException("Error on transforming the parentDstRecord on " + getTransformerDsc(),
			        srcObject, ActionOnEtlException.ABORT_PROCESS);
		}
		
		return new FieldTransformingInfo(field, dstParent.getObjectId().asSimpleValue(),
		        (EtlDataSource) dstParent.getRelatedConfiguration());
		
	}
	
	private boolean usesTemplate() {
		return this.templateName != null;
	}
	
	EtlDatabaseObject resolveSrcParent(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn,
	        Connection dstConn) throws EtlTransformationException, DBException {
		
		EtlDatabaseObject srcParent = null;
		
		if (this.parentSourceIdMapping != null) {
			
			FieldTransformingInfo fieldInfo = null;
			
			try {
				fieldInfo = this.parentSourceIdMapping.getTransformerInstance().transform(processor, srcObject,
				    transformedRecord, additionalSrcObjects, parentSourceIdMapping, srcConn, dstConn);
			}
			catch (Exception e) {
				throw e;
			}
			
			if (fieldInfo != null && fieldInfo.getTransformedValue() != null) {
				SrcConf srcConf = loadSrcConfForExistingSrcParentIfNeeded(srcConn, dstConn);
				
				srcParent = DatabaseObjectDAO.getByOid(srcConf, Oid.fastCreate(srcConf, fieldInfo.getTransformedValue()),
				    srcConn);
				
				if (srcParent == null) {
					throw new InconsistentStateException("The related srcValue (" + fieldInfo.getTransformedValue()
					        + ") does not represent a valid Src Object within " + getTransformerDsc());
				}
			}
		}
		
		return srcParent;
	}
	
	EtlDatabaseObject resolveParent(EtlProcessor processor, EtlDatabaseObject srcParent, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn,
	        Connection dstConn) throws EtlTransformationException, DBException {
		
		if (srcParent != null) {
			ensureDstConfForExistingSrcParentInitialized(dstConn);
			
			DatabaseObjectConfiguration bkp = srcParent.getRelatedConfiguration();
			
			srcParent.setRelatedConfiguration(getDstConfForExistingSrcParent(srcConn, dstConn));
			
			EtlDatabaseObject dstObject = DatabaseObjectDAO.getByUniqueKeys(srcParent, dstConn);
			
			srcParent.setRelatedConfiguration(bkp);
			
			return dstObject;
			
		} else {
			return null;
		}
	}
	
	EtlDatabaseObject createParent(EtlProcessor processor, EtlDatabaseObject srcParent, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		processor.logDebug("Performing on-demand creation of "
		        + this.loadSrcConfForNonExistingSrcParentIfNeeded(srcConn, dstConn).getTableName() + " For "
		        + srcObject.getRelatedConfiguration().getObjectName());
		
		DstConf dstConf = null;
		
		TransformationType transformationType = TransformationType.PRINCIPAL;
		
		if (srcParent != null && sourceParentMayExists()) {
			ensureDstConfForExistingSrcParentInitialized(dstConn);
			
			dstConf = getDstConfForExistingSrcParent(srcConn, dstConn);
		} else {
			ensureDstConfForNonExistingSrcParentInitialized(srcConn, dstConn);
			
			dstConf = getDstConfForNonExistingSrcParent(srcConn, dstConn);
			
			if (srcParent == null) {
				srcParent = loadSrcConfForNonExistingSrcParentIfNeeded(srcConn, dstConn).createRecordInstance();
			}
			
			transformationType = TransformationType.ON_DEMAND;
		}
		
		srcParent.setAuxLoadObject(!srcParent.hasAuxLoadObject() ? new ArrayList<>() : srcParent.getAuxLoadObject());
		srcParent.getAuxLoadObject().addAll(additionalSrcObjects);
		
		EtlLoadHelper loadHelper = EtlLoadHelper.fastLoadRecord(processor, srcParent, dstConf, transformationType, srcConn,
		    dstConn);
		
		List<EtlDatabaseObject> migratedRecs = loadHelper.getAllSuccedTransformedObjects(dstConf);
		
		if (utilities.listHasElement(migratedRecs)) {
			return migratedRecs.get(0);
		}
		
		return null;
		
	}
	
	@Override
	public String toString() {
		return this.getTransformerDsc();
	}
	
	private EtlDatabaseObject retrieveExistingOnDemandParent(EtlProcessor processor, EtlDatabaseObject srcObject,
	        List<EtlDatabaseObject> srcObjects, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		if (!utilities.stringHasValue(this.onDemandCheckCondition)) {
			return null;
		}
		
		SrcConf srcConf = null;
		DstConf dstConf = null;
		
		ensureDstConfForNonExistingSrcParentInitialized(srcConn, dstConn);
		
		srcConf = loadSrcConfForNonExistingSrcParentIfNeeded(srcConn, dstConn);
		dstConf = getDstConfForNonExistingSrcParent(srcConn, dstConn);
		
		ensureOnDemandCheckConditionElementsInitialized(srcConn, dstConn);
		
		String condition = this.parametrizedOnDemandCheckCondition;
		
		Object[] params = new Object[this.onDemandCondtionElements.size()];
		
		EtlDatabaseObject auxObject = dstConf.createRecordInstance();
		
		for (int i = 0; i < params.length; i++) {
			FieldsMapping mapping = this.onDemandCondtionElements.get(i).getMappig();
			
			FieldTransformingInfo paramValueInfo = null;
			
			try {
				mapping.getTransformerInstance().performFieldTransformation(processor, srcObjects.get(0), auxObject,
				    srcObjects, mapping, srcConn, dstConn);
				
				paramValueInfo = auxObject.getField(mapping.getDstField()).getTransformingInfo();
				
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
							throw new EtlTransformationException("The " + refInfo.getTableName() + "(" + params[i] + ") of "
							        + dstConf.getTableName() + "(" + srcObject.getObjectId().asSimpleNumericValue()
							        + ") cannot be found on src db", srcObject, ActionOnEtlException.ABORT_PROCESS);
						}
						
						params[i] = parentInDst.getObjectId().asSimpleNumericValue();
					}
				}
			}
			catch (MissingFieldException e) {
				paramValueInfo = mapping.getTransformerInstance().transform(processor, srcObjects.get(0), auxObject,
				    srcObjects, mapping, srcConn, dstConn);
				
				params[i] = paramValueInfo.getTransformedValue();
			}
		}
		
		return getDstConfForNonExistingSrcParent(srcConn, dstConn).find(condition, params, dstConn);
	}
	
	DstConf getDstConfForExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		return this.existingParentItemConf.getDstConf().get(0);
	}
	
	protected DstConf getDstConfForNonExistingSrcParent(Connection srcConn, Connection dstConn) throws DBException {
		return this.onDemandCreateParentItemConf.getDstConf().get(0);
	}
	
	SrcConf loadSrcConfForNonExistingSrcParentIfNeeded(Connection srcConn, Connection dstConn) throws DBException {
		ensureEtlItemConfForNonExistingSrcParentInitialized(srcConn, dstConn);
		
		return this.onDemandCreateParentItemConf.getSrcConf();
	}
	
	SrcConf loadSrcConfForExistingSrcParentIfNeeded(Connection srcConn, Connection dstConn) throws DBException {
		ensureEtlItemConfForExistingSrcParentInitialized(srcConn, dstConn);
		
		return this.existingParentItemConf.getSrcConf();
	}
	
	protected void ensureDstConfForNonExistingSrcParentInitialized(Connection srcConn, Connection dstConn)
	        throws DBException {
		ensureEtlItemConfForNonExistingSrcParentInitialized(srcConn, dstConn);
		
		DstConf dstConf = getDstConfForNonExistingSrcParent(srcConn, dstConn);
		
		if (!dstConf.isFullLoaded()) {
			synchronized (lock) {
				if (!dstConf.isFullLoaded()) {
					
					if (!usesTemplate()) {
						dstConf.setDoNotUseSrcConfAsDataSource(true);
						dstConf.setIgnorableFields(this.getIgnorableFields());
						dstConf.setMapping(this.onDemandParentFieldMappings);
					}
					
					dstConf.addAllToAvaliableDataSource(this.relatedDstConf.getAllAvaliableDataSource());
					dstConf.addAllToPreferredDataSource(this.relatedDstConf.getAllPrefferredDataSource());
					
					dstConf.fullLoad(dstConn);
				}
			}
		}
	}
	
	void ensureOnDemandCheckConditionElementsInitialized(Connection srcConn, Connection dstConn) throws DBException {
		if (this.onDemandCondtionElements == null) {
			synchronized (lock) {
				
				ensureDstConfForNonExistingSrcParentInitialized(srcConn, dstConn);
				
				String parametrizedOnDemandCheckCondition = this.onDemandCheckCondition;
				
				if (utilities.stringHasValue(this.onDemandCheckCondition)) {
					
					//We force onDemandCheckCondition to be a full query so that can be correctly be processed by extractSqlConditionElements method
					String query = "select * from x where " + this.onDemandCheckCondition;
					
					List<SqlConditionElement> elements = SQLUtilities.extractSqlConditionElements(query);
					
					List<SqlConditionElement> toRemove = new ArrayList<>();
					
					for (SqlConditionElement field : elements) {
						
						//We want to skip same situation where a field is compared to a subqueries
						if (!SQLUtilities.isValidSqlCondition(field.toString())) {
							toRemove.add(field);
							continue;
						}
						
						field.fullLoad(relatedDstConf);
						
						String regex = "\\b" + Pattern.quote(field.getField()) + "\\s*" + Pattern.quote(field.getOperator())
						        + "\\s*" + Pattern.quote(field.getValue()) + "\\b";
						
						parametrizedOnDemandCheckCondition = parametrizedOnDemandCheckCondition.replaceAll(regex,
						    field.getField() + " " + field.getOperator() + " ?");
					}
					
					elements.removeAll(toRemove);
					
					this.parametrizedOnDemandCheckCondition = parametrizedOnDemandCheckCondition;
					this.onDemandCondtionElements = elements;
				}
			}
		}
	}
	
	boolean sourceParentMayExists() {
		return this.parentSourceIdMapping != null;
	}
	
	void ensureDstConfForExistingSrcParentInitialized(Connection dstConn) throws DBException {
		if (!sourceParentMayExists()) {
			throw new EtlExceptionImpl(
			        "Error On " + this + "\nExisting SrcParent not applicable as no parentSourceIdMapping is defined!");
		}
		
		ensureEtlItemConfForExistingSrcParentInitialized(dstConn, dstConn);
		
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
					
					dstConf.setMapping(this.overrideFields);
					
					dstConf.addAllToAvaliableDataSource(avaliableDataSource);
					
					dstConf.addToPrefferedDataSource(dstConf.getSrcConf());
					dstConf.addAllToPreferredDataSource(preferredDataSource);
					
					dstConf.fullLoad(dstConn);
				}
			}
		}
	}
	
	protected void ensureEtlItemConfForNonExistingSrcParentInitialized(Connection srcConn, Connection dstConn)
	        throws DBException {
		
		if (this.onDemandCreateParentItemConf == null) {
			synchronized (lock) {
				if (onDemandCreateParentItemConf == null) {
					
					EtlItemConfiguration conf = generateEtlItemConf(srcConn);
					
					this.onDemandCreateParentItemConf = conf;
				}
			}
		}
	}
	
	private EtlItemConfiguration generateEtlItemConf(Connection srcConn) throws DBException {
		AbstractTableConfiguration parentConf = new GenericTableConfiguration(parentTableName);
		parentConf.setRelatedEtlConfig(relatedDstConf.getRelatedEtlConf());
		
		EtlItemConfiguration conf = EtlItemConfiguration.fastCreate(parentConf, srcConn);
		
		EtlTemplateInfo template = usesTemplate() ? new EtlTemplateInfo(this.templateName) : null;
		
		if (template != null) {
			template.setParameters(this.templateParams);
			conf.setSrcConf(null);
			conf.setTemplate(template);
			conf.init(relatedDstConf.getRelatedEtlConf(), false);
			
			conf.getRelatedEtlConf().logTrace("Template within transformer loaded!");
		}
		
		conf.setParentItemConf(relatedDstConf.getParentConf());
		conf.setRelatedParentDstConfName(relatedDstConf.getTableAlias());
		
		conf.setDoNotFullLoadDstConf(true);
		
		conf.fullLoad(relatedDstConf.getRelatedEtlConf().getOperations().get(0));
		
		return conf;
	}
	
	boolean existingSrcParentIsApplicable() {
		return this.parentSourceIdMapping != null;
	}
	
	void ensureEtlItemConfForExistingSrcParentInitialized(Connection srcConn, Connection dstConn) throws DBException {
		if (this.parentSourceIdMapping == null) {
			throw new EtlExceptionImpl("Existing SrcParent not applicable as no parentSourceIdMapping is defined!");
		}
		
		if (this.existingParentItemConf == null) {
			synchronized (lock) {
				if (existingParentItemConf == null) {
					
					EtlItemConfiguration conf = generateEtlItemConf(srcConn);
					
					this.existingParentItemConf = conf;
				}
			}
		}
	}
	
}
