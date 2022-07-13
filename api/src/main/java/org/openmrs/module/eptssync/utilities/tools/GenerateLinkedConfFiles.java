package org.openmrs.module.eptssync.utilities.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class GenerateLinkedConfFiles {
	public static final String testBaseFilePath ="D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\prodution\\db_quick_copy_template.json";
	public static final String testSitesFilePath = "D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\prodution\\sites.txt";
	
	
	public static void main(String[] args) throws IOException {
		
		if (args == null || args.length < 2) {
			System.err.println("One o all params were not specified! Please specify to params 1. Template conf file path 2. Sites file path");
			
			System.exit(1);
		}
		
		String templateConfFilePath = args[0];
		String sitesFilePath = args[1];
		
		Path path = Paths.get(templateConfFilePath);
		
		
		if (!new File(templateConfFilePath).exists() ) {
			System.err.println("The path [" + templateConfFilePath + "] for template conf file path does not correspond existing file!");
			System.exit(1);
		}
		
		if (!new File(sitesFilePath ).exists()) {
			System.err.println("The path [" + sitesFilePath + "] for Sites file does not correspond existing file!");
			System.exit(1);
		}
		
		System.out.println("Initializing...");
		System.out.println("Using Template File: " + templateConfFilePath);
		System.out.println("Using Sites File: " + sitesFilePath);
			
		List<String> sites = FileUtilities.readAllFileAsListOfString(sitesFilePath);
		
		String linkedConfigAttibuteName = "childConfigFilePath";
		
		String nextConfigFileNamePathern = "next_config_file_name";
		String dataBaseNamePathern = "db_name";
		
		for (int i = 0; i < sites.size(); i++) {
			//Site Pathern: tmp_openmrs_site_code
			
			String dataBaseName = sites.get(i);
			String siteName = dataBaseName.split("openmrs_q3fy22_")[1];
			
			Charset charset = StandardCharsets.UTF_8;

			String content = new String(Files.readAllBytes(path), charset);
			
			content = content.replaceAll(dataBaseNamePathern, dataBaseName);
			
			String nextDataBaseName = i < sites.size() - 1 ? sites.get(i+1) : null;
			
			if (nextDataBaseName != null) {
				String nextFileName =  nextDataBaseName.split("openmrs_q3fy22_")[1];
				
				content = content.replaceAll(nextConfigFileNamePathern, nextFileName + ".json");
			}
			else {
				content = content.replaceAll(linkedConfigAttibuteName, linkedConfigAttibuteName + "_");
			}
			
			Path destPath = Paths.get(path.getParent().toString() + FileUtilities.getPathSeparator()  + siteName + ".json");
			
			Files.write(destPath, content.getBytes(charset));
			
		}
	}
}
