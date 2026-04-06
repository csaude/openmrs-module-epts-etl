package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.DataSourceField;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlValidator;
import org.openmrs.module.epts.etl.conf.interfaces.ValidationRule;
import org.openmrs.module.epts.etl.conf.types.EtlDBConnectionType;
import org.openmrs.module.epts.etl.conf.types.EtlInconsistencyBehavior;
import org.openmrs.module.epts.etl.conf.types.ValidationPhase;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.FieldTransformingInfo;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DefaultEtlValidator implements EtlValidator {
	
	public static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static Object lock = new Object();
	
	private DefaultValidationRule rule;
	
	private EtlInconsistencyBehavior behavior;
	
	private String message;
	
	private String name;
	
	private ValidationPhase phase;
	
	private DataSourceField value;
	
	private boolean initialized;
	
	private EtlDBConnectionType connectionToUse;
	
	private EtlDataConfiguration relatedEtlConf;
	
	public DefaultEtlValidator() {
		phase = ValidationPhase.AFTER_LOAD;
		behavior = EtlInconsistencyBehavior.ABORT_PROCESS;
		connectionToUse = EtlDBConnectionType.SRC;
	}
	
	@Override
	public EtlDBConnectionType getConnectionToUse() {
		return this.connectionToUse;
	}
	
	public void setConnectionToUse(EtlDBConnectionType connectionToUse) {
		this.connectionToUse = connectionToUse;
	}
	
	public EtlDataConfiguration getRelatedEtlConf() {
		return relatedEtlConf;
	}
	
	public void setRelatedEtlConf(EtlDataConfiguration relatedEtlConf) {
		this.relatedEtlConf = relatedEtlConf;
	}
	
	public void init(EtlDataConfiguration relatedEtlConfig) {
		this.relatedEtlConf = relatedEtlConfig;
		
		if (initialized)
			return;
		
		synchronized (lock) {
			getValue().tryToLoadTransformer(null);
			
			if (getValue().getParent() == null) {
				EtlConfiguration etlConf = relatedEtlConfig instanceof EtlConfiguration ? (EtlConfiguration) relatedEtlConfig
				        : relatedEtlConfig.getRelatedEtlConf();
				
				SrcConf srcConf = new SrcConf();
				
				srcConf.setRelatedEtlConfig(etlConf);
				
				srcConf.setTableName(EtlConfiguration.ETL_RECORD_ERROR_TABLE_NAME);
				
				getValue().setParent(srcConf);
			}
			
			initialized = true;
		}
	}
	
	@Override
	public void validate(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject transformedRecord,
	        List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn, Connection dstConn)
	        throws EtlExceptionImpl, DBException {
		
		if (!initialized)
			throw new EtlExceptionImpl("The Validator is not initialized!");
		
		if (connectionToUse.isDst()) {
			getValue().getTransformerInstance().setOverrideConnection(dstConn);
		}
		
		FieldTransformingInfo value = getValue().transform(processor, srcObject, transformedRecord, additionalSrcObjects,
		    srcConn, dstConn);
		
		boolean valid = this.getRule().evaluate(value.getTransformedValue());
		
		if (!valid) {
			handleFailure(transformedRecord);
		}
	}
	
	private void handleFailure(EtlDatabaseObject transformedRecord) {
		EtlExceptionImpl e = new EtlExceptionImpl(buildMessage());
		
		switch (behavior) {
			case MARK_RECORD_AS_FAILED:
				transformedRecord.getEtlInfo().setExceptionOnEtl(e);
				break;
			
			case ABORT_PROCESS:
				throw e;
			
			default:
				throw new EtlExceptionImpl("Unsupported validation fail behavior: " + behavior);
		}
	}
	
	private String buildMessage() {
		return "Validator: " + getName() + " failed"
		        + (utilities.stringHasValue(this.getMessage()) ? " due:" + this.getMessage() + "!" : "!");
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public ValidationPhase getPhase() {
		return this.phase;
	}
	
	@Override
	public EtlInconsistencyBehavior getBehavior() {
		return this.behavior;
	}
	
	@Override
	public DataSourceField getValue() {
		return this.value;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
	
	@Override
	public ValidationRule getRule() {
		return rule;
	}
	
	public void setRule(DefaultValidationRule rule) {
		this.rule = rule;
	}
	
	public void setBehavior(EtlInconsistencyBehavior behavior) {
		this.behavior = behavior;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPhase(ValidationPhase phase) {
		this.phase = phase;
	}
	
	public void setValue(DataSourceField value) {
		this.value = value;
	}
	
	public static void tryToValidate(EtlDataConfiguration conf, Connection srcConn, Connection dstConn)
	        throws EtlExceptionImpl, DBException {
		if (conf.hasValidator()) {
			for (EtlValidator validator : conf.getValidators()) {
				validator.init(conf);
				
				validator.validate(null, null, null, null, srcConn, dstConn);
			}
		}
		
	}
	
}
