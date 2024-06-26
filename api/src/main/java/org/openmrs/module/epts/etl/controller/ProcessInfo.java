package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Date;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProcessInfo {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String processId;
	
	private String startTime;
	
	private String finishTime;
	
	private String elapsedTime;
	
	private Date observationDate;
	
	private EtlConfiguration configuration;
	
	private boolean loadeFromFile;
	
	public ProcessInfo() {
	}
	
	public ProcessInfo(EtlConfiguration configuration) {
		this.configuration = configuration;
		this.processId = this.configuration.generateControllerId();
		this.observationDate = this.configuration.getStartDate();
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	public String getFinishTime() {
		return finishTime;
	}
	
	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}
	
	public String getElapsedTime() {
		return elapsedTime;
	}
	
	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	public Date getObservationDate() {
		return observationDate;
	}
	
	public void setObservationDate(Date observationDate) {
		this.observationDate = observationDate;
	}
	
	public String getProcessId() {
		return processId;
	}
	
	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
	
	@JsonIgnore
	public EtlConfiguration getConfiguration() {
		return configuration;
	}
	
	public ProcessInfo tryToLoadFromFile() {
		try {
			ProcessInfo pi = ProcessInfo.loadFromJSON(new String(Files.readAllBytes(generateProcessStatusFile().toPath())));
			
			pi.loadeFromFile = true;
			
			return pi;
		}
		catch (NoSuchFileException e) {
			return null;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@JsonIgnore
	public boolean isLoadeFromFile() {
		return loadeFromFile;
	}
	
	@JsonIgnore
	public boolean isFinished() {
		return this.finishTime != null;
	}
	
	public static ProcessInfo loadFromJSON(String json) {
		return utilities.loadObjectFormJSON(ProcessInfo.class, json);
	}
	
	public File generateProcessStatusFile() {
		String operationId = this.getProcessId();
		
		String fileName = generateProcessStatusFolder() + FileUtilities.getPathSeparator() + operationId + "_process";
		
		return new File(fileName);
	}
	
	public String generateProcessStatusFolder() {
		String subFolder = "";
		
		if (getConfiguration().isSupposedToRunInOrigin()) {
			subFolder = "source";
		} else if (getConfiguration().isSupposedToRunInDestination()) {
			subFolder = "destination";
		}
		
		return getConfiguration().getEtlRootDirectory() + FileUtilities.getPathSeparator() + "process_status"
		        + FileUtilities.getPathSeparator() + subFolder + FileUtilities.getPathSeparator()
		        + getConfiguration().getDesignation();
	}
	
	public void save() {
		FileUtilities.tryToCreateDirectoryStructureForFile(generateProcessStatusFile().getAbsolutePath());
		
		FileUtilities.write(generateProcessStatusFile().getAbsolutePath(), parseToJSON());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProcessInfo))
			return false;
		
		ProcessInfo pi = (ProcessInfo) obj;
		
		if (this.observationDate != null) {
			if (!this.getObservationDate().equals(pi.getObservationDate())) {
				return false;
			}
		}
		
		return this.getProcessId().equals(pi.getProcessId());
		
	}
}
