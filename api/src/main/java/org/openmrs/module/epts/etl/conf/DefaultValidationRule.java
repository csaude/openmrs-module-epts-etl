package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.conf.interfaces.ValidationRule;
import org.openmrs.module.epts.etl.conf.types.ValidationRuleType;

public class DefaultValidationRule implements ValidationRule {
	
	private ValidationRuleType type;
	
	private Object expectedValue;
	
	public DefaultValidationRule() {
		type = ValidationRuleType.EQUALS;
	}
	
	public DefaultValidationRule(ValidationRuleType type, Object expectedValue) {
		this.type = type;
		this.expectedValue = expectedValue;
	}
	
	@Override
	public boolean evaluate(Object value) {
		
		switch (type) {
			
			case EQUALS:
				return safeEquals(value, expectedValue);
			
			case NOT_EQUALS:
				return !safeEquals(value, expectedValue);
			
			case IS_NULL:
				return value == null;
			
			case NOT_NULL:
				return value != null;
			
			case GREATER_THAN:
				return compare(value, expectedValue) > 0;
			
			case LESS_THAN:
				return compare(value, expectedValue) < 0;
			
			case EXISTS:
				return value != null && !value.toString().isEmpty();
			
			case IN:
				if (expectedValue instanceof Iterable<?>) {
					for (Object v : (Iterable<?>) expectedValue) {
						if (safeEquals(value, v)) {
							return true;
						}
					}
				}
				return false;
			
			default:
				throw new IllegalArgumentException("Unsupported validation type: " + type);
		}
	}
	
	@Override
	public ValidationRuleType getType() {
		return type;
	}
	
	@Override
	public Object getExpectedValue() {
		return expectedValue;
	}
	
	// ========================
	// Helpers
	// ========================
	
	private boolean safeEquals(Object a, Object b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}
	
	@SuppressWarnings("unchecked")
	private int compare(Object a, Object b) {
		if (a == null || b == null) {
			throw new IllegalArgumentException("Cannot compare null values");
		}
		
		if (a instanceof Comparable && b instanceof Comparable) {
			return ((Comparable<Object>) a).compareTo(b);
		}
		
		throw new IllegalArgumentException("Values are not comparable: " + a + ", " + b);
	}
}
