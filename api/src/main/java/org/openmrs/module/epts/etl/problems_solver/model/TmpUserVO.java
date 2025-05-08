package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class TmpUserVO extends GenericDatabaseObject {
	
	private Integer harmonized;
	
	private Integer processed;
	
	private Integer deletable;
	
	private TableConfiguration usersSyncTableConfiguration;
	
	@Override
	public void loadObjectIdData() throws ForbiddenOperationException {
		this.setObjectId(Oid.fastCreate("user_id", this.getUserId()));
		this.getObjectId().asSimpleKey().setDataType("int");
		this.getObjectId().setFullLoaded(true);
	}
	
	@Override
	public void loadObjectIdData(TableConfiguration tabConf) {
		this.loadObjectIdData();
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
	
	public void setWinnerUserId(Long winnerUserId) {
		setFieldValue("winner_user_id", winnerUserId);
	}
	
	public Long getUserId() {
		Object fV = getFieldValue("user_id");
		
		return fV != null ? Long.parseLong(fV.toString()) : null;
	}
	
	public Long getWinnerUserId() {
		Object fV = getFieldValue("winner_user_id");
		
		return fV != null ? Long.parseLong(fV.toString()) : null;
	}
	
	public void setUsersSyncTableConfiguration(TableConfiguration usersSyncTableConfiguration) {
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
	public void save(TableConfiguration tableConfiguration, ConflictResolutionType onConflict, Connection conn)
	        throws DBException {
		BaseDAO.executeQueryWithRetryOnError(
		    "update tmp_user set deletable = " + this.deletable + " where user_id = " + getUserId(), null, conn);
	}
	
	public void harmonize(Connection conn) throws DBException {
		for (ChildTable child : this.usersSyncTableConfiguration.getChildRefInfo()) {
			
			String sql = "";
			
			sql += " update " + child.getTableName();
			sql += " set 	" + child.getChildColumnOnSimpleMapping() + " = " + this.getWinnerUserId();
			sql += " where  " + child.getChildColumnOnSimpleMapping() + " = " + this.getUserId();
			
			BaseDAO.executeQueryWithRetryOnError(sql, null, conn);
		}
		
		GenericDatabaseObject user = new GenericDatabaseObject(this.usersSyncTableConfiguration);
		
		user.setFieldValue("user_id", this.getUserId());
		user.setUuid(this.getUuid());
		user.remove(conn);
		
		markAsHarmonized(conn);
	}
	
	public void markAsHarmonized(Connection conn) throws DBException {
		setHarmonized(1);
		
		BaseDAO.executeQueryWithRetryOnError(
		    "update tmp_user set harmonized = " + this.harmonized + " where user_id = " + getUserId(), null, conn);
	}
	
	public static TmpUserVO getWinningRecord(List<TmpUserVO> dups, Connection conn) throws DBException {
		for (TmpUserVO dup : dups) {
			if (dup.getUserId().equals(dup.getWinnerUserId())) {
				return dup;
			}
		}
		
		throw new ForbiddenOperationException("No winning record found for " + dups.get(0).getUuid());
	}
	
	public void markAsProcessed(Connection conn) throws DBException {
		setProcessed(1);
		
		BaseDAO.executeQueryWithRetryOnError(
		    "update tmp_user set processed = " + this.processed + " where user_id = " + getUserId(), null, conn);
	}
	
	public boolean isWinning() {
		return this.getUserId().equals(this.getWinnerUserId());
	}
}
