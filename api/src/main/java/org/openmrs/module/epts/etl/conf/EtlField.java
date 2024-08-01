package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;

/**
 * Represent a field involved in ETL process for a srcConf.
 */
public class EtlField extends Field {
	
	private static final long serialVersionUID = -2340570420243156339L;
	
	private EtlDataSource srcDataSource;
	
	private Field srcField;
	
	public EtlField() {
	}
	
	public EtlField(Field srcField, EtlDataSource srcDataSource) {
		this(srcField);
		
		this.srcDataSource = srcDataSource;
		
		this.setName(this.getSrcDataSource().getName() + "_" + this.getSrcField().getName());
	}
	
	public EtlField(Field srcField) {
		this.srcField = srcField;
		
		this.copyFrom(srcField);
	}
	
	public EtlDataSource getSrcDataSource() {
		return srcDataSource;
	}
	
	public void setSrcDataSource(EtlDataSource srcDataSource) {
		this.srcDataSource = srcDataSource;
	}
	
	public Field getSrcField() {
		return srcField;
	}
	
	public void setSrcField(Field srcField) {
		this.srcField = srcField;
	}
	
	public static List<EtlField> converteFromDataSourceFields(EtlDataSource dataSource) {
		if (!dataSource.hasFields())
			throw new ForbiddenOperationException("The datasource " + dataSource.getName() + " has no fields!");
		
		List<EtlField> fields = new ArrayList<>(dataSource.getFields().size());
		
		for (Field f : dataSource.getFields()) {
			fields.add(new EtlField(f, dataSource));
		}
		
		return fields;
	}
	
	public static List<EtlField> converteFromDataSourceFields(List<Field> simpleFields) {
		if (utilities.arrayHasNoElement(simpleFields))
			throw new ForbiddenOperationException("The fields is empty!");
		
		List<EtlField> fields = new ArrayList<>(simpleFields.size());
		
		for (Field f : simpleFields) {
			fields.add(new EtlField(f));
		}
		
		return fields;
	}
	
	public static List<Field> convertToSimpleFiled(List<EtlField> etlFields) {
		List<Field> fields = new ArrayList<>(etlFields.size());
		
		try {
			for (EtlField field : etlFields) {
				fields.add((Field) field.clone());
			}
		}
		catch (CloneNotSupportedException e) {
			throw new EtlExceptionImpl(e);
		}
		
		return fields;
	}
}
