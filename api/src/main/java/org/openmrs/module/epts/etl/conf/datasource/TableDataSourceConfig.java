package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DbmsType;

/**
 * Represents a source table configuration. A {@link TableDataSourceConfig} is used as an auxiliary
 * extraction table as well as an extra data source
 */
public class TableDataSourceConfig extends AbstractTableConfiguration implements EtlAdditionalDataSource, JoinableEntity, MainJoiningEntity {
	
	private final String stringLock = new String("LOCK_STRING");
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	private SrcConf relatedSrcConf;
	
	private List<AuxExtractTable> auxExtractTable;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	private PreparedQuery defaultPreparedQuery;
	
	public TableDataSourceConfig() {
	}
	
	@Override
	public boolean doNotUseAsDatasource() {
		return false;
	}
	
	private boolean isPrepared() {
		return this.defaultPreparedQuery != null;
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
	
	public void prepare(List<EtlDatabaseObject> mainObject, Connection conn) throws DBException {
		if (isPrepared()) {
			return;
		}
		
		synchronized (stringLock) {
			PreparedQuery query = PreparedQuery.prepare(this, mainObject, getRelatedEtlConf(),
			    DbmsType.determineFromConnection(conn));
			
			this.defaultPreparedQuery = query;
		}
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
	
	public PreparedQuery getDefaultPreparedQuery() {
		return defaultPreparedQuery;
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
	public EtlDatabaseObject loadRelatedSrcObject(List<EtlDatabaseObject> avaliableSrcObjects, Connection srcConn)
	        throws DBException {
		
		if (!isPrepared()) {
			prepare(avaliableSrcObjects, srcConn);
		}
		
		return this.getDefaultPreparedQuery().cloneAndLoadValues(avaliableSrcObjects).query(srcConn);
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
	public void loadOwnElements(Connection conn) throws DBException {
		this.loadJoinElements(conn);
		this.loadAlias();
		
		this.tryToLoadAuxExtraJoinTable(conn);
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
}
