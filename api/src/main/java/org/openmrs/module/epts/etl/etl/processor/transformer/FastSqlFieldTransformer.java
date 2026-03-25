package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.datasource.QueryDataSourceConfig;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.EmptyTransformedValueException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Field transformer that retrieves the destination field value by executing a SQL query against the
 * source database.
 * <p>
 * The SQL query is executed through a {@link QueryDataSourceConfig}, which loads the result as a
 * temporary data source during the ETL execution. The query must return at least one column; only
 * the value of the first column of the first returned row will be used as the destination field
 * value.
 * </p>
 * <p>
 * The SQL query is executed lazily: the {@link QueryDataSourceConfig} is created and fully loaded
 * only once, during the first invocation of the transformer. Subsequent transformations reuse the
 * previously loaded configuration.
 * </p>
 * <p>
 * If the query returns no result or the resulting value is {@code null}, the transformer behaves as
 * follows:
 * <ul>
 * <li>If the destination field defines a default value, the transformer returns {@code null} so
 * that the default value can be applied later by the ETL processing pipeline.</li>
 * <li>If no default value is defined, an {@link EmptyTransformedValueException} is thrown.</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * FastSqlFieldTransformer("SELECT uuid()")
 * </pre> In this example the transformer executes the SQL query and assigns the resulting UUID
 * value to the destination field.
 */
public class FastSqlFieldTransformer extends AbstractEtlFieldTransformer {
	
	private final Object lock = new Object();
	
	private static final Map<String, FastSqlFieldTransformer> INSTANCES = new ConcurrentHashMap<>();
	
	private String sqlQuery;
	
	private volatile QueryDataSourceConfig dataSourceConfig;
	
	public FastSqlFieldTransformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		super(parameters, relatedDstConf, field);
		
		this.sqlQuery = retrieveSqlQueryFromParameters(parameters);
	}
	
	public String getSqlQuery() {
		return sqlQuery;
	}
	
	public static FastSqlFieldTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		
		String sqlQuery = retrieveSqlQueryFromParameters(parameters);
		
		return INSTANCES.computeIfAbsent(sqlQuery, k -> new FastSqlFieldTransformer(parameters, relatedDstConf, field));
	}
	
	private static String retrieveSqlQueryFromParameters(List<Object> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			throw new EtlExceptionImpl("A FastSqlFieldTransformer needs a sqlQuery as parameter.\n"
			        + "ex: org.openmrs.module.epts.etl.etl.processor.transformer.FastSqlFieldTransformer(select uuid())");
		}
		
		if (parameters.size() > 1) {
			throw new EtlExceptionImpl("A FastSqlFieldTransformer supports only one parameter, the sqlQuery");
		}
		
		String sqlQuery = parameters.get(0).toString();
		return sqlQuery;
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (additionalSrcObjects == null || additionalSrcObjects.isEmpty()) {
			throw new EtlExceptionImpl("FastSqlFieldTransformer requires at least one source object.");
		}
		
		if (dataSourceConfig == null) {
			synchronized (lock) {
				if (dataSourceConfig == null) {
					
					QueryDataSourceConfig conf = new QueryDataSourceConfig(sqlQuery,
					        (SrcConf) srcObject.getRelatedConfiguration());
					
					conf.fullLoad(srcConn);
					
					dataSourceConfig = conf;
				}
			}
		}
		
		EtlDatabaseObject srcObj = dataSourceConfig.loadRelatedSrcObject(processor, srcObject, additionalSrcObjects,
		    srcConn);
		
		Object dstValue = null;
		
		if (srcObj != null && srcObj.getFields() != null && !srcObj.getFields().isEmpty()) {
			
			dstValue = srcObj.getFields().get(0).getValue();
		}
		
		if (dstValue != null) {
			return new FieldTransformingInfo(field, dstValue, null);
		}
		
		if (field.getDefaultValue() == null) {
			throw new EmptyTransformedValueException(additionalSrcObjects.get(0), field.getSrcField(), this,
			        additionalSrcObjects.get(0).getRelatedConfiguration().getGeneralBehaviourOnEtlException());
		}
		
		return null;
	}
	
}
