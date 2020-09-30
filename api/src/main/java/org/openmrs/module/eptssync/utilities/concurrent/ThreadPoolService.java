package org.openmrs.module.eptssync.utilities.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

/**
 * @author jpboane
 *
 */
public class ThreadPoolService {
	private static ThreadPoolService service;
	
	private List<ExecutorServiceManager> createdThreadPools;
	
	private ThreadPoolService(){
		super();
		
		
		this.createdThreadPools = new ArrayList<ExecutorServiceManager>();
	}
	
    public static synchronized ThreadPoolService getInstance() {
        if (service == null) {
            service = new ThreadPoolService();
        }
        
        return service;
    }
	
	public synchronized ExecutorService createNewThreadPoolExecutor(String namingPattern){
		ExecutorServiceManager existingManager = ExecutorServiceManager.find(this.createdThreadPools, namingPattern);
		
		if (existingManager != null) return existingManager.getExecutorService();
		
		BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
				.namingPattern(namingPattern)
				.daemon(true)
				.priority(Thread.MAX_PRIORITY)
				.build();
		
		
		this.createdThreadPools.add(new ExecutorServiceManager(Executors.newSingleThreadExecutor(threadFactory), namingPattern));
		
		return this.createdThreadPools.get(this.createdThreadPools.size()-1).getExecutorService();
	}
}
