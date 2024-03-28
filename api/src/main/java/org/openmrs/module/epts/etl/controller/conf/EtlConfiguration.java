package org.openmrs.module.epts.etl.controller.conf;

import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class EtlConfiguration extends BaseConfiguration {
	
	private String configCode;
	
	private SrcConf srcConf;
	
	private List<DstConf> dstConf;
	
	private SyncConfiguration relatedSyncConfiguration;
	
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
	
	public SyncTableConfiguration getMainSrcTableConf() {
		return this.srcConf.getMainSrcTableConf();
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
					dstConf = utilities.parseToList(DstConf.generateFromSyncTableConfiguration(this.srcConf));
					
					DstConf map = dstConf.get(0);
					
					map.setRelatedAppInfo(otherApps.get(0));
				} else {
					
					for (DstConf map : this.dstConf) {
						map.setRelatedAppInfo(otherApps.get(0));
						
						map.setRelatedSyncConfiguration(getRelatedSyncConfiguration());
						
						map.setSrcConf(this.srcConf);
						
						if (!utilities.arrayHasElement(map.getFieldsMapping())) {
							map.generateMappingFields(this.srcConf.getMainSrcTableConf());
						}
						
						map.loadAdditionalFieldsInfo();
						
						if (DBUtilities.isTableExists(dstConn.getSchema(), map.getDstTableConf().getTableName(), dstConn)) {
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
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return relatedSyncConfiguration;
	}
	
	public void setRelatedSyncConfiguration(SyncConfiguration relatedSyncConfiguration) {
		this.relatedSyncConfiguration = relatedSyncConfiguration;
		
		if (this.srcConf != null) {
			this.srcConf.setRelatedSyncConfiguration(relatedSyncConfiguration);
		}
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public String getConfigCode() {
		return utilities.stringHasValue(configCode) ? configCode : this.srcConf.getMainSrcTableConf().getTableName();
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
	
	public SrcAdditionExtractionInfo getAdditionalExtractionInfo() {
		return this.srcConf.getAdditionalExtractionInfo();
	}
}
