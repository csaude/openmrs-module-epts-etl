package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConditionClauseScope;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents a source table configuration. A {@link TableDataSourceConfig} is used as an auxiliary
 * extraction table as well as an extra data source
 */
public class TableDataSourceConfig extends AbstractTableConfiguration implements EtlAdditionalDataSource, JoinableEntity, MainJoiningEntity {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private SrcConf relatedSrcConf;
	
	private List<AuxExtractTable> auxExtractTable;
	
	/*
	 * Defines the type of SQL join used to link the extra table with the main table. 
	 * The supported values are INNER, LEFT, or RIGHT. If this property is not specified, the default join type is LEFT. 
	 * When the join type is set to INNER, the ETL process will skip the main record if no matching record is found in the extra table.
	 */
	private JoinType joinType;
	
	private ConditionClauseScope joinExtraConditionScope;
	
	public TableDataSourceConfig() {
		this.joinExtraConditionScope = ConditionClauseScope.JOIN_CLAUSE;
		this.joinType = JoinType.LEFT;
	}
	
	@Override
	public ConditionClauseScope getJoinExtraConditionScope() {
		return this.joinExtraConditionScope;
	}
	
	@Override
	public void setJoinExtraConditionScope(ConditionClauseScope joinExtraConditionScope) {
		this.joinExtraConditionScope = joinExtraConditionScope;
	}
	
	@Override
	public boolean doNotUseAsDatasource() {
		return false;
	}
	
	@Override
	public List<? extends JoinableEntity> getJoiningTable() {
		return this.getAuxExtractTable();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setJoiningTable(List<? extends JoinableEntity> joiningTable) {
		setAuxExtractTable((List<AuxExtractTable>) joiningTable);
	}
	
	public List<AuxExtractTable> getAuxExtractTable() {
		return this.auxExtractTable;
	}
	
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
	public String getQuery() {
		String condition = super.generateConditionsFields(null, this.joinFields, this.joinExtraCondition);
		
		return this.generateSelectFromQuery() + " WHERE " + condition;
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
	public void setExtraConditionForExtract(String extraConditionForExtract) {
		throw new ForbiddenOperationException(
		        "Forbiden method for auxExtractTable(" + this + ") please use joinExtraCondition parameter!!!");
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(EtlProcessor processor, EtlDatabaseObject srcObject,
	        List<EtlDatabaseObject> avaliableSrcObjects, Connection srcConn) throws DBException {
		
		if (!isPrepared()) {
			prepare(avaliableSrcObjects, srcConn);
		}
		
		List<EtlDatabaseObject> list = this.getDefaultPreparedQuery().cloneAndLoadValues(avaliableSrcObjects)
		        .query(processor.getEngine(), srcConn);
		
		if (utilities.arrayHasNoElement(list)) {
			return null;
		}
		
		if (utilities.arrayHasMoreThanOneElements(list)) {
			logWarn("The datasource (" + this.getName() + ") returned more than one src objects for src: " + srcObject);
		}
		
		return list.get(0);
	}
	
	@Override
	public boolean allowMultipleSrcObjectsForLoading() {
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
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		this.loadJoinElements(schemaInfo, conn);
		this.loadAlias();
		
		this.tryToLoadAuxExtraJoinTable(schemaInfo, conn);
	}
	
	@Override
	public TableConfiguration getJoiningEntity() {
		return getRelatedSrcConf();
	}
	
	@Override
	public void setMainExtractTable(MainJoiningEntity mainJoiningTable) {
		this.relatedSrcConf = (SrcConf) mainJoiningTable;
	}
	
	@Override
	public MainJoiningEntity getMainExtractTable() {
		return this.relatedSrcConf;
	}
	
	@Override
	public boolean isJoinable() {
		return true;
	}
	
	@Override
	public boolean isMainJoiningEntity() {
		return true;
	}
	
	@Override
	public MainJoiningEntity parseToJoining() throws ForbiddenOperationException {
		return this;
	}
	
	@Override
	public JoinableEntity parseToJoinable() throws ForbiddenOperationException {
		return this;
	}
	
	public static List<TableDataSourceConfig> cloneAll(List<TableDataSourceConfig> allToCloneFrom, SrcConf relatedSrcConf,
	        EtlDatabaseObject schemaInfoSrc, Connection conn) throws DBException {
		
		List<TableDataSourceConfig> allCloned = null;
		
		if (utilities.arrayHasElement(allToCloneFrom)) {
			allCloned = new ArrayList<>(allToCloneFrom.size());
			
			for (TableDataSourceConfig aux : allToCloneFrom) {
				TableDataSourceConfig cloned = new TableDataSourceConfig();
				cloned.clone(aux, relatedSrcConf, schemaInfoSrc, conn);
				
				allCloned.add(cloned);
			}
		}
		
		return allCloned;
	}
	
	public void clone(TableDataSourceConfig toCloneFrom, SrcConf relatedSrcConf, EtlDatabaseObject schemaInfoSrc,
	        Connection conn) throws DBException {
		super.clone(toCloneFrom, schemaInfoSrc, conn);
		
		this.setJoinFields(toCloneFrom.getJoinFields());
		this.setJoinExtraCondition(this.getJoinExtraCondition());
		this.setRelatedSrcConf(relatedSrcConf);
		this.setJoinExtraConditionScope(toCloneFrom.getJoinExtraConditionScope());
		
		if (toCloneFrom.hasAuxExtractTable()) {
			this.setAuxExtractTable(AuxExtractTable.cloneAll(toCloneFrom.getAuxExtractTable(), this, schemaInfoSrc, conn));
		}
		
		this.setJoinType(toCloneFrom.getJoinType());
	}
	
	@Override
	public void loadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists {
		super.loadSchemaInfo(schemaInfoSrc, conn);
		
		if (this.hasAuxExtractTable()) {
			for (AuxExtractTable tab : this.getAuxExtractTable()) {
				tab.loadSchemaInfo(schemaInfoSrc, conn);
			}
		}
	}
	
	public static void tryToReplacePlaceholders(List<TableDataSourceConfig> extraTableDataSource,
	        EtlDatabaseObject schemaInfoSrc) {
		if (utilities.arrayHasElement(extraTableDataSource)) {
			for (TableDataSourceConfig a : extraTableDataSource) {
				a.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
	}
	
	@Override
	public void tryToReplacePlaceholdersOnOwnElements(EtlDatabaseObject schemaInfoSrc) {
		FieldsMapping.tryToReplacePlaceholders(getJoinFields(), schemaInfoSrc);
		setJoinExtraCondition(utilities.tryToReplacePlaceholders(getJoinExtraCondition(), schemaInfoSrc));
		
		AuxExtractTable.tryToReplacePlaceholders(this.getAuxExtractTable(), schemaInfoSrc);
		
	}
}
