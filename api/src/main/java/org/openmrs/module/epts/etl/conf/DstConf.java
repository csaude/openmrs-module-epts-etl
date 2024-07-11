package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.types.EtlDstType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.exceptions.FieldAvaliableInMultipleDataSources;
import org.openmrs.module.epts.etl.exceptions.FieldNotAvaliableInAnyDataSource;
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
	
	public DstConf() {
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
	
	private void addMapping(FieldsMapping fm) throws ForbiddenOperationException {
		if (this.allMapping == null) {
			this.allMapping = new ArrayList<FieldsMapping>();
		}
		
		if (this.allMapping.contains(fm))
			throw new ForbiddenOperationException("The field [" + fm + "] already exists on mapping");
		
		this.allMapping.add(fm);
	}
	
	private EtlDataSource findDataSource(String dsName) {
		for (EtlDataSource ds : this.allAvaliableDataSource) {
			if (ds.getName().equals(dsName)) {
				return ds;
			}
		}
		
		return null;
	}
	
	public void generateAllFieldsMapping(Connection conn) throws DBException {
		if (hasTransformer()) {
			return;
		}
		
		this.allMapping = new ArrayList<>();
		
		List<String> avaliableInMultiDataSources = new ArrayList<>();
		List<String> notAvaliableInSpecifiedDataSource = new ArrayList<>();
		List<String> notAvaliableInAnyDataSource = new ArrayList<>();
		
		if (utilities.arrayHasElement(this.mapping)) {
			for (FieldsMapping fm : this.mapping) {
				
				if (!utilities.stringHasValue(fm.getDataSourceName())) {
					try {
						tryToLoadDataSourceToFieldMapping(fm);
						
						addMapping(fm);
						
					}
					catch (FieldNotAvaliableInAnyDataSource e) {
						Field f = getField(fm.getDstField());
						
						boolean problem = !f.getAttDefinedElements().isPartOfObjectId();
						
						problem = problem ? problem : !this.useAutoIncrementId(conn);
						
						if (problem) {
							notAvaliableInAnyDataSource.add(fm.getSrcField());
						}
					}
					catch (FieldAvaliableInMultipleDataSources e) {
						avaliableInMultiDataSources.add(fm.getSrcField());
					}
					
				} else {
					EtlDataSource ds = findDataSource(fm.getDataSourceName());
					
					if (ds == null) {
						throw new NoSuchElementException("The DataSource '" + fm.getDataSourceName() + "' cannot be found!");
					}
					
					if (ds.containsField(fm.getSrcField())) {
						fm.setDataSourceName(ds.getAlias());
					} else {
						notAvaliableInSpecifiedDataSource.add(fm.getSrcField());
					}
					
					addMapping(fm);
				}
				
			}
		}
		
		List<Field> myFields = this.getFields();
		
		for (Field field : myFields) {
			
			if (isIgnorableField(field)) {
				continue;
			}
			
			FieldsMapping fm = FieldsMapping.fastCreate(field.getName(), field.getName());
			
			if (!this.allMapping.contains(fm)) {
				try {
					tryToLoadDataSourceToFieldMapping(fm);
					
					addMapping(fm);
				}
				catch (FieldNotAvaliableInAnyDataSource e) {
					
					Field f = getField(fm.getDstField());
					
					boolean problem = !f.getAttDefinedElements().isPartOfObjectId();
					
					problem = problem ? problem : !this.useAutoIncrementId(conn);
					
					if (problem) {
						notAvaliableInAnyDataSource.add(fm.getSrcField());
					}
					
				}
				catch (FieldAvaliableInMultipleDataSources e) {
					avaliableInMultiDataSources.add(fm.getSrcField());
				}
			}
			
		}
		
		if (!avaliableInMultiDataSources.isEmpty()) {
			throw new ForbiddenOperationException(getTableName() + " The destination fields "
			        + avaliableInMultiDataSources.toString()
			        + " cannot be automatically mapped as them occurrs in multiple src. Please configure them manually or specify the datasource order preference in prefferredDataSource array ");
		}
		
		if (!notAvaliableInAnyDataSource.isEmpty()) {
			throw new ForbiddenOperationException(getTableName() + " The destination fields "
			        + notAvaliableInAnyDataSource.toString()
			        + " cannot be automatically mapped as them do not occurr in any src. Please configure them manually!");
		}
		
		if (!notAvaliableInSpecifiedDataSource.isEmpty()) {
			throw new ForbiddenOperationException(getTableName() + " The source fields for destination fields ["
			        + notAvaliableInSpecifiedDataSource.toString() + "] do not occurs in specified data sources !");
		}
	}
	
	private void tryToLoadDataSourceToFieldMapping(FieldsMapping fm)
	        throws FieldNotAvaliableInAnyDataSource, FieldAvaliableInMultipleDataSources {
		int qtyOccurences = 0;
		
		if (fm.getSrcValue() != null) {
			
			if (fm.getSrcValue().startsWith("@")) {
				String paramName = utilities.removeCharactersOnString(fm.getSrcValue(), "@");
				
				fm.setSrcValue(getRelatedEtlConf().getParamValue(paramName));
			} else if (fm.getSrcValue().isEmpty() || fm.getSrcValue().equals("null")) {
				fm.setMapToNullValue(true);
			}
			
			return;
			
		}
		
		if (fm.hasTransformer()) {
			fm.tryToLoadTransformer();
			
			return;
		}
		
		for (EtlDataSource pref : this.allPrefferredDataSource) {
			if (pref.containsField(fm.getSrcField())) {
				fm.setDataSourceName(pref.getAlias());
				
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
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		this.tryToGenerateTableAlias(getRelatedEtlConf());
		
		if (!hasManualMapPrimaryKeyOnField()) {
			setManualMapPrimaryKeyOnField(getRelatedEtlConf().getManualMapPrimaryKeyOnField());
		}
		
		super.fullLoad(conn);
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
		
		this.allAvaliableDataSource = new ArrayList<>();
		this.allAvaliableDataSource.add(getSrcConf());
		
		if (utilities.arrayHasElement(getSrcConf().getAvaliableExtraDataSource())) {
			allAvaliableDataSource
			        .addAll(utilities.parseList(getSrcConf().getAvaliableExtraDataSource(), EtlDataSource.class));
		}
		
		this.fullLoadAllRelatedTables(getRelatedEtlConf(), null, conn);
		
		determinePrefferredDataSources();
		
		tryToLoadTransformer(conn);
	}
	
	@SuppressWarnings("unchecked")
	private void tryToLoadTransformer(Connection conn) {
		if (this.hasTransformer()) {
			
			try {
				ClassLoader loader = EtlRecordTransformer.class.getClassLoader();
				
				Class<? extends EtlRecordTransformer> transformerClazz = (Class<? extends EtlRecordTransformer>) loader
				        .loadClass(this.getTransformer());
				
				this.transformerInstance = transformerClazz.newInstance();
			}
			catch (Exception e) {
				throw new ForbiddenOperationException(
				        "Error loading transformer class [" + this.getTransformer() + "]!!! " + e.getLocalizedMessage());
			}
			
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
	
	public EtlDatabaseObject transform(EtlDatabaseObject srcRec, Connection srcConn,
	        TaskProcessor<EtlDatabaseObject> processor) throws DBException, ForbiddenOperationException {
		try {
			processor.logTrace("Transforming dstRecord " + srcRec);
			
			DBConnectionInfo srcAppInfo = processor.getRelatedEtlConfiguration().getSrcConnInfo();
			DBConnectionInfo dstAppInfo = processor.getRelatedEtlConfiguration().getDstConnInfo();
			
			if (hasTransformer()) {
				return getTransformerInstance().transform(srcRec, this, srcConn, srcConn);
			}
			
			List<EtlDatabaseObject> srcObjects = new ArrayList<>();
			
			srcObjects.add(srcRec);
			
			if (srcRec.shasSharedPkObj()) {
				srcObjects.add(srcRec.getSharedPkObj());
			}
			
			for (EtlAdditionalDataSource mappingInfo : this.getSrcConf().getAvaliableExtraDataSource()) {
				EtlDatabaseObject relatedSrcObject = mappingInfo.loadRelatedSrcObject(srcRec, srcConn, srcAppInfo);
				
				if (relatedSrcObject == null) {
					
					if (mappingInfo.isRequired()) {
						return null;
					} else {
						relatedSrcObject = mappingInfo.getSyncRecordClass(srcAppInfo).newInstance();
						relatedSrcObject.setRelatedConfiguration(mappingInfo);
					}
				}
				
				srcObjects.add(relatedSrcObject);
				
			}
			
			EtlDatabaseObject transformedRec = this.getSyncRecordClass(dstAppInfo).newInstance();
			
			transformedRec.setRelatedConfiguration(this);
			transformedRec.setSrcRelatedObject(srcRec);
			
			for (FieldsMapping fieldsMapping : this.allMapping) {
				Object srcValue;
				
				if (fieldsMapping.isMapToNullValue()) {
					srcValue = null;
				} else if (fieldsMapping.getSrcValue() != null) {
					srcValue = fieldsMapping.getSrcValue();
				} else {
					srcValue = fieldsMapping.retrieveValue(transformedRec, srcObjects, dstAppInfo, srcConn);
				}
				
				transformedRec.setFieldValue(fieldsMapping.getDestFieldAsClassField(), srcValue);
			}
			
			if (this.useSharedPKKey()) {
				List<SrcConf> srcForSharedPk = this.getSharedKeyRefInfo().findRelatedSrcConfWhichAsAtLeastOnematchingDst();
				
				if (utilities.arrayHasNoElement(srcForSharedPk)) {
					throw new ForbiddenOperationException(
					        "There are relashioship which cannot auto resolved as there is no configured etl for "
					                + this.getSharedKeyRefInfo().getTableName() + " as source and destination!");
				}
				
				EtlDatabaseObject dstParent = null;
				
				for (SrcConf src : srcForSharedPk) {
					DstConf dst = src.getParentConf().findDstTable(this.getSharedKeyRefInfo().getTableName());
					
					EtlDatabaseObject recordAsSrc = src.createRecordInstance();
					recordAsSrc.setRelatedConfiguration(src);
					
					recordAsSrc.copyFrom(srcRec.getSharedPkObj());
					dstParent = dst.transform(recordAsSrc, srcConn, processor);
					
					if (dstParent != null) {
						
						dstParent.setSrcRelatedObject(srcRec.getSharedPkObj());
						break;
					}
				}
				
				if (dstParent == null) {
					throw new ForbiddenOperationException(
					        "The related shared pk object for record " + srcRec + " cannot be transformed");
				} else {
					transformedRec.setSharedPkObj(dstParent);
				}
			}
			
			transformedRec.loadObjectIdData(this);
			
			if (this.useManualGeneratedObjectId()) {
				transformedRec.getObjectId().asSimpleKey().setValue(retriveNextRecordId(processor));
			}
			
			processor.logTrace("Record " + srcRec + " transformed to " + transformedRec);
			
			return transformedRec;
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private long retriveNextRecordId(TaskProcessor<EtlDatabaseObject> processor) {
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
	
}
