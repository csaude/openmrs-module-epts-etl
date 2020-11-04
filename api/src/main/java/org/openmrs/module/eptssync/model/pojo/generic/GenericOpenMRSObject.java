package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;

public class GenericOpenMRSObject extends AbstractOpenMRSObject {
	private int originRecordId;
	private String originAppLocationCode;
	private int objectId;
	
	@Override
	public String generateDBPrimaryKeyAtt() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public void setOriginRecordId(int originRecordId) {
		this.originRecordId = originRecordId;
	}

	@Override
	public int getOriginRecordId() {
		return this.originRecordId;
	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public Object[] getInsertParams() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public String getInsertSQL() {
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
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}

	@Override
	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}

	@Override
	public String getUuid() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public void setConsistent(int consistent) {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public int getConsistent() {
		throw new ForbiddenOperationException("Forbidden Method");
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
	public boolean isGeneratedFromSkeletonClass() {
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
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public void resolveInconsistence(SyncTableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException {
		throw new ForbiddenOperationException("Forbidden Method");
	}
}
