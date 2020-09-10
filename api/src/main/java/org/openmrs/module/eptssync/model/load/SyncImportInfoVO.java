package org.openmrs.module.eptssync.model.load;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.model.OpenMRSObject;
import org.openmrs.module.eptssync.model.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * Represent the information to be imported from the source to destination data base.
 * This information are first saved on sync stage area
 * 
 * @author jpboane
 *
 */
public class SyncImportInfoVO extends BaseVO implements SyncRecord{
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private int id;
	private int recordId;
	private int mainParentId;
	private String mainParentTable;
	private String syncTableName;
	private String json;
	private String originAppLocationCode;
	
	public SyncImportInfoVO(){
	}
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public String getSyncTableName() {
		return syncTableName;
	}


	public void setSyncTableName(String syncTableName) {
		this.syncTableName = syncTableName;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	@Override
	public int getObjectId() {
		return this.id;
	}

	@Override
	public void setObjectId(int id) {
		this.id = id;
	}

	public int getMainParentId() {
		return mainParentId;
	}
	
	public void setMainParentId(int mainParentId) {
		this.mainParentId = mainParentId;
	}
	
	public String getMainParentTable() {
		return mainParentTable;
	}
	
	public void setMainParentTable(String mainParentTable) {
		this.mainParentTable = mainParentTable;
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}

	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}

	public static List<SyncImportInfoVO> generateFromSyncRecord(List<OpenMRSObject> syncRecords) {
		List<SyncImportInfoVO> importInfo = new ArrayList<SyncImportInfoVO>();
	
		for (OpenMRSObject syncRecord : syncRecords) {
			importInfo.add(generateFromSyncRecord(syncRecord));
		}
		
		return importInfo;
	}
	
	public static SyncImportInfoVO generateFromSyncRecord(OpenMRSObject syncRecord) {
		SyncImportInfoVO syncInfo = new SyncImportInfoVO();
		
		syncInfo.setRecordId(syncRecord.getObjectId());
		syncInfo.setSyncTableName(syncRecord.generateTableName());
		syncInfo.setJson(utilities.parseToJSON(syncRecord));
		syncInfo.setMainParentId(syncRecord.getMainParentId());
		syncInfo.setMainParentTable(syncRecord.getMainParentTable());
		syncInfo.setOriginAppLocationCode(syncRecord.getOriginAppLocationCode());
		
		return syncInfo;
	}

	public void sync(SyncTableInfo tableInfo, OpenConnection conn) throws DBException {
		OpenMRSObject source = utilities.loadObjectFormJSON(tableInfo.getSyncRecordClass(), json);
		
		source.setOriginRecordId(source.getObjectId());
		source.setObjectId(0);
		source.loadDestParentInfo(conn);
		
		OpenMRSObject recordOnDB = OpenMRSObjectDAO.thinGetByOriginRecordId(source.getClass(), source.getOriginRecordId(), source.getOriginAppLocationCode(), conn);
		
		if (recordOnDB != null) {
			source.setObjectId(recordOnDB.getObjectId());
			
			OpenMRSObjectDAO.update(source, conn);
		}
		else {
			OpenMRSObjectDAO.insert(source, conn);
		}
	
	}
}
