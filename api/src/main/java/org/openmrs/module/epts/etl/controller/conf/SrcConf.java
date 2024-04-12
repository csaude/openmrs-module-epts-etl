package org.openmrs.module.epts.etl.controller.conf;

import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class SrcConf extends SyncTableConfiguration {
	
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
						
						FieldsMapping fm = null;
						
						//Assuming that the aux src is parent
						RefInfo pInfo = this.findParent(t.getTableName());
						
						if (pInfo != null) {
	//						fm = new FieldsMapping(pInfo.getRefColumnName(), "",
//							        pInfo.getRefTableConfiguration().getPrimaryKey());
						} else {
							
							//Assuning that the aux src is child
							pInfo = t.findParent(this.getTableName());
							
							if (pInfo != null) {
	//							fm = new FieldsMapping(this.getPrimaryKey(), "", pInfo.getRefColumnName());
							}
						}
						
						if (fm != null) {
							t.addJoinField(fm);
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
	
	public static SrcConf fastCreate(SyncTableConfiguration tableConfig) {
		SrcConf src = new SrcConf();
		
		src.clone(src);
		
		return src;
	}
	
	@Override
	public void setParent(SyncDataConfiguration parent) {
		super.setParent((EtlConfiguration) parent);
	}
	
	@Override
	public EtlConfiguration getParent() {
		return (EtlConfiguration) super.getParent();
	}
	
}
