package org.openmrs.module.eptssync.utilities.db.conn;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.model.base.BaseDAO;

public class DBOperation {

	public static Logger logger = Logger.getLogger(DBOperation.class);
	
	private String sql;
	private Object[] params;
	private Connection conn;
	private int qtyTry;
	private int maxTry;
	private DBException exception;
	
	public DBOperation(String sql, Object[] params, Connection conn, int maxTry) {
		this.sql = sql;
		this.params = params;
		this.conn = conn;
		this.maxTry = maxTry;
	}
	
	public void retry() throws DBException {
		if (qtyTry < maxTry){
			qtyTry++;
			logger.info("DEADLOCK DETECTED");
			logger.info("RETRYING OPERATION [" +qtyTry+ "] OF ["+maxTry+"]");
			try {
				BaseDAO.executeDBQuery(sql, params, conn);
			} catch (DBException e) {
				this.exception = e;
				
				if (e.getMessage() != null && e.getMessage().contains("ORA-00060")){
					try {Thread.sleep(5000);} catch (InterruptedException e1) {}
					
					retry();
				}else {
					throw new DBException(e);
				}
			}
		}else {
			throw this.exception;
		}
	}
	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	public int getQtyTry() {
		return qtyTry;
	}
	public void setQtyTry(int qtyTry) {
		this.qtyTry = qtyTry;
	}

	public int getMaxTry() {
		return maxTry;
	}

	public void setMaxTry(int maxTry) {
		this.maxTry = maxTry;
	}
}
