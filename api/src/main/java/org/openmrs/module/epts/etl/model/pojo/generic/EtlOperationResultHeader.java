package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.EtlException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.parseToCSV;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Contains the information related to database operation on a batch of records
 */
public class EtlOperationResultHeader<T extends EtlDatabaseObject> {
	
	private static parseToCSV utilities = parseToCSV.getInstance();
	
	private List<EtlOperationItemResult<T>> recordsWithUnexpectedErrors;
	
	private List<EtlOperationItemResult<T>> recordsWithResolvedInconsistence;
	
	private List<EtlOperationItemResult<T>> recordsWithUnresolvedInconsistence;
	
	private List<EtlOperationItemResult<T>> recordsWithRecursiveRelashionship;
	
	private List<EtlOperationItemResult<T>> recordsWithNoError;
	
	private Exception fatalException;
	
	private IntervalExtremeRecord interval;
	
	public EtlOperationResultHeader(IntervalExtremeRecord interval) {
		this.interval = interval;
	}
	
	public EtlOperationResultHeader(IntervalExtremeRecord interval, Exception fatalException) {
		this.interval = interval;
		this.fatalException = fatalException;
	}
	
	public EtlOperationResultHeader(List<T> recordsWithNoError) {
		this.recordsWithNoError = EtlOperationItemResult.parseFromEtlDatabaseObject(recordsWithNoError);
	}
	
	public List<EtlOperationItemResult<T>> getRecordsWithUnexpectedErrors() {
		return recordsWithUnexpectedErrors;
	}
	
	public void setRecordsWithUnexpectedErrors(List<EtlOperationItemResult<T>> recordsWithUnexpectedErrors) {
		this.recordsWithUnexpectedErrors = recordsWithUnexpectedErrors;
	}
	
	public List<EtlOperationItemResult<T>> getRecordsWithResolvedInconsistences() {
		return recordsWithResolvedInconsistence;
	}
	
	public void setRecordsWithResolvedInconsistence(List<EtlOperationItemResult<T>> recordsWithResolvedInconsistence) {
		this.recordsWithResolvedInconsistence = recordsWithResolvedInconsistence;
	}
	
	public List<EtlOperationItemResult<T>> getRecordsWithUnresolvedInconsistences() {
		return recordsWithUnresolvedInconsistence;
	}
	
	public void setRecordsWithUnresolvedInconsistence(List<EtlOperationItemResult<T>> recordsWithUnresolvedInconsistence) {
		this.recordsWithUnresolvedInconsistence = recordsWithUnresolvedInconsistence;
	}
	
	public List<EtlOperationItemResult<T>> getRecordsWithRecursiveRelashionship() {
		return recordsWithRecursiveRelashionship;
	}
	
	public void setRecordsWithRecursiveRelashionship(List<EtlOperationItemResult<T>> recordsWithRecursiveRelashionship) {
		this.recordsWithRecursiveRelashionship = recordsWithRecursiveRelashionship;
	}
	
	public List<EtlOperationItemResult<T>> getRecordsWithNoError() {
		return recordsWithNoError;
	}
	
	public void setRecordsWithNoError(List<EtlOperationItemResult<T>> recordsWithNoError) {
		this.recordsWithNoError = recordsWithNoError;
	}
	
	public Exception getFatalException() {
		return fatalException;
	}
	
	public void setFatalException(Exception fatalException) {
		this.fatalException = fatalException;
	}
	
	public IntervalExtremeRecord getInterval() {
		return interval;
	}
	
	public void setInterval(IntervalExtremeRecord interval) {
		this.interval = interval;
	}
	
	public boolean hasFatalException() {
		return getFatalException() != null;
	}
	
	public boolean hasRecordsWithUnexpectedErrors() {
		return utilities.arrayHasElement(getRecordsWithUnexpectedErrors());
	}
	
	public boolean hasRecordsWithRecursiveRelashionships() {
		return utilities.arrayHasElement(getRecordsWithRecursiveRelashionship());
	}
	
	public boolean hasRecordsWithResolvedInconsistences() {
		return utilities.arrayHasElement(getRecordsWithResolvedInconsistences());
	}
	
	public boolean hasRecordsWithUnresolvedInconsistences() {
		return utilities.arrayHasElement(getRecordsWithUnresolvedInconsistences());
	}
	
	public boolean hasRecordsWithNoError() {
		return utilities.arrayHasElement(getRecordsWithNoError());
	}
	
	public boolean hasFatalError() throws DBException {
		
		if (getFatalException() != null) {
			return true;
		} else {
			
			return hasRecordsWithUnexpectedErrors();
		}
	}
	
