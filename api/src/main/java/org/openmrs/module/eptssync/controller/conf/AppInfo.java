package org.openmrs.module.eptssync.controller.conf;

import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;

public class AppInfo {
	public static final String MAIN_APP_CODE = "main";
	
	private String applicationCode;
	private DBConnectionInfo connInfo;
	
	public AppInfo(){
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
}
