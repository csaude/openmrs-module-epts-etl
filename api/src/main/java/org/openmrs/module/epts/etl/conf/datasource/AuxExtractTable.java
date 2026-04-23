package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlTemplateInfo;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlSrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConditionClauseScope;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents an auxiliary table for data extraction. A {@link AuxExtractTable} is used as an
 * auxiliary extraction table usually used to include additional extraction conditions
 */
public class AuxExtractTable extends AbstractTableConfiguration implements JoinableEntity, MainJoiningEntity, EtlDataSource, EtlSrcConf {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private ConditionClauseScope joinExtraConditionScope;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	private List<InnerAuxExtractTable> auxExtractTable;
	
	private Boolean doNotUseAsDatasource;
	
	public AuxExtractTable() {
		this.joinExtraConditionScope = ConditionClauseScope.JOIN_CLAUSE;
	}
	
	@Override
	public void init(EtlDataConfiguration relatedParent, EtlDatabaseObject etlSchemaObject, Connection srcConn,
	        Connection dstConn) throws DBException {
		super.init(relatedParent, etlSchemaObject, srcConn, dstConn);
		
		if (this.auxExtractTable != null) {
			for (InnerAuxExtractTable aux : this.auxExtractTable) {
				aux.init(this, etlSchemaObject, srcConn, dstConn);
			}
		}
	}
	
	@Override
	public ConditionClauseScope getJoinExtraConditionScope() {
		return this.joinExtraConditionScope;
	}
	
	@Override
	public void setJoinExtraConditionScope(ConditionClauseScope joinExtraConditionScope) {
		this.joinExtraConditionScope = joinExtraConditionScope;
	}
	
	public List<InnerAuxExtractTable> getAuxExtractTable() {
		return auxExtractTable;
	}
	
	public Boolean isDoNotUseAsDatasource() {
		return isTrue(doNotUseAsDatasource);
	}
	
	@Override
	public Boolean doNotUseAsDatasource() {
		return isDoNotUseAsDatasource();
	}
	
	public void setDoNotUseAsDatasource(Boolean doNotUseAsDatasource) {
		this.doNotUseAsDatasource = doNotUseAsDatasource;
	}
	
	public void setAuxExtractTable(List<InnerAuxExtractTable> auxExtractTable) {
		this.auxExtractTable = auxExtractTable;
	}
	
	@Override
	public void setExtraConditionForExtract(String extraConditionForExtract) {
		if (extraConditionForExtract != null) {
			throw new ForbiddenOperationException(
			        "Forbiden method for auxExtractTable(" + this + ") please use joinExtraCondition parameter!!!");
		}
	}
	
