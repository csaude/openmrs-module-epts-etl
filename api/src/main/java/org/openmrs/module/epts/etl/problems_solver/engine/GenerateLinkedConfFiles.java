package org.openmrs.module.epts.etl.problems_solver.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class GenerateLinkedConfFiles extends GenericEngine {
	
	private String templateConfFilePath;
	
	private String fileWithListOfDBs;
	
	private File workingDir;
	
	private String partner;
	
	private String province;
	
	private boolean done;
	
	public GenerateLinkedConfFiles(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits) {
		super(monitor, limits);
		
		Extension exItem = this.getRelatedOperationController().getOperationConfig().findExtension("partner");
		
		this.partner = exItem.getValueString();
		
		exItem = this.getRelatedOperationController().getOperationConfig().findExtension("province");
		
		this.province = exItem.getValueString();
		
		this.workingDir = new File(
		        getRelatedEtlConfiguration().getSyncRootDirectory() + File.separator + partner + File.separator + province);
		
		this.fileWithListOfDBs = workingDir + File.separator + "dbs.txt";
		
		this.templateConfFilePath = getRelatedEtlConfiguration().getSyncRootDirectory() + File.separator + "conf"
		        + File.separator + "template.json";
	}
	
	@Override
	public EtlOperationResultHeader<EtlDatabaseObject> performeSync(List<EtlDatabaseObject> etlObjects, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		if (this.templateConfFilePath == null || fileWithListOfDBs == null) {
			throw new ForbiddenOperationException(
			        "One o all params were not specified! Please specify to params 1. Template conf file path 2. File With List of DB names");
		}
		
		Path path = Paths.get(templateConfFilePath);
		
		if (!new File(templateConfFilePath).exists()) {
			throw new ForbiddenOperationException("The path [" + templateConfFilePath
			        + "] for template conf file path does not correspond existing file!");
		}
		
		if (!new File(fileWithListOfDBs).exists()) {
			throw new ForbiddenOperationException(
			        "The path [" + fileWithListOfDBs + "] for databases list does not correspond existing file!");
		}
		
		logDebug("Initializing...");
		logInfo("Using Template File: " + templateConfFilePath);
		logInfo("Using DB List file: " + fileWithListOfDBs);
		
		String linkedConfigAttibuteName = "childConfigFilePath";
		
		String nextConfigFileNamePathern = "next_config_file_name";
		String dataBaseNamePathern = "db_name";
		
		String partnerPathern = "partner";
		
		String provincePathern = "province";
		
		try {
			List<String> dumps = FileUtilities.readAllFileAsListOfString(fileWithListOfDBs);
			
			for (int i = 0; i < dumps.size(); i++) {
				String dataBaseName = FileUtilities.generateFileNameFromRealPath(dumps.get(i));
				String siteName = dataBaseName;
				
				Charset charset = StandardCharsets.UTF_8;
				
				String content = new String(Files.readAllBytes(path), charset);
				
				content = content.replaceAll(dataBaseNamePathern, dataBaseName);
				content = content.replaceAll(partnerPathern, this.partner);
				content = content.replaceAll(provincePathern, this.province);
				
				String nextDataBaseName = i < dumps.size() - 1 ? FileUtilities.generateFileNameFromRealPath(dumps.get(i + 1))
				        : null;
				
				if (nextDataBaseName != null) {
					String nextFileName = nextDataBaseName;
					
					content = content.replaceAll(nextConfigFileNamePathern, nextFileName + ".json");
				} else {
					content = content.replaceAll(linkedConfigAttibuteName, linkedConfigAttibuteName + "_");
				}
				
				Path destPath = Paths
				        .get(this.workingDir.toString() + File.separator + "conf" + File.separator + siteName + ".json");
				
				FileUtilities.tryToCreateDirectoryStructure(this.workingDir.toString() + File.separator + "conf");
				
				Files.write(destPath, content.getBytes(charset));
				
				logInfo("DONE CREATION OF [" + siteName + "] CONF FILE");
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		done = true;
		
		return new EtlOperationResultHeader<>(etlObjects);
	}
	
	public static List<File> getDumps(File rootDirectory) {
		//Assume-se que, os dumps encontram-se armazenados em directorios representado os distritos correspondentes
		
		List<File> dumps = new ArrayList<File>();
		
		//Loop over the districts folders
		for (File file : rootDirectory.listFiles()) {
			
			if (!file.isDirectory())
				continue;
			
			File[] files = file.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith("sql");
				}
			});
			
			if (files.length > 0) {
				dumps.addAll(CommonUtilities.getInstance().parseArrayToList(files));
			}
		}
		
		return dumps;
	}
	
	public boolean done() {
		return this.done;
	}
	
}
