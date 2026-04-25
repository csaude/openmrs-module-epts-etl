package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import org.openmrs.module.epts.etl.conf.DefaultEtlValidator;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlTemplateInfo;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public abstract class AbstractEtlFieldTransformer implements EtlFieldTransformer {
	
	protected List<Object> parameters;
	
	protected DstConf relatedEtlTransformTarget;
	
	protected TransformableField field;
	
	private Connection overrideConnection;
	
	public AbstractEtlFieldTransformer(List<Object> parameters, DstConf relatedEtlTargedConf,
	    TransformableField field) {
		this.parameters = parameters;
		this.relatedEtlTransformTarget = relatedEtlTargedConf;
		this.field = field;
	}
	
	public static String buildCacheKey(DstConf dstConf, TransformableField field, List<Object> parameters) {
		String params = parameters != null && !parameters.isEmpty()
		        ? ("|" + parameters.stream().map(Object::toString).collect(Collectors.joining("|")))
		        : null;
		
		return (dstConf != null ? dstConf.toString() : "No EtlTransformTarget") + "|" + field.toString() + params;
	}
	
	public DstConf getRelatedEtlTransformTarget() {
		return relatedEtlTransformTarget;
	}
	
	public String getTransformerDsc() {
		return field.getTransformer();
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
		this.relatedEtlTransformTarget.getRelatedEtlConf().logTrace(msg);
	}
	
	@Override
	public String toString() {
		return this.getTransformerDsc();
	}
	
	public boolean isTransformerExpression(String value) {
		return value != null && value.contains("(") && value.endsWith(")");
	}
	
	@Override
	public EtlConfiguration getRelatedEtlConf() {
		return this.relatedEtlTransformTarget.getRelatedEtlConf();
	}
	
	@Override
	public EtlDataConfiguration getParentConf() {
		return this.relatedEtlTransformTarget;
	}
	
	@Override
	public List<DefaultEtlValidator> getValidators() {
		return null;
	}
	
	@Override
	public void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration) {
	}
	
	@Override
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
	}
	
	@Override
	public ActionOnEtlException getGeneralBehaviourOnEtlException() {
		return null;
	}
	
	@Override
	public EtlTemplateInfo getTemplate() {
		return null;
	}
	
	@Override
	public void setTemplate(EtlTemplateInfo template) {
	}
	
	@Override
	public List<Extension> getExtension() {
		return null;
	}
	
	@Override
	public void setExtension(List<Extension> extension) {
	}
	
}
