package org.openmrs.module.eptssync.model.load;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.exceptions.SyncExeption;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.openmrs.OpenMRSObject;
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
	private String json;
	private String originAppLocationCode;
	private Date lastMigrationTryDate;
	private String lastMigrationTryErr;
	
	public SyncImportInfoVO(){
	}

	public Date getLastMigrationTryDate() {
		return lastMigrationTryDate;
	}

	public void setLastMigrationTryDate(Date lastMigrationTryDate) {
		this.lastMigrationTryDate = lastMigrationTryDate;
	}


	public String getLastMigrationTryErr() {
		return lastMigrationTryErr;
	}


	public void setLastMigrationTryErr(String lastMigrationTryErr) {
		this.lastMigrationTryErr = lastMigrationTryErr;
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
		syncInfo.setJson(utilities.parseToJSON(syncRecord));
		syncInfo.setOriginAppLocationCode(syncRecord.getOriginAppLocationCode());
		
		return syncInfo;
	}

	public void sync(SyncTableInfo tableInfo, OpenConnection conn) throws DBException {
		OpenMRSObject source = utilities.loadObjectFormJSON(tableInfo.getSyncRecordClass(), json);
		
		source.setOriginRecordId(source.getObjectId());
		
		try {
			source.loadDestParentInfo(conn);
			
			if (!tableInfo.useSharedPKKey()) {
				source.setObjectId(0);
			}
			
			source.save(conn);
			
			if (source.hasIgnoredParent()) {
				markAsToBeCompletedInFuture(tableInfo, conn);
			}
			else {
				markAsMigrated(tableInfo, conn);
			}
		} catch (ParentNotYetMigratedException e) {
			markAsFailedToMigrate(tableInfo, e, conn);
		}
	}
	
	public void markAsToBeCompletedInFuture(SyncTableInfo tableInfo, OpenConnection conn) throws DBException {
		SyncImportInfoDAO.markAsToBeCompletedInFuture(this, tableInfo, conn);
	}

	public void markAsFailedToMigrate(SyncTableInfo tableInfo, SyncExeption exception, Connection conn) throws DBException {
		SyncImportInfoDAO.markAsFailedToMigrate(this, tableInfo, exception, conn);
	}
	
	public void markAsMigrated(SyncTableInfo tableInfo, Connection conn) throws DBException {
		SyncImportInfoDAO.remove(this, tableInfo, conn);
	}
	
}
