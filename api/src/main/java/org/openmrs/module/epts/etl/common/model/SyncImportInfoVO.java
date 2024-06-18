
package org.openmrs.module.epts.etl.common.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MetadataInconsistentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.BaseVO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represent the information to be imported from the source to destination data base. This
 * information are first saved on sync stage area
 * 
 * @author jpboane
 */
public class SyncImportInfoVO extends BaseVO implements EtlDatabaseObject {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static final int MIGRATION_STATUS_PENDING = 1;
	
	public static final int MIGRATION_STATUS_INCOMPLETE = 0;
	
	public static final int MIGRATION_STATUS_FAILED = -1;
	
	private Integer id;
	
	private Integer recordOriginId;
	
	private String recordOriginLocationCode;
	
	private String json;
	
	private Date lastSyncDate;
	
	private String lastSyncTryErr;
	
	private Date lastUpdateDate;
	
	private Integer consistent;
	
	private Integer migrationStatus;
	
	private Integer destinationId;
	
	private List<UniqueKeyInfo> uniqueKeys;
	
	public SyncImportInfoVO() {
		this.migrationStatus = MIGRATION_STATUS_PENDING;
		this.id = Integer.valueOf(0);
	}
	
	@Override
	public void load(ResultSet resultSet) throws SQLException {
		super.load(resultSet);
		
		try {
			this.dateCreated = resultSet.getDate("record_date_created");
		}
		catch (SQLException e) {}
		try {
			this.dateChanged = resultSet.getDate("record_date_changed");
		}
		catch (SQLException e) {}
		try {
			this.dateVoided = resultSet.getDate("record_date_voided");
		}
		catch (SQLException e) {}
	}
	
	@JsonIgnore
	public Integer getDestinationId() {
		return destinationId;
	}
	
	public void setDestinationId(Integer destinationId) {
		this.destinationId = destinationId;
	}
	
	public List<UniqueKeyInfo> getUniqueKeys() {
		return uniqueKeys;
	}
	
	public void setUniqueKeys(List<UniqueKeyInfo> uniqueKeys) {
		this.uniqueKeys = uniqueKeys;
	}
	
	@JsonIgnore
	public Integer getConsistent() {
		return consistent;
	}
	
	@JsonIgnore
	public Oid getRecordOriginIdAsOid() {
		return Oid.fastCreate("", recordOriginId);
	}
	
	public Integer getRecordOriginId() {
		return recordOriginId;
	}
	
