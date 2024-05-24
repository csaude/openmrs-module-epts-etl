package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.etl.model.EtlSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class EtlItemConfiguration extends AbstractEtlDataConfiguration {
	
	private String configCode;
	
	private SrcConf srcConf;
	
	private List<DstConf> dstConf;
	
	private boolean disabled;
	
	private boolean fullLoaded;
	
	public EtlItemConfiguration() {
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
	
	public static EtlItemConfiguration fastCreate(AbstractTableConfiguration tableConfig, Connection conn)
	        throws DBException {
		EtlItemConfiguration etl = new EtlItemConfiguration();
		
		SrcConf src = SrcConf.fastCreate(tableConfig, conn);
		
		etl.setSrcConf(src);
		
		return etl;
	}
	
	public static EtlItemConfiguration fastCreate(String configCode) {
		EtlItemConfiguration etl = new EtlItemConfiguration();
		
		etl.setConfigCode(configCode);
		
		return etl;
	}
	
	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	public void setFullLoaded(boolean fullLoaded) {
		this.fullLoaded = fullLoaded;
	}
	
	public void clone(EtlItemConfiguration toCloneFrom) {
		this.srcConf = toCloneFrom.srcConf;
		this.disabled = toCloneFrom.disabled;
		this.dstConf = toCloneFrom.dstConf;
	}
	
	public synchronized void fullLoad() throws DBException {
		if (this.isFullLoaded()) {
			return;
		}
		
		this.srcConf.fullLoad();
		
		OpenConnection dstConn = null;
		
		try {
			List<AppInfo> otherApps = getRelatedSyncConfiguration().exposeAllAppsNotMain();
			
			if (utilities.arrayHasElement(otherApps)) {
				
				if (utilities.arrayHasMoreThanOneElements(otherApps)) {
					throw new ForbiddenOperationException("Not supported more that one destination apps");
				}
				
				dstConn = otherApps.get(0).openConnection();
				
				if (dstConf == null) {
					dstConf = utilities.parseToList(new DstConf());
				}
				
				for (DstConf map : this.dstConf) {
					if (map.getTableName() == null) {
						map.setTableName(this.srcConf.getTableName());
					}
					
					map.setRelatedAppInfo(otherApps.get(0));
					
					map.setRelatedSyncConfiguration(getRelatedSyncConfiguration());
					
					map.setParentConf(this);
					
					if (DBUtilities.isTableExists(dstConn.getSchema(), map.getTableName(), dstConn)) {
						map.fullLoad(dstConn);
					}
					
					map.generateAllFieldsMapping(dstConn);
				}
			}
			
			this.setFullLoaded(true);
		}
		catch (
		
		SQLException e) {
			throw new DBException(e);
		}
		finally {
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
		}
	}
	
	@Override
	public void setRelatedSyncConfiguration(EtlConfiguration relatedSyncConfiguration) {
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
	
	public boolean hasDstWithJoinFieldsToSrc() {
		for (DstConf dst : this.dstConf) {
			if (dst.hasJoinFields()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return this.configCode;
	}
	
	public EtlDatabaseObject retrieveRecordInSrc(EtlDatabaseObject parentRecordInOrigin, Connection srcConn)
	        throws DBException {
		
		if (getSrcConf().getTableAlias().equals("person_99")) {
			System.out.println("Stop");
		}
		
		EtlSearchParams searchParams = new EtlSearchParams(this, null, null);
		
		searchParams.setExtraCondition(this.getSrcConf().getPrimaryKey().parseToParametrizedStringConditionWithAlias());
		
		searchParams.setSyncStartDate(getRelatedSyncConfiguration().getStartDate());
		
		SearchClauses<EtlDatabaseObject> searchClauses = searchParams.generateSearchClauses(srcConn);
		
		searchClauses.addToParameters(parentRecordInOrigin.getObjectId().parseValuesToArray());
		
		String sql = searchClauses.generateSQL(srcConn);
		
		EtlDatabaseObject simpleValue = DatabaseObjectDAO.find(getSrcConf().getLoadHealper(),
		    getSrcConf().getSyncRecordClass(getMainApp()), sql, searchClauses.getParameters(), srcConn);
		
		return simpleValue;
	}
	
	public boolean containsDstTable(String tableName) {
		if (utilities.arrayHasElement(getDstConf())) {
			for (DstConf dst : getDstConf()) {
				if (dst.getTableName().equals(tableName)) {
					return true;
				}
			}
		} else {
			if (getSrcConf().getTableName().equals(tableName)) {
				return true;
			}
		}
		
		return false;
	}
}
