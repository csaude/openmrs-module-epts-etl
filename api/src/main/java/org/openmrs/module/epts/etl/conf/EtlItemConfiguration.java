package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
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
	
	public boolean hasDstConf() {
		return utilities.arrayHasElement(this.getDstConf());
	}
	
	public void tryToCreateDefaultRecordsForAllTables() throws DBException {
		OpenConnection dstConn = getRelatedEtlConf().tryOpenDstConn();
		
		if (dstConn == null)
			return;
		
		try {
			if (!this.hasDstConf()) {
				this.generateDefaultDstConf();
			}
			
			if (this.hasDstConf()) {
				
				for (DstConf dst : this.getDstConf()) {
					if (!dst.isParentsLoaded()) {
						dst.loadParents(dstConn);
					}
					
					if (!dst.hasParentRefInfo()) {
						continue;
					}
					
					for (ParentTable refInfo : dst.getParentRefInfo()) {
						if (refInfo.isMetadata())
							continue;
						
						if (!refInfo.isFullLoaded()) {
							refInfo.fullLoad(dstConn);
						}
						
						if (refInfo.useSharedPKKey()) {
							if (!refInfo.getSharedKeyRefInfo().isFullLoaded()) {
								refInfo.getSharedKeyRefInfo().fullLoad(dstConn);
							}
						}
						
						if (refInfo.getDefaultObject(dstConn) == null) {
							getRelatedEtlConf()
							        .logDebug("Creating default record for table " + refInfo.getFullTableDescription());
							
							try {
								refInfo.generateAndSaveDefaultObject(dstConn);
							}
							catch (DBException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				
				dstConn.markAsSuccessifullyTerminated();
			}
		}
		finally {
			dstConn.finalizeConnection();
		}
		
	}
	
	public synchronized void fullLoad() throws DBException {
		if (this.isFullLoaded()) {
			return;
		}
		
		this.srcConf.fullLoad();
		
		OpenConnection dstConn = null;
		
		try {
			
			if (getRelatedEtlConf().hasDstConnInfo()) {
				
				dstConn = getRelatedEtlConf().openDstConn();
				
				if (!this.hasDstConf()) {
					this.generateDefaultDstConf();
				}
				
				for (DstConf map : this.getDstConf()) {
					map.setRelatedConnInfo(getRelatedEtlConf().getDstConnInfo());
					
					map.setRelatedSyncConfiguration(getRelatedEtlConf());
					
					map.setParentConf(this);
					
					if (!map.isAutomaticalyGenerated()) {
						map.loadSchemaInfo(dstConn);
					}
					
					if (DBUtilities.isTableExists(map.getSchema(), map.getTableName(), dstConn)) {
						map.loadFields(dstConn);
						
						if (map.isAutomaticalyGenerated() && getSrcConf().hasParents()) {
							map.setParents(getSrcConf().tryToCloneAllParentsForOtherTable(map, dstConn));
						}
						
						map.fullLoad(dstConn);
					}
					
					map.generateAllFieldsMapping(dstConn);
				}
			}
			
			this.setFullLoaded(true);
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
		return getRelatedEtlConf().getOriginAppLocationCode();
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return getRelatedEtlConf().getSrcConnInfo();
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
		
		if (!this.getSrcConf().isComplex()) {
			return DatabaseObjectDAO.getByOid(srcConf, parentRecordInOrigin.getObjectId(), srcConn);
		} else {
			
			Engine<EtlDatabaseObject> engine = new Engine<>(null, this, null);
			
			EtlDatabaseObjectSearchParams searchParams = new EtlDatabaseObjectSearchParams(engine, null);
			
			searchParams.setExtraCondition(this.getSrcConf().getPrimaryKey().parseToParametrizedStringConditionWithAlias());
			
			searchParams.setSyncStartDate(getRelatedEtlConf().getStartDate());
			
			SearchClauses<EtlDatabaseObject> searchClauses = searchParams.generateSearchClauses(null, srcConn, null);
			
			searchClauses.addToParameters(parentRecordInOrigin.getObjectId().parseValuesToArray());
			
			String sql = searchClauses.generateSQL(srcConn);
			
			EtlDatabaseObject simpleValue = DatabaseObjectDAO.find(getSrcConf().getLoadHealper(),
			    getSrcConf().getSyncRecordClass(getSrcConnInfo()), sql, searchClauses.getParameters(), srcConn);
			
			return simpleValue;
		}
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
	
	public DstConf findDstTable(String tableName) throws DBException {
		
		if (!containsDstTable(tableName))
			return null;
		
		if (!isFullLoaded()) {
			fullLoad();
		}
		
		for (DstConf dst : getDstConf()) {
			if (dst.getTableName().equals(tableName)) {
				return dst;
			}
		}
		return null;
	}
	
	public void generateDefaultDstConf() throws DBException {
		OpenConnection dstConn;
		
		if (!hasDstConf()) {
			
			dstConn = getRelatedEtlConf().openDstConn();
			try {
				DstConf map = new DstConf();
				
				map.setTableName(this.getSrcConf().getTableName());
				map.setObservationDateFields(getSrcConf().getObservationDateFields());
				map.setRemoveForbidden(this.getSrcConf().isRemoveForbidden());
				map.setSchema(dstConn.getSchema());
				map.setAutomaticalyGenerated(true);
				map.setOnConflict(getSrcConf().onConflict());
				map.setRelatedConnInfo(getRelatedEtlConf().getDstConnInfo());
				
				map.setRelatedSyncConfiguration(getRelatedEtlConf());
				
				map.setParentConf(this);
				map.setDstType(getSrcConf().getDstType());
				
				if (!map.isAutomaticalyGenerated()) {
					map.loadSchemaInfo(dstConn);
				}
				
				this.setDstConf(utilities.parseToList(map));
			}
			catch (SQLException e) {
				throw new DBException(e);
			}
		}
	}
}
