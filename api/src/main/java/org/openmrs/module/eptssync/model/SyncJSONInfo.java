package org.openmrs.module.eptssync.model;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This class aggregate the information needed for synchronization json file
 * 
 * @author jpboane
 *
 */
public class SyncJSONInfo {

	private static CommonUtilities utilities = CommonUtilities.getInstance();

	private int qtyRecords;
	private Date dateGenerated;
	private String originAppLocationCode;
	
	private List<SyncImportInfoVO> syncInfo;
	/**
	 * The minimal info of this object
	 */
	private SyncJSONInfo minimalJSONInfo;
	
	public SyncJSONInfo() {
	}
	
	public SyncJSONInfo(List<OpenMRSObject> syncRecords, String recordOriginLocationCode, boolean generateRecordJSON) throws DBException {
		this.qtyRecords = utilities.arraySize(syncRecords);
		this.syncInfo = SyncImportInfoVO.generateFromSyncRecord(syncRecords, recordOriginLocationCode, generateRecordJSON);
		this.dateGenerated = DateAndTimeUtilities.getCurrentDate();
		this.originAppLocationCode = recordOriginLocationCode;
	}
	
	public List<SyncImportInfoVO> getSyncInfo() {
		return syncInfo;
	}
	
	public void setSyncInfo(List<SyncImportInfoVO> syncInfo) {
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

	public static SyncJSONInfo generate(List<OpenMRSObject> syncRecords, String recordOriginLocationCode, boolean generateRecordJSON) throws DBException {
		SyncJSONInfo syncJSONInfo = new SyncJSONInfo(syncRecords, recordOriginLocationCode, generateRecordJSON);

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
		} catch (JsonParseException e) {
			e.printStackTrace();
		
			ex = e;
			
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
			ex = e;
			
			throw new RuntimeException(e);
		} catch (IOException e) {
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
}
