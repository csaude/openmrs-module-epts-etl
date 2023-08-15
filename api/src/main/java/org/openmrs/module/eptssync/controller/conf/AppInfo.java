package org.openmrs.module.eptssync.controller.conf;

import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class AppInfo {
	public static final String MAIN_APP_CODE = "main";
	public static final String PERFORMING_MODE_SINGLE="single";
	public static final String PERFORMING_MODE_BATCH="batch";
	
	private String processingMode;
	private String applicationCode;
	private DBConnectionInfo connInfo;
	private String pojoPackageName;
	
	private DBConnectionService dbService;
	
	
	public AppInfo(){
	}
	
	private DBConnectionService getRelatedDBConnectionService() {
		if (this.dbService == null) initRelatedDBConnectionService();
		
		return this.dbService;
	}
	
	public OpenConnection openConnection() {
		return getRelatedDBConnectionService().openConnection();
	}
	
	private synchronized void initRelatedDBConnectionService() {
		if (dbService == null) {
			dbService = DBConnectionService.init(this.connInfo);
		}
	}
	
	public String getPojoPackageName() {
		return pojoPackageName;
	}
	
	public void setPojoPackageName(String pojoPackageName) {
		this.pojoPackageName = pojoPackageName;
	}
	
	public String getProcessingMode() {
		return processingMode;
	}
	
	public void setProcessingMode(String processingMode) {
		this.processingMode = processingMode;
	}

	public AppInfo(String applicationCode) {
		this.applicationCode = applicationCode;
	}

	public String getApplicationCode() {
		return applicationCode;
	}

	public void setApplicationCode(String applicationCode) {
		this.applicationCode = applicationCode;
	}

	public DBConnectionInfo getConnInfo() {
		return connInfo;
	}

	public void setConnInfo(DBConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}

	public boolean isSinglePerformingMode(){
		return this.processingMode.equals(AppInfo.PERFORMING_MODE_SINGLE);
	}
	

	public boolean isBatchPerformingMode(){
		return this.processingMode.equals(AppInfo.PERFORMING_MODE_BATCH);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof AppInfo)) return false;
		
		AppInfo connInfo = (AppInfo)obj;
		
		return this.applicationCode.equalsIgnoreCase(connInfo.applicationCode);
	}
	
	
	public static AppInfo findOnArray(List<AppInfo> list, AppInfo toFind) {
		return CommonUtilities.getInstance().findOnList(list, toFind);
	}
	
	public static AppInfo init(String appCode) {
		return new AppInfo(appCode);
	}
	
	@Override
	public String toString() {
		return this.applicationCode;
	}
}
