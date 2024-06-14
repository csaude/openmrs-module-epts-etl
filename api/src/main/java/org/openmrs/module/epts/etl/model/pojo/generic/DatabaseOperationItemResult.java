package org.openmrs.module.epts.etl.model.pojo.generic;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseOperationItemResult {
	
	private static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	private EtlDatabaseObject record;
	
	private DBException exception;
	
	private List<InconsistenceInfo> inconsistenceInfo;
	
	public DatabaseOperationItemResult(EtlDatabaseObject record) {
		this.record = record;
	}
	
	public DatabaseOperationItemResult(EtlDatabaseObject record, DBException exception) {
		this.record = record;
		this.exception = exception;
	}
	
	public List<InconsistenceInfo> getInconsistenceInfo() {
		return inconsistenceInfo;
	}
	
	public void setInconsistenceInfo(List<InconsistenceInfo> inconsistenceInfo) {
		this.inconsistenceInfo = inconsistenceInfo;
	}
	
	public void addInconsistence(InconsistenceInfo info) {
		if (this.inconsistenceInfo == null)
			this.inconsistenceInfo = new ArrayList<>();
		
		this.inconsistenceInfo.add(info);
		
	}
	
	public DBException getException() {
		return exception;
	}
	
	public EtlDatabaseObject getRecord() {
		return record;
	}
	
	public boolean hasFatalError() throws DBException {
		
		return false;
		
		/*
		if (getException().isDuplicatePrimaryOrUniqueKeyException()) {
			return false;
		}
		
		if (getException().isIntegrityConstraintViolationException()) {
			return false;
		}
		
		return true;*/
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
		if (!(obj instanceof DatabaseOperationItemResult))
			return false;
		
		DatabaseOperationItemResult r = (DatabaseOperationItemResult) obj;
		
		return this.getRecord().equals(r.getRecord());
	}
	
	public boolean hasException() {
		return getException() != null;
	}
}
