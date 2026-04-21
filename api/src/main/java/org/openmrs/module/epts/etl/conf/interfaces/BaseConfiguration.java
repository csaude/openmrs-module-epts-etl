package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public interface BaseConfiguration {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	List<Extension> getExtension();
	
	public void setExtension(List<Extension> extension);
	
	default Extension findExtension(String coding) throws ForbiddenOperationException {
		if (!utilities.listHasElement(this.getExtension()))
			throw new ForbiddenOperationException("Not defined extension '" + coding + "");
		
		for (Extension item : this.getExtension()) {
			if (item.getCoding().equals(coding))
				return item;
		}
		
		throw new ForbiddenOperationException("Not defined extension '" + coding + "");
	}
	
	default void finalizeConnection(OpenConnection conn) {
		if (conn != null) {
			conn.finalizeConnection();
		}
	}
	
	default void commitConn(Connection conn) throws DBException {
		if (conn != null) {
			try {
				conn.commit();
			}
			catch (SQLException e) {
				throw new DBException(e);
			}
		}
	}
}
