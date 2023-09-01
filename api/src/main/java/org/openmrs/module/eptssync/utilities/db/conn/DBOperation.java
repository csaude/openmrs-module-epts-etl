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
		this.qtyTry = 0;
	}
	
	public void retryDueTemporaryDBError(String error) throws DBException {
		if (qtyTry < maxTry) {
			qtyTry++;
			logger.warn(error.toUpperCase() + " DETECTED");
			logger.warn("RETRYING OPERATION [" + qtyTry + "] OF [" + maxTry + "]");
			try {
				BaseDAO.executeQueryWithoutRetry(sql, params, conn);
				
				logger.warn("RECOVERED AFTER " + error.toUpperCase());
			}
			catch (DBException e) {
				this.exception = e;
				
				if (e.isTemporaryDBErrr(conn)) {
					try {
						Thread.sleep(5000);
					}
					catch (InterruptedException e1) {}
					
					retryDueTemporaryDBError(error);
				} else {
					throw e;
				}
			}
		} else {
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
