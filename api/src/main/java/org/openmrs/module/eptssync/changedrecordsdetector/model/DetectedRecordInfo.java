package org.openmrs.module.eptssync.changedrecordsdetector.model;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;

import fgh.spi.changedrecordsdetector.ChangedRecord;

public class DetectedRecordInfo extends BaseVO implements ChangedRecord{
	private Integer id;
	private Integer objectId;
	private String uuid;
	private String tableName;
	private Date operationDate;
	private char operationType;
	
	/**
	 * The application which performed the record detection
	 */
	private String appCode;
	private String recordOriginLocationCode;
	
	public static final char OPERATION_TYPE_INSERT = 'I';
	public static final char OPERATION_TYPE_UPDATE = 'U';
	public static final char OPERATION_TYPE_DELETE = 'D';
	
	public DetectedRecordInfo() {
	}
	
	private DetectedRecordInfo(String tableName, Integer objectId, String uuid, String appCode, String recordOriginLocationCode) {
		this.tableName = tableName;
		this.objectId = objectId;
		this.uuid = uuid;
		this.appCode = appCode;
		this.recordOriginLocationCode = recordOriginLocationCode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Date getOperationDate() {
		return operationDate;
	}

	public void setOperationDate(Date operationDate) {
		this.operationDate = operationDate;
	}

	public char getOperationType() {
		return operationType;
	}

	public void setOperationType(char operationType) {
		this.operationType = operationType;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}
	
	
	public Integer getObjectId() {
		return objectId;
	}

	
	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	
	public String getUuid() {
		return uuid;
	}

	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}

	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}

	public static DetectedRecordInfo generate(OpenMRSObject record,  String appCode, String recordOriginLocationCode) {
		DetectedRecordInfo info = new DetectedRecordInfo();
		
		info.setTableName(record.generateTableName());
		info.setObjectId(record.getObjectId());
		info.setUuid(record.getUuid());
		info.setAppCode(appCode);
		info.setRecordOriginLocationCode(recordOriginLocationCode);
		
		OperationInfo operationInfo = determineOperationType(record);
		
		info.setOperationType(operationInfo.getOperationType());
		info.setOperationDate(operationInfo.getOperationDate());
		info.setDateCreated(record.getDateCreated());
		info.setDateChanged(record.getDateChanged());
		
		return info;
	}
	
	private static OperationInfo determineOperationType(OpenMRSObject record) {
		if (record.getDateVoided() != null) return OperationInfo.fastCreateVoidOperation(record.getDateVoided());
		if (record.getDateChanged() != null) return OperationInfo.fastCreateChangeOperation(record.getDateChanged());
		
		return OperationInfo.fastCreateInsertOperation(record.getDateCreated());
	}
	
	
	public static DetectedRecordInfo generate(String tableName, int recordId, String recordUuid, String appCode, String recordOriginLocationCode) {
		DetectedRecordInfo info = new DetectedRecordInfo(tableName, recordId, recordUuid, appCode, recordOriginLocationCode);
	
		return info;
	}

	public void save(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		DetectedRecordInfoDAO.insert(this, tableConfiguration, conn);
	}
	
	@Override
	public String getOriginLocation() {
		return this.recordOriginLocationCode;
	}

	@Override
	public void refreshLastSyncDateOnOrigin(SyncTableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshLastSyncDateOnDestination(SyncTableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String generateDBPrimaryKeyAtt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadDestParentInfo(SyncTableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInsertSQLWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getInsertParamsWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInsertSQLWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUpdateSQL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getUpdateParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateInsertValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasIgnoredParent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasParents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void consolidateData(SyncTableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resolveInconsistence(SyncTableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SyncImportInfoVO retrieveRelatedSyncInfo(SyncTableConfiguration tableInfo, String recordOriginLocationCode,
	        Connection conn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SyncImportInfoVO getRelatedSyncInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRelatedSyncInfo(SyncImportInfoVO relatedSyncInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String generateMissingInfo(Map<RefInfo, Integer> missingParents) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<RefInfo, Integer> loadMissingParents(SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeDueInconsistency(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents,
	        Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParentToNull(String parentAttName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeObjectId(SyncTableConfiguration syncTableConfiguration, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeParentForAllChildren(OpenMRSObject newParent, SyncTableConfiguration syncTableInfo, Connection conn)
	        throws DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OpenMRSObject retrieveParentInDestination(Integer parentId, SyncTableConfiguration parentTableConfiguration,
	        boolean ignorable, Connection conn) throws ParentNotYetMigratedException, DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
