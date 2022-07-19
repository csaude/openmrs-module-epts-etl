package org.openmrs.module.eptssync.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

public class ProcessProgressInfo {
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<OperationProgressInfo> operationsProgressInfo;
	//private ProcessController controller;
	
	public ProcessProgressInfo(ProcessController controller){
		//this.controller = controller;
		this.operationsProgressInfo = new ArrayList<OperationProgressInfo>();
	}
	
	public OperationProgressInfo initAndAddProgressMeterToList(OperationController operationController) {
		OperationProgressInfo progressInfo;
		
		File operationStatusFile = operationController.generateOperationStatusFile();
		
		if (operationStatusFile.exists()) {
			progressInfo = OperationProgressInfo.loadFromFile(operationController.generateOperationStatusFile());
			progressInfo.setController(operationController);
		}
		else {
			progressInfo = new OperationProgressInfo(operationController);
		}
		
		progressInfo.initProgressMeter();
		
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
