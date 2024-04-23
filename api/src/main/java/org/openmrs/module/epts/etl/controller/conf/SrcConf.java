package org.openmrs.module.epts.etl.controller.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SrcConf extends AbstractTableConfiguration {
	
	private List<AuxiliaryExtractionSrcTable> auxiliaryExtractionSrcTable;
	
	private List<EtlExtraDataSource> extraDataSource;
	
	public List<AuxiliaryExtractionSrcTable> getAuxiliaryExtractionSrcTable() {
		return auxiliaryExtractionSrcTable;
	}
	
	public void setAuxilliaryExtractionSrcTable(List<AuxiliaryExtractionSrcTable> auxiliaryExtractionSrcTable) {
		this.auxiliaryExtractionSrcTable = auxiliaryExtractionSrcTable;
	}
	
	public List<EtlExtraDataSource> getExtraDataSource() {
		return extraDataSource;
	}
	
	public void setExtraDataSource(List<EtlExtraDataSource> extraDataSource) {
		this.extraDataSource = extraDataSource;
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
		
		OpenConnection srcConn = this.getMainApp().openConnection();
		
		try {
			
			if (utilities.arrayHasElement(this.auxiliaryExtractionSrcTable)) {
				for (AuxiliaryExtractionSrcTable t : this.auxiliaryExtractionSrcTable) {
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
			
			if (utilities.arrayHasElement(this.getExtraDataSource())) {
				for (EtlExtraDataSource s : this.getExtraDataSource()) {
					s.setRelatedSrcConf(this);
					s.fullLoad(srcConn);
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
	
}
