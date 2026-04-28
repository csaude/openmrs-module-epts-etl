package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlTranformTarget;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Default field transformer responsible for retrieving the value of a field directly from the
 * configured source data objects.
 * <p>
 * This transformer resolves the destination field value by locating the corresponding source object
 * based on the configured data source alias and retrieving the field value from that object.
 * </p>
 * <p>
 * The field name is resolved using two naming strategies:
 * <ul>
 * <li>snake_case</li>
 * <li>camelCase</li>
 * </ul>
 * The transformer first attempts to retrieve the field using the snake_case representation. If that
 * fails, it falls back to camelCase.
 * </p>
 * <p>
 * If no matching data source is found among the provided source objects, an
 * {@link EtlTransformationException} is thrown.
 * </p>
 * <p>
 * This transformer acts as the base mechanism for retrieving field values from source data sets and
 * is commonly used internally by other transformers.
 * </p>
 */
public class DefaultFieldTransformer extends AbstractEtlFieldTransformer {
	
	private static DefaultFieldTransformer INSTANCE;
	
	private DefaultFieldTransformer(List<Object> parameters, EtlTranformTarget relatedEtlTransformTarget,
	    TransformableField field) {
		super(parameters, relatedEtlTransformTarget, field);
	}
	
	public static DefaultFieldTransformer getInstance(List<Object> parameters, EtlTranformTarget relatedEtlTransformTarget,
	        TransformableField field, Connection conn) {
		
		if (INSTANCE == null) {
			INSTANCE = new DefaultFieldTransformer(parameters, relatedEtlTransformTarget, field);
		}
		
		return INSTANCE;
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects,
	        final TransformableField field, Connection srcConn, Connection dstConn)
	        throws DBException, EtlTransformationException {
		
		Object dstValue = null;
		EtlDataSource ds = null;
		
		boolean found = false;
		
		for (EtlDatabaseObject srcObj : additionalSrcObjects) {
			
			if (field.getDataSourceName() != null
			        && field.getDataSourceName().equals(srcObj.getRelatedConfiguration().getAlias())) {
				
				found = true;
				
				String fieldNameSnake = utilities.parsetoSnakeCase(field.getName());
				String fieldNameCamel = utilities.parsetoCamelCase(field.getName());
				
				Field srcField = null;
				
				try {
					srcField = srcObj.getField(fieldNameSnake);
					
					dstValue = srcObj.getFieldValue(fieldNameSnake);
				}
				catch (ForbiddenOperationException e) {
					srcField = srcObj.getField(fieldNameCamel);
					
					dstValue = srcObj.getFieldValue(fieldNameCamel);
				}
				
				if (srcField.getTransformingInfo() != null && srcField.getTransformingInfo().getTransformedValue() != null) {
					if (srcField.getTransformingInfo().getTransformedValue().equals(dstValue)) {
						return srcField.getTransformingInfo();
					}
				}
				
				ds = (EtlDataSource) srcObj.getRelatedConfiguration();
				
				break;
			}
		}
		
		if (!found) {
			String msg = "The field '" + field.getName()
			        + "' was transformed to null, but it is not configured to accept null values. "
			        + "To avoid process interruption, explicitly allow null values for this field or ensure the transformation produces a valid value.";
			
			if (field.nullValueBehavior().markAsFailed()) {
				throw new EtlTransformationException(msg, srcObject, ActionOnEtlException.LOG);
			} else if (field.nullValueBehavior().abort()) {
				throw new EtlExceptionImpl(msg, srcObject, ActionOnEtlException.ABORT_PROCESS);
			} else if (field.nullValueBehavior().allow()) {
				return null;
			}
		}
		
		return new FieldTransformingInfo(field, dstValue, ds);
	}
	
}
