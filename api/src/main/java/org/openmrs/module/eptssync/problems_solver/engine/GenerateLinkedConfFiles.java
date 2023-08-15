package org.openmrs.module.eptssync.problems_solver.engine;

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

import org.openmrs.module.eptssync.controller.conf.Extension;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class GenerateLinkedConfFiles extends GenericEngine {
	
	public static final String testBaseFilePath = "D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\testing\\db_quick_copy_template.json";
	
	public static final String testSitesFilePath = "D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\testing\\sites.txt";
	
	private String templateConfFilePath;
	
	private String fileWithListOfDBs;
	private File destinationFolder;
	
	public GenerateLinkedConfFiles(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		Extension exItem = this.getRelatedOperationController().getOperationConfig().findExtension("databaseListFile");
		
		this.fileWithListOfDBs = exItem.getValueString();
		
		this.destinationFolder = new File(this.fileWithListOfDBs).getParentFile();
		
		exItem = this.getRelatedOperationController().getOperationConfig().findExtension("templateFile");
		
		this.templateConfFilePath = exItem.getValueString();
		
	}
	
	@Override
	public void performeSync(List<SyncRecord> searchNextRecords, Connection conn) throws DBException {
		if (this.templateConfFilePath == null || fileWithListOfDBs == null) {
			throw new ForbiddenOperationException(
			        "One o all params were not specified! Please specify to params 1. Template conf file path 2. File With List of DB names");
		}
		
		Path path = Paths.get(templateConfFilePath);
		
		if (!new File(templateConfFilePath).exists()) {
			throw new ForbiddenOperationException(
			    "The path [" + templateConfFilePath + "] for template conf file path does not correspond existing file!");
		}
		
		if (!new File(fileWithListOfDBs).exists()) {
			throw new ForbiddenOperationException("The path [" + fileWithListOfDBs + "] for databases list does not correspond existing file!");
		}
		
		logDebug("Initializing...");
		logInfo("Using Template File: " + templateConfFilePath);
		logInfo("Using DB List file: " + fileWithListOfDBs);
		
		String linkedConfigAttibuteName = "childConfigFilePath";
		
		String nextConfigFileNamePathern = "next_config_file_name";
		String dataBaseNamePathern = "db_name";
		
		try {
			List<String> dumps = FileUtilities.readAllFileAsListOfString(fileWithListOfDBs);
			
			for (int i = 0; i < dumps.size(); i++) {
				String dataBaseName = FileUtilities.generateFileNameFromRealPath(dumps.get(i));
				String siteName = dataBaseName;
				
				Charset charset = StandardCharsets.UTF_8;
				
				String content = new String(Files.readAllBytes(path), charset);
				
				content = content.replaceAll(dataBaseNamePathern, dataBaseName);
				
				String nextDataBaseName = i < dumps.size() - 1 ? FileUtilities.generateFileNameFromRealPath(dumps.get(i + 1))
				        : null;
				
				if (nextDataBaseName != null) {
					String nextFileName = nextDataBaseName;
					
					content = content.replaceAll(nextConfigFileNamePathern, nextFileName + ".json");
				} else {
					content = content.replaceAll(linkedConfigAttibuteName, linkedConfigAttibuteName + "_");
				}
				
				Path destPath = Paths.get(this.destinationFolder.toString() + FileUtilities.getPathSeparator() + siteName + ".json");
				
				Files.write(destPath, content.getBytes(charset));
				
				logInfo("DONE CREATION OF [" + siteName + "] CONF FILE");
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void generateUsingDumpDirectory(String[] args) throws IOException {
		
		if (args == null || args.length < 2) {
			System.err.println(
			    "One o all params were not specified! Please specify to params 1. Template conf file path 2. DB dumps location");
			
			System.exit(1);
		}
		
		String templateConfFilePath = args[0];
		String dbExportRootDirectoryPath = args[1];
		
		Path path = Paths.get(templateConfFilePath);
		
		if (!new File(templateConfFilePath).exists()) {
			System.err.println(
			    "The path [" + templateConfFilePath + "] for template conf file path does not correspond existing file!");
			System.exit(1);
		}
		
		if (!new File(dbExportRootDirectoryPath).exists()) {
			System.err.println(
			    "The path [" + dbExportRootDirectoryPath + "] for dumps files does not correspond existing file!");
			System.exit(1);
		}
		
		System.out.println("Initializing...");
		System.out.println("Using Template File: " + templateConfFilePath);
		System.out.println("Using Dumps directory: " + dbExportRootDirectoryPath);
		
		String linkedConfigAttibuteName = "childConfigFilePath";
		
		String nextConfigFileNamePathern = "next_config_file_name";
		String dataBaseNamePathern = "db_name";
		
		List<File> dumps = getDumps(new File(dbExportRootDirectoryPath));
		
		for (int i = 0; i < dumps.size(); i++) {
			File dumpFile = dumps.get(i);
			
			//File Name Pathern: openmrs_site_code
			
			String dataBaseName = generateDBName(dumpFile);
			String siteName = generateSiteName(dumpFile);
			
			//Site Pathern: tmp_openmrs_site_code
			
			Charset charset = StandardCharsets.UTF_8;
			
			String content = new String(Files.readAllBytes(path), charset);
			
			content = content.replaceAll(dataBaseNamePathern, dataBaseName);
			
			String nextDataBaseName = i < dumps.size() - 1 ? generateDBName(dumps.get(i + 1)) : null;
			
			if (nextDataBaseName != null) {
				String nextFileName = nextDataBaseName.split("openmrs_q3fy22_")[1];
				
				content = content.replaceAll(nextConfigFileNamePathern, nextFileName + ".json");
			} else {
				content = content.replaceAll(linkedConfigAttibuteName, linkedConfigAttibuteName + "_");
			}
			
			Path destPath = Paths.get(path.getParent().toString() + FileUtilities.getPathSeparator() + siteName + ".json");
			
			Files.write(destPath, content.getBytes(charset));
			
			System.out.println("DONE CREATION OF [" + siteName + "] CONF FILE");
		}
	}
	
	private static String generateSiteName(File file) {
		String dumpName = FileUtilities.generateFileNameFromRealPath(file.getAbsolutePath());
		return (dumpName.split("openmrs_")[1]).split(".sql")[0];
	}
	
	private static String generateDBName(File file) {
		String dumpName = FileUtilities.generateFileNameFromRealPath(file.getAbsolutePath());
		String siteName = (dumpName.split("openmrs_")[1]).split(".sql")[0];
		
		return "openmrs_q3fy22_" + siteName;
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
	
}
