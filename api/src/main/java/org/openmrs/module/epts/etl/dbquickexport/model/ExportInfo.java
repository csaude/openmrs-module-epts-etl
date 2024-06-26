package org.openmrs.module.epts.etl.dbquickexport.model;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.BaseVO;

public class ExportInfo extends BaseVO{
	private String uuid;
	private String tableName;
	private String recordOriginLocationCode;
	
	public ExportInfo() {
	}
	
	private ExportInfo(String tableName, String uuid,  String recordOriginLocationCode) {
		this.tableName = tableName;
		this.uuid = uuid;
		this.recordOriginLocationCode = recordOriginLocationCode;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getUuid() {
		return uuid;
	}

	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}

	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}

	public static ExportInfo generate(EtlDatabaseObject record,  String appCode, String recordOriginLocationCode) {
		ExportInfo info = new ExportInfo();
		
		info.setTableName(record.generateTableName());
		info.setUuid(record.getUuid());
		info.setRecordOriginLocationCode(recordOriginLocationCode);
		info.setDateCreated(record.getDateCreated());
		info.setDateChanged(record.getDateChanged());
		
		return info;
	}
	
	
	public static ExportInfo generate(String tableName, String recordUuid, String recordOriginLocationCode) {
		ExportInfo info = new ExportInfo(tableName, recordUuid, recordOriginLocationCode);
	
		return info;
	}

	@Override
	public void setFieldValue(String fieldName, Object value) {
		// TODO Auto-generated method stub
		
	}
}
