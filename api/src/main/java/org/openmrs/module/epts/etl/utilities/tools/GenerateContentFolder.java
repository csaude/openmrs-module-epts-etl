package org.openmrs.module.epts.etl.utilities.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.utilities.parseToCSV;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class GenerateContentFolder {
	
	public static final String templateFolder = "D:\\PRG\\JEE\\Projects\\dinat\\maven_project_template";
	
	public static final String projectsSrc = "D:\\PRG\\JEE\\Projects\\lims_3.3_old";
	
	public static final String bkpProjectsSrc = "D:\\PRG\\JEE\\Projects\\lims_3.4_old.bkp";
	
	public static final String destProject = "D:\\PRG\\JEE\\Projects\\dinat\\sigit\\lims_3.3";
	
	static parseToCSV utilities = parseToCSV.getInstance();
	
	public static void main(String[] args) throws IOException {
		
		String[] novembro = { "E001", "C001", "C010", "C038", "C056", "C043", "C009", "C016", "C027", "C045", "C049",
		        "C002" };
		String[] dezembro = { "E002", "C021", "C008", "C025", "C003", "C004", "C006", "C047", "C031", "C054", "C035", "C050",
		        "C048", "C026", "C042", "C030", "C039", "C052", "C053", "C005", "C041", "C018", "C019", "C037", "C017",
		        "C024", "C032", "C036", "C044" };
		String[] janeiro = { "E007", "C023", "C012", "C011", "C028", "C034", "C033", "C029", "C040", "C046", "C051" };
		
		String[] fevereiro = { "E008", "E006" };
		
		String[] marco = { "E004", "E055" };
		
		String[] abril = { "E005", "E003" };
		
		Map<String, String[]> meses = new HashMap<>();
		
		meses.put("novembro", novembro);
		meses.put("dezembro", dezembro);
		meses.put("janeiro", janeiro);
		meses.put("fevereiro", fevereiro);
		meses.put("marco", marco);
		meses.put("abril", abril);
		
		List<String> allActivitities = FileUtilities.readAllFileAsListOfString(
		    "D:\\PROJECTOS\\Minag\\TERRAS\\MOZLAND\\Directorio de Trabalho\\00_Plano\\organizacao_atividades\\all.txt");
		
		String activititeBaseFolder = "D:\\PROJECTOS\\Minag\\TERRAS\\MOZLAND\\Directorio de Trabalho\\00_Plano\\organizacao_atividades\\";
		
		for (String activititie : allActivitities) {
			
			String a = activititie.split(",")[0];
			
			for (Map.Entry<String, String[]> e : meses.entrySet()) {
				if (utilities.existOnArray(e.getValue(), a)) {
					FileUtilities.write(activititeBaseFolder + e.getKey() + ".txt", activititie);
					break;
				}
			}
			
		}
	}
	
	//PROJECT_NAME
	//PROJECT_DESCRIPTION
	
	public static void main_(String[] args) throws IOException {
		File templateProject = new File(templateFolder);
		
		List<File> projects = retrieveAvaliableSrcProjects(new File(projectsSrc));
		
		String separator = FileUtilities.getPathSeparator();
		
		//List<String> excluded = parseToCSV.getInstance().parseToList("lims_ladm");
		//List<String> include = parseToCSV.getInstance().parseToList();
		
		for (File srcProjectFolder : projects) {
			
			String projectName = FileUtilities.generateFileName(srcProjectFolder);
			
			if (!projectName.equals("workflow_base")) {//excluded.contains(projectName) ) {
				continue;
			}
			
			File projectDest = new File(destProject + separator + projectName);
			
			FileUtilities.copyDirectory(templateProject, projectDest);
			
			//File srcProjectPackage = new File( srcProjectFolder.getAbsolutePath() + separator + "src");
			File srcProjectPackage = new File(srcProjectFolder.getAbsolutePath());
			File dstProjectPackage = new File(
			        projectDest.getAbsolutePath() + separator + "src" + separator + "main" + separator + "java");
			File srcProjectMETAINF = new File(srcProjectFolder.getAbsolutePath() + separator + "META-INF");
			File dstProjectMETAINF = new File(projectDest.getAbsolutePath() + separator + "META-INF");
			
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
						return name.toLowerCase().endsWith("dump");
					}
				});
				
				if (files.length > 0) {
					dumps.addAll(parseToCSV.getInstance().parseArrayToList(files));
				}*/
			}
		}
		
		return projects;
	}
	
}
