package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlTranformTarget;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;

public interface TransformerFactory {
	
	EtlFieldTransformer create(List<Object> parameters, EtlTranformTarget relatedDstConf, TransformableField field, Connection conn);
}
