package org.openmrs.module.eptssync.model;

import java.io.IOException;
import java.util.Date;

import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This class aggregate the information information of synchronization json file
 * 
 * @author jpboane
 *
 */
public class MinimalSyncJSONInfo {
	private int qtyRecords;
	private Date dateGenerated;
	private String originAppLocationCode;
	
	public MinimalSyncJSONInfo() {
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

	public static MinimalSyncJSONInfo loadFromJSON (String json) {
		try {
			ObjectMapperProvider mapper = new ObjectMapperProvider();
			
			MinimalSyncJSONInfo synJsonInfo = mapper.getContext(MinimalSyncJSONInfo.class).readValue(json, MinimalSyncJSONInfo.class);
			
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
