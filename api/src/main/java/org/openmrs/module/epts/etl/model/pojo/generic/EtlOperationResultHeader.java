package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Contains the information related to database operation on a batch of records
 */
public class EtlOperationResultHeader<T extends EtlDatabaseObject> {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<EtlOperationItemResult<T>> recordsWithUnresolvedErrors;
	
	private List<EtlOperationItemResult<T>> recordsWithResolvedErrors;
	
	private List<T> recordsWithRecursiveRelashionship;
	
	private List<T> recordsWithNoError;
	
	private Exception fatalException;
	
	private IntervalExtremeRecord interval;
	
	public EtlOperationResultHeader(IntervalExtremeRecord interval) {
		this.interval = interval;
	}
	
	public EtlOperationResultHeader(IntervalExtremeRecord interval, Exception fatalException) {
		this.interval = interval;
		this.fatalException = fatalException;
	}
	
	public boolean hasFatalException() {
		return getFatalException() != null;
	}
	
	public List<T> getRecordsWithRecursiveRelashionship() {
		return recordsWithRecursiveRelashionship;
	}
	
	public void setRecordsWithRecursiveRelashionship(List<T> recordsWithRecursiveRelashionship) {
		this.recordsWithRecursiveRelashionship = recordsWithRecursiveRelashionship;
	}
	
	public IntervalExtremeRecord getInterval() {
		return interval;
	}
	
	public void setInterval(IntervalExtremeRecord interval) {
		this.interval = interval;
	}
	
	public void setFatalException(Exception fatalException) {
		this.fatalException = fatalException;
	}
	
	public Exception getFatalException() {
		return fatalException;
	}
	
	public EtlOperationResultHeader(List<T> recordsWithNoError) {
		this.recordsWithNoError = recordsWithNoError;
		
	}
	
	public void addAllFromOtherResult(EtlOperationResultHeader<T> otherResult) {
		if (otherResult != null) {
			if (otherResult.hasRecordsWithNoError()) {
				addAllToRecordsWithNoError(otherResult.getRecordsWithNoError());
			}
			
			if (otherResult.hasRecordsWithUnresolvedErrors()) {
				addAllToRecordsWithUnresolvedErrors(otherResult.getRecordsWithUnresolvedErrors());
			}
			
			if (otherResult.hasRecordsWithResolvedErrors()) {
				addAllToRecordsWithResolvedErrors(otherResult.getRecordsWithResolvedErrors());
			}
			
			if (otherResult.hasRecordsWithRecursiveRelashionships()) {
				addAllToRecordsWithRecursiveRelashionship(otherResult.getRecordsWithRecursiveRelashionship());
			}
		}
	}
	
	private void addAllToRecordsWithResolvedErrors(List<EtlOperationItemResult<T>> records) {
		if (this.recordsWithResolvedErrors == null) {
			this.recordsWithResolvedErrors = new ArrayList<>();
		}
		
		if (utilities.arrayHasElement(records)) {
			for (EtlOperationItemResult<T> rec : records) {
				addToRecordsWithUnresolvedErrors(rec);
			}
		}
	}
	
	private void addAllToRecordsWithRecursiveRelashionship(List<T> records) {
		if (this.recordsWithRecursiveRelashionship == null) {
			this.recordsWithRecursiveRelashionship = new ArrayList<>();
		}
		
		if (utilities.arrayHasElement(records)) {
			for (T rec : records) {
				addAllToRecordsWithRecursiveRelashionship(rec);
			}
		}
	}
	
	public void addAllToRecordsWithUnresolvedErrors(List<EtlOperationItemResult<T>> records) {
		if (this.recordsWithUnresolvedErrors == null) {
			this.recordsWithUnresolvedErrors = new ArrayList<>();
		}
		
		this.recordsWithUnresolvedErrors.addAll(records);
		
		if (utilities.arrayHasElement(records)) {
			for (EtlOperationItemResult<T> rec : records) {
				addToRecordsWithUnresolvedErrors(rec);
			}
		}
		
	}
	
	public void addAllToRecordsWithNoError(List<T> records) {
		if (this.recordsWithNoError == null) {
			this.recordsWithNoError = new ArrayList<>();
		}
		
		if (utilities.arrayHasElement(records)) {
			for (T rec : records) {
				addToRecordsWithNoError(rec);
			}
		}
	}
	
	public void addAllToRecordsWithRecursiveRelashionship(T record) {
		if (this.recordsWithRecursiveRelashionship == null) {
			this.recordsWithRecursiveRelashionship = new ArrayList<>();
		}
		
		if (!this.recordsWithRecursiveRelashionship.contains(record)) {
			this.recordsWithRecursiveRelashionship.add(record);
		}
	}
	
	public void setRecordsWithNoError(List<T> recordsWithNoError) {
		this.recordsWithNoError = recordsWithNoError;
	}
	
	public void addToRecordsWithNoError(T record) {
		if (this.getRecordsWithNoError() == null) {
			this.setRecordsWithNoError(new ArrayList<>());
		}
		
		if (!this.getRecordsWithNoError().contains(record)) {
			this.getRecordsWithNoError().add(record);
		}
	}
	
	public void addToRecordsWithResolvedErrors(T record, DBException e) {
		addToRecordsWithResolvedErrors(new EtlOperationItemResult<T>(record, e));
	}
	
