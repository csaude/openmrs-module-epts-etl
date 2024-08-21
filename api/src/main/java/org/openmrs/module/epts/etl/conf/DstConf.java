package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.types.EtlDstType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultRecordTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.FieldAvaliableInMultipleDataSources;
import org.openmrs.module.epts.etl.exceptions.FieldNotAvaliableInAnyDataSource;
import org.openmrs.module.epts.etl.exceptions.FieldsMappingException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DstConf extends AbstractTableConfiguration {
	
	/*
	 * The user defined joinFields with #getSrcConf()
	 */
	private List<FieldsMapping> joinFields;
	
	private List<List<FieldsMapping>> generatedJoinFields;
	
	private List<FieldsMapping> allMapping;
	
	private List<FieldsMapping> mapping;
	
	private DBConnectionInfo relatedConnInfo;
	
	private static final int DEFAULT_NEXT_TREAD_ID = -1;
	
	private int currThreadStartId;
	
	private int currQtyRecords;
	
	private final String stringLock = new String("LOCK_STRING");
	
	private List<String> prefferredDataSource;
	
	private List<EtlDataSource> allAvaliableDataSource;
	
	private List<EtlDataSource> allNotPrefferredDataSource;
	
	private List<EtlDataSource> allPrefferredDataSource;
	
	private boolean ignoreUnmappedFields;
	
	private boolean automaticalyGenerated;
	
	private EtlDstType dstType;
	
	private String transformer;
	
	private EtlRecordTransformer transformerInstance;
	
	private boolean inMemoryTable;
	
	/**
	 * If true, when the table does not exists, it will be created with all fields from datasource +
	 * the fields defined on mapping
	 */
	private boolean includeAllFieldsFromDataSource;
	
	public DstConf() {
	}
	
	public DstConf(String tableName) {
		setTableName(tableName);
	}
	
	public boolean isIncludeAllFieldsFromDataSource() {
		return includeAllFieldsFromDataSource;
	}
	
	public void setIncludeAllFieldsFromDataSource(boolean includeAllFieldsFromDataSource) {
		this.includeAllFieldsFromDataSource = includeAllFieldsFromDataSource;
	}
	
	public boolean includeAllFieldsFromDataSource() {
		return isIncludeAllFieldsFromDataSource();
	}
	
	public boolean isInMemoryTable() {
		return inMemoryTable;
	}
	
	public void setInMemoryTable(boolean inMemoryTable) {
		this.inMemoryTable = inMemoryTable;
	}
	
	public EtlRecordTransformer getTransformerInstance() {
		return transformerInstance;
	}
	
	public void setTransformerInstance(EtlRecordTransformer transformerInstance) {
		this.transformerInstance = transformerInstance;
	}
	
	public String getTransformer() {
		return transformer;
	}
	
	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}
	
	public boolean hasTransformer() {
		return getTransformer() != null;
	}
	
	public EtlDstType getDstType() {
		return dstType;
	}
	
	public void setDstType(EtlDstType dstType) {
		this.dstType = dstType;
	}
	
	public List<List<FieldsMapping>> getGeneratedJoinFields() {
		return generatedJoinFields;
	}
	
	public void setGeneratedJoinFields(List<List<FieldsMapping>> generatedJoinFields) {
		this.generatedJoinFields = generatedJoinFields;
	}
	
	public boolean isAutomaticalyGenerated() {
		return automaticalyGenerated;
	}
	
	public void setAutomaticalyGenerated(boolean automaticalyGenerated) {
		this.automaticalyGenerated = automaticalyGenerated;
	}
	
	public boolean isIgnoreUnmappedFields() {
		return ignoreUnmappedFields;
	}
	
	public void setIgnoreUnmappedFields(boolean ignoreUnmappedFields) {
		this.ignoreUnmappedFields = ignoreUnmappedFields;
	}
	
	public List<String> getPrefferredDataSource() {
		return prefferredDataSource;
	}
	
	public void setPrefferredDataSource(List<String> prefferredDataSource) {
		this.prefferredDataSource = prefferredDataSource;
	}
	
	public List<FieldsMapping> getJoinFields() {
		return joinFields;
	}
	
	public void setJoinFields(List<FieldsMapping> joinFields) {
		this.joinFields = joinFields;
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	public List<FieldsMapping> getMapping() {
		return mapping;
	}
	
	public void setMapping(List<FieldsMapping> manualFieldsMapping) {
		this.mapping = manualFieldsMapping;
	}
	
	public List<FieldsMapping> getAllMapping() {
		return allMapping;
	}
	
	private void setAllMapping(List<FieldsMapping> allMapping) {
		this.allMapping = allMapping;
	}
	
	private void addMapping(FieldsMapping fm) throws ForbiddenOperationException {
		if (this.getAllMapping() == null) {
			this.setAllMapping(new ArrayList<FieldsMapping>());
		}
		
		if (this.getAllMapping().contains(fm))
			throw new ForbiddenOperationException("The field [" + fm + "] already exists on mapping");
		
		this.getAllMapping().add(fm);
	}
	
	private EtlDataSource findDataSource(String dsName) {
		for (EtlDataSource ds : this.allAvaliableDataSource) {
			if (ds.getName().equals(dsName)) {
				return ds;
			}
		}
		
		return null;
	}
	
	public boolean useDefaultTransformer() {
		return getTransformerInstance() instanceof DefaultRecordTransformer;
	}
	
	private FieldsMappingIssues loadConfiguredMappingAdditionalInfo(Connection conn) throws DBException {
		FieldsMappingIssues mappingProblem = null;
		
		if (utilities.arrayHasElement(this.getMapping())) {
			mappingProblem = new FieldsMappingIssues();
			
			for (FieldsMapping fm : this.getMapping()) {
				
				if (!utilities.stringHasValue(fm.getDstField())) {
					throw new ForbiddenOperationException(
					        "One or more mapping on dstTable '" + this.getTableName() + "' on Etl Configuration '"
					                + this.getParentConf().getConfigCode() + "' configuration does not have dstField");
				}
				
				fm.tryToLoadTransformer();
				
				fm.tryToLoadDataSourceInfoFromSrcField();
				
				if (!fm.hasDataSourceName()) {
					try {
						tryToLoadDataSourceToFieldMapping(fm);
					}
					catch (FieldNotAvaliableInAnyDataSource e) {
						Field f = getField(fm.getDstField());
						
						boolean problem = !f.getAttDefinedElements().isPartOfObjectId();
						
						problem = problem ? problem : !this.useAutoIncrementId(conn);
						
						if (problem) {
							mappingProblem.getNotAvaliableInAnyDataSource().add(fm);
						}
					}
					catch (FieldAvaliableInMultipleDataSources e) {
						mappingProblem.getAvaliableInMultiDataSources().add(fm);
					}
					
				} else {
					EtlDataSource ds = findDataSource(fm.getDataSourceName());
					
					if (ds == null) {
						throw new NoSuchElementException("The DataSource '" + fm.getDataSourceName() + "' cannot be found!");
					}
					
					if (ds.containsField(fm.getSrcField())) {
						fm.setDataSourceName(ds.getAlias());
					} else {
						mappingProblem.getNotAvaliableInSpecifiedDataSource().add(fm);
					}
				}
				
			}
		}
		
		return mappingProblem;
	}
	
	public void generateAllFieldsMapping(Connection conn) throws DBException, FieldsMappingException {
		if (!useDefaultTransformer()) {
			return;
		}
		
		this.allMapping = new ArrayList<>();
		
		FieldsMappingIssues mappingProblem = null;
		
		if (this.hasMapping()) {
			mappingProblem = loadConfiguredMappingAdditionalInfo(conn);
			
			for (FieldsMapping fm : this.getMapping()) {
				if (!mappingProblem.contains(fm)) {
					addMapping(fm);
				}
			}
		}
		
		if (mappingProblem == null) {
			mappingProblem = new FieldsMappingIssues();
		}
		
		List<Field> myFields = this.getFields();
		
		for (Field field : myFields) {
			
			if (isIgnorableField(field)) {
				continue;
			}
			
			FieldsMapping fm = null;
			
			EtlField etlField = this.getSrcConf().getEtlField(field.getName(), true);
			
			if (etlField != null) {
				fm = FieldsMapping.fastCreate(etlField.getSrcField().getName(), field.getName());
				fm.setDataSourceName(etlField.getSrcDataSource().getName());
			} else {
				fm = FieldsMapping.fastCreate(field.getName(), field.getName());
			}
			
			if (!this.getAllMapping().contains(fm)) {
				try {
					
					fm.tryToLoadTransformer();
					
					tryToLoadDataSourceToFieldMapping(fm);
					
					addMapping(fm);
				}
				catch (FieldNotAvaliableInAnyDataSource e) {
					
					Field f = getField(fm.getDstField());
					
					boolean problem = !f.getAttDefinedElements().isPartOfObjectId();
					
					problem = problem ? problem : !this.useAutoIncrementId(conn);
					
					if (problem) {
						mappingProblem.getNotAvaliableInAnyDataSource().add(fm);
					}
					
				}
				catch (FieldAvaliableInMultipleDataSources e) {
					mappingProblem.getAvaliableInMultiDataSources().add(fm);
				}
			}
		}
		
		if (mappingProblem.hasIssue()) {
			throw new FieldsMappingException(this.getTableName(), mappingProblem);
		}
		
	}
	
	private void tryToLoadDataSourceToFieldMapping(FieldsMapping fm)
	        throws FieldNotAvaliableInAnyDataSource, FieldAvaliableInMultipleDataSources {
		int qtyOccurences = 0;
		
		if (fm.getSrcValue() != null || fm.isMapToNullValue()) {
			return;
		}
		
		if (!fm.useDefaultTransformer()) {
			
			fm.loadType(this, null);
			
			return;
		}
		
		for (EtlDataSource pref : this.allPrefferredDataSource) {
			if (pref.containsField(fm.getSrcField())) {
				fm.setDataSourceName(pref.getAlias());
				
				fm.loadType(this, pref);
				
				qtyOccurences++;
				
				break;
			}
		}
		
		if (qtyOccurences == 0) {
			for (EtlDataSource notPref : this.allNotPrefferredDataSource) {
				if (notPref.containsField(fm.getSrcField())) {
					qtyOccurences++;
					
					if (qtyOccurences > 1) {
						break;
					} else {
						fm.setDataSourceName(notPref.getAlias());
						
						fm.loadType(this, notPref);
					}
				}
			}
		}
		
		if (qtyOccurences == 0 && !isIgnoreUnmappedFields()) {
			throw new FieldNotAvaliableInAnyDataSource(fm.getSrcField());
		}
		
		if (qtyOccurences > 1) {
			throw new FieldAvaliableInMultipleDataSources(fm.getSrcField());
		}
		
	}
	
	public String getMappedField(String srcField) {
		List<FieldsMapping> machedFields = new ArrayList<FieldsMapping>();
		
		for (FieldsMapping field : this.allMapping) {
			if (field.getSrcField().equals(srcField)) {
				machedFields.add(field);
				
				if (machedFields.size() > 1) {
					throw new ForbiddenOperationException("Cannot determine the mapping field for '" + srcField
					        + "' since it has multiple matching fields");
				}
			}
		}
		
		if (machedFields.isEmpty()) {
			throw new ForbiddenOperationException("Cannot determine the mapping field for '" + srcField + "'");
		}
		
		return machedFields.get(0).getDstField();
	}
	
	public DBConnectionInfo getRelatedConnInfo() {
		return relatedConnInfo;
	}
	
	public void setRelatedConnInfo(DBConnectionInfo relatedConnInfo) {
		this.relatedConnInfo = relatedConnInfo;
	}
	
	@Override
	public synchronized void fullLoad() throws DBException {
		OpenConnection conn = this.relatedConnInfo.openConnection();
		
		try {
			this.fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private List<EtlDataSource> avaliableDataSources() {
		List<EtlDataSource> dataSource = new ArrayList<>();
		
		dataSource.add(this.getSrcConf());
		
		if (this.getSrcConf().hasExtraDataSource()) {
			dataSource.addAll(this.getSrcConf().getAvaliableExtraDataSource());
		}
		
		return dataSource;
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		
		if (isInMemoryTable()) {
			try {
				this.setFieldsLoaded(true);
				
				loadDataSourceInfo(conn);
				
				if (this.hasMapping()) {
					
					FieldsMappingIssues mappingProblem = loadConfiguredMappingAdditionalInfo(conn);
					
					if (mappingProblem.hasIssue()) {
						throw new FieldsMappingException(this.getTableName(), mappingProblem);
					}
					
					this.setFields(FieldsMapping.parseAllToField(this.getMapping(), this, avaliableDataSources()));
					
					this.setAllMapping(this.getMapping());
					
					if (this.includeAllFieldsFromDataSource()) {
						for (EtlField field : this.getSrcConf().getEtlFields()) {
							if (!this.containsField(field.getName())) {
								this.getFields().add(field);
								this.addMapping(FieldsMapping.converteFromEtlField(field));
							}
						}
					}
				} else {
					this.setFields(EtlField.convertToSimpleFiled(this.getSrcConf().getEtlFields()));
				}
				
				if (this.getSrcConf().hasPK()) {
					if (this.containsAllFields(
					    utilities.parseList(this.getSrcConf().getPrimaryKey().getFields(), Field.class))) {
						this.setPrimaryKey((PrimaryKey) this.getSrcConf().getPrimaryKey().clone());
					}
				}
				
				this.setPrimaryKeyInfoLoaded(true);
				
				if (this.getSrcConf().hasUniqueKeys()) {
					for (UniqueKeyInfo uk : this.getSrcConf().getUniqueKeys()) {
						if (this.containsAllFields(utilities.parseList(uk.getFields(), Field.class))) {
							this.addUniqueKey(uk);
						}
					}
				}
				
				this.setUniqueKeyInfoLoaded(true);
				
				tryToLoadTransformer(conn);
				
				this.setFullLoaded(true);
			}
			catch (CloneNotSupportedException e) {
				throw new EtlExceptionImpl(e);
			}
			
		} else {
			this.tryToGenerateTableAlias(getRelatedEtlConf());
			
			if (!hasManualMapPrimaryKeyOnField()) {
				setManualMapPrimaryKeyOnField(getRelatedEtlConf().getManualMapPrimaryKeyOnField());
			}
			
			super.fullLoad(conn);
		}
	}
	
	/**
	 * Find a parent which with same name in src
	 * 
	 * @param dstParent the parent to find the correspondent one in the src
	 * @return the related parent in src
	 * @throws ForbiddenOperationException if the @param dstParent is not a parent of this table or
	 *             if it is not in the src
	 */
	public ParentTable findCorrespondentSrcParentConf(ParentTable dstParent) throws ForbiddenOperationException {
		List<ParentTable> parents = findAllRefToParent(dstParent.getTableName());
		
		if (!utilities.arrayHasElement(parents)) {
			throw new ForbiddenOperationException(
			        "The table " + dstParent.getTableName() + " is not parent of " + this.getTableName());
		}
		
		parents = getSrcConf().findAllRefToParent(dstParent.getTableName());
		
		if (!utilities.arrayHasElement(parents)) {
			throw new ForbiddenOperationException(
			        "The table " + dstParent.getTableName() + " is not parent of " + this.getTableName() + " in the src");
		}
		
		return parents.get(0);
	}
	
	@Override
	public void loadOwnElements(Connection conn) throws DBException {
		loadJoinFields(conn);
		
		loadDataSourceInfo(conn);
		
		tryToLoadTransformer(conn);
	}
	
	private void loadDataSourceInfo(Connection conn) throws DBException {
		this.allAvaliableDataSource = new ArrayList<>();
		this.allAvaliableDataSource.add(getSrcConf());
		
		if (utilities.arrayHasElement(getSrcConf().getAvaliableExtraDataSource())) {
			allAvaliableDataSource
			        .addAll(utilities.parseList(getSrcConf().getAvaliableExtraDataSource(), EtlDataSource.class));
		}
		
		this.fullLoadAllRelatedTables(getRelatedEtlConf(), null, conn);
		
		determinePrefferredDataSources();
	}
	
	@SuppressWarnings("unchecked")
	private void tryToLoadTransformer(Connection conn) {
		if (this.hasTransformer()) {
			
			try {
				ClassLoader loader = EtlRecordTransformer.class.getClassLoader();
				
				Class<? extends EtlRecordTransformer> transformerClazz = (Class<? extends EtlRecordTransformer>) loader
				        .loadClass(this.getTransformer());
				
				this.setTransformerInstance(transformerClazz.newInstance());
			}
			catch (Exception e) {
				throw new ForbiddenOperationException(
				        "Error loading transformer class [" + this.getTransformer() + "]!!! " + e.getLocalizedMessage());
			}
			
		} else {
			this.setTransformer(DefaultRecordTransformer.class.getCanonicalName());
			
			this.setTransformerInstance(DefaultRecordTransformer.getInstance());
		}
	}
	
	private void determinePrefferredDataSources() {
		if (this.prefferredDataSource == null) {
			String prefferredDs = null;
			
			for (EtlDataSource tDs : this.allAvaliableDataSource) {
				if (tDs.getName().equals(this.getTableName())) {
					prefferredDs = this.getTableName();
					
					break;
				}
			}
			
			this.prefferredDataSource = new ArrayList<>();
			
			if (prefferredDs != null) {
				this.prefferredDataSource.add(prefferredDs);
				
				if (!prefferredDs.equals(getSrcConf().getTableName())) {
					this.prefferredDataSource.add(getSrcConf().getTableName());
				}
			} else {
				this.prefferredDataSource.add(getSrcConf().getTableName());
			}
		}
		
		this.allNotPrefferredDataSource = new ArrayList<>();
		this.allPrefferredDataSource = new ArrayList<>();
		
		for (String dsName : this.prefferredDataSource) {
			for (EtlDataSource ds : allAvaliableDataSource) {
				if (dsName.equals(ds.getName())) {
					allPrefferredDataSource.add(ds);
				} else {
					allNotPrefferredDataSource.add(ds);
				}
			}
		}
		
	}
	
	private void addToGeneratedJoinFields(List<FieldsMapping> toAdd) {
		if (!hasJoinFields()) {
			this.setGeneratedJoinFields(new ArrayList<>());
		}
		
		this.getGeneratedJoinFields().add(toAdd);
	}
	
	private void loadJoinFields(Connection conn) {
		if (this.getJoinFields() != null) {
			addToGeneratedJoinFields(getJoinFields());
		}
		
		if (this.hasUniqueKeys()) {
			//If no joinField is defined but the dst table has uk, tries to map the uk with src fields
			tryToAutoGenerateJoinFields(this, this.getSrcConf());
			
			if (!hasJoinFields() && this.getSrcConf().hasUniqueKeys()) {
				tryToAutoGenerateJoinFields(this.getSrcConf(), this);
			}
		} else if (this.getSrcConf().hasUniqueKeys()) {
			//If no joinField is defined but the src table has uk, tries to map the uk with dst fields
			tryToAutoGenerateJoinFields(this.getSrcConf(), this);
		}
	}
	
	/**
	 * @param uk
	 */
	public void tryToAutoGenerateJoinFields(AbstractTableConfiguration ukTable, AbstractTableConfiguration targetTable) {
		
		for (UniqueKeyInfo uk : ukTable.getUniqueKeys()) {
			
			UniqueKeyInfo fakeSrcUk = new UniqueKeyInfo(targetTable);
			
			for (Key key : uk.getFields()) {
				if (targetTable.containsField(key.getName())) {
					fakeSrcUk.addKey(key);
				} else if (useSharedPKKey() && getSharedKeyRefInfo().containsField(key.getName())) {
					fakeSrcUk.addKey(key);
				}
			}
			
			if (fakeSrcUk.hasFields() && uk.equals(fakeSrcUk)) {
				List<FieldsMapping> joinFieldsFromUniqueKey = new ArrayList<>();
				
				for (Key key : uk.getFields()) {
					joinFieldsFromUniqueKey.add(FieldsMapping.fastCreate(key.getName()));
				}
				
				addToGeneratedJoinFields(joinFieldsFromUniqueKey);
			}
		}
	}
	
	public SrcConf getSrcConf() {
		return this.getParentConf().getSrcConf();
	}
	
	public long retriveNextRecordId(TaskProcessor<EtlDatabaseObject> processor) {
		return processor.findIdGenerator(this).retriveNextIdForRecord();
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration parent) {
		super.setParentConf((EtlItemConfiguration) parent);
	}
	
	@Override
	public EtlItemConfiguration getParentConf() {
		return (EtlItemConfiguration) super.getParentConf();
	}
	
	public IdGeneratorManager initIdGenerator(TaskProcessor<? extends EtlDatabaseObject> processor,
	        List<? extends EtlObject> etlObjects, Connection conn) throws DBException, ForbiddenOperationException {
		
		return IdGeneratorManager.init(processor, this, etlObjects, conn);
		
	}
	
	public long determineNextStartId(IdGeneratorManager idGeneratorMgt, Connection conn)
	        throws DBException, ForbiddenOperationException {
		synchronized (stringLock) {
			if (this.currThreadStartId == DEFAULT_NEXT_TREAD_ID) {
				this.currQtyRecords = idGeneratorMgt.getEtlObjects().size();
				
				this.currThreadStartId = DatabaseObjectDAO.getLastRecord(this, conn);
				
				this.currThreadStartId = this.currThreadStartId - this.currQtyRecords + 1;
			}
			
			this.currThreadStartId += this.currQtyRecords;
			this.currQtyRecords = idGeneratorMgt.getEtlObjects().size();
			
			return this.currThreadStartId;
		}
	}
	
	/**
	 * Generates SQL join condition between this destination table and its src table using the
	 * {@link #joinField}
	 * 
	 * @param sourceTableAlias alias name for source table
	 * @param destinationTableAlias alias name for destination table
	 * @return the generated join condition based on {@link #joinField}
	 */
	@JsonIgnore
	public String generateJoinConditionWithSrc() {
		String fullCondition = "";
		
		String ownAlias = getTableAlias();
		String relatedTableAlias = getSrcConf().getTableAlias();
		
		for (int outerConter = 0; outerConter < this.getGeneratedJoinFields().size(); outerConter++) {
			List<FieldsMapping> joindFields = this.getGeneratedJoinFields().get(outerConter);
			
			String currJoinCondition = "";
			
			for (int innerCounter = 0; innerCounter < joindFields.size(); innerCounter++) {
				
				if (innerCounter > 0)
					currJoinCondition += " AND ";
				
				//Force the alias to be from the sharedPk table
				if (useSharedPKKey() && !containsField(joindFields.get(innerCounter).getDstField())) {
					ownAlias = getSharedKeyRefInfo().getAlias();
				}
				
				if (getSrcConf().useSharedPKKey()
				        && !getSrcConf().containsField(joindFields.get(innerCounter).getSrcField())) {
					relatedTableAlias = getSrcConf().getSharedKeyRefInfo().getAlias();
				}
				
				currJoinCondition += ownAlias + "." + joindFields.get(innerCounter).getDstField() + " = " + relatedTableAlias
				        + "." + joindFields.get(innerCounter).getSrcField();
			}
			
			if (outerConter > 0) {
				fullCondition = "(" + fullCondition + ") OR (" + currJoinCondition + ")";
			} else {
				fullCondition = "(" + currJoinCondition + ")";
			}
			
		}
		
		if (!utilities.stringHasValue(fullCondition) && this.isMetadata()) {
			fullCondition = getTableAlias() + "." + getPrimaryKey().retrieveSimpleKeyColumnName() + " = "
			        + getSrcConf().getTableAlias() + "." + getPrimaryKey().retrieveSimpleKeyColumnName();
		}
		
		return fullCondition;
	}
	
	public boolean hasJoinFields() {
		return utilities.arrayHasElement(this.getGeneratedJoinFields());
	}
	
	public static List<DstConf> generateDefaultDstConf(final EtlItemConfiguration itemConf) throws DBException {
		
		synchronized (itemConf) {
			
			OpenConnection dstConn;
			
			if (!itemConf.hasDstConf()) {
				
				dstConn = itemConf.getRelatedEtlConf().openDstConn();
				
				try {
					DstConf map = new DstConf();
					
					map.setTableName(itemConf.getSrcConf().getTableName());
					map.setSchema(dstConn.getSchema());
					map.setDstType(itemConf.getSrcConf().getDstType());
					
					map.setObservationDateFields(itemConf.getSrcConf().getObservationDateFields());
					map.setRemoveForbidden(itemConf.getSrcConf().isRemoveForbidden());
					map.setOnConflict(itemConf.getSrcConf().onConflict());
					map.setWinningRecordFieldsInfo(itemConf.getSrcConf().getWinningRecordFieldsInfo());
					map.setManualMapPrimaryKeyOnField(itemConf.getSrcConf().getManualMapPrimaryKeyOnField());
					
					map.setRelatedConnInfo(itemConf.getRelatedEtlConf().getDstConnInfo());
					map.setAutomaticalyGenerated(true);
					map.setRelatedEtlConfig(itemConf.getRelatedEtlConf());
					map.setParentConf(itemConf);
					map.setDstType(itemConf.getSrcConf().getDstType());
					
					return utilities.parseToList(map);
				}
				catch (SQLException e) {
					throw new DBException(e);
				}
			} else {
				return null;
			}
		}
	}
	
	public boolean hasMapping() {
		return utilities.arrayHasElement(this.getMapping());
	}
	
}
