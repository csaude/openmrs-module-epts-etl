package org.openmrs.module.eptssync.utilities.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;


public class ExportDBs {
	static String dir = "/home/eip/bkps/q3fy22";
	
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			System.err.println("One o all params were not specified! Please specify to params 1. DB Export root directory");
			
			System.exit(1);
		}
		
		String dbExportRootDirectoryPath = args[0];
		
		if (!new File(dbExportRootDirectoryPath ).exists()) {
			System.err.println("The path [" + dbExportRootDirectoryPath + "] for Sites file does not correspond existing file!");
			System.exit(1);
		}
		
		System.out.println("Initializing...");
		System.out.println("Using Sites File: " + dbExportRootDirectoryPath);
		
		for (File file : getDumps(new File(dbExportRootDirectoryPath))) {
			//File Name Pathern: openmrs_site_code
			
			String dumpName = FileUtilities.generateFileNameFromRealPath(file.getAbsolutePath());
			String siteName = (dumpName.split("openmrs_")[1]).split(".sql")[0];
			String dbName = "openmrs_q3fy22_" + siteName;
		
			String[] cmd = new String[] { "/bin/bash", dbExportRootDirectoryPath+ "/db_import.sh", dbName, file.getAbsolutePath() };
				
				try {
					System.out.println("Starting import of dump [" + file.getAbsolutePath() + "] to [" + dbName + "] for site ["+ siteName + "]");
					
					Runtime.getRuntime().exec(cmd);
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
		}
		
		System.exit(0);
		
	}
	
	public static List<File> getDumps(File rootDirectory){
		//Assume-se que, os dumps encontram-se armazenados em directorios representado os distritos correspondentes
		
		List<File> dumps = new ArrayList<File>();
		
		//Loop over the districts folders
		for (File file : rootDirectory.listFiles()) {
		
			if (!file.isDirectory()) continue;
			
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
