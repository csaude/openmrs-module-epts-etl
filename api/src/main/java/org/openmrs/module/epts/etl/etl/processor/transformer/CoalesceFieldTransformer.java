package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Transforms a source value into a destination value based on a predefined mapping. The mapping can
 * be defined either: - From database table field-to-field relationships, or - From an external CSV
 * configuration.
 */
public class CoalesceFieldTransformer implements EtlFieldTransformer {
	
	private static CoalesceFieldTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private List<FieldsMapping> coalesceFields;
	
	private DstConf dstConf;
	
	public CoalesceFieldTransformer(List<Object> coalesceValues, DstConf dstConf, TransformableField field) {
		
		this.coalesceFields = new ArrayList<>();
		this.dstConf = dstConf;
		
		for (Object obj : this.getCoalesceValues()) {
			String[] fieldParts = obj.toString().split(".");
			
			String dataSourceName = null;
			String srcFieldName = null;
			EtlDataSource ds = null;
			
			if (fieldParts.length > 1) {
				dataSourceName = fieldParts[0];
				srcFieldName = fieldParts[1];
			} else {
				srcFieldName = fieldParts[0];
			}
			
			FieldsMapping fm = FieldsMapping.fastCreate(srcFieldName, field.getDstField());
			
			if (srcFieldName != null) {
				ds = dstConf.findDataSource(dataSourceName);
				
				if (ds != null) {
					fm.setDataSourceName(ds.getAlias());
				} else {
					throw new ForbiddenOperationException(
					        "Invalid datasource '" + dataSourceName + "' on CoalesceFieldTransforer: " + obj);
				}
				
			} else {
				dstConf.tryToLoadDataSourceToFieldMapping(fm);
			}
			
			if (!fm.hasDataSourceName()) {
				fm.setSrcValue(obj.toString());
			}
			
			this.coalesceFields.add(fm);
		}
		
	}
	
	public DstConf getDstConf() {
		return dstConf;
	}
	
	public List<FieldsMapping> getCoalesceValues() {
		return coalesceFields;
	}
	
	public static CoalesceFieldTransformer getInstance(List<Object> parameters, DstConf dstConf, TransformableField field) {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			if (parameters == null || parameters.size() < 2) {
				throw new ForbiddenOperationException(
				        "A coalesce field transformer need at least 2 parameters. \n Eg: org.openmrs.module.epts.etl.etl.processor.transformer.CoalesceFieldTransformer(@field_value_01, @field_value_02) ");
			}
			
			defaultTransformer = new CoalesceFieldTransformer(parameters, dstConf, field);
			
			return defaultTransformer;
		}
	}
	
	@Override
	public FieldTransformingInfo transform(List<EtlDatabaseObject> srcObjects, TransformableField field, Connection srcConn,
	        Connection dstConn) throws DBException, EtlTransformationException {
		
		Object dstValue = null;
		EtlDataSource ds = null;
		EtlFieldTransformer fielTransformer = DefaultFieldTransformer.getInstance();
		
		for (FieldsMapping map : this.getCoalesceValues()) {
			
			dstValue = fielTransformer.transform(srcObjects, field, srcConn, dstConn);
			
			if (dstValue != null) {
				
				ds = this.getDstConf().findDataSource(map.getDataSourceName());
				
				break;
			}
		}
		
		if (dstValue == null) {
			for (FieldsMapping map : this.getCoalesceValues()) {
				dstValue = fielTransformer.transform(srcObjects, field, srcConn, dstConn);
				
				if (dstValue != null) {
					
					ds = this.getDstConf().findDataSource(map.getDataSourceName());
					
					break;
				}
			}
		}
		
		if (dstValue != null) {
			return new FieldTransformingInfo(field, dstValue, ds);
		}
		
		return null;
		
	}
	
}
