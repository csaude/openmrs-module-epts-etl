package org.openmrs.module.epts.etl.dbquickload.engine;

import org.openmrs.module.epts.etl.engine.RecordLimits;

public class QuickLoadLimits extends RecordLimits {
	private int qtyFiles;
	private int processedFiles;
	
	public QuickLoadLimits(){
	}
	
	public QuickLoadLimits(int qtyFiles){
		this.qtyFiles = qtyFiles;
	}

	@Override
	public boolean canGoNext() {
		return qtyFiles > processedFiles;
	}
	
	@Override
	public synchronized void moveNext(int qtyRecords) {
		processedFiles++;
	}
}
