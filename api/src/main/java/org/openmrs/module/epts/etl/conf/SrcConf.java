package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SrcConf extends AbstractTableConfiguration implements EtlDataSource {
	
	private List<AuxExtractTable> selfJoinTables;
	
	private List<TableDataSourceConfig> extraTableDataSource;
	
	private List<QueryDataSourceConfig> extraQueryDataSource;
	
	public List<AuxExtractTable> getSelfJoinTables() {
		return selfJoinTables;
	}
	
	public void setSelfJoinTables(List<AuxExtractTable> selfJoinTables) {
		this.selfJoinTables = selfJoinTables;
	}
	
	public List<QueryDataSourceConfig> getExtraQueryDataSource() {
		return extraQueryDataSource;
	}
	
	public void setExtraQueryDataSource(List<QueryDataSourceConfig> extraQueryDataSource) {
		this.extraQueryDataSource = extraQueryDataSource;
	}
	
	public List<TableDataSourceConfig> getExtraTableDataSource() {
		return extraTableDataSource;
	}
	
	public void setExtraTableDataSource(List<TableDataSourceConfig> extraTableDataSource) {
		this.extraTableDataSource = extraTableDataSource;
	}
	
	@Override
	public String getName() {
		return getTableName();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		if (this.hasParentRefInfo()) {
			for (ParentTable ref : this.getParentRefInfo()) {
				TableConfiguration fullLoadedTab = findFullConfiguredConfInAllRelatedTable(ref.getFullTableName());
				
				ref.tryToGenerateTableAlias(getRelatedSyncConfiguration());
				
				if (fullLoadedTab != null) {
					ref.clone(fullLoadedTab, conn);
				} else {
					ref.fullLoad();
				}
				
				if (ref.useSharedPKKey()) {
					fullLoadedTab = findFullConfiguredConfInAllRelatedTable(ref.getSharedKeyRefInfo().getFullTableName());
					
					if (!ref.getSharedKeyRefInfo().hasAlias()) {
						ref.getSharedKeyRefInfo().tryToGenerateTableAlias(getRelatedSyncConfiguration());
					}
					if (fullLoadedTab != null) {
						ref.getSharedKeyRefInfo().clone(fullLoadedTab, conn);
					} else {
						ref.getSharedKeyRefInfo().fullLoad();
					}
				}
				
			}
		}
		
		OpenConnection srcConn = this.getRelatedAppInfo().openConnection();
		
		try {
			
			if (hasSelfJoinTables()) {
				for (AuxExtractTable t : this.getSelfJoinTables()) {
					t.setParentConf(this);
					t.tryToGenerateTableAlias(getRelatedSyncConfiguration());
					t.setMainExtractTable(this);
					
					TableConfiguration fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getFullTableName());
					
					if (fullLoadedTab != null) {
						t.clone(fullLoadedTab, conn);
					} else {
						t.fullLoad(srcConn);
					}
					
					if (t.useSharedPKKey()) {
						t.getSharedKeyRefInfo().tryToGenerateTableAlias(getRelatedSyncConfiguration());
						
						fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getFullTableName());
						
						if (fullLoadedTab != null) {
							t.getSharedKeyRefInfo().clone(fullLoadedTab, srcConn);
						} else {
							t.getSharedKeyRefInfo().fullLoad(srcConn);
						}
						
						t.getSharedKeyRefInfo().setParentConf(t);
					}
				}
			}
			
			if (hasExtraTableDataSourceConfig()) {
				for (TableDataSourceConfig t : this.getExtraTableDataSource()) {
					
					TableConfiguration fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getFullTableName());
					
					t.tryToGenerateTableAlias(getRelatedSyncConfiguration());
					
					if (fullLoadedTab != null) {
						t.clone(fullLoadedTab, conn);
					} else {
						t.fullLoad(srcConn);
					}
					
					t.setRelatedSrcConf(this);
					
					if (t.useSharedPKKey()) {
						t.getSharedKeyRefInfo().tryToGenerateTableAlias(getRelatedSyncConfiguration());
						
						fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getSharedKeyRefInfo().getFullTableName());
						
						if (fullLoadedTab != null) {
							t.getSharedKeyRefInfo().clone(fullLoadedTab, conn);
						} else {
							t.getSharedKeyRefInfo().fullLoad();
						}
					}
				}
			}
			
			if (hasExtraQueryDataSourceConfig()) {
				for (QueryDataSourceConfig query : this.getExtraQueryDataSource()) {
					query.setRelatedSrcConf(this);
					query.fullLoad(srcConn);
				}
			}
		}
		finally
		
		{
			srcConn.finalizeConnection();
		}
		
		this.setFullLoaded(true);
	}
	
	public void setFullLoaded(boolean fullLoaded) {
		this.fullLoaded = fullLoaded;
	}
	
	public QueryDataSourceConfig findAdditionalDataSrc(String dsName) {
		if (!hasExtraQueryDataSourceConfig()) {
			return null;
		}
		
		for (QueryDataSourceConfig src : this.getExtraQueryDataSource()) {
			if (src.getName().equals(dsName)) {
				return src;
			}
		}
		
		throw new ForbiddenOperationException("The table '" + dsName + "'cannot be foud on the mapping src tables");
	}
	
	public boolean isFullLoaded() {
		return this.fullLoaded;
	}
	
	public static SrcConf fastCreate(AbstractTableConfiguration tableConfig, Connection conn) throws DBException {
		SrcConf src = new SrcConf();
		
		src.clone(src, conn);
		
		return src;
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration parent) {
		super.setParentConf((EtlItemConfiguration) parent);
	}
	
	@Override
	@JsonIgnore
	public EtlItemConfiguration getParentConf() {
		return (EtlItemConfiguration) super.getParentConf();
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return getMainApp();
	}
	
	@Override
	public void tryToDiscoverySharedKeyInfo(Connection conn) throws DBException {
		super.tryToDiscoverySharedKeyInfo(conn);
		
		if (useSharedPKKey()) {
			//Parce the shared parent to datasource
			utilities.updateOnArray(this.getParentRefInfo(), getSharedKeyRefInfo(),
			    ParentAsSrcDataSource.generateFromSrcConfSharedPkParent(this, getSharedKeyRefInfo(), conn));
		}
	}
	
	@JsonIgnore
	public List<EtlAdditionalDataSource> getAvaliableExtraDataSource() {
		List<EtlAdditionalDataSource> ds = new ArrayList<>();
		
		if (useSharedPKKey()) {
			ds.add((EtlAdditionalDataSource) getSharedKeyRefInfo());
		}
		
		if (hasExtraTableDataSourceConfig()) {
			ds.addAll(this.getExtraTableDataSource());
		}
		
		if (hasExtraQueryDataSourceConfig()) {
			ds.addAll(this.getExtraQueryDataSource());
		}
		
		return ds;
	}
	
	/**
	 * Generate all avaliable fields on this srcConf, this fields will include all field from
	 * {@link #getFields()} and the fields from all {@link #extraTableDataSource} Note that the
	 * duplicated fields will only be included once
	 * 
	 * @return
	 */
	public List<Field> generateAllAvaliableFields() {
		List<Field> fields = new ArrayList<>();
		
		for (Field f : this.getFields()) {
			fields.add(f);
		}
		
		if (hasExtraTableDataSourceConfig()) {
			
			for (EtlAdditionalDataSource ds : this.getExtraTableDataSource()) {
				for (Field f : ds.getFields()) {
					
					if (!fields.contains(f)) {
						fields.add(f);
					}
				}
			}
		}
		
		return fields;
	}
	
	public boolean hasExtraTableDataSourceConfig() {
		return utilities.arrayHasElement(this.extraTableDataSource);
	}
	
	public boolean hasExtraQueryDataSourceConfig() {
		return utilities.arrayHasElement(this.extraQueryDataSource);
		
	}
	
	public boolean hasSelfJoinTables() {
		return utilities.arrayHasElement(getSelfJoinTables());
	}
	
	public boolean hasRequiredExtraDataSource() {
		if (hasExtraDataSource()) {
			return false;
		} else {
			for (EtlAdditionalDataSource ds : getAvaliableExtraDataSource()) {
				if (ds.isRequired()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean hasExtraDataSource() {
		return utilities.arrayHasElement(getAvaliableExtraDataSource());
	}
	
}
