package org.openmrs.module.epts.etl.dbcopy.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.MappedTableInfo;
import org.openmrs.module.epts.etl.dbcopy.controller.DBCopyController;
import org.openmrs.module.epts.etl.dbcopy.model.DBCopySearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DBCopyEngine extends Engine {
	
	public DBCopyEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public DBCopyController getRelatedOperationController() {
		return (DBCopyController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		OpenConnection destConn = getRelatedOperationController().openDestConnection();
		
		if (DBUtilities.isSqlServerDB(destConn)) {
			
			for (MappedTableInfo map : getSyncTableConfiguration().getDestinationTableMappingInfo()) {
				String dstFullTableName = map.generateFullTableName(destConn);
				DBUtilities.executeBatch(destConn, "SET IDENTITY_INSERT " + dstFullTableName + " ON");
			}
		}
		
		String tableName = getSyncTableConfiguration().getTableName();
		try {
			
			logInfo("COPYING  '" + syncRecords.size() + "' " + tableName + " TO DESTINATION DB");
			
			List<DatabaseObject> records = utilities.parseList(syncRecords, DatabaseObject.class);
			
			for (DatabaseObject rec : records) {
				
				for (MappedTableInfo mappingInfo : getSyncTableConfiguration().getDestinationTableMappingInfo()) {
					
					DatabaseObject destObject = null;
					
					destObject = mappingInfo.generateMappedObject(rec, getRelatedOperationController().getDestAppInfo(),
					    conn);
					
					try {
						DatabaseObjectDAO.insertWithObjectId(destObject, destConn);
					}
					catch (DBException e) {
						try {
							boolean connIsClosed = true;
							
							try {
								connIsClosed = destConn.isClosed();
							}
							catch (SQLException e2) {
								e2.printStackTrace();
							}
							
							if (connIsClosed) {
								destConn.finalizeConnection();
								
								logWarn("Connection is closed... the current work will be restarted...");
								
								performeSync(syncRecords, conn);
							} else if (DBUtilities.isPostgresDB(destConn)) {
								/*
								 * PosgresSql fails when you continue to use a connection which previously encountered an exception
								 * So we are committing before try to use the connection again
								 * 
								 * NOTE that we are taking risk if some other bug happen and the transaction need to be aborted
								 */
								try {
									destConn.commit();
								}
								catch (SQLException e1) {
									throw new DBException(e1);
								}
							}
							
							if (e.isDuplicatePrimaryOrUniqueKeyException()) {
								logDebug("Record " + rec.getObjectId() + " alredy on DB");
							} else {
								logError("Error while copying record [" + rec.toString() + "]");
								
								throw e;
							}
						}
						catch (DBException e1) {
							throw e1;
						}
					}
				}
			}
			
			logInfo("'" + syncRecords.size() + "' " + tableName + " COPIED TO DESTINATION DB");
		}
		finally {
			if (DBUtilities.isSqlServerDB(destConn)) {
				for (MappedTableInfo map : getSyncTableConfiguration().getDestinationTableMappingInfo()) {
					String dstFullTableName = map.generateFullTableName(destConn);
					DBUtilities.executeBatch(destConn, "SET IDENTITY_INSERT " + dstFullTableName + " OFF");
				}
			}
			
			destConn.markAsSuccessifullyTerminated();
			destConn.finalizeConnection();
		}
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBCopySearchParams(this.getSyncTableConfiguration(),
		        limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSyncConfiguration().getObservationDate());
		
		return searchParams;
	}
	
}
