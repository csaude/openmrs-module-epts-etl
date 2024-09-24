package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.JoinType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents an auxiliary table for data extraction. A {@link AuxExtractTable} is used as an
 * auxiliary extraction table usually used to include additional extraction conditions
 */
public class AuxExtractTable extends AbstractTableConfiguration implements JoinableEntity, MainJoiningEntity, EtlDataSource {
	
	private List<FieldsMapping> joinFields;
	
	private String joinExtraCondition;
	
	/*
	 * The join type between this additional src table with the main src table. It could be INNER or LEFT.
	 * If empty, a INNER join will be applied if the main table has only one additional src, and will be LEFT join if there are more than one additional src tables 
	 */
	private JoinType joinType;
	
	private MainJoiningEntity mainExtractTable;
	
	private List<InnerAuxExtractTable> auxExtractTable;
	
	private boolean doNotUseAsDatasource;
	
	public AuxExtractTable() {
	}
	
	public List<InnerAuxExtractTable> getAuxExtractTable() {
		return auxExtractTable;
	}
	
	public boolean isDoNotUseAsDatasource() {
		return doNotUseAsDatasource;
	}
	
	@Override
	public boolean doNotUseAsDatasource() {
		return isDoNotUseAsDatasource();
	}
	
	public void setDoNotUseAsDatasource(boolean doNotUseAsDatasource) {
		this.doNotUseAsDatasource = doNotUseAsDatasource;
	}
	
	public void setAuxExtractTable(List<InnerAuxExtractTable> auxExtractTable) {
		this.auxExtractTable = auxExtractTable;
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
		this.mainExtractTable = mainExtractTable;
	}
	
	@Override
	public MainJoiningEntity getMainExtractTable() {
		return mainExtractTable;
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
	public AbstractTableConfiguration getParentConf() {
		return (AbstractTableConfiguration) getMainExtractTable();
	}
	
	@Override
	public EtlConfiguration getRelatedEtlConf() {
		return getParentConf().getRelatedEtlConf();
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return this.getParentConf().getRelatedConnInfo();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public TableConfiguration getJoiningEntity() {
		return (TableConfiguration) mainExtractTable;
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
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
	public boolean isJoinable() {
		return true;
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
		
		if (utilities.arrayHasElement(allToCloneFrom)) {
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
		super.clone(toCloneFrom, schemaInfoSrc, conn);
		
		this.setJoinFields(toCloneFrom.getJoinFields());
		this.setJoinExtraCondition(toCloneFrom.getJoinExtraCondition());
		this.setJoinType(toCloneFrom.getJoinType());
		this.setMainExtractTable(relatedMainExtractTable);
		
		if (utilities.arrayHasElement(toCloneFrom.getAuxExtractTable())) {
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
	public void loadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists {
		super.loadSchemaInfo(schemaInfoSrc, conn);
		
		if (this.hasAuxExtractTable()) {
			for (InnerAuxExtractTable tab : this.getAuxExtractTable()) {
				tab.loadSchemaInfo(schemaInfoSrc, conn);
			}
		}
	}
	
}
