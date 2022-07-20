package org.openmrs.module.eptssync.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class ModuleContentUpdater {
	
	public static void copyPOJOContentToModule(SyncConfiguration syncConfiguration, AppInfo app) throws IOException {
		File jarTmpFolder = new File (syncConfiguration.getSyncRootDirectory() + FileUtilities.getPathSeparator() + "temp");
	
		FileUtilities.tryToCreateDirectoryStructure(jarTmpFolder.getAbsolutePath());
		
		String omodFileName = jarTmpFolder.getAbsoluteFile() + FileUtilities.getPathSeparator() + FileUtilities.generateFileNameFromRealPath(retrieveModuleFile().getAbsolutePath());
		
		FileOutputStream newJarFileOut = new FileOutputStream(omodFileName);
		JarOutputStream newJar = new JarOutputStream(newJarFileOut);
		
		File classPathContentTempDir = new File(jarTmpFolder + FileUtilities.getPathSeparator() + "classPathContent");
		
		FileUtilities.tryToCreateDirectoryStructure(classPathContentTempDir.getAbsolutePath());
		
		copyClassPathContentToFolder(syncConfiguration.getPojoPackageAsDirectory(app), new File(classPathContentTempDir.getAbsoluteFile() + FileUtilities.getPathSeparator() + syncConfiguration.getPojoPackageRelativePath(app)));
	
		for(File f : classPathContentTempDir.listFiles()) {
			copyEntryToJar(f, newJar, classPathContentTempDir);
		}
		
		newJar.close();
		newJarFileOut.close();
		
		FileUtilities.removeFile(syncConfiguration.getClassPath());
		FileUtilities.copyFile(new File(omodFileName), syncConfiguration.getClassPathAsFile());
	}
	
	/*
	public static void updateClassPathJar(SyncConfiguration syncConfiguration) throws IOException {
		File jarTmpFolder = new File (syncConfiguration.getSyncRootDirectory() + FileUtilities.getPathSeparator() + "temp");
	
		FileUtilities.tryToCreateDirectoryStructure(jarTmpFolder.getAbsolutePath());
		
		String jarFileName = jarTmpFolder.getAbsoluteFile() + FileUtilities.getPathSeparator() + FileUtilities.generateFileNameFromRealPath(syncConfiguration.getClassPath());
		
		syncConfiguration.getClass().getClassLoader().getResource("org/openmrs/module/eptssync/controller/conf/SyncConfiguration.class");
		
		FileOutputStream newJarFileOut = new FileOutputStream(jarFileName);
		JarOutputStream newJar = new JarOutputStream(newJarFileOut);
		
		File classPathContentTempDir = new File(jarTmpFolder + FileUtilities.getPathSeparator() + "classPathContent");
		
		FileUtilities.tryToCreateDirectoryStructure(classPathContentTempDir.getAbsolutePath());
		
		copyJarContentToFolder(syncConfiguration.getClassPathAsFile(), classPathContentTempDir);
		
		copyClassPathContentToFolder(syncConfiguration.getPojoPackageAsDirectory(), new File(classPathContentTempDir.getAbsoluteFile() + FileUtilities.getPathSeparator() + syncConfiguration.getPojoPackageRelativePath()));
	
		for(File f : classPathContentTempDir.listFiles()) {
			copyEntryToJar(f, newJar, classPathContentTempDir);
		}
		
		newJar.close();
		newJarFileOut.close();
	}*/
	
	public static void copyEntryToJar(File source, JarOutputStream jarOut, File jarLocationRootFolder) throws IOException {
		if (source.isDirectory()) {
			
			for(File f : source.listFiles()) {
				copyEntryToJar(f, jarOut, jarLocationRootFolder);
			}
		}
		else {
			String entryName = getEntryNamePackage(source, jarLocationRootFolder);
			entryName += (!entryName.isEmpty() ? FileUtilities.getPathSeparator() : "") + source.getName();
			
			jarOut.putNextEntry(new ZipEntry(entryName));
			jarOut.write(FileUtilities.readFileAsByte(source.getAbsolutePath()));
		
			jarOut.closeEntry();
		}
	}
	
	public static String getEntryNamePackage(File file, File jarLocationRootFolder) throws FileNotFoundException {
		String path;
		
		if (file.isDirectory()) {
			path = file.getAbsolutePath();
		}
		else {
			path = FileUtilities.getDirectory(file.getAbsolutePath()).getAbsolutePath();
		}
		
		return path.substring((int)jarLocationRootFolder.getAbsolutePath().length());
	}
	
	public static void copyClassPathContentToFolder(File classPath, File destinationFolder) throws IOException {
		if (classPath.isDirectory()) {
			for (File file : classPath.listFiles()) {
				if (file.isDirectory()) {
					copyClassPathContentToFolder(classPath, destinationFolder);
				}
				else
				if (FileUtilities.determineExtencaoApartirDoNome(file.getAbsolutePath()).equalsIgnoreCase("jar")){
					copyJarContentToFolder(file, destinationFolder);
				}
				else {
					FileUtilities.copyFile(file, new File(destinationFolder.getAbsolutePath() + FileUtilities.getPathSeparator() + file.getName()));	
				}
			} 
		}
		else
		if (FileUtilities.determineExtencaoApartirDoNome(classPath.getAbsolutePath()).equalsIgnoreCase("jar")){
			copyJarContentToFolder(classPath, destinationFolder);
		}
		else {
			FileUtilities.copyFile(classPath, new File(destinationFolder.getAbsolutePath() + FileUtilities.getPathSeparator() + classPath.getName()));	
		}
	}
	
	public static void copyJarContentToFolder(File jarFile, File destinationFolder) throws IOException {
		JarFile jar = new JarFile(jarFile);
 
		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();
 
			String fileName = destinationFolder.getAbsolutePath() + FileUtilities.getPathSeparator() + entry.getName();
			
			FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
			
			File f = new File(fileName);
 
			if (!fileName.endsWith("/")) {
				InputStream is = jar.getInputStream(entry);
				FileOutputStream fos = new FileOutputStream(f);
 
				while (is.available() > 0) {
					fos.write(is.read());
				}
 
				fos.close();
				is.close();
			}
		}
		
		jar.close();
	}
	
	public static void addToClasspath(File file, SyncConfiguration syncConfiguration, AppInfo app) throws IOException {
		FileUtilities.copyFile(file, new File(retrievePojoFolderOnModuleDirectory(syncConfiguration, app) + FileUtilities.getPathSeparator() + file.getName()));
	}	
	
	public static void tryToAddAllPOJOToClassPath(SyncConfiguration syncConfiguration, AppInfo app) {
		File pojoPackageDir = new File(syncConfiguration.getPOJOCompiledFilesDirectory().getAbsolutePath() + "/org/openmrs/module/eptssync/model/pojo/" + syncConfiguration.getPojoPackage(app));
		
		File[] existingClasses = pojoPackageDir.listFiles();
		
		if (existingClasses != null) {
			try {
				URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				
				for (File classFile : existingClasses) {
					method.invoke(classLoader, classFile.toURI().toURL());
				}
			}catch (Exception e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			} 
		}
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
	
	protected static File retrievePojoFolderOnModuleDirectory(SyncConfiguration syncConfiguration, AppInfo app) {
		String pojoFolderOnModule = "";
		
		pojoFolderOnModule += syncConfiguration.getModuleRootDirectory().getAbsoluteFile() + FileUtilities.getPathSeparator();
		pojoFolderOnModule += "eptssync" + FileUtilities.getPathSeparator();
		pojoFolderOnModule += syncConfiguration.getPojoPackageRelativePath(app);
		
		return new File( pojoFolderOnModule);
	}
}
