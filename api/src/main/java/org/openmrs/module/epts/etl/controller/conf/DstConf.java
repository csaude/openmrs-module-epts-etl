package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DstConf extends AbstractTableConfiguration {
	
	private List<FieldsMapping> joinField;
	
	private List<FieldsMapping> allFieldsMapping;
	
	private List<FieldsMapping> manualFieldsMapping;
	
	private AppInfo relatedAppInfo;
	
	private static final int DEFAULT_NEXT_TREAD_ID = -1;
	
	private int currThreadStartId;
	
	private int currQtyRecords;
	
	private final String stringLock = new String("LOCK_STRING");
	
	public DstConf() {
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
	
	public List<FieldsMapping> getManualFieldsMapping() {
		return manualFieldsMapping;
	}
	
	public void setManualFieldsMapping(List<FieldsMapping> manualFieldsMapping) {
		this.manualFieldsMapping = manualFieldsMapping;
	}
	
	public List<FieldsMapping> getAllFieldsMapping() {
		return allFieldsMapping;
	}
	
	private void addMapping(FieldsMapping fm) throws ForbiddenOperationException {
		if (this.allFieldsMapping == null) {
			this.allFieldsMapping = new ArrayList<FieldsMapping>();
		}
		
		if (this.allFieldsMapping.contains(fm))
			throw new ForbiddenOperationException("The field [" + fm + "] already exists on mapping");
		
		this.allFieldsMapping.add(fm);
	}
	
	public void generateAllFieldsMapping(Connection conn) throws DBException {
		this.allFieldsMapping = new ArrayList<>();
		
		if (utilities.arrayHasElement(this.manualFieldsMapping)) {
			for (FieldsMapping fm : this.manualFieldsMapping) {
				if (!utilities.stringHasValue(fm.getDataSourceName())) {
					fm.setDataSourceName(getParent().getSrcConf().getTableName());
				}
				
				addMapping(fm);
			}
		}
		
		List<Field> myFields = this.getFields();
		
		List<String> notAutomaticalMappedFields = new ArrayList<>();
		
		for (Field field : myFields) {
			FieldsMapping fm = FieldsMapping.fastCreate(field.getName(), field.getName());
			
			if (!this.allFieldsMapping.contains(fm)) {
				
				//The main src has high priority for being the source of any field os dst table
				if (this.getSrcConf().containsField(field.getName())) {
					fm.setDataSourceName(this.getSrcConf().getTableName());
					
					this.addMapping(fm);
				} else {
					//Continue looking for the field on extra sources
					
					List<SyncDataSource> avaliableDs = this.getSrcConf().getAvaliableExtraDataSource();
					
					int qtyDsContainingField = 0;
					
					for (SyncDataSource ds : avaliableDs) {
						if (ds.containsField(field.getName())) {
							qtyDsContainingField++;
							
							if (qtyDsContainingField > 1) {
								notAutomaticalMappedFields.add(field.getName());
								
								break;
							} else {
								
								if (ds instanceof QueryDataSourceConfig) {
									fm.setDataSourceName(ds.getName());
								} else if (ds instanceof TableDataSourceConfig) {
									//All the tableSrcData are loaded with the src
									fm.setDataSourceName(this.getSrcConf().getTableName());
								} else {
									throw new ForbiddenOperationException(
									        "Unkown data source type " + ds.getClass().getCanonicalName());
								}
								
								this.addMapping(fm);
							}
						}
					}
				}
			}
		}
		
		if (!notAutomaticalMappedFields.isEmpty()) {
			throw new ForbiddenOperationException("The destination fields " + notAutomaticalMappedFields.toString()
			        + " cannot be automatically mapped as them occurrs in multiple src. Please configure them manually");
		}
	}
	
	public String getMappedField(String srcField) {
		List<FieldsMapping> machedFields = new ArrayList<FieldsMapping>();
		
		for (FieldsMapping field : this.allFieldsMapping) {
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
		super.fullLoad(conn);
		
		loadJoinFields(conn);
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
			
			UniqueKeyInfo fakeSrcUk = new UniqueKeyInfo();
			
			for (Key key : uk.getFields()) {
				if (targetTable.containsField(key.getName())) {
					fakeSrcUk.addKey(key);
				}
			}
			
			if (uk.equals(fakeSrcUk)) {
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
		return this.getParent().getSrcConf();
	}
	
	public DatabaseObject generateMappedObject(DatabaseObject srcObject, Connection srcConn, AppInfo srcAppInfo,
	        AppInfo dstAppInfo) throws DBException, ForbiddenOperationException {
		try {
			
			List<DatabaseObject> srcObjects = new ArrayList<>();
			
			srcObjects.add(srcObject);
			
			if (utilities.arrayHasElement(this.getSrcConf().getExtraQueryDataSource())) {
				for (QueryDataSourceConfig mappingInfo : this.getSrcConf().getExtraQueryDataSource()) {
					DatabaseObject relatedSrcObject = mappingInfo.loadRelatedSrcObject(srcObject, srcConn, srcAppInfo);
					
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
			}
			
			DatabaseObject mappedObject = this.getSyncRecordClass(dstAppInfo).newInstance();
			
			mappedObject.setRelatedConfiguration(this);
			
			for (FieldsMapping fieldsMapping : this.allFieldsMapping) {
				
				Object srcValue = fieldsMapping.retrieveValue(mappedObject, srcObjects, dstAppInfo, srcConn);
				
				mappedObject.setFieldValue(fieldsMapping.getDestFieldAsClassField(), srcValue);
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
	public void setParent(SyncDataConfiguration parent) {
		super.setParent((EtlItemConfiguration) parent);
	}
	
	@Override
	public EtlItemConfiguration getParent() {
		return (EtlItemConfiguration) super.getParent();
	}
	
	public int generateNextStartIdForThread(List<SyncRecord> syncRecords, Connection conn)
	        throws DBException, ForbiddenOperationException {
		
		synchronized (stringLock) {
			
			if (this.currThreadStartId == DEFAULT_NEXT_TREAD_ID) {
				this.currQtyRecords = syncRecords.size();
				
				this.currThreadStartId = DatabaseObjectDAO.getLastRecord(this, conn);
				
				this.currThreadStartId = this.currThreadStartId - this.currQtyRecords + 1;
			}
			
			this.currThreadStartId += this.currQtyRecords;
			this.currQtyRecords = syncRecords.size();
			
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
	public String generateJoinConditionWithSrc(String sourceTableAlias, String destinationTableAlias) {
		String joinCondition = "";
		
		for (int i = 0; i < this.joinField.size(); i++) {
			if (i > 0)
				joinCondition += " AND ";
			
			joinCondition += "dest_." + this.joinField.get(i).getDstField() + " = src_."
			        + this.joinField.get(i).getSrcField();
		}
		
		if (!utilities.stringHasValue(joinCondition) && this.isMetadata()) {
			joinCondition = "dest_." + getPrimaryKey().retrieveSimpleKeyColumnName() + " = src_."
			        + getPrimaryKey().retrieveSimpleKeyColumnName();
		}
		
		return joinCondition;
	}
	
	public boolean hasJoinFields() {
		return utilities.arrayHasElement(this.joinField);
	}
	
}
