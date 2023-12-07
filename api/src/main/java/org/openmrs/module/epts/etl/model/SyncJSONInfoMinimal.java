package org.openmrs.module.epts.etl.model;

import java.io.IOException;
import java.util.Date;

import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This class aggregate the information needed for synchronization json file
 * 
 * @author jpboane
 */
public class SyncJSONInfoMinimal {
	
	private int qtyRecords;
	
	private Date dateGenerated;
	
	private String originAppLocationCode;
	
	private String tableName;
	
	public SyncJSONInfoMinimal() {
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
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
	
	public static SyncJSONInfoMinimal loadFromJSON(String json) {
		Exception ex = null;
		
		try {
			ObjectMapperProvider mapper = new ObjectMapperProvider();
			
			SyncJSONInfoMinimal synJsonInfo = mapper.getContext(SyncJSONInfoMinimal.class).readValue(json,
			    SyncJSONInfoMinimal.class);
			
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
}
