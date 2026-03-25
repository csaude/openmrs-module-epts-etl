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
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.MissingMappingException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Field transformer that performs value translation using a mapping table stored in the database.
 * <p>
 * This transformer retrieves the destination field value by looking up a mapping table using the
 * source field value as the lookup key.
 * </p>
 * <p>
 * The transformer requires three parameters:
 * <ul>
 * <li><b>mappingTable</b> – the name of the table containing the mapping definitions</li>
 * <li><b>mappingSrcField</b> – the column used to match the source value</li>
 * <li><b>mappingDstField</b> – the column whose value will be returned as the transformed
 * value</li>
 * </ul>
 * </p>
 * <p>
 * During execution the source value is obtained from the field being transformed. Any dynamic
 * parameters in the source value are resolved before performing the lookup operation.
 * </p>
 * <p>
 * The mapping table is loaded once using {@link GenericTableConfiguration#fullLoad(Connection)} and
 * reused for subsequent transformations.
 * </p>
 * <p>
 * If a mapping entry is found, the value of {@code mappingDstField} is returned. If no mapping is
 * found:
 * <ul>
 * <li>If the destination field defines a default value, the transformer returns {@code null} so the
 * default value can be applied later in the ETL pipeline.</li>
 * <li>If no default value exists, a {@link MissingMappingException} is thrown.</li>
 * </ul>
 * </p>
 * Example: <pre>
 * MappingFieldTransformer("gender_mapping", "source_gender", "openmrs_gender")
 * </pre> If the source value is {@code "M"} and the mapping table contains: <pre>
 * source_gender | openmrs_gender
 * --------------|---------------
 * M             | Male
 * F             | Female
 * </pre> the transformer will return {@code "Male"}.
 */
public class MappingFieldTransformer extends AbstractEtlFieldTransformer {
	
	private String mappingTable;
	
	private String mappingSrcField;
	
	private String mappingDstField;
	
	private String extraConditionForExtract;
	
	private volatile TableConfiguration tableConfig;
	
	private static final Map<String, MappingFieldTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private volatile Map<String, Object> mappingCache;
	
	private final Object lock = new Object();
	
	public MappingFieldTransformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		
		super(parameters, relatedDstConf, field);
		
		validateParams(parameters);
		
		this.mappingTable = parameters.get(0).toString();
		this.mappingSrcField = parameters.get(1).toString();
		this.mappingDstField = parameters.get(2).toString();
		this.extraConditionForExtract = parameters.size() > 3 ? parameters.get(3).toString() : null;
	}
	
	private void buildMappingCache(Connection srcConn) throws DBException {
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
	
	public String getMappingTable() {
		return mappingTable;
	}
	
	public static MappingFieldTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		
		String table = parameters.get(0).toString();
		String srcField = parameters.get(1).toString();
		String dstField = parameters.get(2).toString();
		String extraConditionForExtract = parameters.size() > 3 ? parameters.get(3).toString() : null;
		
		String key = buildCacheKey(table, srcField, dstField, extraConditionForExtract);
		
		return INSTANCES.computeIfAbsent(key, k -> new MappingFieldTransformer(parameters, relatedDstConf, field));
	}
	
	public static void validateParams(List<Object> parameters) {
		if (parameters == null || parameters.size() < 3) {
			throw new EtlExceptionImpl(
			        "MappingFieldTransformer requires at least 3 parameters: mappingTable, mappingSrcField and mappingDstField.");
		} else if (parameters.size() > 4) {
			throw new EtlExceptionImpl(
			        "MappingFieldTransformer support at most 4 parameters: mappingTable, mappingSrcField, mappingDstField and extraConditionForExtract.");
		}
		
	}
	
	private static String buildCacheKey(String table, String srcField, String dstField, String extraConditionForExtract) {
		
		return table + "|" + srcField + "|" + dstField
		        + (extraConditionForExtract != null ? "|" + extraConditionForExtract : "");
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (additionalSrcObjects == null || additionalSrcObjects.isEmpty()) {
			throw new EtlExceptionImpl("MappingFieldTransformer requires at least one source object.");
		}
		
		if (mappingCache == null) {
			
			synchronized (lock) {
				
				if (mappingCache == null) {
					
					this.tableConfig = new GenericTableConfiguration(mappingTable,
					        (TableConfiguration) additionalSrcObjects.get(0).getRelatedConfiguration());
					
					this.tableConfig.setExtraConditionForExtract(this.extraConditionForExtract);
					this.tableConfig.fullLoad(srcConn);
					
					buildMappingCache(srcConn);
				}
			}
		}
		
		String srcValueWithParamsReplaced = EtlFieldTransformer
		        .tryToReplaceParametersOnSrcValue(additionalSrcObjects, field.getValueToTransform()).toString();
		
		Object dstValue = mappingCache.get(srcValueWithParamsReplaced);
		
		if (dstValue != null) {
			FieldTransformingInfo transformingInfo = new FieldTransformingInfo(field, dstValue, null);
			
			transformingInfo.setLoadedWithDefaultValue(true);
			
			return transformingInfo;
		}
		
		if (field.getDefaultValue() == null) {
			throw new MissingMappingException(additionalSrcObjects.get(0), field.getSrcField(), srcValueWithParamsReplaced,
			        this, additionalSrcObjects.get(0).getRelatedConfiguration().getGeneralBehaviourOnEtlException());
		}
		
		return null;
	}
	
}
