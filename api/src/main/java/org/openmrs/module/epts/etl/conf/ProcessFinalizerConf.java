package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.conf.types.EtlConnectionType;
import org.openmrs.module.epts.etl.controller.ProcessFinalizer;
import org.openmrs.module.epts.etl.controller.SqlProcessFInalizer;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class ProcessFinalizerConf {
	
	/**
	 * The finalizer class
	 */
	private String finalizerFullClassName;
	
	private String sqlFinalizerQuery;
	
	private Class<? extends ProcessFinalizer> finalizerClazz;
	
	private EtlConnectionType connectionToUse;
	
	public ProcessFinalizerConf() {
		this.connectionToUse = EtlConnectionType.srcConnInfo;
	}
	
	public EtlConnectionType getConnectionToUse() {
		return connectionToUse;
	}
	
	public void setConnectionToUse(EtlConnectionType connectionToUse) {
		this.connectionToUse = connectionToUse;
	}
	
	public String getFinalizerFullClassName() {
		return finalizerFullClassName;
	}
	
	public void setFinalizerFullClassName(String finalizerFullClassName) {
		this.finalizerFullClassName = finalizerFullClassName;
	}
	
	public String getSqlFinalizerQuery() {
		return sqlFinalizerQuery;
	}
	
	public void setSqlFinalizerQuery(String sqlFinalizerQuery) {
		this.sqlFinalizerQuery = sqlFinalizerQuery;
	}
	
	public Class<? extends ProcessFinalizer> getFinalizerClazz() {
		return finalizerClazz;
	}
	
	public void setFinalizerClazz(Class<? extends ProcessFinalizer> finalizerClazz) {
		this.finalizerClazz = finalizerClazz;
	}
	
	public boolean hasSqlFinalizerQuery() {
		return CommonUtilities.getInstance().stringHasValue(this.getSqlFinalizerQuery());
	}
	
	public boolean hasFinalizerFullClassName() {
		return CommonUtilities.getInstance().stringHasValue(this.getFinalizerFullClassName());
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ProcessFinalizer> void loadFinalizer() {
		
		try {
			
			if (hasSqlFinalizerQuery() && !hasFinalizerFullClassName()) {
				this.setFinalizerFullClassName(SqlProcessFInalizer.class.getCanonicalName());
			}
			
			ClassLoader loader = ProcessFinalizer.class.getClassLoader();
			
			Class<S> c = (Class<S>) loader.loadClass(this.getFinalizerFullClassName());
			
			this.finalizerClazz = (Class<S>) c;
			
			if (SqlProcessFInalizer.class.isAssignableFrom(this.finalizerClazz)) {
				if (!hasSqlFinalizerQuery()) {
					throw new ForbiddenOperationException("You must specifiy the 'sqlFinalizerQuery'");
				} else {
					
				}
			}
		}
		catch (ClassNotFoundException e) {}
	}
	
}