	@Override
	public List<? extends JoinableEntity> getJoiningTable() {
		return this.getAuxExtractTable();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setJoiningTable(List<? extends JoinableEntity> joiningTable) {
		setAuxExtractTable((List<InnerAuxExtractTable>) joiningTable);
	}
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	@Override
	public void setMainExtractTable(MainJoiningEntity mainExtractTable) {
		this.setParentConf(mainExtractTable);
	}
	
	@Override
	public MainJoiningEntity getParentConf() {
		return (MainJoiningEntity) super.getParentConf();
	}
	
	@Override
	public MainJoiningEntity getMainExtractTable() {
		return this.getParentConf();
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
	public DBConnectionInfo getRelatedConnInfo() {
		return this.getMainExtractTable().getRelatedConnInfo();
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration parentConf) {
		if (parentConf instanceof MainJoiningEntity) {
			super.setParentConf(parentConf);
		} else
			throw new EtlExceptionImpl("Only MainJoiningEntity are accepted as parent of an AuxExtractTable!");
	}
	
	@Override
	public Boolean isGeneric() {
		return false_();
	}
	
	@Override
	public TableConfiguration getJoiningEntity() {
		return (TableConfiguration) this.getMainExtractTable();
	}
	
	@Override
	public void loadOwnElements(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
	}
	
	@Override
	public Boolean isMainJoiningEntity() {
		return true_();
	}
	
	@Override
	public MainJoiningEntity parseToJoining() throws ForbiddenOperationException {
		return this;
	}
	
	@Override
	public Boolean isJoinable() {
		return true_();
	}
	
	@Override
	public JoinableEntity parseToJoinable() throws ForbiddenOperationException {
		return this;
	}
	
	@Override
	public String getName() {
		return this.getTableName();
	}
	
	public static List<AuxExtractTable> cloneAll(List<AuxExtractTable> allToCloneFrom,
	        MainJoiningEntity relatedMainExtractTable, EtlDatabaseObject schemaInfoSrc, Connection conn) throws DBException {
		List<AuxExtractTable> allCloned = null;
		
		if (utilities.listHasElement(allToCloneFrom)) {
			allCloned = new ArrayList<>(allToCloneFrom.size());
			
			for (AuxExtractTable aux : allToCloneFrom) {
				AuxExtractTable cloned = new AuxExtractTable();
				cloned.clone(aux, relatedMainExtractTable, schemaInfoSrc, conn);
				
				allCloned.add(cloned);
			}
		}
		
		return allCloned;
	}
	
	public void clone(AuxExtractTable toCloneFrom, MainJoiningEntity relatedMainExtractTable,
	        EtlDatabaseObject schemaInfoSrc, Connection conn) throws DBException {
		
		this.setIgnoreMissingParameters(relatedMainExtractTable.ignoreMissingParameters());
		
		super.clone(toCloneFrom, relatedMainExtractTable, schemaInfoSrc, conn);
		
		this.setParentConf(relatedMainExtractTable);
		this.setIgnoreMissingParameters(relatedMainExtractTable.ignoreMissingParameters());
		this.setJoinFields(FieldsMapping.cloneAll(toCloneFrom.getJoinFields(), conn));
		this.setJoinExtraCondition(toCloneFrom.getJoinExtraCondition());
		this.setJoinType(toCloneFrom.getJoinType());
		this.setJoinExtraConditionScope(toCloneFrom.getJoinExtraConditionScope());
		if (utilities.listHasElement(toCloneFrom.getAuxExtractTable())) {
			this.setAuxExtractTable(new ArrayList<>(toCloneFrom.getAuxExtractTable().size()));
			
			for (InnerAuxExtractTable aux : toCloneFrom.getAuxExtractTable()) {
				InnerAuxExtractTable cloned = new InnerAuxExtractTable();
				cloned.clone(aux, this, schemaInfoSrc, conn);
				
				this.getAuxExtractTable().add(cloned);
			}
		}
		
		this.setDoNotUseAsDatasource(toCloneFrom.isDoNotUseAsDatasource());
	}
	
	@Override
	public void tryToLoadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists {
		
		super.tryToLoadSchemaInfo(schemaInfoSrc, conn);
		
		if (this.hasAuxExtractTable()) {
			for (InnerAuxExtractTable tab : this.getAuxExtractTable()) {
				tab.tryToLoadSchemaInfo(schemaInfoSrc, conn);
			}
		}
	}
	
	public static void tryToReplacePlaceholders(List<AuxExtractTable> auxExtractTable, EtlDatabaseObject schemaInfoSrc) {
		if (utilities.listHasElement(auxExtractTable)) {
			for (AuxExtractTable a : auxExtractTable) {
				a.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
	}
	
	@Override
	public void tryToReplacePlaceholdersOnOwnElements(EtlDatabaseObject schemaInfoSrc) {
		FieldsMapping.tryToReplacePlaceholders(getJoinFields(), schemaInfoSrc);
		
		setJoinExtraCondition(utilities.tryToReplacePlaceholders(getJoinExtraCondition(), schemaInfoSrc));
		
		InnerAuxExtractTable.tryToReplacePlaceholders(this.getAuxExtractTable(), schemaInfoSrc);
		
	}
	
	@Override
	public String getQuery() {
		return null;
	}
	
	@Override
	public EtlTemplateInfo retrieveNearestTemplate() {
		return this.getTemplate() != null ? this.getTemplate() : getMainExtractTable().retrieveNearestTemplate();
	}
}
