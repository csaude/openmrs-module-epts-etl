package org.openmrs.module.epts.etl.conf.datasource;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.model.Field;

public class DataSourceField extends Field {
	
	private static final long serialVersionUID = -7824136202167355998L;
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
	private Extension extension;
	
	private String dataType;
	
	private boolean dataTypeLoaded;
	
}
