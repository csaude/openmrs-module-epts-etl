package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.QueryDataSourceConfig;
import org.openmrs.module.epts.etl.controller.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncDataConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncDataSource;
import org.openmrs.module.epts.etl.controller.conf.TableDataSourceConfig;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class represent a data from any related table
 */
public class EtlExtraDataSource extends SyncDataConfiguration {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private QueryDataSourceConfig querySrc;
	
	private TableDataSourceConfig tableSrc;
	
	private SrcConf relatedSrcConf;
	
	public EtlExtraDataSource() {
	}
	
	public QueryDataSourceConfig getQuerySrc() {
		return querySrc;
	}
	
	public void setQuerySrc(QueryDataSourceConfig querySrc) {
		this.querySrc = querySrc;
	}
	
	public TableDataSourceConfig getTableSrc() {
		return tableSrc;
	}
	
	public void setTableSrc(TableDataSourceConfig tableSrc) {
		this.tableSrc = tableSrc;
	}
	
	public SrcConf getRelatedSrcConf() {
		return relatedSrcConf;
	}
	
	public void setRelatedSrcConf(SrcConf relatedSrcTableConf) {
		this.relatedSrcConf = relatedSrcTableConf;
		this.setRelatedSyncConfiguration(relatedSrcTableConf.getRelatedSyncConfiguration());
		
		if (this.querySrc != null) {
			this.querySrc.setRelatedSrcExtraDataSrc(this);
		}
		
		if (this.tableSrc != null) {
			this.tableSrc.setRelatedSrcExtraDataSrc(this);
		}
	}
	
	public DatabaseObject loadRelatedSrcObject(DatabaseObject mainObject, Connection srcConn, AppInfo srcAppInfo)
	        throws DBException {
		
		return getAvaliableSrc().loadRelatedSrcObject(mainObject, srcConn, srcAppInfo);
	}
	
	public SyncDataSource getAvaliableSrc() {
		if (tableSrc != null && querySrc != null) {
			throw new ForbiddenOperationException("You cannot cofigure both 'tableConfig' and 'queryConfig'");
		}
		
		if (tableSrc != null) {
			return this.tableSrc;
		}
		
		if (querySrc != null) {
			return this.querySrc;
		}
		
		throw new ForbiddenOperationException("You need to cofigure 'tableConfig' or 'queryConfig'");
		
	}
	
	public String getName() {
		return getAvaliableSrc().getName();
	}
	
	public Class<DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		return getAvaliableSrc().getSyncRecordClass(application);
	}
	
	public void fullLoad(Connection conn) throws DBException {
		if (!getAvaliableSrc().isFullLoaded()) {
			getAvaliableSrc().fullLoad(conn);
		}
	}
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return this.relatedSrcConf.getRelatedSyncConfiguration();
	}
	
}
