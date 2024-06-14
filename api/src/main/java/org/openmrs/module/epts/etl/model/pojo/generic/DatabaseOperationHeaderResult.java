package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Contains the information related to database operation on a batch of records
 */
public class DatabaseOperationHeaderResult {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<DatabaseOperationItemResult> recordsWithUnresolvedErrors;
	
	private List<EtlDatabaseObject> recordsWithNoError;
	
	public void addAllFromOtherResult(DatabaseOperationHeaderResult otherResult) {
		if (otherResult != null) {
			if (otherResult.hasRecordsWithNoError()) {
				addAllToRecordsWithNoError(otherResult.getRecordsWithNoError());
			}
			
			if (otherResult.hasRecordsWithUnresolvedErrors()) {
				addAllToRecordsWithUnresolvedErrors(otherResult.getRecordsWithUnresolvedErrors());
			}
		}
	}
	
	private void addAllToRecordsWithUnresolvedErrors(List<DatabaseOperationItemResult> records) {
		if (this.recordsWithUnresolvedErrors == null) {
			this.recordsWithUnresolvedErrors = new ArrayList<>();
		}
		
		this.recordsWithUnresolvedErrors.addAll(records);
	}
	
	public void addAllToRecordsWithNoError(List<EtlDatabaseObject> records) {
		if (this.recordsWithNoError == null) {
			this.recordsWithNoError = new ArrayList<>();
		}
		
		this.recordsWithNoError.addAll(records);
	}
	
	public void setRecordsWithNoError(List<EtlDatabaseObject> recordsWithNoError) {
		this.recordsWithNoError = recordsWithNoError;
	}
	
	public void addToRecordsWithNoError(EtlDatabaseObject record) {
		if (this.getRecordsWithNoError() == null) {
			this.setRecordsWithNoError(new ArrayList<>());
		}
		
		if (!this.getRecordsWithNoError().contains(record)) {
			this.getRecordsWithNoError().add(record);
		}
	}
	
	public void addToRecordsWithUnresolvedErrors(EtlDatabaseObject record, DBException e) {
		addToRecordsWithUnresolvedErrors(new DatabaseOperationItemResult(record, e));
	}
	
	public void addToRecordsWithUnresolvedErrors(DatabaseOperationItemResult rec) {
		if (this.recordsWithUnresolvedErrors == null) {
			this.recordsWithUnresolvedErrors = new ArrayList<>();
		}
		
		if (!getRecordsWithUnresolvedErrors().contains(rec)) {
			this.getRecordsWithUnresolvedErrors().add(rec);
		} else {
			utilities.updateOnArray(getRecordsWithUnresolvedErrors(), rec, rec);
		}
	}
	
	public List<DatabaseOperationItemResult> getRecordsWithUnresolvedErrors() {
		return recordsWithUnresolvedErrors;
	}
	
	public List<EtlDatabaseObject> getRecordsWithNoError() {
		return recordsWithNoError;
	}
	
	public boolean hasRecordsWithUnresolvedErrors() {
		return utilities.arrayHasElement(getRecordsWithUnresolvedErrors());
	}
	
	public boolean hasRecordsWithNoError() {
		return utilities.arrayHasElement(getRecordsWithNoError());
	}
	
	public boolean hasFatalError() throws DBException {
		if (!hasRecordsWithUnresolvedErrors())
			return false;
		
		for (DatabaseOperationItemResult r : getRecordsWithUnresolvedErrors()) {
			if (r.hasFatalError()) {
				return true;
			}
		}
		
		return false;
	}
	
	public void documentErrors(Connection srcConn, Connection dstConn) throws DBException {
		
		for (DatabaseOperationItemResult r : getRecordsWithUnresolvedErrors()) {
			if (r.hasInconsistences()) {
				for (InconsistenceInfo i : r.getInconsistenceInfo()) {
					i.save((TableConfiguration) r.getRecord().getRelatedConfiguration(), srcConn);
				}
			} else {
				
				EtlConfigurationTableConf etlErr = r.getRecord().getRelatedConfiguration().getRelatedSyncConfiguration()
				        .getEtlRecordErrorTabCof();
				
				if (!etlErr.isFullLoaded()) {
					etlErr.setTableAlias(etlErr.getTableName());
					etlErr.fullLoad(srcConn);
				}
				
				EtlDatabaseObject obj = etlErr.createRecordInstance();
				obj.setRelatedConfiguration(etlErr);
				
				obj.setFieldValue("record_id", r.getRecord().getObjectId().getSimpleValue());
				obj.setFieldValue("table_name", r.getRecord().generateTableName());
				obj.setFieldValue("origin_location_code",
				    r.getRecord().getRelatedConfiguration().getRelatedSyncConfiguration().getOriginAppLocationCode());
				obj.setFieldValue("exception", r.getException().getClass().getName());
				obj.setFieldValue("exception_description", r.getException().getLocalizedMessage());
				obj.setFieldValue("table_name", r.getRecord().generateTableName());
				
				obj.save(etlErr, srcConn);
			}
		}
		
	}
	
	public void printStackErrorOfFatalErrors() {
		for (DatabaseOperationItemResult r : getRecordsWithUnresolvedErrors()) {
			if (r.hasException()) {
				r.getException().printStackTrace();
			}
		}
	}
	
}
