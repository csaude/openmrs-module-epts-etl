package org.openmrs.module.epts.etl.engine;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.AuxiliaryExtractionSrcTable;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.AbstractSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public abstract class SyncSearchParams<T extends SyncRecord> extends AbstractSearchParams<T> {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date syncStartDate;
	
	private RecordLimits limits;
	
	private EtlConfiguration config;
	
	private SearchSourceType searchSourceType;
	
	public SyncSearchParams(EtlConfiguration config, RecordLimits limits) {
		this.config = config;
		this.limits = limits;
		this.searchSourceType = SearchSourceType.SOURCE;
	}
	
	public void setSearchSourceType(SearchSourceType searchSourceType) {
		this.searchSourceType = searchSourceType;
	}
	
	public SearchSourceType getSearchSourceType() {
		return searchSourceType;
	}
	
	public Date getSyncStartDate() {
		return syncStartDate;
	}
	
	public void setSyncStartDate(Date syncStartDate) {
		this.syncStartDate = syncStartDate;
	}
	
	public EtlConfiguration getConfig() {
		return config;
	}
	
	public void setConfig(EtlConfiguration config) {
		this.config = config;
	}
	
	public RecordLimits getLimits() {
		return limits;
	}
	
	public void setLimits(RecordLimits limits) {
		this.limits = limits;
	}
	
	protected boolean hasLimits() {
		return this.limits != null;
	}
	
	public void removeLimits() {
		this.limits = null;
	}
	
	/**
	 * @param searchClauses
	 */
	public void tryToAddExtraConditionForExport(SearchClauses<DatabaseObject> searchClauses) {
		if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
			String extraContidion = this.getConfig().getSrcConf().getExtraConditionForExtract();
			
			//@formatter:off
			Object[] params = DBUtilities.loadParamsValues(extraContidion, getConfig().getRelatedSyncConfiguration());
			
			String query = DBUtilities.replaceSqlParametersWithQuestionMarks(extraContidion);
			
			searchClauses.addToClauses(query);
			
			searchClauses.addToParameters(params);		
		}
	}
	
	/**
	 * @param searchClauses
	 * @param tableInfo
	 */
	public void tryToAddLimits(SearchClauses<DatabaseObject> searchClauses) {
		if (this.getLimits() != null) {
			
			if (getSrcTableConf().getPrimaryKey().isSimpleNumericKey()) {
				searchClauses.addToClauses("src_." + getSrcTableConf().getPrimaryKey().retrieveSimpleKeyColumnName() + " between ? and ?");
				searchClauses.addToParameters(this.getLimits().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getLimits().getCurrentLastRecordId());
			}else {
				throw new ForbiddenOperationException("Not supported composite or not numeric key for limit query!");
			}
		}		
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getRecordClass() {
		return (Class<T>) getSrcTableConf().getSyncRecordClass(getSrcTableConf().getMainApp());
	}

	public SrcConf getSrcConf() {
		return this.getConfig().getSrcConf();
	}
	
	/**
	 * @return
	 */
	public AbstractTableConfiguration getSrcTableConf() {
		return this.getConfig().getSrcConf();
	}
	
	/**
	 * @return
	 */
	public List<AuxiliaryExtractionSrcTable> getAuxiliaryExtractionSrcTable() {
		return this.getConfig().getSrcConf().getAuxiliaryExtractionSrcTable();
	}
	/**
	 * @return
	 */
	public AbstractTableConfiguration getDstLastTableConfiguration() {
		return  utilities.getLastRecordOnArray(getConfig().getDstConf());
	}	
	
	public abstract int countAllRecords(Connection conn) throws DBException;
	
	public abstract int countNotProcessedRecords(Connection conn) throws DBException;
}
