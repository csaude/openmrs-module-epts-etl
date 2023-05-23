package org.openmrs.module.eptssync.problems_solver.model.mozart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MozartValidateInfoReport {
	
	private String partner;
	
	private String province;
	
	private List<DBValidateInfo> dbValidateReport;
	
	private List<String> missingDBs;
	
	private File jsonFile;
	
	public MozartValidateInfoReport() {
	}
	
	public MozartValidateInfoReport(String partner, String province, File jsonFile) {
		this.province = province;
		this.partner = partner;
		this.jsonFile = jsonFile;
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
	
	public void addReport(DBValidateInfo report) {
		if (this.dbValidateReport == null)
			this.dbValidateReport = new ArrayList<DBValidateInfo>();
		
		this.dbValidateReport.add(report);
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
	
	public void saveOnFile() {
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
