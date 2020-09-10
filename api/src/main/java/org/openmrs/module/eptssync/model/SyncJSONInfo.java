package org.openmrs.module.eptssync.model;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;

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
	
	private List<OpenMRSObject> syncRecords;

	public SyncJSONInfo() {
	}
	
	public SyncJSONInfo(List<OpenMRSObject> syncRecords) {
		this.syncRecords = syncRecords;
		this.qtyRecords = utilities.arraySize(syncRecords);

		this.dateGenerated = DateAndTimeUtilities.getCurrentDate();
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

	public List<OpenMRSObject> getSyncRecords() {
		return syncRecords;
	}

	public void setSyncRecords(List<OpenMRSObject> syncRecords) {
		this.syncRecords = syncRecords;
	}

	public static SyncJSONInfo generate(List<OpenMRSObject> syncRecords) {
		SyncJSONInfo syncJSONInfo = new SyncJSONInfo(syncRecords);

		return syncJSONInfo;
	}

	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
	
	public static SyncJSONInfo loadFromJSON (String json) {
		try {
			ObjectMapperProvider mapper = new ObjectMapperProvider();
			
			SyncJSONInfo synJsonInfo = mapper.getContext(SyncJSONInfo.class).readValue(json, SyncJSONInfo.class);
			
			return synJsonInfo;
		} catch (JsonParseException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}	
	
}
