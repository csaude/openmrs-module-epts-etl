package org.openmrs.module.epts.etl.utilities.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;

/**
 * @author jpboane
 */
public class ThreadPoolService {
	
	private static ThreadPoolService service;
	
	private List<ExecutorServiceManager> createdThreadPools;
	
	private ThreadPoolService() {
		super();
		
		this.createdThreadPools = new ArrayList<ExecutorServiceManager>();
	}
	
	public static synchronized ThreadPoolService getInstance() {
		if (service == null) {
			service = new ThreadPoolService();
		}
		
		return service;
	}
	
	private ExecutorServiceManager retrieveExistingExecutor(String threadId) {
		return ExecutorServiceManager.find(this.createdThreadPools, threadId);
	}
	
	public void terminateTread(EptsEtlLogger logger, String threadId, Runnable runnable) {
		logger.info("TRYING TO TERMINATE THREAD [" + threadId + "]");
		
		ExecutorServiceManager manager = retrieveExistingExecutor(threadId);
		
		if (manager != null) {
			ThreadPoolExecutor service = (ThreadPoolExecutor) manager.getExecutorService();
			
			service.remove(runnable);
			
			List<Runnable> a = service.shutdownNow();
			
			if (a != null && a.size() > 1)
				throw new ForbiddenOperationException("There were thread awating... " + a);
			
			logger.info("THREAD [" + threadId + "] WAS TERMINATED SUCCESSIFULY!");
			
			this.createdThreadPools.remove(manager);
		} else {
			logger.warn("THREAD [" + threadId + "] WAS NOT FOUND  IN THREAD POOL!!!!");
		}
	}
	
	public synchronized ExecutorService createNewThreadPoolExecutor(String namingPattern) {
		ExecutorServiceManager existingManager = ExecutorServiceManager.find(this.createdThreadPools, namingPattern);
		
		if (existingManager != null) {
			return existingManager.getExecutorService();
		}
		
		BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern(namingPattern).daemon(true)
		        .priority(Thread.MAX_PRIORITY).build();
		
		this.createdThreadPools.add(new ExecutorServiceManager(Executors.newCachedThreadPool(threadFactory), namingPattern));
		
		ThreadPoolExecutor eService = (ThreadPoolExecutor) this.createdThreadPools.get(this.createdThreadPools.size() - 1)
		        .getExecutorService();
		
		return eService;
	}
}
