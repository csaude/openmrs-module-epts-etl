package org.openmrs.module.epts.etl.problems_solver.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.epts.etl.problems_solver.model.TmpUserVO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class ProblemsSolverEngineMissingUserRoles extends GenericEngine {
	
	DatabasesInfo[] fghDBInfo = {
			/*new DatabasesInfo("Server 24", DatabasesInfo.DB_NAMES_24, new DBConnectionInfo("root", "root", "jdbc:mysql://10.0.0.24:3307/openmrs_gile_alto_ligonha?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")),
			new DatabasesInfo("Server 23", DatabasesInfo.DB_NAMES_23, new DBConnectionInfo("root", "0pen10mrs4FGh", "jdbc:mysql://10.0.0.23:3307/export_db_lugela_mulide?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")),
			new DatabasesInfo("Server 22", DatabasesInfo.DB_NAMES_22, new DBConnectionInfo("root", "Fgh397$@Wy$Q7", "jdbc:mysql://10.0.0.22:3307/openmrs_derre?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")) ,
			new DatabasesInfo("Server 21", DatabasesInfo.DB_NAMES_21, new DBConnectionInfo("root", "root", "jdbc:mysql://10.0.0.21:3307/openmrs_ile_mugulama?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver"))
			*/ };
			
	DatabasesInfo[] DBsInfo = {
			//new DatabasesInfo("Echo Central Server", DatabasesInfo.DB_NAMES_ECHO, new DBConnectionInfo("root", "root", "jdbc:mysql://10.0.0.24:3307/openmrs_gile_alto_ligonha?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")),
	};
	
	@SuppressWarnings("unused")
	private AbstractTableConfiguration userRoleTableConf;
	
	@SuppressWarnings("unused")
	private Class<? extends EtlDatabaseObject> userRoleRecordClass;
	
	public ProblemsSolverEngineMissingUserRoles(Engine monitor, ThreadRecordIntervalsManager limits) {
		super(monitor, limits);
		
		utilities.throwReviewMethodException();
		
		/*
		this.userRoleTableConf = AbstractTableConfiguration.initGenericTabConf("user_role",
			getEtlConfiguration().getSrcConf(), getEtlConfiguration().getSrcConf());
		this.userRoleRecordClass = userRoleTableConf.getSyncRecordClass(getDefaultApp());*/
	}
	
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		logInfo("RESOLVING PROBLEM MERGE ON " + etlObjects.size() + "' " + getMainSrcTableName());
		
		int i = 1;
		
		for (TmpUserVO record : utilities.parseList(etlObjects, TmpUserVO.class)) {
			boolean found = false;
			
			try {
				for (DatabasesInfo dbsInfo : DBsInfo) {
					String startingStrLog = utilities.garantirXCaracterOnNumber(i,
					    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
					
					logInfo(startingStrLog + " TRYING TO RETRIVE DATA FOR RECORD [" + record + "] ON SERVER "
					        + dbsInfo.getServerName());
					
					found = performeOnServer(record, dbsInfo, conn);
					
					if (found)
						break;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				i++;
				record.markAsProcessed(conn);
				if (found) {
					((TmpUserVO) record).markAsHarmonized(conn);
				} else {
					logError("Not found [" + record + "]");
					
					//throw new ForbiddenOperationException("Not found");
				}
			}
		}
	}
	
	private boolean performeOnServer(TmpUserVO record, DatabasesInfo dbInfo, Connection conn) throws DBException {
		utilities.throwReviewMethodException();

		return false;
		/*
		boolean found = false;
		
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Retrieving user on [" + dbName + "]");
			
			UsersVO userOnSrc = new UsersVO();
			userOnSrc.setUuid(record.getUuid());
			
			userOnSrc = (UsersVO) DatabaseObjectDAO.getByUniqueKeysOnSpecificSchema(getMainSrcTableConf(), userOnSrc,
			    dbName, srcConn);
			
			if (userOnSrc == null) {
				logDebug("The user was not found on [" + dbName + "], Skipping role check");
				continue;
			}
			
			List<EtlDatabaseObject> roles = DatabaseObjectDAO.getByParentIdOnSpecificSchema(this.userRoleRecordClass, "user_id",
			    userOnSrc.getObjectId(), dbName, srcConn);
			
			if (utilities.arrayHasElement(roles)) {
				
				logInfo("RESOLVING USER PROBLEM USING DATA FROM [" + dbName + "]");
				
				List<UserRoleVO> userRoles = utilities.parseList(roles, UserRoleVO.class);
				
				for (UserRoleVO userRole : userRoles) {
					userRole.setUserId(record.getObjectId());
					try {
						logDebug("Saving Role " + userRole.getRole() + " for Record [" + record + "]");
						userRole.save(userRoleTableConf, conn);
						logDebug("Role " + userRole.getRole() + " for Record [" + record + "] saved!");
					}
					catch (DBException e) {
						e.printStackTrace();
					}
				}
				
				found = true;
				
				break;
			} else {
				logDebug("NO ROLE FOUND ON [" + dbName + "]");
			}
		}
		
		return found;*/
	}
	
	protected void resolveDuplicatedUuidOnUserTable(List<EtlObject> etlObjects, Connection conn)
	        throws DBException, ForbiddenOperationException {
		logDebug("RESOLVING PROBLEM MERGE ON " + etlObjects.size() + "' " + getMainSrcTableName());
		
		int i = 1;
		
		List<EtlObject> recordsToIgnoreOnStatistics = new ArrayList<EtlObject>();
		
		for (EtlObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			EtlDatabaseObject rec = (EtlDatabaseObject) record;
			
			List<EtlDatabaseObject> dups = new ArrayList<EtlDatabaseObject>();//DatabaseObjectDAO.getByUuid(getSyncTableConfiguration().getSyncRecordClass(getDefaultApp()), rec.getUuid(), conn);
			
			logDebug(startingStrLog + " RESOLVING..." + rec);
			
			for (int j = 1; j < dups.size(); j++) {
				EtlDatabaseObject dup = dups.get(j);
				
				dup.setUuid(dup.getUuid() + "_" + j);
				
				dup.save(getSrcConf(), conn);
			}
			
			i++;
		}
		
		if (utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
			logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
			etlObjects.removeAll(recordsToIgnoreOnStatistics);
		}
		
		logDebug("MERGE DONE ON " + etlObjects.size() + " " + getMainSrcTableName() + "!");
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadRecordIntervalsManager limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new ProblemsSolverSearchParams(this.getEtlConfiguration(),
		        null, this);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
