package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlField;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.JoinableEntity;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.ObjectDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.EtlDstType;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SrcConf extends AbstractTableConfiguration implements EtlDataSource, MainJoiningEntity {
	
	private List<AuxExtractTable> auxExtractTable;
	
	private List<TableDataSourceConfig> extraTableDataSource;
	
	private List<QueryDataSourceConfig> extraQueryDataSource;
	
	private List<ObjectDataSource> extraObjectDataSource;
	
	private EtlDstType dstType;
	
	/**
	 * The fields involved in ETL process for this srcConf. Note that when the dstConf is not
	 * configured on related {@link EtlItemConfiguration}, then this fields will automatically
	 * mapped to target table
	 */
	private List<EtlField> etlFields;
	
	public SrcConf() {
	}
	
	@Override
	public boolean doNotUseAsDatasource() {
		return false;
	}
	
	public List<ObjectDataSource> getExtraObjectDataSource() {
		return extraObjectDataSource;
	}
	
	public void setExtraObjectDataSource(List<ObjectDataSource> extraObjectDataSource) {
		this.extraObjectDataSource = extraObjectDataSource;
	}
	
	public List<EtlField> getEtlFields() {
		return etlFields;
	}
	
	public void setEtlFields(List<EtlField> etlFields) {
		this.etlFields = etlFields;
	}
	
	public EtlDstType getDstType() {
		return dstType;
	}
	
	public void setDstType(EtlDstType dstType) {
		this.dstType = dstType;
	}
	
	public List<AuxExtractTable> getAuxExtractTable() {
		return auxExtractTable;
	}
	
	public void setAuxExtractTable(List<AuxExtractTable> auxExtractTable) {
		this.auxExtractTable = (List<AuxExtractTable>) auxExtractTable;
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
	public String getName() {
		return getTableName();
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	public boolean hasDstType() {
		return getDstType() != null;
	}
	
	@Override
	public void fullLoad(Connection conn) throws DBException {
		if (!hasManualMapPrimaryKeyOnField()) {
			setManualMapPrimaryKeyOnField(getRelatedEtlConf().getManualMapPrimaryKeyOnField());
		}
		
		super.fullLoad(conn);
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		
		this.tryToLoadParentRefInfo(conn);
		
		this.tryToLoadAuxExtraJoinTable(conn);
		
		this.tryToLoadExtraDatasource(conn);
		
		this.loadEtlFields();
		
		this.setFullLoaded(true);
	}
	
	private void loadEtlFields() {
		if (hasExtraDataSource() || hasAuxExtractTable()) {
			this.setEtlFields(new ArrayList<>());
			
			this.loadOwnFieldsToEtlFields(this.getEtlFields(), false);
			
			for (EtlDataSource ds : this.getAvaliableExtraDataSource()) {
				ds.loadOwnFieldsToEtlFields(this.getEtlFields(), false);
			}
		} else {
			this.setEtlFields(new ArrayList<>());
			
			//Preserve the original names if there is only the main ds
			this.loadOwnFieldsToEtlFields(this.getEtlFields(), true);
		}
	}
	
	/**
	 * @param conn
	 * @param srcConn
	 * @throws DBException
	 */
	private void tryToLoadExtraDatasource(Connection conn) throws DBException {
		OpenConnection srcConn = this.getRelatedConnInfo().openConnection();
		
		try {
			
			if (hasExtraTableDataSourceConfig()) {
				for (TableDataSourceConfig t : this.getExtraTableDataSource()) {
					
					TableConfiguration fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getFullTableName());
					
					t.tryToGenerateTableAlias(getRelatedEtlConf());
					
					if (fullLoadedTab != null) {
						t.clone(fullLoadedTab, null, conn);
					} else {
						t.fullLoad(srcConn);
					}
					
					t.setRelatedSrcConf(this);
					
					if (t.useSharedPKKey()) {
						t.getSharedKeyRefInfo().tryToGenerateTableAlias(getRelatedEtlConf());
						
						fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getSharedKeyRefInfo().getFullTableName());
						
						if (fullLoadedTab != null) {
							t.getSharedKeyRefInfo().clone(fullLoadedTab, null, conn);
						} else {
							t.getSharedKeyRefInfo().fullLoad();
						}
					}
				}
			}
			
			if (hasExtraQueryDataSourceConfig()) {
				for (QueryDataSourceConfig query : this.getExtraQueryDataSource()) {
					query.setRelatedSrcConf(this);
					query.fullLoad(srcConn);
				}
			}
			
			if (hasExtraObjectDataSourceConfig()) {
				for (ObjectDataSource query : this.getExtraObjectDataSource()) {
					query.setRelatedSrcConf(this);
					query.fullLoad(srcConn);
				}
			}
		}
		finally {
			srcConn.finalizeConnection();
		}
	}
	
	/**
	 * @param conn
	 * @throws DBException
	 */
	private void tryToLoadParentRefInfo(Connection conn) throws DBException {
		if (this.hasParentRefInfo()) {
			for (ParentTable ref : this.getParentRefInfo()) {
				TableConfiguration fullLoadedTab = findFullConfiguredConfInAllRelatedTable(ref.getFullTableName());
				
				ref.tryToGenerateTableAlias(getRelatedEtlConf());
				
				if (fullLoadedTab != null) {
					ref.clone(fullLoadedTab, null, conn);
				} else {
					ref.fullLoad();
				}
				
				if (ref.useSharedPKKey()) {
					fullLoadedTab = findFullConfiguredConfInAllRelatedTable(ref.getSharedKeyRefInfo().getFullTableName());
					
					if (!ref.getSharedKeyRefInfo().hasAlias()) {
						ref.getSharedKeyRefInfo().tryToGenerateTableAlias(getRelatedEtlConf());
					}
					if (fullLoadedTab != null) {
						ref.getSharedKeyRefInfo().clone(fullLoadedTab, null, conn);
					} else {
						ref.getSharedKeyRefInfo().fullLoad();
					}
				}
				
			}
		}
	}
	
	public void setFullLoaded(boolean fullLoaded) {
		this.fullLoaded = fullLoaded;
	}
	
	public QueryDataSourceConfig findAdditionalDataSrc(String dsName) {
		if (!hasExtraQueryDataSourceConfig()) {
			return null;
		}
		
		for (QueryDataSourceConfig src : this.getExtraQueryDataSource()) {
			if (src.getName().equals(dsName)) {
				return src;
			}
		}
		
		throw new ForbiddenOperationException("The table '" + dsName + "'cannot be foud on the mapping src tables");
	}
	
	public boolean isFullLoaded() {
		return this.fullLoaded;
	}
	
	public static SrcConf fastCreate(AbstractTableConfiguration tableConfig, Connection conn) throws DBException {
		SrcConf src = new SrcConf();
		
		src.copyFromOther(src, null, conn);
		
		return src;
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration parent) {
		/*if (!(parent instanceof EtlItemConfiguration))
			throw new ForbiddenOperationException("Only 'EtlItemConfiguration' is allowed to be a parent of an SrcConf");
		*/
		super.setParentConf(parent);
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return getSrcConnInfo();
	}
	
	@Override
	public void tryToDiscoverySharedKeyInfo(Connection conn) throws DBException {
		super.tryToDiscoverySharedKeyInfo(conn);
		
		if (useSharedPKKey()) {
			
			ParentTable shrd = this.getSharedKeyRefInfo();
			
			//Parse the shared parent to data source
			ParentAsSrcDataSource sharedAsSrcConf = ParentAsSrcDataSource.generateFromSrcConfSharedPkParent(this, shrd,
			    conn);
			
			utilities.updateOnArray(this.getParentRefInfo(), shrd, sharedAsSrcConf);
		}
	}
	
	@JsonIgnore
	public List<EtlAdditionalDataSource> getAvaliableExtraDataSource() {
		List<EtlAdditionalDataSource> ds = new ArrayList<>();
		
		if (useSharedPKKey() && isFullLoaded()) {
			ds.add((EtlAdditionalDataSource) getSharedKeyRefInfo());
		}
		
		if (hasExtraTableDataSourceConfig()) {
			ds.addAll(this.getExtraTableDataSource());
		}
		
		if (hasExtraQueryDataSourceConfig()) {
			ds.addAll(this.getExtraQueryDataSource());
		}
		
		if (hasExtraObjectDataSourceConfig()) {
			ds.addAll(this.getExtraObjectDataSource());
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
		
		if (hasExtraTableDataSourceConfig()) {
			
			for (EtlAdditionalDataSource ds : this.getExtraTableDataSource()) {
				for (Field f : ds.getFields()) {
					
					if (!fields.contains(f)) {
						fields.add(f);
					}
				}
			}
		}
		
		return fields;
	}
	
	public boolean hasExtraTableDataSourceConfig() {
		return utilities.arrayHasElement(this.getExtraTableDataSource());
	}
	
	public boolean hasExtraQueryDataSourceConfig() {
		return utilities.arrayHasElement(this.getExtraQueryDataSource());
		
	}
	
	public boolean hasExtraObjectDataSourceConfig() {
		return utilities.arrayHasElement(this.getExtraObjectDataSource());
	}
	
	public boolean hasRequiredExtraDataSource() {
		if (hasExtraDataSource()) {
			return false;
		} else {
			for (EtlAdditionalDataSource ds : getAvaliableExtraDataSource()) {
				if (ds.isRequired()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean hasEtlFields() {
		return utilities.arrayHasElement(this.getEtlFields());
	}
	
	public EtlField getEtlField(String fieldName, boolean deepCheck) {
		if (this.hasEtlFields()) {
			for (EtlField f : this.getEtlFields()) {
				
				if (f.getName().equals(fieldName)) {
					return f;
				}
				
				if (f.hasSrcField()) {
					if (f.getSrcField().getName().equals(fieldName)) {
						return f;
					}
				}
			}
		}
		
		if (deepCheck) {
			
			List<EtlDataSource> allDs = new ArrayList<>();
			
			allDs.add(this);
			
			if (this.hasExtraDataSource()) {
				allDs.addAll(this.getAvaliableExtraDataSource());
			}
			
			if (this.hasExtraDataSource()) {
				for (EtlDataSource ds : allDs) {
					EtlField f = this.getEtlField(EtlField.fastCreate(fieldName, ds, false).getName(), false);
					
					if (f != null)
						return f;
				}
			}
		}
		
		return null;
	}
	
	public boolean hasExtraDataSource() {
		return utilities.arrayHasElement(getAvaliableExtraDataSource());
	}
	
	public boolean isComplex() {
		return hasRequiredExtraDataSource();
	}
	
	@Override
	public boolean isJoinable() {
		return false;
	}
	
	@Override
	public JoinableEntity parseToJoinable() throws ForbiddenOperationException {
		throw new ForbiddenOperationException("Not joinable entity!!!");
	}
	
	public void copyFromOther(SrcConf toCloneFrom, EtlDatabaseObject schemaInfoSrc, Connection conn) throws DBException {
		super.clone(toCloneFrom, schemaInfoSrc, conn);
		
		if (utilities.arrayHasElement(toCloneFrom.getAuxExtractTable())) {
			this.setAuxExtractTable(AuxExtractTable.cloneAll(toCloneFrom.getAuxExtractTable(), this, schemaInfoSrc, conn));
		}
		
		if (toCloneFrom.hasExtraTableDataSourceConfig()) {
			this.setExtraTableDataSource(
			    TableDataSourceConfig.cloneAll(toCloneFrom.getExtraTableDataSource(), this, schemaInfoSrc, conn));
		}
		
		if (toCloneFrom.hasExtraQueryDataSourceConfig()) {
			this.setExtraQueryDataSource(QueryDataSourceConfig.cloneAll(toCloneFrom.getExtraQueryDataSource(), this, conn));
		}
		
		if (toCloneFrom.hasExtraObjectDataSourceConfig()) {
			this.setExtraObjectDataSource(ObjectDataSource.cloneAll(toCloneFrom.getExtraObjectDataSource(), this, conn));
		}
		
		this.setDstType(toCloneFrom.getDstType());
		
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
		
		if (this.hasExtraTableDataSourceConfig()) {
			for (TableDataSourceConfig tab : this.getExtraTableDataSource()) {
				tab.loadSchemaInfo(schemaInfoSrc, conn);
			}
		}
		
		if (this.hasExtraQueryDataSourceConfig()) {
			for (QueryDataSourceConfig q : this.getExtraQueryDataSource()) {
				q.tryToFillParams(schemaInfoSrc);
			}
		}
		
	}
	
}
