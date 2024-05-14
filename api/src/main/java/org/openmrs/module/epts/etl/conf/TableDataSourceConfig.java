package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents a source table configuration. A {@link TableDataSourceConfig} is used as an auxiliary
 * extraction table as well as an extra datasource
 */
public class TableDataSourceConfig extends AbstractTableConfiguration implements EtlAdditionalDataSource {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private SrcConf relatedSrcConf;
	
	private List<AuxExtractTable> selfJoinTables;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	public TableDataSourceConfig() {
	}
	
	public List<AuxExtractTable> getSelfJoinTables() {
		return selfJoinTables;
	}
	
	public void setSelfJoinTables(List<AuxExtractTable> selfJoinTables) {
		this.selfJoinTables = selfJoinTables;
	}
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	@Override
	public boolean isRequired() {
		return this.joinType.isLeftJoin();
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		
		if (isFullLoaded()) {
			return;
		}
		
		this.setTableAlias(getRelatedSrcConf().generateAlias(this));
		
		super.fullLoad(conn);
		
		tryToLoadJoinFields();
		
		if (hasExtraConditionForExtract()) {
			this.setJoinExtraCondition(this.joinExtraCondition.replaceAll(getTableName() + "\\.", getTableAlias() + "\\."));
		}
		
		if (!hasJoinFields()) {
			throw new ForbiddenOperationException("No join fields were difined between " + this.relatedSrcConf.getTableName()
			        + " And " + this.getTableName());
		}
		
		if (!hasJoinType()) {
			if (utilities.arrayHasMoreThanOneElements(this.getParentConf().getExtraTableDataSource())) {
				this.joinType = JoinType.LEFT;
			} else {
				this.joinType = JoinType.INNER;
			}
		}
		
		if (hasSelfJoinTables()) {
			for (AuxExtractTable t : this.getSelfJoinTables()) {
				t.setParentConf(this);
				t.setMainExtractTable(this);
				
				t.setTableAlias(this.getParentConf().generateAlias(t));
				
				t.fullLoad(conn);
			}
			
		}
	}
	
	public boolean hasJoinType() {
		return this.joinType != null;
	}
	
	public boolean hasSelfJoinTables() {
		return utilities.arrayHasElement(this.selfJoinTables);
	}
	
	public void tryToLoadJoinFields() {
		if (!utilities.arrayHasElement(this.joinFields)) {
			this.joinFields = tryToLoadJoinFields(this.relatedSrcConf);
		}
	}
	
	public boolean hasJoinFields() {
		return utilities.arrayHasElement(this.joinFields);
	}
	
	public String getJoinExtraCondition() {
		return joinExtraCondition;
	}
	
	public void setJoinExtraCondition(String joinExtraCondition) {
		this.joinExtraCondition = joinExtraCondition;
	}
	
	public List<FieldsMapping> getJoinFields() {
		return joinFields;
	}
	
	public void setJoinFields(List<FieldsMapping> joinFields) {
		this.joinFields = joinFields;
	}
	
	@Override
	public SrcConf getRelatedSrcConf() {
		return relatedSrcConf;
	}
	
	@Override
	public void setRelatedSrcConf(SrcConf relatedSrcConf) {
		this.relatedSrcConf = relatedSrcConf;
		
		setParentConf(relatedSrcConf);
	}
	
	@Override
	public SrcConf getParentConf() {
		return this.relatedSrcConf;
	}
	
	@Override
	public EtlConfiguration getRelatedSyncConfiguration() {
		return getParentConf().getRelatedSyncConfiguration();
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(EtlDatabaseObject mainObject, Connection srcConn, AppInfo srcAppInfo)
	        throws DBException {
		String condition = super.generateConditionsFields(mainObject, this.joinFields, this.joinExtraCondition);
		
		return DatabaseObjectDAO.find(this.getLoadHealper(), this.getSyncRecordClass(srcAppInfo), condition, srcConn);
	}
	
	public String generateJoinCondition() {
		return super.generateJoinCondition(this.relatedSrcConf, this.joinFields, this.joinExtraCondition);
	}
	
	public void addJoinField(FieldsMapping fm) {
		if (this.joinFields == null) {
			this.joinFields = new ArrayList<>();
		}
		
		this.joinFields.add(fm);
	}
	
	@Override
	public String getName() {
		return super.getTableName();
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return this.relatedSrcConf.getRelatedAppInfo();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
}
