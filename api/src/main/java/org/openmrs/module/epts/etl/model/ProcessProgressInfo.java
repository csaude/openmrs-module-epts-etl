package org.openmrs.module.epts.etl.model;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.utilities.parseToCSV;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProcessProgressInfo {
	
	private static parseToCSV utilities = parseToCSV.getInstance();
	
	private List<OperationProgressInfo> operationsProgressInfo;
	
	private ProcessController controller;
	
	public ProcessProgressInfo(ProcessController controller) {
		this.operationsProgressInfo = new ArrayList<OperationProgressInfo>();
		
		this.controller = controller;
	}
	
	@JsonIgnore
	public ProcessController getController() {
		return controller;
	}
	
	public OperationProgressInfo initAndAddProgressMeterToList(OperationController<? extends EtlDatabaseObject> operationController, Connection conn)
	        throws DBException {
		OperationProgressInfo progressInfo;
		
		File operationStatusFile = operationController.generateOperationStatusFile();
		
		if (operationController.isResumable() && operationStatusFile.exists() && !FileUtilities.isEmpty(operationStatusFile) ) {
			
			progressInfo = OperationProgressInfo.loadFromFile(operationController.generateOperationStatusFile());
			progressInfo.setController(operationController);
		} else {
			progressInfo = new OperationProgressInfo(operationController);
		}
		
		progressInfo.initProgressMeter(conn);
		
		this.operationsProgressInfo.add(progressInfo);
		
		return progressInfo;
	}
	
	public List<OperationProgressInfo> getOperationsProgressInfo() {
		return operationsProgressInfo;
	}
	
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
}