	public void addAllFromOtherResult(EtlOperationResultHeader<T> otherResult) {
		if (otherResult != null) {
			if (otherResult.hasRecordsWithNoError()) {
				addAllToRecordsWithNoError(otherResult.getRecordsWithNoError());
			}
			
			if (otherResult.hasRecordsWithUnexpectedErrors()) {
				addAllToRecordsWithUnexpectedErrors(otherResult.getRecordsWithUnexpectedErrors());
			}
			
			if (otherResult.hasRecordsWithResolvedInconsistences()) {
				addToRecordsWithResolvedInconsistences(otherResult.getRecordsWithResolvedInconsistences());
			}
			
			if (otherResult.hasRecordsWithUnresolvedInconsistences()) {
				addToRecordsWithUnresolvedInconsistences(otherResult.getRecordsWithUnresolvedInconsistences());
			}
			
			if (otherResult.hasRecordsWithRecursiveRelashionships()) {
				addAllToRecordsWithRecursiveRelashionship(otherResult.getRecordsWithRecursiveRelashionship());
			}
		}
	}
	
	public void addAllToRecordsWithNoError(List<EtlOperationItemResult<T>> records) {
		addAll(records, EtlOperationResultItemType.NO_ERROR);
	}
	
	public void addAllToRecordsWithUnexpectedErrors(List<EtlOperationItemResult<T>> records) {
		addAll(records, EtlOperationResultItemType.UNEXPECTED_ERRORS);
	}
	
	public void addToRecordsWithResolvedInconsistences(List<EtlOperationItemResult<T>> records) {
		addAll(records, EtlOperationResultItemType.RESOLVED_INCONSISTENCES);
	}
	
	public void addToRecordsWithUnresolvedInconsistences(List<EtlOperationItemResult<T>> records) {
		addAll(records, EtlOperationResultItemType.UNRESOLVED_INCONSISTENCES);
	}
	
	public void addAllToRecordsWithRecursiveRelashionship(List<EtlOperationItemResult<T>> records) {
		addAll(records, EtlOperationResultItemType.RECURSIVE_RELATIONSHIPS);
	}
	
	public void addToRecordsWithNoError(EtlOperationItemResult<T> record) {
		add(record, EtlOperationResultItemType.NO_ERROR);
	}
	
	public void addToRecordsWithUnresolvedErrors(EtlOperationItemResult<T> records) {
		add(records, EtlOperationResultItemType.UNEXPECTED_ERRORS);
	}
	
	public void addAllToRecordsWithResolvedInconsistences(EtlOperationItemResult<T> records) {
		add(records, EtlOperationResultItemType.RESOLVED_INCONSISTENCES);
	}
	
	public void addAllToRecordsWithUnresolvedInconsistences(EtlOperationItemResult<T> records) {
		add(records, EtlOperationResultItemType.UNRESOLVED_INCONSISTENCES);
	}
	
	public void addAllToRecordsWithRecursiveRelashionship(EtlOperationItemResult<T> records) {
		add(records, EtlOperationResultItemType.RECURSIVE_RELATIONSHIPS);
	}
	
	public void addToRecordsWithNoError(T record) {
		addToRecordsWithNoError(new EtlOperationItemResult<T>(record));
	}
	
	public void addToRecordsWithUnresolvedErrors(T record) {
		addToRecordsWithUnresolvedErrors(new EtlOperationItemResult<T>(record));
	}
	
	public void addToRecordsWithUnresolvedErrors(T records, EtlException e) {
		add(records, e);
	}
	
	public void addToRecordsWithRecursiveRelashionship(T record) {
		addAllToRecordsWithRecursiveRelashionship(new EtlOperationItemResult<T>(record));
	}
	
	public void addToRecordsWithResolvedErrors(T record, InconsistenceInfo i) {
		addToRecordsWithUnresolvedErrors(new EtlOperationItemResult<T>(record, i));
	}
	
	private void addAll(List<EtlOperationItemResult<T>> toAdd, EtlOperationResultItemType type) {
		if (utilities.arrayHasElement(toAdd)) {
			
			for (EtlOperationItemResult<T> rec : toAdd) {
				add(rec, type);
			}
		}
	}
	
	private void add(EtlOperationItemResult<T> record, EtlOperationResultItemType type) {
		remove(record);
		
		List<EtlOperationItemResult<T>> toAddTo = determineListToAddTo(type);
		
		record.setType(type);
		
		toAddTo.add(record);
	}
	
	public void remove(EtlOperationItemResult<T> resultItem) {
		List<EtlOperationItemResult<T>> toRemoveFrom = null;
		
		if (!resultItem.hasType())
			throw new ForbiddenOperationException("No type defined for item");
		
		toRemoveFrom = determineListToAddTo(resultItem.getType());
		
		if (toRemoveFrom != null) {
			toRemoveFrom.remove(resultItem);
		}
	}
	
	public void add(T record, EtlException e) {
		add(new EtlOperationItemResult<T>(record, e), EtlOperationResultItemType.UNEXPECTED_ERRORS);
	}
	
