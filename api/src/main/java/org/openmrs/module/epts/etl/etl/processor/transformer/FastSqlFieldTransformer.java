package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.QueryDataSourceConfig;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.exceptions.EmptyTransformedValueException;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Transforms the dst value from a sql query
 */
public class FastSqlFieldTransformer implements EtlFieldTransformer {
	
	private static FastSqlFieldTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private String sqlQuery;
	
	private QueryDataSourceConfig dataSourceConfig;
	
	public FastSqlFieldTransformer(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}
	
	public String getSqlQuery() {
		return sqlQuery;
	}
	
	public static FastSqlFieldTransformer getInstance(List<Object> parameters) {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			if (parameters == null || parameters.isEmpty()) {
				throw new ForbiddenOperationException(
				        "A FastSqlFieldTransformer needs a sqlQuery as parameter. \n ex: org.openmrs.module.epts.etl.etl.processor.transformer.FastSqlFieldTransformer(select uuid()) ");
			}
			
			if (parameters.size() > 1) {
				throw new ForbiddenOperationException("A FastSqlFieldTransformer supports only one parameter, the sqlQuery");
			}
			
			defaultTransformer = new FastSqlFieldTransformer(parameters.get(0).toString());
			
			return defaultTransformer;
		}
	}
	
	@Override
	public Object transform(List<EtlDatabaseObject> srcObjects, TransformableField field, Connection srcConn,
	        Connection dstConn) throws DBException, EtlTransformationException {
		
		synchronized (LOCK_STRING) {
			if (this.dataSourceConfig == null) {
				this.dataSourceConfig = new QueryDataSourceConfig(this.sqlQuery,
				        (SrcConf) srcObjects.get(0).getRelatedConfiguration());
				
				this.dataSourceConfig.fullLoad(srcConn);
			}
		}
		
		EtlDatabaseObject srcObject = this.dataSourceConfig.loadRelatedSrcObject(srcObjects, srcConn);
		
		Object dstValue = null;
		
		if (srcObject != null) {
			dstValue = srcObject.getFields().get(0).getValue();
		}
		
		if (dstValue != null) {
			return dstValue;
		} else if (field.getDefaultValue() == null) {
			//We assume that the defaultValue will be loaded from EtlFieldTransformer.transform
			throw new EmptyTransformedValueException(srcObjects.get(0), field.getSrcField(), this,
			        srcObjects.get(0).getRelatedConfiguration().getGeneralBehaviourOnEtlException());
		} else
			return null;
		
	}
	
}
