package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Field transformer that performs direct value assignment without applying any additional
 * transformation logic.
 * <p>
 * The transformer simply resolves any dynamic parameters present in the source value and assigns
 * the resulting value to the destination field.
 * </p>
 * <p>
 * This transformer is used as the default transformer when no explicit transformer is defined for a
 * field mapping.
 * </p>
 * <p>
 * Before returning the value, any dynamic parameters contained in the source value are resolved
 * using {@link EtlFieldTransformer#tryToReplaceParametersOnSrcValue}.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 * valueToTransform = "@patient_uuid"
 * </pre> If {@code patient_uuid = "1234-5678"}, the transformer will assign {@code "1234-5678"}
 * directly to the destination field.
 */
public class SimpleValueTransformer extends AbstractEtlFieldTransformer {
	
	protected static final Map<String, SimpleValueTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private SimpleValueTransformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		super(parameters, relatedDstConf, field);
	}
	
	public static SimpleValueTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field, Connection conn) {
		
		String key = buildCacheKey(relatedDstConf, field, parameters);
		
		return INSTANCES.computeIfAbsent(key, k -> new SimpleValueTransformer(parameters, relatedDstConf, field));
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (additionalSrcObjects == null || additionalSrcObjects.isEmpty()) {
			throw new EtlTransformationException("SimpleValueTransformer requires at least one source object.", srcObject,
			        ActionOnEtlException.ABORT_PROCESS);
		}
		
		Object result = EtlFieldTransformer.tryToReplaceParametersOnSrcValue(additionalSrcObjects,
		    field.getValueToTransform());
		
		FieldTransformingInfo transformingInfo = new FieldTransformingInfo(field, result, null);
		
		transformingInfo.setLoadedWithDefaultValue(result != null && result.toString().equals(field.getValueToTransform()));
		
		return transformingInfo;
	}
}
