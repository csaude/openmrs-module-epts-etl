package org.openmrs.module.eptssync.controller.conf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.model.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.OpenMRSClassGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncTableInfo {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	private String tableName;
	private ParentRefInfo mainParentRefInfo;
	private List<ParentRefInfo> otherParentRefInfo;

	private boolean mustRecompileTable;

	private Class<OpenMRSObject> syncRecordClass;

	private SyncTableInfoSource relatedSyncTableInfoSource;

	private List<ParentRefInfo> auxAllParentInfo;

	private String primaryKey;
	
	private String extraConditionForExport;
	
	public SyncTableInfo() {
	}

	public String getExtraConditionForExport() {
		return extraConditionForExport;
	}
	
	public void setExtraConditionForExport(String extraConditionForExport) {
		this.extraConditionForExport = extraConditionForExport;
	}
	
	@JsonIgnore
	public Class<OpenMRSObject> determineMainParentClass() {
		return this.mainParentRefInfo.determineParentClass();
	}

	@JsonIgnore
	public Class<OpenMRSObject> getSyncRecordClass() {
		return syncRecordClass;
	}

	public void setSyncRecordClass(Class<OpenMRSObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}

	public SyncTableInfoSource getRelatedSyncTableInfoSource() {
		return relatedSyncTableInfoSource;
	}

	public void setRelatedSyncTableInfoSource(SyncTableInfoSource relatedSyncTableInfoSource) {
		this.relatedSyncTableInfoSource = relatedSyncTableInfoSource;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
	public String getPrimaryKeyAsClassAtt() {
		return convertTableAttNameToClassAttName(getPrimaryKey());
	}
	
	public String getPrimaryKey() {
		if (primaryKey == null) {
			OpenConnection conn = DBConnectionService.getInstance().openConnection();
			
			try {
				ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, tableName);
	
				rs.next();
				
				this.primaryKey = rs.getString("COLUMN_NAME");
			} catch (SQLException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		return primaryKey;
	}
	
	public boolean hasMainParent() {
		return this.getMainParentRefInfo() != null;
	}
	
	public ParentRefInfo getMainParentRefInfo() {
		if (this.mainParentRefInfo != null) {
			this.mainParentRefInfo.loadFullRefInfo();
		}

		return mainParentRefInfo;
	}

	public String getMainParentTableName() {
		return this.getMainParentRefInfo().getTableName();
	}

	public void setOtherParentRefInfo(List<ParentRefInfo> otherParentRefInfo) {
		this.otherParentRefInfo = otherParentRefInfo;
	}

	public List<ParentRefInfo> getOtherParentRefInfo() {

		if (this.otherParentRefInfo != null) {
			for (ParentRefInfo parentInfo : this.otherParentRefInfo) {
				parentInfo.loadFullRefInfo();
			}
		}

		return otherParentRefInfo;
	}

	public synchronized List<ParentRefInfo> getAllParentInfo() {
		if (this.auxAllParentInfo != null)
			return this.auxAllParentInfo;

		this.auxAllParentInfo = new ArrayList<ParentRefInfo>();

		if (this.getMainParentRefInfo() != null) {
			this.auxAllParentInfo.add(this.getMainParentRefInfo());
		}

		if (this.otherParentRefInfo != null) {
			this.auxAllParentInfo.addAll(utilities.cloneList(this.otherParentRefInfo));
		}

		return this.auxAllParentInfo;
	}

	public String getMainParentReferenceColumn() {
		if (this.getMainParentRefInfo() != null) {
			return this.getMainParentRefInfo().getReferenceColumnName();
		}

		return null;
	}

	public String getMainParentReferencedColumn() {
		if (this.getMainParentRefInfo() != null) {
			return this.getMainParentRefInfo().getReferencedColumnName();
		}

		return null;
	}

	public String getFullMainParentReferenceColumn() {
		return this.mainParentRefInfo.getFullReferenceColumn();
	}

	public String getFullMainParentReferencedColumn() {
		return this.getMainParentRefInfo().getFullParentReferencedColumn();
	}

	public void setMainParentRefInfo(ParentRefInfo mainParentRefInfo) {
		this.mainParentRefInfo = mainParentRefInfo;
	}

	public Class<OpenMRSObject> getRecordClass() {
		return this.syncRecordClass;
	}

	public String getOriginAppLocationCode() {
		return getRelatedSyncTableInfoSource().getOriginAppLocationCode();
	}
	
	private OpenConnection openConnection() {
		return DBConnectionService.getInstance().openConnection();
	}

	public void generateRecordClass() {
		try {
			this.syncRecordClass = OpenMRSClassGenerator.generate(this);
		} catch (ClassNotFoundException e) {
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

	public String generateClassName() {
		return generateClassName(this.tableName);
	}

	private String generateClassName(String tableName) {
		String[] nameParts = tableName.split("_");

		String className = utilities.capitalize(nameParts[0]);

		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}

		return className + "VO";
	}

	public void setMustRecompileTable(boolean mustRecompileTable) {
		this.mustRecompileTable = mustRecompileTable;
	}

	public boolean isMustRecompileTable() {
		return mustRecompileTable;
	}

	public boolean mustRecompileTableClass() {
		return isMustRecompileTable();
	}

	/**
	 * Try to generate on related table the aditional information needed for
	 * synchronization process
	 * 
	 * @throws SQLException
	 */
	public void tryToUpgradeDataBaseInfo() throws SQLException {
		OpenConnection conn = DBConnectionService.getInstance().openConnection();

		if (!existRelatedExportStageTable()) {
			createRelatedExportStageTable();
		}

		if (!isLastSyncDateColumnExistOnTable(conn)) {
			createLastSyncDateOnTable(conn);
		}

		if (!isOriginRecordIdColumnExistOnTable(conn)) {
			createOriginRecordIdColumnOnTable(conn);
		}

		if (!isDateChangedColumnExistOnTable(conn)) {
			createDateChangedColumnOnTable(conn);
		}
	
		if (!isOriginAppLocationCodeColumnExistsOnTable(conn)) {
			createOriginAppLocationCodeColumnOnTable(conn);
		}
		
		if (!isExistRelatedTriggers(conn)) {
			createLastUpdateDateMonitorTrigger(conn);
		}
	}

	private void createRelatedExportStageTable() {
		String sql = "";

		sql += "CREATE TABLE " + getSyncStageSchema() + "." + generateRelatedStageTableName() + "(\n";
		sql += "	id int(11) NOT NULL AUTO_INCREMENT,\n";
		sql += "	sync_table_name VARCHAR(64) NOT NULL,\n";
		sql += "	record_id int(11) NOT NULL,\n";
		sql += "	main_parent_id int(11) NULL,\n";
		sql += "	main_parent_table VARCHAR(64) NULL,\n";
		sql += "	creation_date DATETIME DEFAULT CURRENT_TIMESTAMP,\n";
		sql += "	json VARCHAR(7500) NOT NULL,\n";
		sql += "	origin_app_location_code VARCHAR(100) NOT NULL,\n";
		sql += "	PRIMARY KEY (id)\n";
		sql += ")\n";
		sql += " ENGINE=InnoDB DEFAULT CHARSET=utf8";
		
		OpenConnection conn = openConnection();

		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();

			st.close();

			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		} finally {
			conn.finalizeConnection();
		}
	}

	public String generateRelatedStageTableName() {
		return this.getTableName() + "_stage";
	}

	public String getSyncStageSchema() {
		return getRelatedSyncTableInfoSource().getSyncStageSchema();
	}

	public String generateFullStageTableName() {
		return getSyncStageSchema() + "." + generateRelatedStageTableName();
	}
	
	private boolean existRelatedExportStageTable() {
		OpenConnection conn = openConnection();

		String schema = getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = generateRelatedStageTableName();

		try {
			return DBUtilities.isResourceExist(schema, resourceType, tabName, conn);
		} catch (SQLException e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	private boolean isOriginAppLocationCodeColumnExistsOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "origin_app_location_code", conn);
	}

	private void createOriginAppLocationCodeColumnOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD origin_app_location_code VARCHAR(100) NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
	}

	
	private void createDateChangedColumnOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD date_changed datetime NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
	}

	private boolean isDateChangedColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "date_changed", conn);
	}

	private void createLastUpdateDateMonitorTrigger(OpenConnection conn) throws SQLException {
		Statement st = conn.createStatement();

		st.addBatch(generateTriggerCode(this.generateLastUpdateDateInsertTriggerMonitor(), "INSERT"));
		st.addBatch(generateTriggerCode(this.generateLastUpdateDateUpdateTriggerMonitor(), "UPDATE"));

		st.executeBatch();

		st.close();

	}

	private String generateTriggerCode(String triggerName, String triggerEvent) {
		String sql = "";

		// sql += "DELIMITER $$\n";
		sql += "CREATE TRIGGER " + triggerName + " BEFORE " + triggerEvent + " ON " + this.tableName + "\n";
		sql += "FOR EACH ROW\n";
		sql += "	BEGIN\n";
		sql += "		SET NEW.date_changed = CURRENT_TIMESTAMP();\n";
		sql += "	END;\n";
		// sql += "$$\n";
		// sql += "DELIMITER ;";

		return sql;
	}

	private boolean isExistRelatedTriggers(OpenConnection conn) throws SQLException {
		return DBUtilities.isResourceExist(conn.getCatalog(), DBUtilities.RESOURCE_TYPE_TRIGGER,
				generateLastUpdateDateInsertTriggerMonitor(), conn);
	}

	private String generateLastUpdateDateInsertTriggerMonitor() {
		return this.tableName + "_date_changed_insert_monitor";
	}

	private String generateLastUpdateDateUpdateTriggerMonitor() {
		return this.tableName + "_date_changed_update_monitor";
	}

	private void createLastSyncDateOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD last_sync_date datetime NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
	}

	private boolean isLastSyncDateColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "last_sync_date", conn);
	}

	private void createOriginRecordIdColumnOnTable(Connection conn) throws SQLException {
		String sql = "ALTER TABLE " + this.getTableName() + " ADD origin_record_id int(11) NULL";

		Statement st = conn.createStatement();
		st.addBatch(sql);
		st.executeBatch();

		st.close();
	}

	private boolean isOriginRecordIdColumnExistOnTable(Connection conn) throws SQLException {
		return DBUtilities.isColumnExistOnTable(getTableName(), "origin_record_id", conn);
	}
	
	private static String convertTableAttNameToClassAttName(String tableAttName) {
		return utilities.convertTableAttNameToClassAttName(tableAttName);
	}
}
