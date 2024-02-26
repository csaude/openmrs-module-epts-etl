package org.openmrs.module.epts.etl.controller.conf;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class EtlConfiguration extends BaseConfiguration {
	
	private SyncTableConfiguration srcTableConfiguration;
	
	private String extraConditionForExport;
	
	private boolean disabled;
	
	private String configCode;
	
	private List<SyncDestinationTableConfiguration> dstTableConfiguration;
	
	private SyncConfiguration relatedSyncConfiguration;
	
	private boolean fullLoaded;
	
	public EtlConfiguration() {
	}
	
	public static EtlConfiguration fastCreate(SyncTableConfiguration tableConfig) {
		EtlConfiguration etl = new EtlConfiguration();
		
		etl.setTableConfiguration(tableConfig);
		
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
		this.srcTableConfiguration = toCloneFrom.srcTableConfiguration;
		
		this.extraConditionForExport = toCloneFrom.extraConditionForExport;
		this.disabled = toCloneFrom.disabled;
		this.dstTableConfiguration = toCloneFrom.dstTableConfiguration;
	}
	
	public synchronized void fullLoad() throws DBException {
		
		if (this.fullLoaded) {
			return;
		}
		
		this.srcTableConfiguration.fullLoad();
		
		OpenConnection dstConn = null;
		
		try {
			List<AppInfo> otherApps = getRelatedSyncConfiguration().exposeAllAppsNotMain();
			
			if (utilities.arrayHasElement(otherApps)) {
				dstConn = otherApps.get(0).openConnection();
				
				if (dstTableConfiguration == null) {
					dstTableConfiguration = SyncDestinationTableConfiguration
					        .generateFromSyncTableConfiguration(this.srcTableConfiguration);
					
					SyncDestinationTableConfiguration map = dstTableConfiguration.get(0);
					
					map.setRelatedAppInfo(otherApps.get(0));
					
					if (DBUtilities.isTableExists(dstConn.getSchema(), map.getTableName(), dstConn)) {
						map.fullLoad(dstConn);
					}
					
				} else {
					
					for (SyncDestinationTableConfiguration map : this.dstTableConfiguration) {
						map.setRelatedAppInfo(otherApps.get(0));
						
						map.setRelatedSyncConfiguration(getRelatedSyncConfiguration());
						map.setSourceTableConfiguration(this.srcTableConfiguration);
						
						if (!utilities.arrayHasElement(map.getFieldsMapping())) {
							map.generateMappingFields(this.srcTableConfiguration);
						}
						
						map.loadAdditionalFieldsInfo();
						
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
	
	public SyncTableConfiguration getSrcTableConfiguration() {
		return srcTableConfiguration;
	}
	
	public void setTableConfiguration(SyncTableConfiguration tableConfiguration) {
		this.srcTableConfiguration = tableConfiguration;
	}
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return relatedSyncConfiguration;
	}
	
	public void setRelatedSyncConfiguration(SyncConfiguration relatedSyncConfiguration) {
		this.relatedSyncConfiguration = relatedSyncConfiguration;
		
		if (this.srcTableConfiguration != null) {
			this.srcTableConfiguration.setRelatedSyncConfiguration(relatedSyncConfiguration);
		}
	}
	
	public String getExtraConditionForExport() {
		return extraConditionForExport;
	}
	
	public void setExtraConditionForExport(String extraConditionForExport) {
		this.extraConditionForExport = extraConditionForExport;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public String getConfigCode() {
		return configCode;
	}
	
	public void setConfigCode(String configCode) {
		this.configCode = configCode;
	}
	
	public List<SyncDestinationTableConfiguration> getDstTableConfiguration() {
		return dstTableConfiguration;
	}
	
	public void setDstTableConfiguration(List<SyncDestinationTableConfiguration> dstTableConfiguration) {
		this.dstTableConfiguration = dstTableConfiguration;
	}
	
	public String getOriginAppLocationCode() {
		return getRelatedSyncConfiguration().getOriginAppLocationCode();
	}
	
	public AppInfo getMainApp() {
		return getRelatedSyncConfiguration().getMainApp();
	}
	
	public List<String> parseDstTableToString() {
		List<String> dst = new ArrayList<>();
		
		for (SyncDestinationTableConfiguration d : this.getDstTableConfiguration()) {
			dst.add(d.getTableName());
		}
		
		return dst;
	}
}
