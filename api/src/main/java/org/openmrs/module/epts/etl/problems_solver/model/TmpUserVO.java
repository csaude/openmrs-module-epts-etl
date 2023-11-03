package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.openmrs._default.UsersVO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;


public class TmpUserVO extends GenericDatabaseObject {
	private int deletable;
	private int harmonized;
	private int processed;
	
	private SyncTableConfiguration usersSyncTableConfiguration;
	
	public void load(ResultSet rs) throws SQLException{ 
		try {
			setObjectId(rs.getInt("user_id"));
			setUuid(rs.getString("user_uuid"));
			
		} catch (SQLException e) {}
	}
	
	public void setUsersSyncTableConfiguration(SyncTableConfiguration usersSyncTableConfiguration) {
		this.usersSyncTableConfiguration = usersSyncTableConfiguration;
	}
	
	public void setDeletable(int deletable) {
		this.deletable = deletable;
	}
	
	public void setHarmonized(int harmonized) {
		this.harmonized = harmonized;
	}

	public void markAsDeletable() {
		this.deletable = 1;
	}
	

	public void markAsUndeletable() {
		this.deletable = 0;
	}	
	
	public void setProcessed(int processed) {
		this.processed = processed;
	}

	@Override
	public String generateTableName() {
		return "tmp_user";
	}
	
	@Override
	public void save(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		BaseDAO.executeQueryWithRetryOnError("update tmp_user set deletable = " + this.deletable + " where user_id = " + getObjectId(), null, conn);
	}

	public void harmonize(TmpUserVO dup, Connection conn) throws DBException{
		for (RefInfo child : this.usersSyncTableConfiguration.getChildred()) {
			
			String sql = "";
			
			sql += " update " + child.getRefTableConfiguration().getTableName();
			sql += " set 	" + child.getRefColumnName() + " = " + this.getObjectId();
			sql += " where  " + child.getRefColumnName() + " = " + dup.getObjectId();
			
			BaseDAO.executeQueryWithRetryOnError(sql, null, conn);
		}
		
		UsersVO user = new UsersVO();
		
		user.setUserId(dup.getObjectId());
		user.setUuid(dup.getUuid());
		user.remove(conn);
		
		markAsHarmonized(conn);
	}

	public void markAsHarmonized(Connection conn) throws DBException {
		setHarmonized(1);
		
		BaseDAO.executeQueryWithRetryOnError("update tmp_user set harmonized = " + this.harmonized + " where user_id = " + getObjectId(), null, conn);
	}

	public static TmpUserVO getWinningRecord(List<TmpUserVO> dups, Connection conn) throws DBException {
		for (TmpUserVO dup : dups) {
			List<UsersVO> wins = null; //DatabaseObjectDAO.getByUuid(UsersVO.class, dup.getUuid(), conn);
		
			if (wins != null && !wins.isEmpty()) return dup;
		}
		
		throw new ForbiddenOperationException("No winning record found for " + dups.get(0).getUuid());
	}

	public void markAsProcessed(Connection conn) throws DBException {
		setProcessed(1);
		
		BaseDAO.executeQueryWithRetryOnError("update tmp_user set processed = " + this.processed + " where user_id = " + getObjectId(), null, conn);
	}
	
	
	
}
