package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.EtlItemSrcConf;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.AutoIncrementHandlingType;
import org.openmrs.module.epts.etl.conf.types.ThreadingMode;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.EtlDynamicItemSearchParams;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class EtlItemConfiguration extends AbstractEtlDataConfiguration {
	
	private String configCode;
	
	private EtlItemSrcConf etlItemSrcConf;
	
	private SrcConf srcConf;
	
	private List<DstConf> dstConf;
	
	private boolean disabled;
	
	private boolean fullLoaded;
	
	/**
	 * If present, the value from this field will be mapped as a primary key for all tables under
	 * this configuration that don't have a primary key but have a field with name matching this
	 * field. <br>
	 * This value will be overridden by the correspondent value on {@link TableConfiguration} if
	 * present there
	 */
	private String manualMapPrimaryKeyOnField;
	
	private boolean createDstTableIfNotExists;
	
	private boolean testing;
	
	private EtlDatabaseObject relatedEtlSchemaObject;
	
	private ThreadingMode threadingMode;
	
	private AutoIncrementHandlingType autoIncrementHandlingType;
	
	/**
	 * A numeric value added to the primary key of the very first destination record for all tables
	 * defined in the ETL Item Configuration. This property cannot be used when
	 * autoIncrementHandlingType is explicitly set to AS_SCHEMA_DEFINED. If this property is
	 * provided and autoIncrementHandlingType is not specified, it will automatically be set to
	 * IGNORE_SCHEMA_DEFINITION.
	 */
	private Integer primaryKeyInitialIncrementValue;
	
	private List<EtlItemConfiguration> childItemConf;
	
	private EtlItemConfiguration parentItemConf;
	
	private DstConf relatedParentDstConf;
	
	private String relatedParentDstConfName;
	
	private boolean doNotFullLoadDstConf;
	
	private String shortCode;
	
	public EtlItemConfiguration() {
	}
	
	public String getShortCode() {
		return shortCode;
	}
	
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}
	
	public boolean isDoNotFullLoadDstConf() {
		return doNotFullLoadDstConf;
	}
	
	public void setDoNotFullLoadDstConf(boolean doNotFullLoadDstConf) {
		this.doNotFullLoadDstConf = doNotFullLoadDstConf;
	}
	
	public String getRelatedParentDstConfName() {
		return relatedParentDstConfName;
	}
	
	public void setRelatedParentDstConfName(String relatedParentDstConfName) {
		this.relatedParentDstConfName = relatedParentDstConfName;
	}
	
	public EtlItemConfiguration getParentItemConf() {
		return parentItemConf;
	}
	
	public void setParentItemConf(EtlItemConfiguration parentItemConf) {
		this.parentItemConf = parentItemConf;
	}
	
	public DstConf getRelatedParentDstConf() {
		return relatedParentDstConf;
	}
	
	public void setRelatedParentDstConf(DstConf relatedParentDstConf) {
		this.relatedParentDstConf = relatedParentDstConf;
	}
	
	public List<EtlItemConfiguration> getChildItemConf() {
		return childItemConf;
	}
	
	public void setChildItemConf(List<EtlItemConfiguration> childItemConf) {
		this.childItemConf = childItemConf;
	}
	
	public Integer getPrimaryKeyInitialIncrementValue() {
		return primaryKeyInitialIncrementValue;
	}
	
	public void setPrimaryKeyInitialIncrementValue(Integer primaryKeyInitialIncrementValue) {
		this.primaryKeyInitialIncrementValue = primaryKeyInitialIncrementValue;
	}
	
	public AutoIncrementHandlingType getAutoIncrementHandlingType() {
		return autoIncrementHandlingType;
	}
	
	public void setAutoIncrementHandlingType(AutoIncrementHandlingType autoIncrementHandlingType) {
		this.autoIncrementHandlingType = autoIncrementHandlingType;
	}
	
	public ThreadingMode getThreadingMode() {
		return threadingMode;
	}
	
	public void setThreadingMode(ThreadingMode threadingMode) {
		this.threadingMode = threadingMode;
	}
	
	public boolean hasThreadingMode() {
		return this.getThreadingMode() != null;
	}
	
	public EtlDatabaseObject getRelatedEtlSchemaObject() {
		return relatedEtlSchemaObject;
	}
	
	public void setRelatedEtlSchemaObject(EtlDatabaseObject relatedEtlSchemaObject) {
		this.relatedEtlSchemaObject = relatedEtlSchemaObject;
	}
	
	public EtlItemSrcConf getEtlItemSrcConf() {
		return etlItemSrcConf;
	}
	
	public boolean isTesting() {
		return testing;
	}
	
	public void setTesting(boolean testing) {
		this.testing = testing;
	}
	
	public void setEtlItemSrcConf(EtlItemSrcConf srcOfSrc) {
		this.etlItemSrcConf = srcOfSrc;
	}
	
	public void setCreateDstTableIfNotExists(boolean createDstTableIfNotExists) {
		this.createDstTableIfNotExists = createDstTableIfNotExists;
	}
	
	public boolean isCreateDstTableIfNotExists() {
		return createDstTableIfNotExists;
	}
	
	public boolean createDstTableIfNotExists() {
		return this.isCreateDstTableIfNotExists();
	}
	
	public String getManualMapPrimaryKeyOnField() {
		return manualMapPrimaryKeyOnField;
	}
	
	public void setManualMapPrimaryKeyOnField(String manualMapPrimaryKeyOnField) {
		this.manualMapPrimaryKeyOnField = manualMapPrimaryKeyOnField;
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
		
		SrcConf src = SrcConf.fastCreate(tableConfig, etl, conn);
		
		etl.setSrcConf(src);
		
		etl.setRelatedEtlConfig(tableConfig.getRelatedEtlConf());
		
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
	
	public boolean hasDstConf() {
		return utilities.listHasElement(this.getDstConf());
	}
	
	public void tryToCreateDefaultRecordsForAllTables() throws DBException {
		OpenConnection dstConn = getRelatedEtlConf().tryOpenDstConn();
		
		if (dstConn == null)
			return;
		
		try {
			if (!this.hasDstConf()) {
				this.setDstConf(DstConf.generateDefaultDstConf(this));
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
							if (!refInfo.getSharedKeyRefInfo(dstConn).isFullLoaded()) {
								refInfo.getSharedKeyRefInfo(dstConn).fullLoad(dstConn);
							}
						}
						
						if (refInfo.getDefaultObject(dstConn) == null) {
							getRelatedEtlConf()
							        .logDebug("Creating default dstRecord for table " + refInfo.getFullTableDescription());
							
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
	
	public boolean hasManualMapPrimaryKeyOnField() {
		return getManualMapPrimaryKeyOnField() != null;
	}
	
	public synchronized void fullLoad(EtlOperationConfig operationConfig) throws DBException {
		if (this.isFullLoaded()) {
			return;
		}
		
		if (!hasManualMapPrimaryKeyOnField()) {
			setManualMapPrimaryKeyOnField(getRelatedEtlConf().getManualMapPrimaryKeyOnField());
		}
		
		if (!this.srcConf.hasDstType()) {
			this.srcConf.setDstType(operationConfig.getDstType());
		}
		
		this.srcConf.fullLoad();
		
		OpenConnection dstConn = null;
		OpenConnection srcConn = null;
		
		try {
			
			getSrcConf().setParentConf(this);
			
			if (this.getRelatedEtlConf().hasDstConnInfo()) {
				
				dstConn = this.getRelatedEtlConf().openDstConn();
				
				if (!this.hasDstConf()) {
					this.setDstConf(DstConf.generateDefaultDstConf(this));
				}
				
				for (DstConf map : this.getDstConf()) {
					map.setRelatedConnInfo(getRelatedEtlConf().getDstConnInfo());
					
					map.setRelatedEtlConfig(getRelatedEtlConf());
					
					map.setParentConf(this);
					map.setDstType(this.getSrcConf().getDstType());
					
					try {
						map.loadSchemaInfo(null, dstConn);
					}
					catch (DatabaseResourceDoesNotExists e) {
						if (map.getDstType().isDb() && !this.createDstTableIfNotExists()) {
							throw e;
						}
						
						map.setInMemoryTable(true);
					}
					
					if (map.isInMemoryTable()) {
						map.fullLoad(dstConn);
						
						if (map.getDstType().isDb() && this.createDstTableIfNotExists()) {
							map.createTable(dstConn);
						}
					} else {
						map.loadFields(dstConn);
						
						if (map.isAutomaticalyGenerated() && getSrcConf().hasParents()) {
							map.setParents(getSrcConf().tryToCloneAllParentsForOtherTable(map, dstConn));
						}
						
						if (!isDoNotFullLoadDstConf()) {
							map.fullLoad(dstConn);
							
							map.generateAllFieldsMapping(dstConn);
						}
						
					}
					
				}
			} else {
				if (hasDstConf()) {
					//Force the dstConf to be inMemory
					
					for (DstConf dstConf : this.getDstConf()) {
						dstConf.setInMemoryTable(true);
						dstConf.setDstType(this.getSrcConf().getDstType());
						
						dstConf.fullLoad(srcConn);
					}
					
				}
			}
			
			if (operationConfig.writeOperationHistory()
			        || operationConfig.getRelatedEtlConfig().getGeneralBehaviourOnEtlException().log()) {
				srcConn = this.getRelatedEtlConf().openSrcConn();
				
				this.getSrcConf().generateStagingTables(srcConn);
				
				srcConn.markAsSuccessifullyTerminated();
			}
			
			if (this.hasParentItemConf() && this.getRelatedParentDstConf() == null) {
				
				if (!utilities.stringHasValue(this.getRelatedParentDstConfName())) {
					if (utilities.arrayHasExactlyOneElement(this.getParentItemConf().getDstConf())) {
						this.setRelatedParentDstConfName(this.getParentItemConf().getDstConf().get(0).getName());
					} else {
						throw new ForbiddenOperationException("The relatedParentDstConfName was not defined for the conf "
						        + this.getParentItemConf().getConfigCode());
					}
				}
				
				this.setRelatedParentDstConf(this.getParentItemConf().findDstConf(this.getRelatedParentDstConfName()));
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
			
			if (srcConn != null) {
				srcConn.finalizeConnection();
			}
		}
	}
	
	public DstConf findDstConf(String dstConfName) {
		if (this.hasDstConf()) {
			for (DstConf conf : this.getDstConf()) {
				if (conf.getTableAlias() != null && conf.getTableAlias().equals(dstConfName)) {
					return conf;
				}
			}
			
			DstConf found = null;
			
			for (DstConf conf : this.getDstConf()) {
				
				if (conf.getTableName().equals(dstConfName)) {
					if (found != null) {
						throw new ForbiddenOperationException("Multiple dstParent found with name '" + dstConfName
						        + "'. Please use tableAlias to refere one of them.");
					}
					
					found = conf;
				}
			}
			
			return found;
		}
		
		return null;
	}
	
	public boolean hasChildItemConf() {
		return utilities.listHasElement(this.childItemConf);
	}
	
	public boolean hasParentItemConf() {
		return this.parentItemConf != null;
	}
	
	@Override
	public void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration) {
		super.setRelatedEtlConfig(relatedSyncConfiguration);
		
		if (this.srcConf != null) {
			this.srcConf.setRelatedEtlConfig(relatedSyncConfiguration);
		}
		
		if (this.dstConf != null) {
			
			for (DstConf conf : this.dstConf) {
				conf.setRelatedEtlConfig(relatedSyncConfiguration);
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
		if (utilities.listHasElement(getDstConf())) {
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
	
	public DstConf findDstTable(EtlOperationConfig operationConfig, String tableName) throws DBException {
		
		if (!containsDstTable(tableName))
			return null;
		
		if (!isFullLoaded()) {
			fullLoad(operationConfig);
		}
		
		for (DstConf dst : getDstConf()) {
			if (dst.getTableName().equals(tableName)) {
				return dst;
			}
		}
		return null;
	}
	
	public boolean isDynamic() {
		return this.getEtlItemSrcConf() != null;
	}
	
	@Override
	public EtlDataConfiguration getParentConf() {
		return this.getRelatedEtlConf();
	}
	
	public List<EtlItemConfiguration> generateDynamicItems(EtlConfiguration relatedEtlConf, Connection conn)
	        throws DBException {
		if (!this.isDynamic()) {
			throw new ForbiddenOperationException(
			        "This item [" + this.getConfigCode() + " Is not dynamic!!! You cannot generate Dynamic Items");
		}
		
		this.getEtlItemSrcConf().setRelatedItemConf(this);
		this.getEtlItemSrcConf().setRelatedEtlConfig(this.getRelatedEtlConf());
		
		this.getEtlItemSrcConf().fullLoad(conn);
		
		EtlDynamicItemSearchParams searchParams = new EtlDynamicItemSearchParams(this.getEtlItemSrcConf());
		
		List<EtlDatabaseObject> itemsSrc = searchParams.search(null, conn, conn);
		
		List<EtlItemConfiguration> items = new ArrayList<>(itemsSrc.size());
		
		for (EtlDatabaseObject itemSrc : itemsSrc) {
			items.add(cloneDynamic(itemSrc, relatedEtlConf, conn));
		}
		
		return items;
	}
	
	private EtlItemConfiguration cloneDynamic(EtlDatabaseObject schemaInfoSrc, EtlConfiguration relatedEtlConf,
	        Connection conn) throws DBException {
		
		EtlItemConfiguration item = new EtlItemConfiguration();
		item.setRelatedEtlConfig(relatedEtlConf);
		
		item.setSrcConf(new SrcConf());
		item.getSrcConf().copyFromOther(this.getSrcConf(), schemaInfoSrc, item, conn);
		
		if (this.hasDstConf()) {
			item.setDstConf(DstConf.cloneAll(this.getDstConf(), this, schemaInfoSrc, conn));
		}
		item.setThreadingMode(this.getThreadingMode());
		item.setDisabled(this.isDisabled());
		item.setManualMapPrimaryKeyOnField(this.getManualMapPrimaryKeyOnField());
		item.setCreateDstTableIfNotExists(this.isCreateDstTableIfNotExists());
		item.setTesting(this.isTesting());
		item.setRelatedEtlSchemaObject(schemaInfoSrc);
		item.setAutoIncrementHandlingType(this.getAutoIncrementHandlingType());
		item.setPrimaryKeyInitialIncrementValue(this.getPrimaryKeyInitialIncrementValue());
		item.tryToReplacePlaceholders(schemaInfoSrc);
		
		return item;
	}
	
	public void copyFromOther(EtlItemConfiguration toCopyFrom, EtlConfiguration relatedEtlConf,
	        boolean ignoreMissingParamsOnElements, Connection conn) throws DBException {
		
		this.setSrcConf(new SrcConf());
		this.setRelatedEtlConfig(relatedEtlConf);
		
		this.getSrcConf().setIgnoreMissingParameters(ignoreMissingParamsOnElements);
		this.getSrcConf().copyFromOther(toCopyFrom.getSrcConf(), null, this, conn);
		
		if (toCopyFrom.hasDstConf()) {
			this.setDstConf(DstConf.cloneAll(toCopyFrom.getDstConf(), this, null, conn));
		}
		
		this.setDisabled(toCopyFrom.isDisabled());
		this.setManualMapPrimaryKeyOnField(toCopyFrom.getManualMapPrimaryKeyOnField());
		this.setCreateDstTableIfNotExists(toCopyFrom.isCreateDstTableIfNotExists());
		this.setTesting(toCopyFrom.isTesting());
		this.setAutoIncrementHandlingType(toCopyFrom.getAutoIncrementHandlingType());
		this.setPrimaryKeyInitialIncrementValue(toCopyFrom.getPrimaryKeyInitialIncrementValue());
	}
	
	@Override
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
		this.getSrcConf().tryToReplacePlaceholders(schemaInfoSrc);
		
		if (hasDstConf()) {
			for (DstConf dstConf : this.getDstConf()) {
				dstConf.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
	}
	
	public void doMinimalTableInitialization(Connection srcConn, Connection dstConn)
	        throws DatabaseResourceDoesNotExists, DBException, ForbiddenOperationException {
		this.srcConf.loadSchemaInfo(null, srcConn);
		
		if (hasDstConf()) {
			for (DstConf conf : this.getDstConf()) {
				conf.loadSchemaInfo(null, dstConn);
			}
		}
	}
	
	@Override
	public void tryToLoadFromTemplate() {
		boolean disabled = this.isDisabled();
		
		super.tryToLoadFromTemplate();
		
		this.getSrcConf().tryToLoadFromTemplate();
		this.getSrcConf().setRelatedEtlConfig(this.getRelatedEtlConf());
		this.getSrcConf().setParentConf(this);
		
		if (this.hasDstConf()) {
			for (DstConf conf : this.getDstConf()) {
				conf.tryToLoadFromTemplate();
				conf.setRelatedEtlConfig(getRelatedEtlConf());
				conf.setParentConf(this);
			}
		}
		
		setDisabled(disabled);
	}
}
