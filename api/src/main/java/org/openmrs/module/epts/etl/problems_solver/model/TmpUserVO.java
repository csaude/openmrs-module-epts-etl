package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class TmpUserVO extends GenericDatabaseObject {
	
	private int harmonized;
	
	private int processed;
	
	private Integer winnerUserId;
	
	private int deletable;
	
	private AbstractTableConfiguration usersSyncTableConfiguration;
	
	public void load(ResultSet rs) throws SQLException {
		try {
			setObjectId(Oid.fastCreate("user_id", rs.getInt("user_id")));
		}
		catch (SQLException e) {}
		try {
			setWinnerUserId(rs.getInt("winner_user_id"));
		}
		catch (SQLException e) {}
		try {
			setUuid(rs.getString("user_uuid"));
		}
		catch (SQLException e) {}
	}
	
	public AbstractTableConfiguration getUsersSyncTableConfiguration() {
		return usersSyncTableConfiguration;
	}
	
	public void setDeletable(int deletable) {
		this.deletable = deletable;
	}
	
	public void markAsDeletable() {
		this.deletable = 1;
	}
	
	public void markAsUndeletable() {
		this.deletable = 0;
	}
	
	public void setWinnerUserId(Integer winnerUserId) {
		this.winnerUserId = winnerUserId;
	}
	
	public Integer getWinnerUserId() {
		return winnerUserId;
	}
	
	public void setUsersSyncTableConfiguration(AbstractTableConfiguration usersSyncTableConfiguration) {
		this.usersSyncTableConfiguration = usersSyncTableConfiguration;
	}
	
	public void setHarmonized(int harmonized) {
		this.harmonized = harmonized;
	}
	
	public void setProcessed(int processed) {
		this.processed = processed;
	}
	
	@Override
	public String generateTableName() {
		return "tmp_user";
	}
	
	@Override
	public void save(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		BaseDAO.executeQueryWithRetryOnError(
		    "update tmp_user set deletable = " + this.deletable + " where user_id = " + getObjectId(), null, conn);
	}
	
	public void harmonize(Connection conn) throws DBException {
		utilities.throwReviewMethodException();
		
		/*
		for (RefInfo child : this.usersSyncTableConfiguration.getChildRefInfo()) {
			
			String sql = "";
			
			sql += " update " + child.getChildTableName();
			sql += " set 	" + child.getChildColumnOnSimpleMapping() + " = " + this.getWinnerUserId();
			sql += " where  " + child.getChildColumnOnSimpleMapping() + " = " + this.getObjectId();
			
			BaseDAO.executeQueryWithRetryOnError(sql, null, conn);
		}
		
		UsersVO user = new UsersVO();
		
		user.setUserId(this.getObjectId());
		user.setUuid(this.getUuid());
		user.remove(conn);
		
		markAsHarmonized(conn);*/
	}
	
	public void markAsHarmonized(Connection conn) throws DBException {
		setHarmonized(1);
		
		BaseDAO.executeQueryWithRetryOnError(
		    "update tmp_user set harmonized = " + this.harmonized + " where user_id = " + getObjectId(), null, conn);
	}
	
	public static TmpUserVO getWinningRecord(List<TmpUserVO> dups, Connection conn) throws DBException {
		for (TmpUserVO dup : dups) {
			if (dup.getObjectId().equals(dup.getWinnerUserId())) {
				return dup;
			}
		}
		
		throw new ForbiddenOperationException("No winning record found for " + dups.get(0).getUuid());
	}
	
	public void markAsProcessed(Connection conn) throws DBException {
		setProcessed(1);
		
		BaseDAO.executeQueryWithRetryOnError(
		    "update tmp_user set processed = " + this.processed + " where user_id = " + getObjectId(), null, conn);
	}
	
	public boolean isWinning() {
		return this.getObjectId().equals(this.getWinnerUserId());
	}
	
}
