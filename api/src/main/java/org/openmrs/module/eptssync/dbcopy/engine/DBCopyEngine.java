package org.openmrs.module.eptssync.dbcopy.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.MappedTableInfo;
import org.openmrs.module.eptssync.dbcopy.controller.DBCopyController;
import org.openmrs.module.eptssync.dbcopy.model.DBCopySearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

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
		
		/*TmpSQLServerConnectionMonitor cm = new TmpSQLServerConnectionMonitor(destConn, this);
		
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor("Conn Monitor " + destConn.getId());
		executor.execute(cm);*/
		
		if (DBUtilities.isSqlServerDB(destConn)) {
			String dstFullTableName = getSyncTableConfiguration().getMappedTableInfo().generateFullTableName(destConn);
			DBUtilities.executeBatch(destConn, "SET IDENTITY_INSERT " + dstFullTableName + " ON");
		}
		
		String tableName = getSyncTableConfiguration().getTableName();
		try {
			
			logInfo("COPYING  '" + syncRecords.size() + "' " + tableName + " TO DESTINATION DB");
			
			List<DatabaseObject> records = utilities.parseList(syncRecords, DatabaseObject.class);
			
			for (DatabaseObject rec : records) {
				DatabaseObject destObject = null;
				
				MappedTableInfo mappingInfo = getSyncTableConfiguration().getMappedTableInfo();
				
				destObject = mappingInfo.generateMappedObject(rec, getRelatedOperationController().getDestAppInfo());
				
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
						}
						else
						if (DBUtilities.isPostgresDB(destConn)) {
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
						logWarn("An error ocurred");
						logError("----------------------------------------------------------------------------------------------------------------------------");
						e.printStackTrace();
						System.out.println();
						logError("----------------------------------------------------------------------------------------------------------------------------");
						logError("----------------------------------------------------------------------------------------------------------------------------");
						e1.printStackTrace();
					}
				}
			}
			
			logInfo("'" + syncRecords.size() + "' " + tableName + " COPIED TO DESTINATION DB");
		}
		finally {
			if (DBUtilities.isSqlServerDB(destConn)) {
				String dstFullTableName = getSyncTableConfiguration().getMappedTableInfo().generateFullTableName(destConn);
				DBUtilities.executeBatch(destConn, "SET IDENTITY_INSERT " + dstFullTableName + " OFF");
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
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
	
}
