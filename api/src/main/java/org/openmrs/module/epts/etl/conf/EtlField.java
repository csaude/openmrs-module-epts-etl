package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
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
	
	private String dataSource;
	
	private Field srcField;
	
	public EtlField() {
	}
	
	public EtlField(Field srcField, EtlDataSource srcDataSource, boolean preserveOriginalName) {
		this(srcField);
		
		this.srcDataSource = srcDataSource;
		
		this.setName(preserveOriginalName ? this.getSrcField().getName()
		        : this.getSrcDataSource().getAlias() + "_" + this.getSrcField().getName());
	}
	
	public static EtlField fastCreate(String srcFieldName, EtlDataSource srcDataSource, boolean preserveOriginalName) {
		return new EtlField(Field.fastCreateField(srcFieldName), srcDataSource, preserveOriginalName);
	}
	
	public EtlField(Field srcField) {
		this.srcField = srcField;
		
		this.copyFrom(srcField);
	}
	
	public String getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
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
	
	public boolean hasSrcField() {
		return this.getSrcField() != null;
	}
	
	public static List<EtlField> converteFromDataSourceFields(EtlDataSource dataSource, boolean preserveOriginalName) {
		if (!dataSource.hasFields())
			throw new ForbiddenOperationException("The datasource " + dataSource.getAlias() + " has no fields!");
		
		List<EtlField> fields = new ArrayList<>(dataSource.getFields().size());
		
		for (Field f : dataSource.getFields()) {
			fields.add(new EtlField(f, dataSource, preserveOriginalName));
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
	
	public void fullLoad(SrcConf srcConf) {
		if (this.getSrcDataSource() == null) {
			List<EtlDataSource> avaliableDs = utilities.parseToList(srcConf);
			
			if (srcConf.hasExtraDataSource()) {
				avaliableDs.addAll(srcConf.getAvaliableExtraDataSource());
			}
			
			if (utilities.stringHasValue(this.getDataSource())) {
				
				for (EtlDataSource ds : avaliableDs) {
					if (ds.getAlias().equals(this.getDataSource())) {
						this.setSrcDataSource(ds);
						
						break;
					}
				}
				
				if (this.getSrcDataSource() == null) {
					throw new ForbiddenOperationException("The dataSource " + this.getDataSource()
					        + " cannot be found on the dataSource list on the item configuration "
					        + ((EtlItemConfiguration) srcConf.getParentConf()).getConfigCode() + "!!!");
				}
			} else {
				//Discovery the Ds
				
				for (EtlDataSource ds : avaliableDs) {
					if (ds.containsField(this.getName())) {
						this.setSrcDataSource(ds);
						
						break;
					}
				}
				
				throw new ForbiddenOperationException("The etlField " + this.getName()
				        + " cannot be found on any dataSource listed on the item configuration "
				        + ((EtlItemConfiguration) srcConf.getParentConf()) + "!!!");
			}
		} else {
			this.setDataSource(this.getSrcDataSource().getAlias());
		}
		
		this.setSrcField(this.getSrcDataSource().getField(this.getName()));
		
		if (this.getSrcField() == null) {
			throw new ForbiddenOperationException(
			        "The dataSource '" + this.getDataSource() + "' does not contain the srcField " + this.getName());
		}
		
		if (this.getDataType() == null) {
			this.setDataType(this.getSrcField().getDataType());
		}
		
	}
}