	public void setRecordOriginId(Integer recordOriginId) {
		this.recordOriginId = recordOriginId;
	}
	
	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}
	
	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}
	
	@JsonIgnore
	public Integer isConsistent() {
		return consistent;
	}
	
	public void setConsistent(Integer consistent) {
		this.consistent = consistent;
	}
	
	@JsonIgnore
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
	
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	@JsonIgnore
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getJson() {
		return json;
	}
	
	public void setJson(String json) {
		this.json = json;
	}
	
	@JsonIgnore
	public Date getLastSyncDate() {
		return lastSyncDate;
	}
	
	public void setLastSyncDate(Date lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}
	
	@JsonIgnore
	public String getLastSyncTryErr() {
		return lastSyncTryErr;
	}
	
	public void setLastSyncTryErr(String lastSyncTryErr) {
		this.lastSyncTryErr = lastSyncTryErr;
	}
	
	public void setMigrationStatus(Integer migrationStatus) {
		this.migrationStatus = migrationStatus;
	}
	
	public void markAsPartialMigrated() {
		this.migrationStatus = MIGRATION_STATUS_INCOMPLETE;
	}
	
	public void markAsConsistent(TableConfiguration tableInfo, Connection conn) throws DBException {
		SyncImportInfoDAO.markAsConsistent(tableInfo, this, conn);
	}
	
	public void markAsInconsistent(TableConfiguration tableInfo, Connection conn) throws DBException {
		SyncImportInfoDAO.markAsInconsistent(tableInfo, this, conn);
	}
	
	public void markAsPartialMigrated(TableConfiguration tableInfo, String errMsg, Connection conn) throws DBException {
		this.migrationStatus = MIGRATION_STATUS_INCOMPLETE;
		this.lastSyncTryErr = errMsg;
		
		SyncImportInfoDAO.updateMigrationStatus(tableInfo, this, conn);
	}
	
	public void markAsSyncFailedToMigrate(TableConfiguration tableInfo, String errMsg, Connection conn) throws DBException {
		this.migrationStatus = MIGRATION_STATUS_FAILED;
		this.lastSyncTryErr = errMsg;
		
		SyncImportInfoDAO.updateMigrationStatus(tableInfo, this, conn);
	}
	
	public void markAsSyncFailedToMigrate() {
		this.migrationStatus = MIGRATION_STATUS_FAILED;
	}
	
	@JsonIgnore
	public Integer getMigrationStatus() {
		return migrationStatus;
	}
	
	public static List<SyncImportInfoVO> generateFromSyncRecord(List<EtlDatabaseObject> syncRecords,
	        String recordOriginLocationCode, boolean generateRecordJSON) throws DBException {
		List<SyncImportInfoVO> importInfo = new ArrayList<SyncImportInfoVO>();
		
		for (EtlDatabaseObject syncRecord : syncRecords) {
			importInfo.add(generateFromSyncRecord(syncRecord, recordOriginLocationCode, generateRecordJSON));
		}
		
		return importInfo;
	}
	
	public static SyncImportInfoVO generateFromSyncRecord(EtlDatabaseObject syncRecord, String recordOriginLocationCode,
	        boolean generateRecordJSON) throws DBException {
		SyncImportInfoVO syncInfo = new SyncImportInfoVO();
		
		syncInfo.setRecordOriginId(syncRecord.getObjectId().getSimpleValueAsInt());
		syncInfo.setRecordOriginLocationCode(recordOriginLocationCode);
		
		syncInfo.setDateChanged(syncRecord.getDateChanged());
		syncInfo.setDateCreated(syncRecord.getDateCreated());
		syncInfo.setDateVoided(syncRecord.getDateVoided());
		syncInfo.setJson(generateRecordJSON ? utilities.parseToJSON(syncRecord) : null);
		syncInfo.setLastUpdateDate(syncRecord.getDateChanged());
		syncInfo.setUniqueKeys(syncRecord.getUniqueKeysInfo());
		
		return syncInfo;
	}
	
	public static SyncImportInfoVO retrieveFromSyncRecord(TableConfiguration tableConfiguration,
	        EtlDatabaseObject syncRecord, String recordOriginLocationCode, Connection conn) throws DBException {
		SyncImportInfoVO syncInfo = SyncImportInfoDAO.retrieveFromOpenMRSObject(tableConfiguration, syncRecord,
		    recordOriginLocationCode, conn);
		syncRecord.setRelatedSyncInfo(syncInfo);
		
		return syncInfo;
	}
	
	public void sync(TableConfiguration tableInfo, Class<EtlDatabaseObject> objectClass, Connection conn)
	        throws DBException {
		EtlDatabaseObject source = utilities.loadObjectFormJSON(objectClass, this.json);
		source.setRelatedSyncInfo(this);
		try {
			
			if (tableInfo.isDoIntegrityCheckInTheEnd(EtlOperationType.DB_MERGE_FROM_JSON)) {
				if (source.hasParents()) {
					this.markAsConsistent(tableInfo, conn);
				}
				
				if (tableInfo.useSharedPKKey()) {
					refrieveSharedPKKey(tableInfo, source, 0, conn);
				} else {
					source.setObjectId(new Oid());
				}
				
				//Migrate now and ajust later
				SyncImportInfoDAO.refreshLastMigrationTrySyncDate(tableInfo, this, conn);
			} else {
				source.loadDestParentInfo(tableInfo, this.recordOriginLocationCode, conn);
				
				if (source.hasIgnoredParent()) {
					markAsToBeCompletedInFuture(tableInfo, conn);
				} else {
					markAsMigrated(tableInfo, conn);
				}
				
				if (!tableInfo.useSharedPKKey()) {
					source.setObjectId(new Oid());
				}
			}
			
			source.save(tableInfo, conn);
		}
		catch (ParentNotYetMigratedException e) {
			markAsFailedToMigrate(tableInfo, e.getLocalizedMessage(), conn);
		}
		catch (MetadataInconsistentException e) {
			markAsFailedToMigrate(tableInfo, e.getLocalizedMessage(), conn);
		}
		catch (Exception e) {
			markAsFailedToMigrate(tableInfo, e.getLocalizedMessage(), conn);
		}
	}
	
	/**
	 * @param source
	 * @param qtyTry number of attempts before "give up"
	 * @param conn
	 * @throws ParentNotYetMigratedException
	 * @throws DBException
	 */
	private void refrieveSharedPKKey(TableConfiguration tableConfiguration, EtlDatabaseObject source, int qtyTry,
	        Connection conn) throws ParentNotYetMigratedException, DBException {
		try {
			EtlDatabaseObject obj = DatabaseObjectDAO.getByUniqueKeys(source, conn);
			
			if (obj != null) {
				source.setObjectId(obj.getObjectId());
			} else
				throw new ParentNotYetMigratedException(null);
			
		}
		catch (ParentNotYetMigratedException e) {
			
			if (qtyTry > 0) {
				//Wait 10 seconds before try again
				TimeCountDown.sleep(1);
				refrieveSharedPKKey(tableConfiguration, source, --qtyTry, conn);
			} else
				throw e;
		}
	}
	
	public void markAsToBeCompletedInFuture(TableConfiguration tableInfo, Connection conn) throws DBException {
		SyncImportInfoDAO.markAsToBeCompletedInFuture(this, tableInfo, conn);
	}
	
	public void markAsFailedToMigrate(TableConfiguration tableInfo, String errMsg, Connection conn) throws DBException {
		SyncImportInfoDAO.markAsFailedToMigrate(this, tableInfo, errMsg, conn);
	}
	
	public void markAsMigrated(TableConfiguration tableInfo, Connection conn) throws DBException {
		SyncImportInfoDAO.remove(this, tableInfo, conn);
	}
	
	public static List<EtlDatabaseObject> convertAllToOpenMRSObject(TableConfiguration tableInfo,
	        Class<EtlDatabaseObject> objectClass, List<SyncImportInfoVO> toParse, Connection conn) {
		List<EtlDatabaseObject> records = new ArrayList<EtlDatabaseObject>();
		
		for (SyncImportInfoVO imp : toParse) {
			String modifiedJSON = imp.getJson();
			
			//String modifiedJSON = imp.getJson().replaceFirst(imp.retrieveSourcePackageName(tableInfo), tableInfo.getClasspackage());
			
			EtlDatabaseObject rec = null;
			
			try {
				rec = utilities.loadObjectFormJSON(objectClass, modifiedJSON);
			}
			catch (Exception e) {
				
				//try to resolve pathern problems
				modifiedJSON = utilities.resolveScapeCharacter(modifiedJSON);
				rec = utilities.loadObjectFormJSON(objectClass, modifiedJSON);
			}
			
			if (!tableInfo.isMetadata()) {
				rec.setObjectId(new Oid());
			}
			
			rec.setRelatedSyncInfo(imp);
			
			imp.setConsistent(EtlDatabaseObject.INCONSISTENCE_STATUS);
			records.add(rec);
		}
		
		return records;
	}
	
	public EtlDatabaseObject convertToOpenMRSObject(TableConfiguration tableInfo, Connection conn) {
		String modifiedJSON = this.getJson();
		
		EtlDatabaseObject rec = null;
		
		try {
			rec = utilities.loadObjectFormJSON(tableInfo.getSyncRecordClass(tableInfo.getMainApp()), modifiedJSON);
		}
		catch (Exception e) {
			
			//try to resolve pathern problems
			modifiedJSON = utilities.resolveScapeCharacter(modifiedJSON);
			rec = utilities.loadObjectFormJSON(tableInfo.getSyncRecordClass(tableInfo.getMainApp()), modifiedJSON);
		}
		
		if (!tableInfo.isMetadata()) {
			rec.setObjectId(new Oid());
		}
		
		this.setConsistent(EtlDatabaseObject.INCONSISTENCE_STATUS);
		
		return rec;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!(obj instanceof SyncImportInfoVO))
			return false;
		
		SyncImportInfoVO otherObj = (SyncImportInfoVO) obj;
		
		return this.getRecordOriginLocationCode().equalsIgnoreCase(otherObj.getRecordOriginLocationCode())
		        && this.getRecordOriginId() == otherObj.getRecordOriginId();
	}
	
	public void delete(TableConfiguration tableInfo, Connection conn) throws DBException {
		SyncImportInfoDAO.remove(this, tableInfo, conn);
	}
	
	public void save(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		if (getId() > 0) {
			SyncImportInfoDAO.update(this, tableConfiguration, conn);
		} else {
			SyncImportInfoDAO.insert(this, tableConfiguration, conn);
		}
	}
	
	@Override
	public String toString() {
		return " RecordOriginId: " + recordOriginId + ", recordOriginLocationCode: " + recordOriginLocationCode;
	}
	
	public static SyncImportInfoVO chooseMostRecent(List<SyncImportInfoVO> records) {
		SyncImportInfoVO mostRecent = records.get(0);
		
		for (SyncImportInfoVO rec : records) {
			if (rec.getDateChanged() != null) {
				if (mostRecent.getDateChanged() == null) {
					mostRecent = rec;
				} else if (DateAndTimeUtilities.compareTo(rec.getDateChanged(), mostRecent.getDateChanged()) > 0) {
					mostRecent = rec;
				}
			}
			
			if (rec.getDateVoided() != null) {
				if (mostRecent.getDateVoided() == null) {
					mostRecent = rec;
				} else if (DateAndTimeUtilities.compareTo(rec.getDateVoided(), mostRecent.getDateVoided()) > 0) {
					mostRecent = rec;
				}
			}
		}
		
		return mostRecent;
	}
	
	@Override
	public String getObjectName() {
		return this.getClass().getName();
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
	public Oid getObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setObjectId(Oid objectId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<UniqueKeyInfo> getUniqueKeysInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setUniqueKeysInfo(List<UniqueKeyInfo> uniqueKeysInfo) {
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
	public boolean hasIgnoredParent() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void update(TableConfiguration syncTableInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setUuid(String uuid) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasParents() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object getParentValue(ParentTable refInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateFullFilledUpdateSql() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void loadObjectIdData(TableConfiguration tabConf) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EtlDatabaseObject getSharedPkObj() {
		// TODO Auto-generated method stub
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
	public SyncImportInfoVO retrieveRelatedSyncInfo(TableConfiguration tableInfo, String recordOriginLocationCode,
	        Connection conn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public EtlDatabaseObject retrieveParentInDestination(Integer parentId, String recordOriginLocationCode,
	        TableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
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
	public String generateMissingInfo(Map<ParentTableImpl, Integer> missingParents) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void remove(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
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
	public void changeObjectId(TableConfiguration abstractTableConfiguration, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void changeParentForAllChildren(EtlDatabaseObject newParent, TableConfiguration syncTableInfo, Connection conn)
	        throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasExactilyTheSameDataWith(EtlDatabaseObject srcObj) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object getFieldValue(String fieldName) throws ForbiddenOperationException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void fastCreateSimpleNumericKey(long i) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadWithDefaultValues(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void copyFrom(EtlDatabaseObject parentRecordInOrigin) {
		// TODO Auto-generated method stub
		
	}
}
