package org.openmrs.module.epts.etl.databasepreparation.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

public class DatabasePreparationRecord implements EtlDatabaseObject{

	private AbstractTableConfiguration tableConfiguration;
	
	public DatabasePreparationRecord(AbstractTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}
	
	public AbstractTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
	}

	@Override
	public String generateTableName() {
		return null;
	}

	@Override
	public boolean isExcluded() {
		return false;
	}

	@Override
	public void setExcluded(boolean excluded) {
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
	public void save(TableConfiguration syncTableInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
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
	public Date getDateChanged() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDateVoided() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDateCreated() {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public EtlDatabaseObject getSrcRelatedObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSrcRelatedObject(EtlDatabaseObject srcRelatedObject) {
		// TODO Auto-generated method stub
		
	}
}