	public void add(EtlOperationItemResult<T> resultItem) {
		if (!resultItem.hasType())
			throw new ForbiddenOperationException("No type defined for item");
		
		add(resultItem, resultItem.getType());
	}
	
	private List<EtlOperationItemResult<T>> determineListToAddTo(EtlOperationResultItemType type) {
		if (type.isNoError()) {
			if (this.getRecordsWithNoError() == null) {
				this.setRecordsWithNoError(new ArrayList<>());
			}
			
			return this.getRecordsWithNoError();
		} else if (type.isRecursiverelationships()) {
			if (this.getRecordsWithRecursiveRelashionship() == null) {
				this.setRecordsWithRecursiveRelashionship(new ArrayList<>());
			}
			
			return this.getRecordsWithRecursiveRelashionship();
			
		} else if (type.isResolvedInconsistences()) {
			if (this.getRecordsWithResolvedInconsistences() == null) {
				this.setRecordsWithResolvedInconsistence(new ArrayList<>());
			}
			
			return this.getRecordsWithResolvedInconsistences();
			
		} else if (type.isUnresolvedInconsistences()) {
			if (this.getRecordsWithUnresolvedInconsistences() == null) {
				this.setRecordsWithUnresolvedInconsistence(new ArrayList<>());
			}
			
			return this.getRecordsWithUnresolvedInconsistences();
			
		} else if (type.isUnexpectedErros()) {
			if (this.getRecordsWithUnexpectedErrors() == null) {
				this.setRecordsWithUnexpectedErrors(new ArrayList<>());
			}
			
			return this.getRecordsWithUnexpectedErrors();
		}
		
		throw new ForbiddenOperationException("Unsupported type " + type);
	}
	
	public void documentErrors(Connection srcConn, Connection dstConn) throws DBException {
		List<EtlOperationItemResult<T>> toDocument = new ArrayList<>();
		
		for (EtlOperationItemResult<T> r : toDocument) {
			if (r.hasInconsistences()) {
				for (InconsistenceInfo i : r.getInconsistenceInfo()) {
					i.save((TableConfiguration) r.getRecord().getRelatedConfiguration(), srcConn);
				}
			} else {
				
				EtlConfigurationTableConf etlErr = r.getRecord().getRelatedConfiguration().getRelatedEtlConf()
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
				    r.getRecord().getRelatedConfiguration().getRelatedEtlConf().getOriginAppLocationCode());
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
			
			for (EtlOperationItemResult<T> r : getRecordsWithUnexpectedErrors()) {
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
		
		for (EtlOperationItemResult<T> o : getRecordsWithUnexpectedErrors()) {
			if (o.getException() != null) {
				try {
					throw o.getException().getException();
				}
				catch (Throwable e) {
					throw new RuntimeException(o.getException().getException());
				}
			}
		}
		
		throw new ForbiddenOperationException("No exception found");
	}
	
	public int countAllSuccessfulyProcessedRecords() {
		return utilities.arraySize(getRecordsWithNoError()) + utilities.arraySize(getRecordsWithResolvedInconsistences());
	}
	
	public List<T> getAllSuccessfulyProcessedRecords() {
		List<T> success = new ArrayList<>();
		
		if (hasRecordsWithNoError()) {
			success.addAll(EtlOperationItemResult.parseToEtlDatabaseObject(getRecordsWithNoError()));
		}
		
		if (hasRecordsWithResolvedInconsistences()) {
			success.addAll(EtlOperationItemResult.parseToEtlDatabaseObject(getRecordsWithResolvedInconsistences()));
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
	
	public boolean hasRecordsWithErrors() {
		return hasRecordsWithResolvedInconsistences() || hasRecordsWithUnresolvedInconsistences()
		        || hasRecordsWithUnexpectedErrors();
	}
	
	public List<EtlOperationItemResult<T>> getAllRecordsWithErros() {
		
		List<EtlOperationItemResult<T>> recordsWithErros = new ArrayList<>();
		
		if (hasRecordsWithUnexpectedErrors()) {
			recordsWithErros.addAll(getRecordsWithUnexpectedErrors());
		}
		
		if (hasRecordsWithResolvedInconsistences()) {
			recordsWithErros.addAll(getRecordsWithResolvedInconsistences());
		}
		
		if (hasRecordsWithUnresolvedInconsistences()) {
			recordsWithErros.addAll(getRecordsWithUnresolvedInconsistences());
		}
		
		return recordsWithErros;
	}
	
	public List<T> getRecordsWithErrorsAsEtlDatabaseObject() {
		if (hasRecordsWithErrors())
			return EtlOperationItemResult.parseToEtlDatabaseObject(getAllRecordsWithErros());
		
		return null;
	}
	
}
