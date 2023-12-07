package org.openmrs.module.epts.etl.utilities.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class GenerateContentFolder {
	
	public static final String templateFolder = "D:\\PRG\\JEE\\Projects\\dinat\\maven_project_template";
	
	public static final String projectsSrc = "D:\\PRG\\JEE\\Projects\\lims_3.3_old";
	
	public static final String bkpProjectsSrc = "D:\\PRG\\JEE\\Projects\\lims_3.4_old.bkp";
	
	public static final String destProject = "D:\\PRG\\JEE\\Projects\\dinat\\sigit\\lims_3.3";

	
	//PROJECT_NAME
	//PROJECT_DESCRIPTION
	
	public static void main(String[] args) throws IOException {
		File templateProject = new File(templateFolder);
		
		List<File> projects = retrieveAvaliableSrcProjects(new File(projectsSrc));
		
		String separator = FileUtilities.getPathSeparator();
		
		List<String> excluded = CommonUtilities.getInstance().parseToList("lims_ladm");
		//List<String> include = CommonUtilities.getInstance().parseToList();
			
		for (File srcProjectFolder : projects) {
			
			String projectName = FileUtilities.generateFileName(srcProjectFolder);
			
			if (!projectName.equals("workflow_base")){//excluded.contains(projectName) ) {
				continue;
			}
			
			File projectDest =  new File(destProject + separator + projectName);
			
			FileUtilities.copyDirectory(templateProject, projectDest);
			
			//File srcProjectPackage = new File( srcProjectFolder.getAbsolutePath() + separator + "src");
			File srcProjectPackage = new File( srcProjectFolder.getAbsolutePath());
			File dstProjectPackage = new File (projectDest.getAbsolutePath() + separator + "src" + separator + "main" + separator + "java");
			File srcProjectMETAINF = new File( srcProjectFolder.getAbsolutePath() + separator + "META-INF");
			File dstProjectMETAINF = new File (projectDest.getAbsolutePath() + separator + "META-INF");
			
			FileUtilities.copyDirectory(srcProjectPackage, dstProjectPackage);
			
			if (srcProjectMETAINF.exists()) {
				FileUtilities.copyDirectory(srcProjectMETAINF, dstProjectMETAINF);
			}
			
			File eclipseProjectInfoFile = new File(projectDest.getAbsolutePath() + separator + ".project");
			File mavenPomFile = new File(projectDest.getAbsolutePath() + separator + "pom.xml");
			
			FileUtilities.replaceAllInFile(eclipseProjectInfoFile, "PROJECT_NAME", projectName);
			FileUtilities.replaceAllInFile(mavenPomFile, "PROJECT_NAME", projectName);
			FileUtilities.replaceAllInFile(mavenPomFile, "PROJECT_DESCRIPTION", projectName);
		}
		
	}
	
	public static List<File> retrieveAvaliableSrcProjects(File rootDirectory) {
		List<File> projects = new ArrayList<File>();
		
		//Loop over the all folder content
		for (File file : rootDirectory.listFiles()) {
			
			if (file.isDirectory()) {
				projects.add(file);
			} else {
				/*files = file.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith("sql");
					}
				});
				
				if (files.length > 0) {
					dumps.addAll(CommonUtilities.getInstance().parseArrayToList(files));
				}*/
			}
		}
		
		return projects;
	}
	
}
