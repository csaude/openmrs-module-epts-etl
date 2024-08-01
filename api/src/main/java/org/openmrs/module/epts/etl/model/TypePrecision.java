package org.openmrs.module.epts.etl.model;

public class TypePrecision {
	
	private int length;
	
	private int decimalDigits;
	
	public TypePrecision(int length, int decimalDigits) {
		this.length = length;
		this.decimalDigits = decimalDigits;
	}
	
	public TypePrecision(int length) {
		this.length = length;
		this.decimalDigits = 0;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public int getDecimalDigits() {
		return decimalDigits;
	}
	
	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	
	public static TypePrecision init(int precision, int scale) {
		return new TypePrecision(precision, scale);
	}
	
}
