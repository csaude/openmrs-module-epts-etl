package fgh.spi.changedrecordsdetector;

import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;

import fgh.sp.openmrs_changed_records_action.eip.EipChangedRecordDetectedAction;

/**
 * @author jpboane
 *
 */
public class DetectedRecordService extends GenericOperationsService<DetectedRecordAction>{
	private static final long serialVersionUID = 6025599346549221230L;
	
	private static DetectedRecordService service;
	
	private DetectedRecordService(){
		super();
	}
	
    public static synchronized DetectedRecordService getInstance() {
        if (service == null) {
            service = new DetectedRecordService();
        }
        
        return service;
    }

    public boolean isDBServiceConfigured(String appCode) {
    			
    	DetectedRecordAction action = detectAction(appCode);
    	
    	return action.isDBServiceConfigured();
   }
    
	public void configureDBService(String appCode, DBConnectionInfo dbConnectionInfo) {
		DetectedRecordAction action = detectAction(appCode);
		action.configureDBService(dbConnectionInfo);
	}
	
	public void performeAction(String appCode, ChangedRecord record, SyncTableConfiguration syncTableConfiguration) {
		detectAction(appCode).performeAction(record, syncTableConfiguration);
	}
	
	public void performeAction(String appCode, List<ChangedRecord> records, SyncTableConfiguration syncTableConfiguration) {
		detectAction(appCode).performeAction(records, syncTableConfiguration);
	}
      
      
    static DetectedRecordAction[] staticServices = {new EipChangedRecordDetectedAction()}; 
    
    @SuppressWarnings("unused")
	private DetectedRecordAction detectAction(String appCode) {
    	
    	for (DetectedRecordAction action : staticServices) {
			if (action.getAppCode().equals(appCode)) {
				return action;
			}
    	}
		
    	/*for (DetectedRecordAction action : this.operations) {
    		if (action.getAppCode().equals(appCode)) {
    			return action;
    		}
    	}*/
    	
    	throw new RuntimeException("No service found for [" + appCode + "] application!");
    }
    
	@Override
	protected Class<DetectedRecordAction> getServiceClass() {
		return DetectedRecordAction.class;
	}
}
