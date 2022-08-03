package org.openmrs.module.eptssync.utilities.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.logging.Log;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

/**
 * @author jpboane
 *
 */
public class ThreadPoolService {
	private static CommonUtilities utilities = CommonUtilities.getInstance();

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
    
    private ExecutorService retrieveExistingExecutor(String threadId) {
    	ExecutorServiceManager manager = ExecutorServiceManager.find(this.createdThreadPools, threadId);
    	
    	if (manager != null) return manager.getExecutorService();
    	
    	return null;
    }
    
    public void terminateTread(Log logger, Level logLevel, String threadId) {
    	utilities.logDebug("TRYING TO TERMINATE THREAD [" + threadId + "]", logger, logLevel);
    	
    	ExecutorService service = retrieveExistingExecutor(threadId);
    	
    	if (service != null) {
    		List<Runnable> a = service.shutdownNow();
    		
    		if (a != null && a.size() > 1) throw new ForbiddenOperationException("There were thread awating... " + a);
    	
    		utilities.logDebug("THREAD [" + threadId + "] WAS TERMINATED SUCCESSIFULY!", logger, logLevel);
       }
    	else {
    		utilities.logErr("THREAD [" + threadId + "] WAS NOT FOUND  IN THREAD POOL!!!!", logger, logLevel);
     	}
    }
	
	public synchronized ExecutorService createNewThreadPoolExecutor(String namingPattern){
		ExecutorServiceManager existingManager = ExecutorServiceManager.find(this.createdThreadPools, namingPattern);
		
		if (existingManager != null) return existingManager.getExecutorService();
		
		BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
				.namingPattern(namingPattern)
				.daemon(true)
				.priority(Thread.MAX_PRIORITY)
				.build();
		
		
		this.createdThreadPools.add(new ExecutorServiceManager(Executors.newCachedThreadPool(threadFactory), namingPattern));
		
		return this.createdThreadPools.get(this.createdThreadPools.size()-1).getExecutorService();
	}
}
