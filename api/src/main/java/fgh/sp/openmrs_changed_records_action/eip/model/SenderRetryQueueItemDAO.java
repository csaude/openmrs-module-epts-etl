package fgh.sp.openmrs_changed_records_action.eip.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SenderRetryQueueItemDAO extends BaseDAO{
	
	public static void insert(SenderRetryQueueItem record, Connection conn) throws DBException{
		try {
			Object[] params = {record.getEvent().getTableName(),
							   record.getEvent().getPrimaryKeyId(),
							   record.getEvent().getIdentifier(),
							   record.getEvent().getOperation(),
							   record.getAttemptCount(),
							   record.getEvent().getSnapshot(),
							   record.getMessage(),
							   record.getDateCreated(),
							   record.getDateChanged(),
							   "java.lang.Exception"
							 };
			
			String sql = "";
			
			sql += "INSERT INTO sender_retry_queue(	TABLE_NAME,\n";
			sql += "									PRIMARY_KEY_ID,\n";
			sql += "									IDENTIFIER,\n";
			sql += "									OPERATION,\n";
			sql += "									ATTEMPT_COUNT,\n";
			sql += "									SNAPSHOT,\n";
			sql += "									MESSAGE,\n";
			sql += "									DATE_CREATED,\n";
			sql += "									DATE_CHANGED,\n";
			sql += "									EXCEPTION_TYPE)\n";
			sql += "	VALUES(?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?);";
			
			executeQuery(sql, params, conn);
		} catch (DBException e) {
			if (!e.isDuplicatePrimaryKeyException()) {
				throw e;
			}
		}
	}
	
	
	public static SenderRetryQueueItem getByUUID(String recordUuid, Connection conn) throws DBException {
		Object[] params = {recordUuid};
		
		String sql = "";
		
		sql += " SELECT * ";
		sql += " FROM sender_retry_queue";
		sql += " WHERE identifier = ? ";
		
		return find(SenderRetryQueueItem.class, sql, params, conn);
	}
}
