package org.openmrs.module.eptssync.controller.conf;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SyncTableInfoSource {
	private String syncRootDirectory;
	private String syncStageSchema;
	
	private String originAppLocationCode;
	
	private List<SyncTableInfo> syncTableInfo;
	
	private boolean firstExport;
	
	private SyncTableInfoSource() {
	}
	
	public boolean isFirstExport() {
		return firstExport;
	}

	public void setFirstExport(boolean firstExport) {
		this.firstExport = firstExport;
	}

	public List<SyncTableInfo> getSyncTableInfo() {
		return syncTableInfo;
	}
	
	public String getSyncRootDirectory() {
		return syncRootDirectory;
	}

	public void setSyncRootDirectory(String syncRootDirectory) {
		this.syncRootDirectory = syncRootDirectory;
	}

	public void setSyncTableInfo(List<SyncTableInfo> syncTableInfo) {
		this.syncTableInfo = syncTableInfo;
	}

	public String getSyncStageSchema() {
		return syncStageSchema;
	}
	
	public void setSyncStageSchema(String syncStageSchema) {
		this.syncStageSchema = syncStageSchema;
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}

	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}

	public static SyncTableInfoSource loadFromJSON (String json) {
		try {
			SyncTableInfoSource syncTableInfoSource = new ObjectMapperProvider().getContext(SyncTableInfoSource.class).readValue(json, SyncTableInfoSource.class);
		
			if (!syncTableInfoSource.isImportStageSchemaExists()) {
				syncTableInfoSource.createStageSchema();
			}
			
			for (SyncTableInfo info : syncTableInfoSource.syncTableInfo) {
				info.setRelatedSyncTableInfoSource(syncTableInfoSource);
				info.tryToUpgradeDataBaseInfo();
				info.generateRecordClass();
			}
			
			return syncTableInfoSource;
		} catch (JsonParseException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private OpenConnection openConnection() {
		return DBConnectionService.getInstance().openConnection();
	}
	
	private void createStageSchema() {
		OpenConnection conn = openConnection();
		
		try {
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE " + getSyncStageSchema());

			st.executeBatch();

			st.close();
			
			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}

	}

	private boolean isImportStageSchemaExists() {
		OpenConnection conn = openConnection();
		
		try {
			return DBUtilities.isResourceExist(null, DBUtilities.RESOURCE_TYPE_SCHEMA, getSyncStageSchema(), conn);
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

}
