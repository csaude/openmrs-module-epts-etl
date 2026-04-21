package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.conf.types.MappingNotFoundBehavior;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingMappingException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.EtlInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Field transformer that performs value translation using a mapping table stored in the database.
 * <p>
 * This transformer resolves a destination field value by performing a lookup in a mapping table,
 * using the source field value as the lookup key.
 * </p>
 * <p>
 * <b>Parameters</b>:
 * </p>
 * <ul>
 * <li><b>mappingTable</b> – name of the table containing the mapping definitions</li>
 * <li><b>mappingSrcField</b> – column in the mapping table used to match the source value</li>
 * <li><b>mappingDstField</b> – column in the mapping table whose value will be returned as the
 * transformed value</li>
 * <li><b>extraConditionForExtract</b> (optional) – additional condition used to filter the mapping
 * table during lookup</li>
 * <li><b>onMissing</b> (optional) – defines the behavior when no mapping entry is found</li>
 * </ul>
 * <p>
 * During execution, the source value is obtained from the field being transformed. Any dynamic
 * parameters (e.g. values prefixed with <code>@</code>) are resolved prior to executing the lookup.
 * </p>
 * <p>
 * The mapping table is loaded once using {@link GenericTableConfiguration#fullLoad(Connection)} and
 * cached for reuse across subsequent transformations, improving performance.
 * </p>
 * <p>
 * <b>Behavior</b>:
 * </p>
 * <ul>
 * <li>If a matching mapping entry is found, the value of {@code mappingDstField} is returned.</li>
 * <li>If no mapping entry is found:
 * <ul>
 * <li>If <b>onMissing</b> is defined, the configured behavior is applied (e.g. set null, mark
 * record as failed, or abort process).</li>
 * <li>If no <b>onMissing</b> is defined:
 * <ul>
 * <li>If the destination field has a default value, {@code null} is returned so that the default
 * value can be applied later in the ETL pipeline.</li>
 * <li>Otherwise, a {@link MissingMappingException} is thrown.</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * <b>Example</b>:
 * </p>
 * <pre>
 * MappingFieldTransformer("gender_mapping", "source_gender", "openmrs_gender")
 * </pre>
 * <p>
 * Given the following mapping table:
 * </p>
 * <pre>
 * source_gender | openmrs_gender
 * --------------|---------------
 * M             | Male
 * F             | Female
 * </pre>
 * <p>
 * If the source value is {@code "M"}, the transformer will return {@code "Male"}.
 * </p>
 */
public class MappingFieldTransformer extends AbstractEtlFieldTransformer {
	
	private String mappingTable;
	
	private String mappingSrcField;
	
	private String mappingDstField;
	
	private String extraCondition;
	
	private MappingNotFoundBehavior onMissing;
	
	private volatile TableConfiguration tableConfig;
	
	private static final Map<String, MappingFieldTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private volatile Map<String, Object> mappingCache;
	
	private final Object lock = new Object();
	
	private List<String> rawParameterDefinitions;
	
	private FieldsMapping input;
	
	public MappingFieldTransformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		
		super(parameters, relatedDstConf, field);
		
		validateParams(parameters);
		
		this.onMissing = MappingNotFoundBehavior.ABORT_PROCESS;
		this.mappingTable = parameters.get(0).toString();
		this.mappingSrcField = parameters.get(1).toString();
		this.mappingDstField = parameters.get(2).toString();
		
		this.rawParameterDefinitions = parameters.size() > 3
		        ? parameters.subList(3, parameters.size()).stream().map(Object::toString).toList()
		        : null;
		
		if (utilities.listHasElement(rawParameterDefinitions)) {
			
			for (String fieldData : rawParameterDefinitions) {
				String[] mapping = fieldData.split(":", 2);
				
				if (mapping.length != 2) {
					throw new EtlExceptionImpl("Wrong format for conditional parameters within the tranformer "
					        + getTransformerDsc() + "\n" + "Each object param must be specified as paramName:paramValue");
				}
				
				String paramName = mapping[0];
				String paramValue = mapping[1];
				
				if (!utilities.stringHasValue(paramValue)) {
					throw new EtlExceptionImpl("The paramValue for parameter " + paramName
					        + " has no value on transformer:  " + getTransformerDsc());
				}
				
				if (paramName.equals("input")) {
					if (isTransformerExpression(paramValue)) {
						this.input = FieldsMapping.fastCreate(field.getDstField(), field.getDstField(), false);
						this.input.setTransformer(paramValue);
						this.input.tryToLoadTransformer(relatedDstConf);
						
					} else {
						this.input = FieldsMapping.fastCreate(paramValue, paramValue, relatedDstConf);
					}
				} else if (paramName.equals("extra_condition")) {
					this.extraCondition = paramValue;
				} else if (paramName.equals("on_missing")) {
					try {
						this.onMissing = MappingNotFoundBehavior.valueOf(paramValue);
					}
					catch (Exception e) {
						throw new EtlExceptionImpl("Unsupported value paramValue for parameter " + paramName
						        + " on transformer:  " + getTransformerDsc());
					}
					
				} else {
					throw new ForbiddenOperationException(
					        "Unsupported parameter " + paramName + " on transformer:  " + getTransformerDsc());
				}
			}
		}
	}
	
	public MappingNotFoundBehavior onMissing() {
		return this.onMissing;
	}
	
	private void buildMappingCache(List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn) throws DBException {
		if (mappingCache == null) {
			synchronized (lock) {
				
				if (mappingCache == null) {
					
					this.tableConfig = new GenericTableConfiguration(mappingTable,
					        (TableConfiguration) additionalSrcObjects.get(0).getRelatedConfiguration());
					
					this.tableConfig.setExtraConditionForExtract(this.extraCondition);
					this.tableConfig.fullLoad(srcConn);
					
					List<EtlDatabaseObject> rows = this.tableConfig.searchRecords(null, null, srcConn);
					
					Map<String, Object> cache = new HashMap<>();
					
					for (EtlDatabaseObject obj : rows) {
						
						Object src = obj.getFieldValue(this.mappingSrcField);
						Object dst = obj.getFieldValue(this.mappingDstField);
						
						if (src != null) {
							cache.put(src.toString(), dst);
						}
					}
					
					this.mappingCache = cache;
				}
			}
		}
		
	}
	
	public String getMappingTable() {
		return mappingTable;
	}
	
	public static MappingFieldTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		
		String key = buildCacheKey(relatedDstConf, field, parameters);
		
		return INSTANCES.computeIfAbsent(key, k -> new MappingFieldTransformer(parameters, relatedDstConf, field));
	}
	
	public static void validateParams(List<Object> parameters) {
		if (parameters == null || parameters.size() < 3) {
			throw new EtlExceptionImpl(
			        "MappingFieldTransformer requires at least 3 parameters: mapping_table, mapping_src_field and mapping_dst_field.");
		}
		
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (additionalSrcObjects == null || additionalSrcObjects.isEmpty()) {
			throw new EtlExceptionImpl("MappingFieldTransformer requires at least one source object.");
		}
		
		buildMappingCache(additionalSrcObjects, srcConn);
		
		Object valueToTransform = null;
		
		FieldTransformingInfo transformingInfo = null;
		
		if (this.hasInput()) {
			transformingInfo = this.input.getTransformerInstance().transform(processor, srcObject, transformedRecord,
			    additionalSrcObjects, this.input, srcConn, dstConn);
			
			try {
				valueToTransform = transformingInfo.getTransformedValue();
			}
			catch (Exception e) {
				throw e;
			}
			
		} else {
			valueToTransform = field.getValueToTransform();
		}
		
		if (valueToTransform == null)
			return null;
		
		String srcValueWithParamsReplaced = EtlFieldTransformer
		        .tryToReplaceParametersOnSrcValue(additionalSrcObjects, valueToTransform).toString();
		
		Object dstValue = mappingCache.get(srcValueWithParamsReplaced);
		
		if (dstValue != null) {
			transformingInfo = new FieldTransformingInfo(field, dstValue, null);
			
			transformingInfo.setLoadedWithDefaultValue(true);
			
			return transformingInfo;
		} else if (onMissing().useInputOnMissingMapping()) {
			if (transformingInfo != null) {
				return transformingInfo;
			} else {
				transformingInfo = new FieldTransformingInfo(field, dstValue, null);
				
				transformingInfo.setLoadedWithDefaultValue(true);
				
				return transformingInfo;
			}
		}
		
		if (field.getDefaultValue() == null) {
			
			MissingMappingException e = new MissingMappingException(additionalSrcObjects.get(0), field.getSrcField(),
			        srcValueWithParamsReplaced, this,
			        additionalSrcObjects.get(0).getRelatedConfiguration().getGeneralBehaviourOnEtlException());
			
			if (onMissing().abortProcess()) {
				throw e;
			}
			
			if (onMissing().markRecordAsFailed()) {
				EtlInfo info = transformedRecord.getEtlInfo();
				
				info.setExceptionOnEtl(e);
			}
		}
		
		return null;
	}
	
	private boolean hasInput() {
		return this.input != null;
	}
	
}
