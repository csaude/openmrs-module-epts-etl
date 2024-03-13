package org.openmrs.module.epts.etl.controller.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class SrcConf extends SyncDataConfiguration {
	
	/*
	 * The main source table
	 */
	private SyncTableConfiguration mainSrcTableConf;
	
	/*
	 * The auxiliary source table
	 */
	private SyncTableConfiguration auxSrcTableConf;
	
	private List<EtlExtraDataSource> extraDataSource;
	
	/*
	 * Main source and auxiliary join fields
	 */
	private List<FieldsMapping> joinFields;
	
	private String extraConditionForExtract;
	
	private String joinExtraCondition;
	
	private boolean fullLoaded;
	
	public List<EtlExtraDataSource> getExtraDataSource() {
		return extraDataSource;
	}
	
	public void setExtraDataSource(List<EtlExtraDataSource> extraDataSource) {
		this.extraDataSource = extraDataSource;
	}
	
	public SyncTableConfiguration getMainSrcTableConf() {
		return mainSrcTableConf;
	}
	
	public String getAuxTableName() {
		return this.auxSrcTableConf.getTableName();
	}
	
	public String getMainTableName() {
		return this.mainSrcTableConf.getTableName();
	}
	
	public void setMainSrcTableConf(SyncTableConfiguration mainSrcTableConfiguration) {
		this.mainSrcTableConf = mainSrcTableConfiguration;
	}
	
	public SyncTableConfiguration getAuxSrcTableConf() {
		return auxSrcTableConf;
	}
	
	public void setAuxSrcTableConf(SyncTableConfiguration auxSrcTableConfiguration) {
		this.auxSrcTableConf = auxSrcTableConfiguration;
	}
	
	public List<FieldsMapping> getMainAndExtrajoinFields() {
		return joinFields;
	}
	
	public void setMainAndExtrajoinFields(List<FieldsMapping> joinFields) {
		this.joinFields = joinFields;
	}
	
	public String getExtraConditionForExtract() {
		return extraConditionForExtract;
	}
	
	public void setExtraConditionForExtract(String extraConditionForExtract) {
		this.extraConditionForExtract = extraConditionForExtract;
	}
	
	public synchronized void fullLoad() throws DBException {
		
		if (this.fullLoaded) {
			return;
		}
		
		this.mainSrcTableConf.fullLoad();
		
		if (this.auxSrcTableConf != null) {
			this.auxSrcTableConf.fullLoad();
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
		
		if (this.auxSrcTableConf != null) {
			if (!utilities.arrayHasElement(this.joinFields)) {
				//Try to autoload join fields
				
				FieldsMapping fm = null;
				
				//Assuming that the aux src is parent
				RefInfo pInfo = this.mainSrcTableConf.findParent(RefInfo.init(this.auxSrcTableConf.getTableName()));
				
				if (pInfo != null) {
					fm = new FieldsMapping(pInfo.getRefColumnName(), "", pInfo.getRefColumnName());
				} else {
					
					//Assuning that the aux src is child
					pInfo = this.auxSrcTableConf.findParent(RefInfo.init(this.mainSrcTableConf.getTableName()));
					
					if (pInfo != null) {
						fm = new FieldsMapping(pInfo.getRefColumnName(), "", pInfo.getRefColumnName());
					}
				}
				
				if (fm != null) {
					this.joinFields = new ArrayList<>();
					this.joinFields.add(fm);
				}
			}
			
			if (utilities.arrayHasNoElement(this.joinFields)) {
				throw new ForbiddenOperationException("No join fields were difined between "
				        + this.mainSrcTableConf.getTableName() + " And " + this.auxSrcTableConf.getTableName());
			}
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
	
	public String generateConditionsFields() {
		String conditionFields = "";
		
		for (int i = 0; i < this.joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.joinFields.get(i);
			
			conditionFields += "src_." + field.getSrcField() + " = " + getAuxSrcTableConf().getTableName() + "."
			        + field.getDstField();
		}
		
		if (utilities.stringHasValue(this.getJoinExtraCondition())) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	public String generateConditionsFields(DatabaseObject dbObject) {
		String conditionFields = "";
		
		for (int i = 0; i < this.joinFields.size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			FieldsMapping field = this.joinFields.get(i);
			
			Object value = dbObject.getFieldValue(field.getSrcFieldAsClassField());
			
			conditionFields += AttDefinedElements.defineSqlAtribuitionString(field.getDstField(), value);
		}
		
		if (utilities.stringHasValue(this.getJoinExtraCondition())) {
			conditionFields += " AND (" + this.getJoinExtraCondition() + ")";
		}
		
		return conditionFields;
	}
	
	public boolean isFullLoaded() {
		return this.fullLoaded;
	}
	
	public String getJoinExtraCondition() {
		return joinExtraCondition;
	}
	
	public void setJoinExtraCondition(String joinExtraCondition) {
		this.joinExtraCondition = joinExtraCondition;
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
