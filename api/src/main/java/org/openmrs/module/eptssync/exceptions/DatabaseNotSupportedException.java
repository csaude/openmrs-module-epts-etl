package org.openmrs.module.eptssync.exceptions;

import java.sql.Connection;

import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;

public class DatabaseNotSupportedException extends SyncExeption {
	private static final long serialVersionUID = 5037844207229381866L;

	public DatabaseNotSupportedException(Connection conn) throws DBException {
		super("Database not supported [" + DBUtilities.determineDataBaseFromConnection(conn) + "]");
	}
}
