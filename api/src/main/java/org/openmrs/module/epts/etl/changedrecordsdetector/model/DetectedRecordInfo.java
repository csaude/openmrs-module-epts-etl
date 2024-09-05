package org.openmrs.module.epts.etl.changedrecordsdetector.model;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.EtlDatabaseObjectUniqueKeyInfo;
import org.openmrs.module.epts.etl.model.base.BaseVO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

import fgh.spi.changedrecordsdetector.ChangedRecord;

public class DetectedRecordInfo extends BaseVO implements ChangedRecord {
	
	private Integer id;
	
	private Oid objectId;
	
	private String uuid;
	
	private String tableName;
	
	private Date operationDate;
	
	private char operationType;
	
	/**
	 * The application which performed the dstRecord detection
	 */
	private String appCode;
	
	private String recordOriginLocationCode;
	
	public static final char OPERATION_TYPE_INSERT = 'I';
	
	public static final char OPERATION_TYPE_UPDATE = 'U';
	
	public static final char OPERATION_TYPE_DELETE = 'D';
	
	public DetectedRecordInfo() {
	}
	
	private DetectedRecordInfo(String tableName, Oid objectId, String uuid, String appCode,
	    String recordOriginLocationCode) {
		this.tableName = tableName;
		this.objectId = objectId;
		this.uuid = uuid;
		this.appCode = appCode;
		this.recordOriginLocationCode = recordOriginLocationCode;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
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
	
	public Oid getObjectId() {
		return objectId;
	}
	
	public void setObjectId(Oid objectId) {
		this.objectId = objectId;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}
	
	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}
	
	public static DetectedRecordInfo generate(EtlDatabaseObject record, String appCode, String recordOriginLocationCode) {
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
	
	private static OperationInfo determineOperationType(EtlDatabaseObject record) {
		if (record.getDateVoided() != null)
			return OperationInfo.fastCreateVoidOperation(record.getDateVoided());
		if (record.getDateChanged() != null)
			return OperationInfo.fastCreateChangeOperation(record.getDateChanged());
		
		return OperationInfo.fastCreateInsertOperation(record.getDateCreated());
	}
	
	public static DetectedRecordInfo generate(String tableName, Oid recordId, String recordUuid, String appCode,
	        String recordOriginLocationCode) {
		DetectedRecordInfo info = new DetectedRecordInfo(tableName, recordId, recordUuid, appCode, recordOriginLocationCode);
		
		return info;
	}
	
	public void save(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		DetectedRecordInfoDAO.insert(this, tableConfiguration, conn);
	}
	
	@Override
	public String getOriginLocation() {
		return this.recordOriginLocationCode;
	}
	
	@Override
	public void refreshLastSyncDateOnOrigin(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void refreshLastSyncDateOnDestination(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadDestParentInfo(TableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
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
	public Integer getParentValue(ParentTable parentInfo) {
		return null;
	}
	
	@Override
	public void consolidateData(TableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void resolveInconsistence(TableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EtlStageRecordVO retrieveRelatedSyncInfo(TableConfiguration tableInfo, String recordOriginLocationCode,
	        Connection conn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public EtlStageRecordVO getRelatedSyncInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setRelatedSyncInfo(EtlStageRecordVO relatedSyncInfo) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void remove(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void changeObjectId(TableConfiguration abstractTableConfiguration, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void changeParentForAllChildren(EtlDatabaseObject newParent, TableConfiguration syncTableInfo, Connection conn)
	        throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EtlDatabaseObject retrieveParentInDestination(Integer parentId, String a,
	        TableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
		return null;
	}
	
	@Override
	public boolean hasExactilyTheSameDataWith(EtlDatabaseObject srcObj) {
		return false;
	}
	
	@Override
	public Object getFieldValue(String fieldName) {
		return null;
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
	}
	
	@Override
	public void loadObjectIdData(TableConfiguration tabConf) {
		this.objectId = tabConf.getPrimaryKey().generateOid(this);
	}
	
	@Override
	public void fastCreateSimpleNumericKey(long i) {
		Oid oid = new Oid();
		
		Key k = new Key("");
		
		k.setValue(i);
		
		oid.addKey(k);
	}
	
	@Override
	public String generateInsertValuesWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateInsertValuesWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public EtlDatabaseObject getSharedPkObj() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateMissingInfo(Map<ParentTableImpl, Integer> missingParents) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Map<ParentTableImpl, Integer> loadMissingParents(TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void removeDueInconsistency(TableConfiguration syncTableInfo, Map<ParentTableImpl, Integer> missingParents,
	        Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void changeParentValue(ParentTable refInfo, EtlDatabaseObject newParent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setParentToNull(ParentTableImpl refInfo) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update(TableConfiguration syncTableInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadWithDefaultValues(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithObjectId(String insertQuestionMarks) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithoutObjectId(String insertQuestionMarks) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateFullFilledUpdateSql() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void copyFrom(EtlDatabaseObject parentRecordInOrigin) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EtlDatabaseObject getSrcRelatedObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setSrcRelatedObject(EtlDatabaseObject srcRelatedObject) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setSharedPkObj(EtlDatabaseObject sharedPkObj) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ConflictResolutionType getConflictResolutionType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setConflictResolutionType(ConflictResolutionType conflictResolutionType) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<EtlDatabaseObjectUniqueKeyInfo> getUniqueKeysInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setUniqueKeysInfo(List<EtlDatabaseObjectUniqueKeyInfo> uniqueKeysInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(TableConfiguration syncTableInfo, ConflictResolutionType onConflict, Connection conn)
	        throws DBException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<? extends EtlDatabaseObject> getAuxLoadObject() {
		// TODO Auto-generated method stub
		return null;
	}
}
