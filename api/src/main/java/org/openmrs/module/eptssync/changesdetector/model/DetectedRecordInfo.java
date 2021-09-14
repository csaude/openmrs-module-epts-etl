package org.openmrs.module.eptssync.changesdetector.model;

import java.sql.Connection;
import java.util.Date;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

import fgh.spi.changedrecordsdetector.ChangedRecord;

public class DetectedRecordInfo extends BaseVO implements ChangedRecord{
	private int id;
	private String tableName;
	private int recordId;
	private String recordUuid;
	private Date operationDate;
	private char operationType;
	
	/**
	 * The application which performed the record detection
	 */
	private String appCode;
	private String recordOriginLocationCode;
	
	public static final char OPERATION_TYPE_INSERT = 'I';
	public static final char OPERATION_TYPE_UPDATE = 'U';
	public static final char OPERATION_TYPE_DELETE = 'D';
	
	public DetectedRecordInfo() {
	}
	
	private DetectedRecordInfo(String tableName, int recordId, String recordUuid, String appCode, String recordOriginLocationCode) {
		this.tableName = tableName;
		this.recordId = recordId;
		this.recordUuid = recordUuid;
		this.appCode = appCode;
		this.recordOriginLocationCode = recordOriginLocationCode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Date getOperationDate() {
		return operationDate;
	}

	public void setOperationDate(Date operationDate) {
		this.operationDate = operationDate;
	}

	public char getOperationType() {
		return operationType;
	}

	public void setOperationType(char operationType) {
		this.operationType = operationType;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public String getRecordUuid() {
		return recordUuid;
	}

	public void setRecordUuid(String recordUuid) {
		this.recordUuid = recordUuid;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	
	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}

	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}

	public static DetectedRecordInfo generate(OpenMRSObject record,  String appCode, String recordOriginLocationCode) {
		DetectedRecordInfo info = new DetectedRecordInfo();
		
		info.setTableName(record.generateTableName());
		info.setRecordId(record.getObjectId());
		info.setRecordUuid(record.getUuid());
		info.setAppCode(appCode);
		info.setRecordOriginLocationCode(recordOriginLocationCode);
		
		OperationInfo operationInfo = determineOperationType(record);
		
		info.setOperationType(operationInfo.getOperationType());
		info.setOperationDate(operationInfo.getOperationDate());
		info.setDateCreated(record.getDateCreated());
		info.setDateChanged(record.getDateChanged());
		
		return info;
	}
	
	private static OperationInfo determineOperationType(OpenMRSObject record) {
		if (record.getDateVoided() != null) return OperationInfo.fastCreateVoidOperation(record.getDateVoided());
		if (record.getDateChanged() != null) return OperationInfo.fastCreateChangeOperation(record.getDateChanged());
		
		return OperationInfo.fastCreateInsertOperation(record.getDateCreated());
	}
	
	
	public static DetectedRecordInfo generate(String tableName, int recordId, String recordUuid, String appCode, String recordOriginLocationCode) {
		DetectedRecordInfo info = new DetectedRecordInfo(tableName, recordId, recordUuid, appCode, recordOriginLocationCode);
	
		return info;
	}


	public void save(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		DetectedRecordInfoDAO.insert(this, tableConfiguration, conn);
	}

	@Override
	public String getOriginLocation() {
		return this.recordOriginLocationCode;
	}
}
