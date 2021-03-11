package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;

public class GenericOpenMRSObject extends AbstractOpenMRSObject {
	private int objectId;
	private String uuid;
	
	private SyncTableConfiguration syncTableConfiguration;
	
	public GenericOpenMRSObject() {
	}
	
	public GenericOpenMRSObject(SyncTableConfiguration syncTableConfiguration) {
		this.syncTableConfiguration = syncTableConfiguration;
	}
	
	@Override
	public String generateDBPrimaryKeyAtt() {
		return this.syncTableConfiguration.getPrimaryKey();
	}
	
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public String getInsertSQLWithoutObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}
	
	@Override
	public Object[] getInsertParamsWithObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public String getInsertSQLWithObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}


	@Override
	public String getUpdateSQL() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public Object[] getUpdateParams() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public String generateInsertValues() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public String getUuid() {
		return this.uuid;
	}

	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public boolean hasParents() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public int getParentValue(String parentAttName) {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public int getObjectId() {
		return this.objectId;
	}

	@Override
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}
	
	@Override
	public String generateTableName() {
		return this.syncTableConfiguration.getTableName();
	}

	@Override
	public void resolveInconsistence(SyncTableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {
		throw new ForbiddenOperationException("Forbidden Method");
	}
	
	public static GenericOpenMRSObject fastCreate(SyncImportInfoVO syncImportInfo, SyncTableConfiguration syncTableConfiguration) {
		GenericOpenMRSObject obj = new GenericOpenMRSObject(syncTableConfiguration);
		obj.setObjectId(syncImportInfo.getRecordOriginId());
		obj.setUuid(syncImportInfo.getRecordUuid());
		
		return obj;
	}
	
}
