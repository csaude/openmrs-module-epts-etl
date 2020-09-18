/**
 * 
 */
package org.openmrs.module.eptssync.utilities.concurrent;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Classe auxiliar para associar {@link ExecutorService} ao namingPattern a ele associado
 * 
 * @author jpboane 09/01/2020
 *
 */
public class ExecutorServiceManager {
	private ExecutorService executorService;
	private String namingPattern;
	
	public ExecutorServiceManager(ExecutorService executorService, String namingPattern){
		this.executorService = executorService;
		this.namingPattern = namingPattern;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExecutorServiceManager) {
			ExecutorServiceManager manager = (ExecutorServiceManager)obj;
			
			return this.namingPattern.equals(manager.namingPattern);
		}
		else return super.equals(obj);
	}
	
	/**
	 * @return o valor do atributo {@link #executorService}
	 */
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	public static ExecutorServiceManager find(List<ExecutorServiceManager> managers, String namingPattern){
		ExecutorServiceManager toFind = new ExecutorServiceManager(null, namingPattern);
		
		for (ExecutorServiceManager manager : managers){
			if (toFind.equals(manager)) return manager;
		}
		
		return null;
	}
}
