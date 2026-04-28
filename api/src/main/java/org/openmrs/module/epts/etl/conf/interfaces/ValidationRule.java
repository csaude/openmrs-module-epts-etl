package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.conf.types.ValidationRuleType;

/**
 * Defines a validation rule to be applied on a given value during the ETL process.
 * <p>
 * A ValidationRule is responsible for evaluating whether a given value satisfies a specific
 * condition. It is used by {@link EtlValidator} implementations to determine whether a validation
 * passes or fails.
 * </p>
 * <p>
 * Implementations may represent simple comparisons (e.g. equals, not null) or more complex rules
 * (e.g. range checks, existence in database, pattern matching).
 * </p>
 */
public interface ValidationRule {
	
	/**
	 * Evaluates the rule against the provided value.
	 *
	 * @param value the value to validate
	 * @return {@code true} if the value satisfies the rule, {@code false} otherwise
	 */
	boolean evaluate(Object value);
	
	/**
	 * @return the type of validation rule
	 */
	ValidationRuleType getType();
	
	/**
	 * @return the expected value used in the validation (if applicable)
	 */
	Object getExpectedValue();
	
}
