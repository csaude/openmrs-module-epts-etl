package org.openmrs.module.epts.etl.dbquickexport.controller;

import java.io.File;
import java.io.IOException;

import org.openmrs.module.epts.etl.controller.AbstractProcessFinalizer;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.utilities.parseToCSV;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DbQuickExportFinalizer extends AbstractProcessFinalizer {
	
	public DbQuickExportFinalizer(ProcessController relatedProcessController) {
		super(relatedProcessController);
	}
	
	@Override
	public void performeFinalizationTasks() {
		try {
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(getConfiguration().getOriginAppLocationCode());
			
			File jsonFIle = generateJSONTempFile(jsonInfo);
			
			FileUtilities.write(jsonFIle.getAbsolutePath(), jsonInfo.parseToJSON());
			
			FileUtilities.renameTo(jsonFIle.getAbsolutePath(), jsonFIle.getAbsolutePath() + ".json");
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public parseToCSV utilities() {
		return parseToCSV.getInstance();
	}
	
	public File generateJSONTempFile(SyncJSONInfo jsonInfo) throws IOException {
		
		String fileName = "";
		
		fileName += getConfiguration().getEtlRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		//Use "_" at begining of folder name to avoid situation were the starting character cause escape (ex: 't' on '\t')
		
		fileName += "_" + getConfiguration().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += "finalization";
		fileName += FileUtilities.getPathSeparator();
		fileName += "_" + getConfiguration().getOriginAppLocationCode().toLowerCase() + "_";
		
		fileName += "finalization";
		
		fileName += "_" + utilities().garantirXCaracterOnNumber(0, 10);
		fileName += "_" + utilities().garantirXCaracterOnNumber(0, 10);
		
		if (new File(fileName).exists()) {
			new File(fileName).delete();
		}
		
		if (new File(fileName + ".json").exists()) {
			new File(fileName + ".json").delete();
		}
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		File file = new File(fileName);
		file.createNewFile();
		
		return file;
	}
}
