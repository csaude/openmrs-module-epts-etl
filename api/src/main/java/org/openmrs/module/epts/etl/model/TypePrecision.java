package org.openmrs.module.epts.etl.model;

public class TypePrecision {
	
	private Integer length;
	
	private Integer decimalDigits;
	
	public TypePrecision(Integer length, Integer decimalDigits) {
		this.length = length;
		this.decimalDigits = decimalDigits;
	}
	
	public TypePrecision(Integer length) {
		this.length = length;
		this.decimalDigits = 0;
	}
	
	public Integer getLength() {
		return length;
	}
	
	public void setLength(Integer length) {
		this.length = length;
	}
	
	public Integer getDecimalDigits() {
		return decimalDigits;
	}
	
	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	
	public static TypePrecision init(Integer precision, Integer scale) {
		return new TypePrecision(precision, scale);
	}
	
	@Override
	public String toString() {
		return this.getLength() + (this.getDecimalDigits() != null ? ", " + this.getDecimalDigits() : "");
	}
	
}
