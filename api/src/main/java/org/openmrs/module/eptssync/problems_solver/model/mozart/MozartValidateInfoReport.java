package org.openmrs.module.eptssync.problems_solver.model.mozart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class MozartValidateInfoReport {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String partner;
	
	private String province;
	
	private List<DBValidateInfo> dbValidateReport;
	
	private List<String> missingDBs;
	
	private File jsonFile;
	
	private MozartReportType reportType;
	
	public MozartValidateInfoReport() {
	}
	
	public MozartValidateInfoReport(MozartReportType reportType, String partner, String province, File jsonFile) {
		this.province = province;
		this.partner = partner;
		this.jsonFile = jsonFile;
		
		this.reportType = reportType;
		
		if (this.reportType == null) throw new ForbiddenOperationException("You must especify the type");
	}
	
	public MozartReportType getReportType() {
		return reportType;
	}
	
	public void setReportType(MozartReportType reportType) {
		this.reportType = reportType;
	}
	
	public String getPartner() {
		return partner;
	}
	
	public void setPartner(String partner) {
		this.partner = partner;
	}
	
	public String getProvince() {
		return province;
	}
	
	public void setProvince(String province) {
		this.province = province;
	}
	
	public void addMissingDb(String dbName) {
		if (missingDBs == null)
			missingDBs = new ArrayList<String>();
		
		missingDBs.add(dbName);
	}
	
	public List<String> getMissingDBs() {
		return missingDBs;
	}
	
	public void setMissingDBs(List<String> missingDBs) {
		this.missingDBs = missingDBs;
	}
	
	/**
	 * Add the db info passed by parameter to this report
	 * 
	 * @param dbInfo to be added
	 * @throws ForbiddenOperationException if the db already exists in this report
	 */
	public void addReport(DBValidateInfo dbInfo) throws ForbiddenOperationException {
		if (this.dbValidateReport == null)
			this.dbValidateReport = new ArrayList<DBValidateInfo>();
		
		DBValidateInfo existing = findDBValidatedInfo(dbInfo.getDatabase());
		
		if (existing == null) {
			dbInfo.setReport(this);
			
			this.dbValidateReport.add(dbInfo);
			
		} else {
			throw new ForbiddenOperationException("The db info for '" + dbInfo.getDatabase() + "' already exists on '"
			        + this.partner + "_" + this.province + "' report");
		}
	}
	
	public List<DBValidateInfo> getDbValidateReport() {
		return dbValidateReport;
	}
	
	public void setDbValidateReport(List<DBValidateInfo> dbValidateReport) {
		this.dbValidateReport = dbValidateReport;
	}
	
	public static MozartValidateInfoReport loadFromFile(File file) throws IOException {
		if (!file.exists())
			return null;
		
		MozartValidateInfoReport conf = CommonUtilities.getInstance().loadObjectFormJSON(MozartValidateInfoReport.class,
		    new String(Files.readAllBytes(file.toPath())));
		
		conf.jsonFile = file;
		
		return conf;
	}
	
	private void tryToCleanEmptyDBsInfo() {
		List<DBValidateInfo> toBeRemoved = new ArrayList<DBValidateInfo>();
		
		try {
			for (DBValidateInfo dbInfo : this.dbValidateReport) {
				if (this.reportType.isResolvedProblemsReport() && !dbInfo.hasResolvedProblem()) {
					toBeRemoved.add(dbInfo);
				} else if (this.reportType.isDetectedProblemsReport() && !dbInfo.hasProblem()) {
					toBeRemoved.add(dbInfo);
				}
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.dbValidateReport.removeAll(toBeRemoved);
	}
	
	@JsonIgnore
	public File getJsonFile() {
		return jsonFile;
	}
	
	public void saveOnFile() {
		if (!utilities.arrayHasElement(this.dbValidateReport))
			return;
		
		this.tryToCleanEmptyDBsInfo();
		
		try {
			FileUtilities.tryToCreateDirectoryStructureForFile(this.jsonFile.getAbsolutePath());
			
			String json = new ObjectMapperProvider().getContext(MozartValidateInfoReport.class).writeValueAsString(this);
			
			if (this.jsonFile.exists())
				FileUtilities.removeFile(this.jsonFile.getAbsolutePath());
			
			FileUtilities.write(jsonFile.getAbsolutePath(), json);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DBValidateInfo initDBValidatedInfo(String dbName) {
		DBValidateInfo dbInfo = findDBValidatedInfo(dbName);
		
		if (dbInfo == null) {
			dbInfo = new DBValidateInfo(dbName);
			
			this.addReport(dbInfo);
		} else {
			dbInfo.setReport(this);
		}
		
		return dbInfo;
	}
	
	public DBValidateInfo findDBValidatedInfo(String dbName) {
		if (!utilities.arrayHasElement(this.dbValidateReport))
			return null;
		
		DBValidateInfo toFind = new DBValidateInfo(dbName);
		
		return utilities.findOnArray(this.dbValidateReport, toFind);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MozartValidateInfoReport))
			return false;
		
		MozartValidateInfoReport otherObj = (MozartValidateInfoReport) obj;
		
		return this.partner.equalsIgnoreCase(otherObj.partner) && this.province.equalsIgnoreCase(otherObj.province);
	}
	
	public void removeDBValidateInfo(DBValidateInfo dbValidateInfo) {
		this.dbValidateReport.remove(dbValidateInfo);
	}
	
}
