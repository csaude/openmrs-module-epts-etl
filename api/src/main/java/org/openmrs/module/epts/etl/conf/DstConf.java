package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.FieldAvaliableInMultipleDataSources;
import org.openmrs.module.epts.etl.exceptions.FieldNotAvaliableInAnyDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DstConf extends AbstractTableConfiguration {
	
	private List<FieldsMapping> joinField;
	
	private List<FieldsMapping> allMapping;
	
	private List<FieldsMapping> mapping;
	
	private AppInfo relatedAppInfo;
	
	private static final int DEFAULT_NEXT_TREAD_ID = -1;
	
	private int currThreadStartId;
	
	private int currQtyRecords;
	
	private final String stringLock = new String("LOCK_STRING");
	
	private List<String> prefferredDataSource;
	
	private List<EtlDataSource> allAvaliableDataSource;
	
	private List<EtlDataSource> allNotPrefferredDataSource;
	
	private List<EtlDataSource> allPrefferredDataSource;
	
	public DstConf() {
	}
	
	public List<String> getPrefferredDataSource() {
		return prefferredDataSource;
	}
	
	public void setPrefferredDataSource(List<String> prefferredDataSource) {
		this.prefferredDataSource = prefferredDataSource;
	}
	
	public List<FieldsMapping> getJoinField() {
		return joinField;
	}
	
	public void setJoinField(List<FieldsMapping> joinField) {
		this.joinField = joinField;
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
						notAvaliableInAnyDataSource.add(fm.getSrcField());
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
			FieldsMapping fm = FieldsMapping.fastCreate(field.getName(), field.getName());
			
			if (!this.allMapping.contains(fm)) {
				try {
					tryToLoadDataSourceToFieldMapping(fm);
					
					addMapping(fm);
				}
				catch (FieldNotAvaliableInAnyDataSource e) {
					notAvaliableInAnyDataSource.add(fm.getSrcField());
				}
				catch (FieldAvaliableInMultipleDataSources e) {
					avaliableInMultiDataSources.add(fm.getSrcField());
				}
			}
			
		}
		
		if (!avaliableInMultiDataSources.isEmpty()) {
			throw new ForbiddenOperationException("The destination fields " + avaliableInMultiDataSources.toString()
			        + " cannot be automatically mapped as them occurrs in multiple src. Please configure them manually or specify the datasource order preference in prefferredDataSource array ");
		}
		
		if (!notAvaliableInAnyDataSource.isEmpty()) {
			throw new ForbiddenOperationException("The destination fields " + notAvaliableInAnyDataSource.toString()
			        + " cannot be automatically mapped as them do not occurr in any src. Please configure them manually!");
		}
		
		if (!notAvaliableInSpecifiedDataSource.isEmpty()) {
			throw new ForbiddenOperationException("The source fields for destination fields ["
			        + notAvaliableInSpecifiedDataSource.toString() + "] do not occurs in specified data sources !");
		}
	}
	
	private void tryToLoadDataSourceToFieldMapping(FieldsMapping fm)
	        throws FieldNotAvaliableInAnyDataSource, FieldAvaliableInMultipleDataSources {
		int qtyOccurences = 0;
		
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
		
		if (qtyOccurences == 0) {
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
	
	public AppInfo getRelatedAppInfo() {
		return relatedAppInfo;
	}
	
	public void setRelatedAppInfo(AppInfo relatedAppInfo) {
		this.relatedAppInfo = relatedAppInfo;
	}
	
	@Override
	public synchronized void fullLoad() throws DBException {
		OpenConnection conn = this.relatedAppInfo.openConnection();
		
		try {
			this.fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		if (!hasAlias()) {
			setTableAlias(getSrcConf().generateAlias(this));
		}
		
		super.fullLoad(conn);
		
		loadJoinFields(conn);
		
		this.allAvaliableDataSource = new ArrayList<>();
		this.allAvaliableDataSource.add(getSrcConf());
		
		if (utilities.arrayHasElement(getSrcConf().getAvaliableExtraDataSource())) {
			allAvaliableDataSource
			        .addAll(utilities.parseList(getSrcConf().getAvaliableExtraDataSource(), EtlDataSource.class));
		}
		
		this.fullLoadAllRelatedTables(getSrcConf(), null, conn);
		
		determinePrefferredDataSources();
		
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
	
	private void loadJoinFields(Connection conn) {
		if (this.joinField != null)
			return;
		
		if (this.hasUniqueKeys()) {
			//If no joinField is defined but the dst table has uk, tries to map the uk with src fields
			tryToAutoGenerateJoinFields(this, this.getSrcConf());
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
				}
			}
			
			if (fakeSrcUk.hasFields() && uk.equals(fakeSrcUk)) {
				if (this.joinField == null) {
					this.joinField = new ArrayList<>();
				}
				
				for (Key key : uk.getFields()) {
					this.joinField.add(FieldsMapping.fastCreate(key.getName()));
				}
			}
		}
	}
	
	private SrcConf getSrcConf() {
		return this.getParentConf().getSrcConf();
	}
	
	public EtlDatabaseObject generateDstObject(EtlDatabaseObject srcObject, Connection srcConn, AppInfo srcAppInfo,
	        AppInfo dstAppInfo) throws DBException, ForbiddenOperationException {
		try {
			
			List<EtlDatabaseObject> srcObjects = new ArrayList<>();
			
			srcObjects.add(srcObject);
			
			if (srcObject.shasSharedPkObj()) {
				srcObjects.add(srcObject.getSharedPkObj());
			}
			
			if (srcObject.getExtraDataSourceObjects() != null) {
				for (EtlDatabaseObject obj : srcObject.getExtraDataSourceObjects()) {
					srcObjects.add(obj);
				}
			}
			
			for (EtlAdditionalDataSource mappingInfo : this.getSrcConf().getAvaliableExtraDataSource()) {
				EtlDatabaseObject relatedSrcObject = mappingInfo.loadRelatedSrcObject(srcObject, srcConn, srcAppInfo);
				
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
			
			EtlDatabaseObject mappedObject = this.getSyncRecordClass(dstAppInfo).newInstance();
			
			mappedObject.setRelatedConfiguration(this);
			
			for (FieldsMapping fieldsMapping : this.allMapping) {
				Object srcValue;
				
				if (fieldsMapping.getSrcValue() != null) {
					srcValue = fieldsMapping.getSrcValue();
				} else {
					srcValue = fieldsMapping.retrieveValue(mappedObject, srcObjects, dstAppInfo, srcConn);
				}
				
				mappedObject.setFieldValue(fieldsMapping.getDestFieldAsClassField(), srcValue);
			}
			
			if (this.useSharedPKKey() && srcObject.shasSharedPkObj()) {
				//Force same fields copy as there is no mapping for sharedPktable
				
				//TODO: create mapped dst configuration for shared pk table in destination
				
				for (Field field : this.getSharedKeyRefInfo().getFields()) {
					EtlDatabaseObject sharedDstObj = mappedObject.getSharedPkObj();
					EtlDatabaseObject sharedSrcObj = srcObject.getSharedPkObj();
					
					try {
						sharedDstObj.setFieldValue(field.getName(), sharedSrcObj.getFieldValue(field.getName()));
					}
					catch (ForbiddenOperationException e) {
						try {
							sharedDstObj.setFieldValue(field.getNameAsClassAtt(),
							    sharedSrcObj.getFieldValue(field.getNameAsClassAtt()));
						}
						catch (ForbiddenOperationException e1) {}
					}
				}
			}
			
			mappedObject.loadObjectIdData(this);
			
			return mappedObject;
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration parent) {
		super.setParentConf((EtlItemConfiguration) parent);
	}
	
	@Override
	public EtlItemConfiguration getParentConf() {
		return (EtlItemConfiguration) super.getParentConf();
	}
	
	public int generateNextStartIdForThread(List<EtlObject> etlObjects, Connection conn)
	        throws DBException, ForbiddenOperationException {
		
		synchronized (stringLock) {
			
			if (this.currThreadStartId == DEFAULT_NEXT_TREAD_ID) {
				this.currQtyRecords = etlObjects.size();
				
				this.currThreadStartId = DatabaseObjectDAO.getLastRecord(this, conn);
				
				this.currThreadStartId = this.currThreadStartId - this.currQtyRecords + 1;
			}
			
			this.currThreadStartId += this.currQtyRecords;
			this.currQtyRecords = etlObjects.size();
			
			return this.currThreadStartId;
		}
	}
	
	public static synchronized int generateNextStartIdForThread(int dbCurrId, int currThreadStartId,
	        int qtyRecordsPerProcessing) {
		if (currThreadStartId == DEFAULT_NEXT_TREAD_ID) {
			
			currThreadStartId = dbCurrId;
			
			if (currThreadStartId == 0) {
				currThreadStartId = 1 - qtyRecordsPerProcessing;
			} else {
				currThreadStartId = dbCurrId - qtyRecordsPerProcessing + 1;
			}
		}
		
		currThreadStartId += qtyRecordsPerProcessing;
		
		return currThreadStartId;
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
		String joinCondition = "";
		
		for (int i = 0; i < this.joinField.size(); i++) {
			if (i > 0)
				joinCondition += " AND ";
			
			joinCondition += getTableAlias() + "." + this.joinField.get(i).getDstField() + " = "
			        + getSrcConf().getTableAlias() + "." + this.joinField.get(i).getSrcField();
		}
		
		if (!utilities.stringHasValue(joinCondition) && this.isMetadata()) {
			joinCondition = getTableAlias() + "." + getPrimaryKey().retrieveSimpleKeyColumnName() + " = "
			        + getSrcConf().getTableAlias() + "." + getPrimaryKey().retrieveSimpleKeyColumnName();
		}
		
		return joinCondition;
	}
	
	public boolean hasJoinFields() {
		return utilities.arrayHasElement(this.joinField);
	}
	
}
