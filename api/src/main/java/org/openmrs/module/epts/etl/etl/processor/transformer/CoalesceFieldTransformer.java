package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Field transformer that implements a COALESCE-like behavior for ETL field transformations.
 * <p>
 * This transformer evaluates a sequence of candidate values and returns the first non-null value
 * obtained from them. Each parameter is interpreted as a possible source for the destination field
 * value and is evaluated in the order it is defined. If the result of evaluating the first
 * parameter is {@code null}, the transformer evaluates the next one, continuing until a non-null
 * value is found. If all evaluated parameters result in {@code null}, the transformation result
 * will also be {@code null}.
 * </p>
 * <p>
 * Parameters accepted by this transformer may represent:
 * <ul>
 * <li>Fixed literal values</li>
 * <li>Dynamic parameters resolved during ETL execution</li>
 * <li>Fields from available ETL data sources</li>
 * </ul>
 * </p>
 * <p>
 * When referencing a field from a data source, it can be specified using either:
 * <ul>
 * <li>A simple field name: {@code "field"}</li>
 * <li>A qualified name including the data source alias: {@code "dataSourceName.field"}</li>
 * </ul>
 * The qualified form should be used when multiple data sources may contain fields with the same
 * name to avoid ambiguity.
 * </p>
 * <p>
 * At least two parameters must be provided when configuring this transformer.
 * </p>
 * Example usage: <pre>
 * CoalesceFieldTransformer(patient_id, encounter.patient_id, 'UNKNOWN')
 * </pre> In this example the transformer will:
 * <ol>
 * <li>Try {@code patient_id}</li>
 * <li>If null, try {@code encounter.patient_id}</li>
 * <li>If still null, use the literal value {@code 'UNKNOWN'}</li>
 * </ol>
 */
public class CoalesceFieldTransformer extends AbstractEtlFieldTransformer {
	
	private static final Map<String, CoalesceFieldTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private final List<FieldsMapping> coalesceFields;
	
	public CoalesceFieldTransformer(List<Object> parameters, DstConf dstConf, TransformableField field) {
		
		super(parameters, dstConf, field);
		
		this.coalesceFields = new ArrayList<>();
		
		for (Object obj : parameters) {
			
			String[] fieldParts = obj.toString().split("\\.");
			
			String dataSourceName = null;
			String srcFieldName;
			
			if (fieldParts.length > 1) {
				dataSourceName = fieldParts[0];
				srcFieldName = fieldParts[1];
			} else {
				srcFieldName = fieldParts[0];
			}
			
			FieldsMapping fm = FieldsMapping.fastCreate(srcFieldName, field.getDstField(), true);
			fm.tryToLoadTransformer(dstConf);
			
			if (dataSourceName != null) {
				
				EtlDataSource ds = dstConf.findDataSource(dataSourceName);
				
				if (ds != null) {
					fm.setDataSourceName(ds.getAlias());
				} else {
					throw new EtlExceptionImpl(
					        "Invalid datasource '" + dataSourceName + "' on CoalesceFieldTransformer: " + obj);
				}
				
			} else {
				dstConf.tryToLoadDataSourceToFieldMapping(fm);
			}
			
			if (!fm.hasDataSourceName()) {
				fm.setSrcValue(obj);
			}
			
			this.coalesceFields.add(fm);
		}
		
		dstConf.getRelatedEtlConf().logTrace("CoalesceFieldTransformer initialized");
	}
	
	private static String buildCacheKey(List<Object> parameters, DstConf dstConf, TransformableField field) {
		
		String params = parameters.stream().map(Object::toString).collect(Collectors.joining("|"));
		
		return dstConf.hashCode() + ":" + field.getDstField() + ":" + params;
	}
	
	public List<FieldsMapping> getCoalesceFields() {
		return coalesceFields;
	}
	
	public static CoalesceFieldTransformer getInstance(List<Object> parameters, DstConf dstConf, TransformableField field) {
		
		if (parameters == null || parameters.size() < 2) {
			throw new ForbiddenOperationException("A CoalesceFieldTransformer needs at least 2 parameters.\n"
			        + "Eg: CoalesceFieldTransformer(field1, field2)");
		}
		
		String key = buildCacheKey(parameters, dstConf, field);
		
		return INSTANCES.computeIfAbsent(key, k -> new CoalesceFieldTransformer(parameters, dstConf, field));
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		for (FieldsMapping map : this.getCoalesceFields()) {
			
			FieldTransformingInfo transformingInfo = map.getTransformerInstance().transform(processor, srcObject,
			    transformedRecord, additionalSrcObjects, map, srcConn, dstConn);
			
			if (transformingInfo != null && transformingInfo.getTransformedValue() != null) {
				return transformingInfo;
			}
		}
		
		return null;
	}
	
}
