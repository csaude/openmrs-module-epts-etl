package org.openmrs.module.epts.etl.etl.model;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

/**
 * A {@link EtlLoadHelperRecord} group destination records sharing same source dstRecord.
 */
public class EtlLoadHelperRecord {
	
	private EtlDatabaseObject srcObject;
	
	private List<LoadRecord> loadRecord;
	
	public EtlLoadHelperRecord(EtlDatabaseObject srcObject, EtlProcessor processor) {
		this.srcObject = srcObject;
		this.loadRecord = new ArrayList<>(processor.getEtlItemConfiguration().getDstConf().size());
	}
	
	public EtlLoadHelperRecord(LoadRecord loadRecord) {
		this(loadRecord.getSrcRecord(), loadRecord.getProcessor());
		
		addLoadRecord(loadRecord);
	}
	
	public EtlDatabaseObject getSrcObject() {
		return srcObject;
	}
	
	private List<LoadRecord> getLoadRecord() {
		return loadRecord;
	}
	
	public void addLoadRecord(LoadRecord lr) {
		if (lr.getSrcRecord() != this.srcObject) {
			throw new ForbiddenOperationException("You cannot combine records with differents srcObjects");
		}
		
		if (loadRecord.contains(lr)) {
			throw new ForbiddenOperationException("The dstRecord you are trying to add is already on this helper");
		}
		
		this.getLoadRecord().add(lr);
	}
	
	/**
	 * Determine the global status for {@link #srcObject}
	 * 
	 * @return the global status
	 */
	public LoadStatus determineGlobalStatus() {
		LoadStatus status = LoadStatus.UNDEFINED;
		
		for (LoadRecord r : getLoadRecord()) {
			if (status.isLessThan(r.getStatus())) {
				status = r.getStatus();
			}
		}
		
		return status;
	}
	
	public LoadRecord getLoadRecord(DstConf dstConf) {
		for (LoadRecord r : getLoadRecord()) {
			if (r.getDstConf() == dstConf) {
				return r;
			}
		}
		
		throw new ForbiddenOperationException("No dstRecord found for DstConf " + dstConf);
	}
	
	public List<EtlDatabaseObject> getDstRecords() {
		List<EtlDatabaseObject> dstrecords = new ArrayList<>();
		
		for (LoadRecord lr : getLoadRecord()) {
			dstrecords.add(lr.getDstRecord());
		}
		
		return dstrecords;
	}
	
	public LoadRecord getDstRecrelatedToGlobalStatus() {
		LoadStatus status = determineGlobalStatus();
		
		for (LoadRecord lr : getLoadRecord()) {
			if (lr.getStatus().equals(status))
				return lr;
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		String str = determineGlobalStatus().isGreaterThan(LoadStatus.UNDEFINED)
		        ? getDstRecrelatedToGlobalStatus().toString()
		        : "NONE";
		
		return determineGlobalStatus().toString() + ": For " + str;
	}
	
}
