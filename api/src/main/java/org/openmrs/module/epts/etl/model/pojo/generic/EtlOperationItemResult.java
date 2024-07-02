package org.openmrs.module.epts.etl.model.pojo.generic;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.EtlException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class EtlOperationItemResult<T extends EtlDatabaseObject> {
	
	private static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	private T record;
	
	private EtlException exception;
	
	private List<InconsistenceInfo> inconsistenceInfo;
	
	private EtlOperationResultItemType type;
	
	public EtlOperationItemResult(T record) {
		this.record = record;
		
		this.type = EtlOperationResultItemType.NO_ERROR;
	}
	
	public EtlOperationItemResult(T record, List<InconsistenceInfo> inconsistence) {
		this.record = record;
		this.inconsistenceInfo = inconsistence;
		
		tryToDetermineType();
	}
	
	public EtlOperationItemResult(T record, InconsistenceInfo inconsistence) {
		this.record = record;
		this.inconsistenceInfo = utilities.parseToList(inconsistence);
	}
	
	public EtlOperationItemResult(T record, EtlException exception) {
		this.record = record;
		this.exception = exception;
		this.type = EtlOperationResultItemType.UNEXPECTED_ERRORS;
	}
	
	public EtlOperationResultItemType getType() {
		return type;
	}
	
	public void setType(EtlOperationResultItemType type) {
		this.type = type;
	}
	
	public void setException(EtlException exception) {
		this.exception = exception;
	}
	
	public List<InconsistenceInfo> getInconsistenceInfo() {
		return inconsistenceInfo;
	}
	
	public void setInconsistenceInfo(List<InconsistenceInfo> inconsistenceInfo) {
		this.inconsistenceInfo = inconsistenceInfo;
		
		tryToDetermineType();
	}
	
	public void addInconsistence(InconsistenceInfo info) {
		if (this.inconsistenceInfo == null)
			this.inconsistenceInfo = new ArrayList<>();
		
		this.inconsistenceInfo.add(info);
		
		tryToDetermineType();
	}
	
	public EtlException getException() {
		return exception;
	}
	
	public T getRecord() {
		return record;
	}
	
	public static <T extends EtlDatabaseObject> List<EtlOperationItemResult<T>> parseFromEtlDatabaseObject(
	        List<T> etlObjects) {
		
		if (!utilities.arrayHasElement(etlObjects))
			return null;
		
		List<EtlOperationItemResult<T>> converted = new ArrayList<>(etlObjects.size());
		
		for (T record : etlObjects) {
			converted.add(new EtlOperationItemResult<T>(record));
		}
		
		return converted;
	}
	
	public static <T extends EtlDatabaseObject> List<T> parseToEtlDatabaseObject(
	        List<EtlOperationItemResult<T>> etlObjects) {
		
		if (!utilities.arrayHasElement(etlObjects))
			return null;
		
		List<T> converted = new ArrayList<>(etlObjects.size());
		
		for (EtlOperationItemResult<T> record : etlObjects) {
			converted.add(record.getRecord());
		}
		
		return converted;
	}
	
	public boolean hasInconsistences() {
		return utilities.arrayHasElement(getInconsistenceInfo());
	}
	
	public boolean hasUnresolvedInconsistences() {
		if (hasInconsistences()) {
			for (InconsistenceInfo i : getInconsistenceInfo()) {
				if (i.getDefaultParentId() == null)
					return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EtlOperationItemResult))
			return false;
		
		@SuppressWarnings("unchecked")
		EtlOperationItemResult<T> r = (EtlOperationItemResult<T>) obj;
		
		return this.getRecord().equals(r.getRecord());
	}
	
	public boolean hasException() {
		return getException() != null;
	}
	
	public void tryToDetermineType() {
		
		if (hasException()) {
			setType(EtlOperationResultItemType.UNEXPECTED_ERRORS);
		} else if (hasUnresolvedInconsistences()) {
			setType(EtlOperationResultItemType.UNRESOLVED_INCONSISTENCES);
		} else if (hasInconsistences()) {
			setType(EtlOperationResultItemType.RESOLVED_INCONSISTENCES);
		} else if (!hasException()) {
			setType(EtlOperationResultItemType.NO_ERROR);
		}
	}
	
	public boolean hasType() {
		return this.getType() != null;
	}
	
}
