package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

/**
 * Represents a source table configuration. A {@link TableDataSourceConfig} is used as an auxiliary
 * extraction table as well as an extra datasource
 */
public class TableDataSourceConfig extends AbstractTableConfiguration implements EtlAdditionalDataSource, JoinableEntity {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private SrcConf relatedSrcConf;
	
	private List<AuxExtractTable> auxExtractTable;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	public TableDataSourceConfig() {
	}
	
	@Override
	public List<AuxExtractTable> getAuxExtractTable() {
		return this.auxExtractTable;
	}
	
	@Override
	public void setAuxExtractTable(List<AuxExtractTable> auxExtractTable) {
		this.auxExtractTable = auxExtractTable;
	}
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	@Override
	public boolean isRequired() {
		return this.joinType.isInnerJoin();
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		if (isFullLoaded()) {
			return;
		}
		
		this.tryToGenerateTableAlias(getRelatedEtlConf());
		
		super.fullLoad(conn);
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
	public EtlConfiguration getRelatedEtlConf() {
		return getParentConf().getRelatedEtlConf();
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(List<EtlDatabaseObject> avaliableSrcObjects, Connection srcConn)
	        throws DBException {
		
		String condition = super.generateConditionsFields(avaliableSrcObjects.get(0), this.joinFields,
		    this.joinExtraCondition);
		
		String sql = this.generateSelectFromQuery() + " WHERE " + condition;
		
		return DatabaseObjectDAO.find(this.getLoadHealper(), this.getSyncRecordClass(), sql, null, srcConn);
	}
	
	@Override
	public boolean allowMultipleSrcObjects() {
		return false;
	}
	
	public String generateJoinCondition() {
		return super.generateJoinCondition(this.relatedSrcConf, this.joinFields, this.joinExtraCondition);
	}
	
	@Override
	public String getName() {
		return super.getTableName();
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return this.relatedSrcConf.getRelatedConnInfo();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public JoinType determineJoinType() {
		if (utilities.arrayHasMoreThanOneElements(this.getParentConf().getExtraTableDataSource())) {
			return JoinType.LEFT;
		} else {
			return JoinType.INNER;
		}
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		loadJoinElements(conn);
		
		if (hasSelfJoinTables()) {
			for (AuxExtractTable t : this.getAuxExtractTable()) {
				t.setParentConf(this);
				t.setMainExtractTable(this);
				
				t.tryToGenerateTableAlias(this.getRelatedEtlConf());
				
				t.fullLoad(conn);
			}
			
		}
		
		if (hasJoinExtraCondition() && !isUsingManualDefinedAlias()) {
			this.setJoinExtraCondition(
			    this.getJoinExtraCondition().replaceAll(getTableName() + "\\.", getTableAlias() + "\\."));
			
			String condition = DBUtilities.tryToPutTableNameInFieldsInASqlClause(this.getJoinExtraCondition(),
			    this.getTableAlias(), this.getFields());
			
			this.setJoinExtraCondition(condition);
		}
	}
	
	private boolean hasSelfJoinTables() {
		return utilities.arrayHasElement(this.getAuxExtractTable());
	}
	
	@Override
	public TableConfiguration getJoiningEntity() {
		return getRelatedSrcConf();
	}
	
}
