package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Allow the custom field transformation.
 */
public interface EtlFieldTransformer {
	
	static final String DEFAULT_TRANSFORMER = DefaultFieldTransformer.class.getCanonicalName();
	
	static final String ARITHMETIC_TRANSFORMER = ArithmeticFieldTransformer.class.getCanonicalName();
	
	/**
	 * Generates the transformed value for a given dtsField and set it to dstObject.
	 * 
	 * @param transformedRecord the transformed record were the field will be set to
	 * @param srcObjects the available src objects
	 * @param fieldsMapping the field mapping containing the mapping information
	 * @param srcConn
	 * @param dstConn
	 * @return the transformed value for dstField
	 */
	void transform(EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> srcObjects, FieldsMapping fieldsMapping,
	        Connection srcConn, Connection dstConn) throws DBException, ForbiddenOperationException;
}
