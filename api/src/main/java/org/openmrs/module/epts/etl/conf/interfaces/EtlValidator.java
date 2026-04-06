package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.DataSourceField;
import org.openmrs.module.epts.etl.conf.types.EtlDBConnectionType;
import org.openmrs.module.epts.etl.conf.types.EtlInconsistencyBehavior;
import org.openmrs.module.epts.etl.conf.types.ValidationPhase;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Defines a validation rule that can be applied during the ETL process.
 * <p>
 * Validators allow dynamic validation of data or configuration values during different phases of
 * the ETL pipeline. A validator typically:
 * </p>
 * <ul>
 * <li>Extracts a value from the ETL context (source, transformed record, or external query)</li>
 * <li>Applies a validation rule</li>
 * <li>Triggers a configured behavior if the validation fails</li>
 * </ul>
 * <p>
 * Validators can be attached to any ETL configuration element and executed either before or after
 * loading.
 * </p>
 */
public interface EtlValidator {
	
	/**
	 * Executes the validation logic.
	 *
	 * @throws EtlExceptionImpl if validation fails and behavior is ABORT_PROCESS
	 */
	void validate(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject transformedRecord,
	        List<EtlDatabaseObject> additionalSrcObjects, Connection srcConn, Connection dstConn)
	        throws EtlExceptionImpl, DBException;
	
	String getName();
	
	ValidationPhase getPhase();
	
	EtlInconsistencyBehavior getBehavior();
	
	DataSourceField getValue();
	
	String getMessage();
	
	ValidationRule getRule();
	
	EtlDBConnectionType getConnectionToUse();
	
	void init(EtlDataConfiguration relatedEtlConfig);
}
