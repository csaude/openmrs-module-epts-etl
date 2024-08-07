package org.openmrs.module.epts.etl.model;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This class aggregate the information needed for synchronization json file
 * 
 * @author jpboane
 */
public class SyncJSONInfo {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private int qtyRecords;
	
	private Date dateGenerated;
	
	private String originAppLocationCode;
	
	private String tableName;
	
	private List<EtlStageRecordVO> syncInfo;
	
	/**
	 * The minimal info of this object
	 */
	private SyncJSONInfo minimalJSONInfo;
	
	public SyncJSONInfo() {
	}
	
	public SyncJSONInfo(String tableName, List<EtlDatabaseObject> syncRecords, String recordOriginLocationCode,
	    boolean generateRecordJSON) throws DBException {
		/*this.qtyRecords = utilities.arraySize(syncRecords);
		this.syncInfo = EtlStageRecordVO.generateFromSyncRecord(syncRecords, recordOriginLocationCode, generateRecordJSON);
		this.dateGenerated = DateAndTimeUtilities.getCurrentDate();
		this.originAppLocationCode = recordOriginLocationCode;
		this.tableName = tableName;*/
	}
	
	public SyncJSONInfo(String recordOriginLocationCode) throws DBException {
		this.dateGenerated = DateAndTimeUtilities.getCurrentDate();
		this.originAppLocationCode = recordOriginLocationCode;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public List<EtlStageRecordVO> getSyncInfo() {
		return syncInfo;
	}
	
	public void setSyncInfo(List<EtlStageRecordVO> syncInfo) {
		this.syncInfo = syncInfo;
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}
	
	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}
	
	public int getQtyRecords() {
		return qtyRecords;
	}
	
	public void setQtyRecords(int qtyRecords) {
		this.qtyRecords = qtyRecords;
	}
	
	public Date getDateGenerated() {
		return dateGenerated;
	}
	
	public void setDateGenerated(Date dateGenerated) {
		this.dateGenerated = dateGenerated;
	}
	
	public static SyncJSONInfo generate(String tableName, List<EtlDatabaseObject> syncRecords,
	        String recordOriginLocationCode, boolean generateRecordJSON) throws DBException {
		SyncJSONInfo syncJSONInfo = new SyncJSONInfo(tableName, syncRecords, recordOriginLocationCode, generateRecordJSON);
		
		return syncJSONInfo;
	}
	
	public static SyncJSONInfo generate(String recordOriginLocationCode) throws DBException {
		SyncJSONInfo syncJSONInfo = new SyncJSONInfo(recordOriginLocationCode);
		
		return syncJSONInfo;
	}
	
	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
	
	public static SyncJSONInfo loadFromJSON(String json) {
		Exception ex = null;
		
		try {
			ObjectMapperProvider mapper = new ObjectMapperProvider();
			
			SyncJSONInfo synJsonInfo = mapper.getContext(SyncJSONInfo.class).readValue(json, SyncJSONInfo.class);
			
			return synJsonInfo;
		}
		catch (JsonParseException e) {
			e.printStackTrace();
			
			ex = e;
			
			throw new RuntimeException(e);
		}
		catch (JsonMappingException e) {
			e.printStackTrace();
			
			ex = e;
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			ex = e;
			
			throw new RuntimeException(e);
		}
		finally {
			if (ex != null) {
				//System.out.println(json);
			}
		}
		
	}
	
	public SyncJSONInfo generateMinimalInfo() {
		this.minimalJSONInfo = new SyncJSONInfo();
		this.minimalJSONInfo.qtyRecords = this.qtyRecords;
		this.minimalJSONInfo.originAppLocationCode = this.originAppLocationCode;
		this.minimalJSONInfo.dateGenerated = this.dateGenerated;
		
		return this.minimalJSONInfo;
	}
	
	private String fileName;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void clearOriginApplicationCodeForAllChildren() {
		if (utilities.arrayHasNoElement(this.syncInfo))
			return;
		
		for (EtlStageRecordVO info : this.syncInfo) {
			info.setRecordOriginLocationCode(null);
		}
		
	}
}
