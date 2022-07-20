package org.openmrs.module.eptssync.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.openmrs.module.ModuleUtil;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsClassLoader;

public class ClassPathUtilities {
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static void addFileToZip(File source, File file, String path){
		File[] files = {file};
		
		addFilesToZip(source, files, path);		
	}
	
	public static void addFilesToZip(File source, File[] files, String path){
	    try{
	        File tmpZip = File.createTempFile(source.getName(), null);
	        tmpZip.delete();
	        
	        source.setExecutable(true); 
	        
	        FileUtilities.copyFile(source, tmpZip);
	        source.delete();
	        
	        /*if(!source.renameTo(tmpZip)){
	            throw new Exception("Could not make temp file (" + source.getName() + ")");
	        }*/
	        
	        byte[] buffer = new byte[4096];
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(source));
	        for(int i = 0; i < files.length; i++){
	            InputStream in = new FileInputStream(files[i]);
	            out.putNextEntry(new ZipEntry(path + files[i].getName()));
	           
	            //out.write(FileUtilities.readFileAsByte(files[i].getAbsolutePath()));
	    		
	            for(int read = in.read(buffer); read > -1; read = in.read(buffer)){
	                out.write(buffer, 0, read);
	            }
	            
	            out.closeEntry();
	            in.close();
	        }
	       
	        ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
		     
	        for(ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()){
	            if(!zipEntryMatch(ze.getName(), files, path)){
	                out.putNextEntry(ze);
	                for(int read = zin.read(buffer); read > -1; read = zin.read(buffer)){
	                    out.write(buffer, 0, read);
	                }
	                out.closeEntry();
	            }
	        }
	        
	        out.close();
	        zin.close();
	        tmpZip.delete();
	    }catch(Exception e){
	       // e.printStackTrace();
	    }
	}
	
	
	public static void addFilesToFolder(File folder, File[] files, String path)  {
		 try {
			for (File file : files) {
				FileUtilities.copyFile(file, new File (folder.getAbsoluteFile() + FileUtilities.getPathSeparator() + path + FileUtilities.getPathSeparator() + file.getName())); 
			 }
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static boolean zipEntryMatch(String zeName, File[] files, String path){
	    for(int i = 0; i < files.length; i++){
	        if((path + files[i].getName()).equals(zeName)){
	            return true;
	        }
	    }
	    return false;
	}
	
	/**
	 * Identify a eptssync module file
	 */
	public static File retrieveModuleFile() {
		File modulesDirectory = null;
		
		try {
			modulesDirectory = ModuleUtil.getModuleRepository();
		} catch (NoClassDefFoundError e) {
			//e.printStackTrace();
		} catch (Error e) {
			//e.printStackTrace();
		}
		
		if (modulesDirectory != null) {
			File[] allFiles = modulesDirectory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().startsWith("eptssync") && pathname.getName().endsWith("omod");
				}
			}); 
			
			if (allFiles == null) throw new ForbiddenOperationException("The modules directory was not located on " + modulesDirectory.getAbsolutePath());
			
			if (allFiles.length == 0) 		throw new ForbiddenOperationException("The application was not able to identify the eptssync module on " + modulesDirectory.getAbsolutePath());
			 
			if (allFiles.length > 1) 		throw new ForbiddenOperationException("There are multiple eptssync modules on " + modulesDirectory.getAbsolutePath());
	
			return allFiles[0];
		}
		
		return null;
	}
	
	public static File retrieveModuleJar() {
		try {
			File moduleFilesDirectory = new File(OpenmrsClassLoader.getLibCacheFolder().getAbsolutePath() + FileUtilities.getPathSeparator() + FileUtilities.getPathSeparator() + "eptssync");

			return new File(moduleFilesDirectory.getAbsolutePath() + FileUtilities.getPathSeparator() + "eptssync.jar");
		} catch (NoClassDefFoundError e) {
			//e.printStackTrace();
		} catch (Error e) {
			//e.printStackTrace();
		}
		
		return null;
	}
	
	public static File retrieveModuleFolder() {
		File moduleJar = retrieveModuleJar();
		
		if (moduleJar != null) return new File(retrieveModuleJar().getParent());
		
		return null;
	}
	
	/*private static File retrieveModuleJarOnOpenMRS2x() {
		File f = OpenmrsClassLoader.getLibCacheFolder();
		
		File moduleFilesDirectory = new File(OpenmrsUtil.getApplicationDataDirectory() + FileUtilities.getPathSeparator() + ".openmrs-lib-cache" + FileUtilities.getPathSeparator() + "eptssync");
		
		return new File(moduleFilesDirectory.getAbsolutePath() + FileUtilities.getPathSeparator() + "eptssync.jar");	
	}
	
	private static File retrieveModuleJarOnOpenMRS1x() {
		String rootDirectory = Paths.get(".").normalize().toAbsolutePath().toString();
		
		File[] allFiles = new File(rootDirectory + FileUtilities.getPathSeparator() + "temp").listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().contains("openmrs-lib-cache");
			}
		}); 
		
		Arrays.sort(allFiles);
		
		for (int i = allFiles.length - 1; i >= 0; i--) {
			if (allFiles[i].isDirectory()) {
				//File classPath = new File(allFiles[i].getAbsoluteFile() + FileUtilities.getPathSeparator() + "eptssync" + FileUtilities.getPathSeparator() + "lib" + FileUtilities.getPathSeparator() + "eptssync.jar");
				return new File(allFiles[i].getAbsoluteFile() + FileUtilities.getPathSeparator() + "eptssync" + FileUtilities.getPathSeparator() + "eptssync.jar");
			}
		}
		
		return null;
	}
	*/
	
	public static File retrieveOpenMRSWebAppFolder() {
		String rootDirectory = Paths.get(".").normalize().toAbsolutePath().toString();
		
		return  new File(rootDirectory + FileUtilities.getPathSeparator() + "webapps" + FileUtilities.getPathSeparator() + "openmrs");
	}
	
	public static void copyFileToOpenMRSTagsDirectory(File file) throws IOException {
		File tagDir = new File(retrieveOpenMRSWebAppFolder().getAbsoluteFile() + FileUtilities.getPathSeparator() + "WEB-INF" + FileUtilities.getPathSeparator() + "tags");
	
		FileUtilities.copyFile(file, new File(tagDir.getAbsolutePath() + FileUtilities.getPathSeparator() + file.getName()));
	}

	public static void copyModuleTagsToOpenMRS() {
		try {
			ZipFile zipfile = new ZipFile(retrieveModuleJar());
			
			ZipEntry tagEntry = zipfile.getEntry("web/module/tags/syncStatusTab.tag");
			
			File tagDir = new File(retrieveOpenMRSWebAppFolder().getAbsoluteFile() + FileUtilities.getPathSeparator() + "WEB-INF" + FileUtilities.getPathSeparator() + "tags");
			
			File destFile = new File(tagDir.getAbsolutePath() + FileUtilities.getPathSeparator() + FileUtilities.generateFileNameFromRealPath(tagEntry.getName()));
			
			destFile.delete();
			
			FileUtilities.write(destFile.getAbsolutePath(), zipfile.getInputStream(tagEntry));
			
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	public static void addClassToClassPath(SyncTableConfiguration tableConfiguration, AppInfo app){
		String pojoPackageDir = tableConfiguration.getRelatedSynconfiguration().getPojoPackageAsDirectory(app).getAbsolutePath();
		
		File clazzFile = new File(pojoPackageDir + FileUtilities.getPathSeparator() + tableConfiguration.generateClassName() + ".class");
		
		if (clazzFile.exists()) {
			addClassToClassPath(utilities.parseObjectToArray(clazzFile), tableConfiguration.getRelatedSynconfiguration().getPojoPackageRelativePath(app), tableConfiguration.getRelatedSynconfiguration());
		}
	}
	
	public static void addClassToClassPath(File[] clazzFiless, String path, SyncConfiguration syncConfiguration){
		try {
			if (syncConfiguration.getClassPathAsFile().exists()) ClassPathUtilities.addFilesToZip(syncConfiguration.getClassPathAsFile(), clazzFiless, path);
		} catch (Exception e) {}
		
		try {
			File moduleJar = ClassPathUtilities.retrieveModuleJar();
			
			if (moduleJar != null && moduleJar.exists()) ClassPathUtilities.addFilesToZip(moduleJar, clazzFiless, path);
		} catch (Exception e) {}
		
		try {
			File moduleFile = ClassPathUtilities.retrieveModuleJar();
			
			
			if (moduleFile != null && moduleFile.exists()) ClassPathUtilities.addFilesToZip(moduleFile, clazzFiless, path);
		} catch (Exception e) {}
			
		try {
			File moduleFolder= ClassPathUtilities.retrieveModuleJar();
			
			if (moduleFolder != null && moduleFolder.exists()) ClassPathUtilities.addFilesToFolder(moduleFolder, clazzFiless, path);
		} catch (Exception e) {}
	}
	

	public static void tryToCopyPOJOToClassPath(SyncConfiguration syncConfiguration, AppInfo app) {
		if (syncConfiguration.getPojoPackageAsDirectory(app).exists()) {
			String pojoPackageDir = syncConfiguration.getPojoPackageAsDirectory(app).getAbsolutePath();
			
			List<File> clazzListFiless = new  ArrayList<File>();
			
			
			for (SyncTableConfiguration tableConfiguration : syncConfiguration.getTablesConfigurations()) {
				File clazzFile = new File(pojoPackageDir + FileUtilities.getPathSeparator() + tableConfiguration.generateClassName() + ".class");
				
				if (clazzFile.exists()) {
					clazzListFiless.add(clazzFile);
				}
			}
			
			addClassToClassPath(utilities.parseListToArray(clazzListFiless), syncConfiguration.getPojoPackageRelativePath(app), syncConfiguration);
		}
	}
}
