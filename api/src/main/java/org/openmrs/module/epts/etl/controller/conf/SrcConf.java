package org.openmrs.module.epts.etl.controller.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SrcConf extends AbstractTableConfiguration {
	
	private List<TableDataSourceConfig> extraTableDataSource;
	
	private List<QueryDataSourceConfig> extraQueryDataSource;
	
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
	public boolean isGeneric() {
		return false;
	}
	
	public synchronized void fullLoad() throws DBException {
		
		if (this.fullLoaded) {
			return;
		}
		
		super.fullLoad();
		
		OpenConnection srcConn = this.getRelatedAppInfo().openConnection();
		
		try {
			
			if (utilities.arrayHasElement(this.extraTableDataSource)) {
				for (TableDataSourceConfig t : this.extraTableDataSource) {
					t.fullLoad(srcConn);
					
					if (!utilities.arrayHasElement(t.getJoinFields())) {
						//Try to autoload join fields
						
						List<FieldsMapping> fm = null;
						
						//Assuming that the aux src is parent
						List<RefInfo> pInfo = this.findAllRefToParent(t.getTableName());
						
						if (utilities.arrayHasElement(pInfo)) {
							fm = new ArrayList<>();
							
							for (RefInfo ref : pInfo) {
								for (RefMapping map : ref.getFieldsMapping()) {
									fm.add(new FieldsMapping(map.getChildField().getName(), "",
									        map.getParentField().getName()));
								}
							}
						} else {
							fm = new ArrayList<>();
							
							//Assuning that the aux src is child
							pInfo = t.findAllRefToParent(this.getTableName());
							
							if (pInfo != null) {
								for (RefInfo ref : pInfo) {
									for (RefMapping map : ref.getFieldsMapping()) {
										fm.add(new FieldsMapping(map.getParentField().getName(), "",
										        map.getChildField().getName()));
									}
								}
							}
						}
						
						if (fm != null) {
							for (FieldsMapping f : fm) {
								t.addJoinField(f);
							}
						}
					}
					
					if (utilities.arrayHasNoElement(t.getJoinFields())) {
						throw new ForbiddenOperationException(
						        "No join fields were difined between " + this.getTableName() + " And " + t.getTableName());
					}
				}
				
			}
			
			if (utilities.arrayHasElement(this.extraQueryDataSource)) {
				for (QueryDataSourceConfig query : this.extraQueryDataSource) {
					query.setRelatedSrcConf(this);
					query.fullLoad(srcConn);
				}
			}
		}
		catch (Exception e) {
			srcConn.finalizeConnection();
			
			throw new RuntimeException(e);
		}
		
		this.fullLoaded = true;
	}
	
	public QueryDataSourceConfig findAdditionalDataSrc(String dsName) {
		if (!utilities.arrayHasElement(this.extraQueryDataSource)) {
			return null;
		}
		
		for (QueryDataSourceConfig src : this.extraQueryDataSource) {
			if (src.getName().equals(dsName)) {
				return src;
			}
		}
		
		throw new ForbiddenOperationException("The table '" + dsName + "'cannot be foud on the mapping src tables");
	}
	
	public boolean isFullLoaded() {
		return this.fullLoaded;
	}
	
	public static SrcConf fastCreate(AbstractTableConfiguration tableConfig) {
		SrcConf src = new SrcConf();
		
		src.clone(src);
		
		return src;
	}
	
	@Override
	public void setParent(SyncDataConfiguration parent) {
		super.setParent((EtlItemConfiguration) parent);
	}
	
	@Override
	@JsonIgnore
	public EtlItemConfiguration getParent() {
		return (EtlItemConfiguration) super.getParent();
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return getMainApp();
	}
	
	@JsonIgnore
	public List<SyncDataSource> getAvaliableExtraDataSource() {
		List<SyncDataSource> ds = new ArrayList<>();
		
		if (utilities.arrayHasElement(this.extraTableDataSource)) {
			ds.addAll(utilities.parseList(this.extraTableDataSource, SyncDataSource.class));
		}
		
		if (utilities.arrayHasElement(this.extraQueryDataSource)) {
			ds.addAll(utilities.parseList(this.extraQueryDataSource, SyncDataSource.class));
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
		
		if (this.extraTableDataSource != null) {
			
			for (SyncDataSource ds : this.extraTableDataSource) {
				for (Field f : ds.getFields()) {
					
					if (!fields.contains(f)) {
						fields.add(f);
					}
				}
			}
		}
		
		return fields;
	}
	
}
