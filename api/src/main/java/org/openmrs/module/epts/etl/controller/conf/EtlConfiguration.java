package org.openmrs.module.epts.etl.controller.conf;

import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class EtlConfiguration extends SyncDataConfiguration {
	
	private String configCode;
	
	private SrcConf srcConf;
	
	private List<DstConf> dstConf;
	
	private boolean disabled;
	
	private boolean fullLoaded;
	
	public EtlConfiguration() {
	}
	
	public SrcConf getSrcConf() {
		return srcConf;
	}
	
	public void setSrcConf(SrcConf srcConf) {
		this.srcConf = srcConf;
	}
	
	public List<DstConf> getDstConf() {
		return dstConf;
	}
	
	public void setDstConf(List<DstConf> dstConf) {
		this.dstConf = dstConf;
	}
	
	public static EtlConfiguration fastCreate(SyncTableConfiguration tableConfig) {
		EtlConfiguration etl = new EtlConfiguration();
		
		SrcConf src = SrcConf.fastCreate(tableConfig);
		
		etl.setSrcConf(src);
		
		return etl;
	}
	
	public static EtlConfiguration fastCreate(String configCode) {
		EtlConfiguration etl = new EtlConfiguration();
		
		etl.setConfigCode(configCode);
		
		return etl;
	}
	
	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	public void setFullLoaded(boolean fullLoaded) {
		this.fullLoaded = fullLoaded;
	}
	
	public void clone(EtlConfiguration toCloneFrom) {
		this.srcConf = toCloneFrom.srcConf;
		this.disabled = toCloneFrom.disabled;
		this.dstConf = toCloneFrom.dstConf;
	}
	
	public synchronized void fullLoad() throws DBException {
		
		if (this.fullLoaded) {
			return;
		}
		
		this.srcConf.fullLoad();
		
		OpenConnection dstConn = null;
		
		try {
			List<AppInfo> otherApps = getRelatedSyncConfiguration().exposeAllAppsNotMain();
			
			if (utilities.arrayHasElement(otherApps)) {
				dstConn = otherApps.get(0).openConnection();
				
				if (dstConf == null) {
					dstConf = utilities.parseToList(DstConf.generateDefaultDstConf(this));
					
					DstConf map = dstConf.get(0);
					
					map.setRelatedAppInfo(otherApps.get(0));
				} else {
					
					for (DstConf map : this.dstConf) {
						map.setRelatedAppInfo(otherApps.get(0));
						
						map.setRelatedSyncConfiguration(getRelatedSyncConfiguration());
						
						map.setParent(this);
						
						map.generateAllFieldsMapping();
						
						if (DBUtilities.isTableExists(dstConn.getSchema(), map.getTableName(), dstConn)) {
							map.fullLoad(dstConn);
						}
					}
				}
			}
			
			this.fullLoaded = true;
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
		}
	}
	
	@Override
	public void setRelatedSyncConfiguration(SyncConfiguration relatedSyncConfiguration) {
		super.setRelatedSyncConfiguration(relatedSyncConfiguration);
		
		if (this.srcConf != null) {
			this.srcConf.setRelatedSyncConfiguration(relatedSyncConfiguration);
		}
		
		if (this.dstConf != null) {
			
			for (DstConf conf : this.dstConf) {
				conf.setRelatedSyncConfiguration(relatedSyncConfiguration);
			}
		}
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public String getConfigCode() {
		return utilities.stringHasValue(configCode) ? configCode : this.srcConf.getTableName();
	}
	
	public void setConfigCode(String configCode) {
		this.configCode = configCode;
	}
	
	public String getOriginAppLocationCode() {
		return getRelatedSyncConfiguration().getOriginAppLocationCode();
	}
	
	public AppInfo getMainApp() {
		return getRelatedSyncConfiguration().getMainApp();
	}
}
