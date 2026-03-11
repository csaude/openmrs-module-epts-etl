package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingMappingException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Transforms a source value into a destination value based on a predefined mapping. The mapping can
 * be defined either: - From database table field-to-field relationships, or - From an external CSV
 * configuration.
 */
public class MappingFieldTransformer implements EtlFieldTransformer {
	
	private static MappingFieldTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private String mappingTable;
	
	private String mappingSrcField;
	
	private String mappingDstField;
	
	private TableConfiguration tableConfig;
	
	public MappingFieldTransformer(String mappingTable, String mappingSrcField, String mappingDstField) {
		this.mappingTable = mappingTable;
		this.mappingSrcField = mappingSrcField;
		this.mappingDstField = mappingDstField;
	}
	
	public String getMappingTable() {
		return mappingTable;
	}
	
	public static MappingFieldTransformer getInstance(List<Object> parameters) {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			if (parameters == null || parameters.size() < 3) {
				throw new ForbiddenOperationException(
				        "A mapping field transformer need at least 3 parameters: mappingTable, mappingSrcField and mappingDstField. \n ex: org.openmrs.module.epts.etl.etl.processor.transformer.MappingFieldTransformer(mapping_table, srcField, dstField) ");
			}
			
			defaultTransformer = new MappingFieldTransformer(parameters.get(0).toString(), parameters.get(1).toString(),
			        parameters.get(2).toString());
			
			return defaultTransformer;
		}
	}
	
	@Override
	public FieldTransformingInfo transform(List<EtlDatabaseObject> srcObjects, TransformableField field, Connection srcConn,
	        Connection dstConn) throws DBException, EtlTransformationException {
		
		if (this.tableConfig == null) {
			this.tableConfig = new GenericTableConfiguration(mappingTable,
			        (TableConfiguration) srcObjects.get(0).getRelatedConfiguration());
			
			this.tableConfig.fullLoad(srcConn);
		}
		
		String srcValueWithParamsReplaced = EtlFieldTransformer
		        .tryToReplaceParametersOnSrcValue(srcObjects, field.getValueToTransform()).toString();
		
		List<EtlDatabaseObject> list = DatabaseObjectDAO.getByField(this.tableConfig, this.mappingSrcField,
		    srcValueWithParamsReplaced, srcConn);
		
		Object dstValue = null;
		
		if (list != null && list.size() > 0) {
			dstValue = list.get(0).getFieldValue(this.mappingDstField);
		}
		
		if (dstValue != null) {
			return new FieldTransformingInfo(field, dstValue, null);
		} else if (field.getDefaultValue() == null) {
			//We assume that the defaultValue will be loaded from EtlFieldTransformer.transform
			throw new MissingMappingException(srcObjects.get(0), field.getSrcField(), srcValueWithParamsReplaced, this,
			        srcObjects.get(0).getRelatedConfiguration().getGeneralBehaviourOnEtlException());
		} else
			return null;
		
	}
	
}
