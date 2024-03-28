package org.openmrs.module.epts.etl.controller.conf;

import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class SrcConf extends SyncDataConfiguration {
	
	/*
	 * The main source table
	 */
	private SyncTableConfiguration mainSrcTableConf;
	
	private SrcAdditionExtractionInfo additionalExtractionInfo;
	
	private List<EtlExtraDataSource> extraDataSource;
	
	private boolean fullLoaded;
	
	public SrcAdditionExtractionInfo getAdditionalExtractionInfo() {
		return additionalExtractionInfo;
	}
	
	public void setAdditionalExtractionInfo(SrcAdditionExtractionInfo additionalExtractionInfo) {
		this.additionalExtractionInfo = additionalExtractionInfo;
	}
	
	public List<EtlExtraDataSource> getExtraDataSource() {
		return extraDataSource;
	}
	
	public void setExtraDataSource(List<EtlExtraDataSource> extraDataSource) {
		this.extraDataSource = extraDataSource;
	}
	
	public SyncTableConfiguration getMainSrcTableConf() {
		return mainSrcTableConf;
	}
	
	public String getMainTableName() {
		return this.mainSrcTableConf.getTableName();
	}
	
	public void setMainSrcTableConf(SyncTableConfiguration mainSrcTableConfiguration) {
		this.mainSrcTableConf = mainSrcTableConfiguration;
	}
	
	public synchronized void fullLoad() throws DBException {
		
		if (this.fullLoaded) {
			return;
		}
		
		this.mainSrcTableConf.fullLoad();
		
		if (this.additionalExtractionInfo != null) {
			this.additionalExtractionInfo.fullLoad();
		}
		
		OpenConnection srcConn = this.getMainApp().openConnection();
		
		try {
			if (utilities.arrayHasElement(this.getExtraDataSource())) {
				for (EtlExtraDataSource src : this.getExtraDataSource()) {
					src.setRelatedSrcConf(this);
					
					src.fullLoad(srcConn);
				}
			}
		}
		catch (Exception e) {
			srcConn.finalizeConnection();
			
			throw new RuntimeException(e);
		}
		
		this.fullLoaded = true;
	}
	
	public EtlExtraDataSource findAdditionalDataSrc(String tableName) {
		if (!utilities.arrayHasElement(this.extraDataSource)) {
			return null;
		}
		
		for (EtlExtraDataSource src : this.extraDataSource) {
			if (src.getName().equals(tableName)) {
				return src;
			}
		}
		
		throw new ForbiddenOperationException("The table '" + tableName + "'cannot be foud on the mapping src tables");
	}
	
	public boolean isFullLoaded() {
		return this.fullLoaded;
	}
	
	public static SrcConf fastCreate(SyncTableConfiguration tableConfig) {
		SrcConf src = new SrcConf();
		
		src.setMainSrcTableConf(tableConfig);
		src.setRelatedSyncConfiguration(tableConfig.getParent().getRelatedSyncConfiguration());
		
		return src;
	}
	
	@Override
	public void setRelatedSyncConfiguration(SyncConfiguration relatedSyncConfiguration) {
		super.setRelatedSyncConfiguration(relatedSyncConfiguration);
	}
	
}
