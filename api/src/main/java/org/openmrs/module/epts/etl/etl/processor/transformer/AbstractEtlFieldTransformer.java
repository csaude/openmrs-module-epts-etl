package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;

public abstract class AbstractEtlFieldTransformer implements EtlFieldTransformer {
	
	protected List<Object> parameters;
	
	protected DstConf relatedDstConf;
	
	protected TransformableField field;
	
	private Connection overrideConnection;
	
	public AbstractEtlFieldTransformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		this.parameters = parameters;
		this.relatedDstConf = relatedDstConf;
		this.field = field;
	}
	
	@Override
	public Connection getOverrideConnection() {
		return overrideConnection;
	}
	
	@Override
	public void setOverrideConnection(Connection overrideConnection) {
		this.overrideConnection = overrideConnection;
	}
	
	protected void logTrace(String msg) {
		this.relatedDstConf.getRelatedEtlConf().logTrace(msg);
	}
	
	@Override
	public String toString() {
		return field.getTransformer();
	}
}
