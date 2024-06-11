package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
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
	
	public void addToRecordsWithNoError(EtlDatabaseObject record) {
		if (this.recordsWithNoError == null) {
			this.recordsWithNoError = new ArrayList<>();
		}
		
		this.recordsWithNoError.add(record);
	}
	
	public void addToRecordsWithUnresolvedErrors(EtlDatabaseObject record, DBException e) {
		if (this.recordsWithUnresolvedErrors == null) {
			this.recordsWithUnresolvedErrors = new ArrayList<>();
		}
		
		this.recordsWithUnresolvedErrors.add(new DatabaseOperationItemResult(record, e));
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
			if (r.getException().isIntegrityConstraintViolationException()) {
				MissingParentException e = (MissingParentException) r.getException();
				
				EtlDatabaseObject rec = r.getRecord();
				
				InconsistenceInfo inconsistenceInfo = InconsistenceInfo.generate(rec.generateTableName(), rec.getObjectId(),
				    e.getParentTable(), e.getParentId(), null, e.getOriginAppLocationConde());
				
				inconsistenceInfo.save((TableConfiguration) rec.getRelatedConfiguration(), srcConn);
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
			
			r.getException().printStackTrace();
		}
	}
	
}
