package org.openmrs.module.eptssync.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class ZipUtilities {
	public static void addFileToZip(File source, File file, String path){
		File[] files = {file};
		
		addFilesToZip(source, files, path);		
	}
	
	public static void addFilesToZip(File source, File[] files, String path){
	    try{
	        File tmpZip = File.createTempFile(source.getName(), null);
	        tmpZip.delete();
	        if(!source.renameTo(tmpZip)){
	            throw new Exception("Could not make temp file (" + source.getName() + ")");
	        }
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
	        e.printStackTrace();
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
	
	public static File retrieveModuleFolder() {
		return retrieveModuleFile().getParentFile();
	}	
	
	/**
	 * Identify a eptssync module file
	 */
	public static File retrieveModuleFile() {
		File modulesDirectory = new File(OpenmrsUtil.getApplicationDataDirectory() + FileUtilities.getPathSeparator() + "modules");
			
		File[] allFiles = modulesDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().contains("eptssync");
			}
		}); 
		
		if (allFiles == null) throw new ForbiddenOperationException("The modules directory was not located on " + modulesDirectory.getAbsolutePath());
		
		if (allFiles.length == 0) 		throw new ForbiddenOperationException("The application was not able to identify the eptssync module on " + modulesDirectory.getAbsolutePath());
		 
		if (allFiles.length > 1) 		throw new ForbiddenOperationException("There are multiple eptssync modules on " + modulesDirectory.getAbsolutePath());

		return allFiles[0];
	}
	
	public static File retrieveModuleJar() {
		File moduleJar = null;
		
		try {
			moduleJar = retrieveModuleJarOnOpenMRS2x();
			
			if (moduleJar != null) return moduleJar;
			
		} catch (Exception e) {
		}
		
		
		return retrieveModuleJarOnOpenMRS1x();
	}
	
	private static File retrieveModuleJarOnOpenMRS2x() {
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
	
	public static File retrieveOpenMRSWebAppFolder() {
		String rootDirectory = Paths.get(".").normalize().toAbsolutePath().toString();
		
		return  new File(rootDirectory + FileUtilities.getPathSeparator() + "webapps" + FileUtilities.getPathSeparator() + "openmrs");
	}
	
	public static void copyFileToOpenMRSTagsDirectory(File file) throws IOException {
		File tagDir = new File(retrieveOpenMRSWebAppFolder().getAbsoluteFile() + FileUtilities.getPathSeparator() + "WEB-INF" + FileUtilities.getPathSeparator() + "tags");
	
		FileUtilities.copyFile(file, new File(tagDir.getAbsolutePath() + FileUtilities.getPathSeparator() + file.getName()));
	}
	
	public static void main(String[] args) {
		File[] files = new File[1];
		files[0] = new File("/home/jpboane/working/prg/jee/tmp/cs_1_de_maio");
		
		//addFilesToZip(new File("/home/jpboane/working/prg/jee/workspace/w02/openmrs-module-eptssync/omod/target/eptssync-1.0-SNAPSHOT_bkp.omod"), files, "/org/openmrs/module/eptssync/model/pojo/");
		addFilesToZip(new File("/home/jpboane/working/prg/jee/workspace/w02/openmrs-module-eptssync/omod/target/eptssync-1.0-SNAPSHOT.omod"), new File("/home/jpboane/working/prg/jee/tmp/cs_1_de_maio").listFiles(), "org/openmrs/module/eptssync/model/pojo/cs_1_de_maio/");
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
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		
	}
}
