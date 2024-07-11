package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;

public class DefaultRecordTransformer implements EtlRecordTransformer {
	
	@Override
	public EtlDatabaseObject transform(EtlDatabaseObject srcObject, DstConf dstConf, Connection srcConn,
	        Connection dstConn) {
		EtlDatabaseObject transformed = (GenericDatabaseObject) dstConf.createRecordInstance();
		
		transformed.setFieldValue("id", srcObject.getFieldValue("user_id"));
		transformed.setFieldValue("name", srcObject.getFieldValue("username"));
		
		return transformed;
	}
	
}