	public void addToRecordsWithResolvedErrors(T record, InconsistenceInfo i) {
		addToRecordsWithResolvedErrors(new EtlOperationItemResult<T>(record, i));
	}
	
	public void addToRecordsWithResolvedErrors(EtlOperationItemResult<T> rec) {
		if (this.recordsWithResolvedErrors == null) {
			this.recordsWithResolvedErrors = new ArrayList<>();
		}
		
		if (!getRecordsWithResolvedErrors().contains(rec)) {
			this.getRecordsWithResolvedErrors().add(rec);
		} else {
			utilities.updateOnArray(getRecordsWithResolvedErrors(), rec, rec);
		}
	}
	
	public void addToRecordsWithUnresolvedErrors(T record, DBException e) {
		addToRecordsWithUnresolvedErrors(new EtlOperationItemResult<T>(record, e));
	}
	
	public void addToRecordsWithUnresolvedErrors(EtlOperationItemResult<T> rec) {
		if (this.recordsWithUnresolvedErrors == null) {
			this.recordsWithUnresolvedErrors = new ArrayList<>();
		}
		
		if (!getRecordsWithUnresolvedErrors().contains(rec)) {
			this.getRecordsWithUnresolvedErrors().add(rec);
		} else {
			utilities.updateOnArray(getRecordsWithUnresolvedErrors(), rec, rec);
		}
	}
	
	public List<EtlOperationItemResult<T>> getRecordsWithResolvedErrors() {
		return recordsWithResolvedErrors;
	}
	
	public List<EtlOperationItemResult<T>> getRecordsWithUnresolvedErrors() {
		return recordsWithUnresolvedErrors;
	}
	
	public List<T> getRecordsWithNoError() {
		return recordsWithNoError;
	}
	
	public boolean hasRecordsWithUnresolvedErrors() {
		return utilities.arrayHasElement(getRecordsWithUnresolvedErrors());
	}
	
	public boolean hasRecordsWithRecursiveRelashionships() {
		return utilities.arrayHasElement(getRecordsWithRecursiveRelashionship());
	}
	
	public boolean hasRecordsWithResolvedErrors() {
		return utilities.arrayHasElement(getRecordsWithResolvedErrors());
	}
	
	public boolean hasRecordsWithNoError() {
		return utilities.arrayHasElement(getRecordsWithNoError());
	}
	
	public boolean hasFatalError() throws DBException {
		
		if (getFatalException() != null) {
			return true;
		} else {
			
			return hasRecordsWithUnresolvedErrors();
		}
	}
	
	public void documentErrors(Connection srcConn, Connection dstConn) throws DBException {
		
		List<EtlOperationItemResult<T>> toDocument = new ArrayList<>();
		
		if (hasRecordsWithUnresolvedErrors()) {
			toDocument.addAll(getRecordsWithUnresolvedErrors());
		}
		
		if (hasRecordsWithResolvedErrors()) {
			toDocument.addAll(getRecordsWithResolvedErrors());
		}
		
		for (EtlOperationItemResult<T> r : toDocument) {
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
		if (getFatalException() != null) {
			getFatalException().printStackTrace();
		} else {
			
			for (EtlOperationItemResult<T> r : getRecordsWithUnresolvedErrors()) {
				if (r.hasException()) {
					r.getException().printStackTrace();
				}
			}
		}
	}
	
	public void throwDefaultExcetions() throws RuntimeException {
		if (hasFatalException()) {
			throw new RuntimeException(getFatalException());
		}
		
		for (EtlOperationItemResult<T> o : getRecordsWithUnresolvedErrors()) {
			if (o.getException() != null) {
				throw new RuntimeException(o.getException());
			}
		}
		
		throw new ForbiddenOperationException("No exception found");
	}
	
	public int countAllSuccessfulyProcessedRecords() {
		return utilities.arraySize(getRecordsWithNoError()) + utilities.arraySize(getRecordsWithResolvedErrors());
	}
	
	public List<T> getAllSuccessfulyProcessedRecords() {
		List<T> success = new ArrayList<>();
		
		if (hasRecordsWithNoError()) {
			success.addAll(getRecordsWithNoError());
		}
		
		if (hasRecordsWithResolvedErrors()) {
			for (EtlOperationItemResult<T> r : getRecordsWithResolvedErrors()) {
				success.add(r.getRecord());
			}
		}
		
		return success;
	}
	
	public static <T extends EtlDatabaseObject> boolean hasAtLeastOneFatalError(List<EtlOperationResultHeader<T>> results)
	        throws DBException {
		
		for (EtlOperationResultHeader<T> result : results) {
			if (result.hasFatalError()) {
				return true;
			}
		}
		
		return false;
		
	}
	
	public static <T extends EtlDatabaseObject> EtlOperationResultHeader<T> getDefaultResultWithFatalError(
	        List<EtlOperationResultHeader<T>> results) throws DBException {
		for (EtlOperationResultHeader<T> result : results) {
			if (result.hasFatalError()) {
				return result;
			}
		}
		
		return null;
	}
	
	public static <T extends EtlDatabaseObject> boolean hasAtLeastOneRecordsWithRecursiveRelashionships(
	        List<EtlOperationResultHeader<T>> results) {
		
		for (EtlOperationResultHeader<T> result : results) {
			if (result.hasRecordsWithRecursiveRelashionships()) {
				return true;
			}
		}
		
		return false;
	}
	
}
