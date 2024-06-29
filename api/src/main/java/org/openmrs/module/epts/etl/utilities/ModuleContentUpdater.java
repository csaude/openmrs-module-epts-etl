package org.openmrs.module.epts.etl.utilities;

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

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsUtil;

public class ModuleContentUpdater {
	
	public static void copyPOJOContentToModule(EtlConfiguration etlConfiguration, DBConnectionInfo app) throws IOException {
		File jarTmpFolder = new File (etlConfiguration.getEtlRootDirectory() + FileUtilities.getPathSeparator() + "temp");
	
		FileUtilities.tryToCreateDirectoryStructure(jarTmpFolder.getAbsolutePath());
		
		String omodFileName = jarTmpFolder.getAbsoluteFile() + FileUtilities.getPathSeparator() + FileUtilities.generateFileNameFromRealPath(retrieveModuleFile().getAbsolutePath());
		
		FileOutputStream newJarFileOut = new FileOutputStream(omodFileName);
		JarOutputStream newJar = new JarOutputStream(newJarFileOut);
		
		File classPathContentTempDir = new File(jarTmpFolder + FileUtilities.getPathSeparator() + "classPathContent");
		
		FileUtilities.tryToCreateDirectoryStructure(classPathContentTempDir.getAbsolutePath());
		
		copyClassPathContentToFolder(etlConfiguration.getPojoPackageAsDirectory(app), new File(classPathContentTempDir.getAbsoluteFile() + FileUtilities.getPathSeparator() + etlConfiguration.getPojoPackageRelativePath(app)));
	
		for(File f : classPathContentTempDir.listFiles()) {
			copyEntryToJar(f, newJar, classPathContentTempDir);
		}
		
		newJar.close();
		newJarFileOut.close();
		
		FileUtilities.removeFile(etlConfiguration.getClassPath());
		FileUtilities.copyFile(new File(omodFileName), etlConfiguration.getClassPathAsFile());
	}
	
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
	
	public static void addToClasspath(File file, EtlConfiguration etlConfiguration, DBConnectionInfo app) throws IOException {
		FileUtilities.copyFile(file, new File(retrievePojoFolderOnModuleDirectory(etlConfiguration, app) + FileUtilities.getPathSeparator() + file.getName()));
	}	
	
	public static void tryToAddAllPOJOToClassPath(EtlConfiguration etlConfiguration, DBConnectionInfo app) {
		File pojoPackageDir = new File(etlConfiguration.getPOJOCompiledFilesDirectory().getAbsolutePath() + "/org/openmrs/module/epts/etl/model/pojo/" + etlConfiguration.getPojoPackage(app));
		
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
	 * Identify a epts-etl module file
	 */
	public static File retrieveModuleFile() {
		File modulesDirectory = new File(OpenmrsUtil.getApplicationDataDirectory() + FileUtilities.getPathSeparator() + "modules");
			
		File[] allFiles = modulesDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().contains("epts-etl");
			}
		}); 
		
		if (allFiles == null) throw new ForbiddenOperationException("The modules directory was not located on " + modulesDirectory.getAbsolutePath());
		
		if (allFiles.length == 0) 		throw new ForbiddenOperationException("The application was not able to identify the epts-etlmodule on " + modulesDirectory.getAbsolutePath());
		 
		if (allFiles.length > 1) 		throw new ForbiddenOperationException("There are multiple epts-etl modules on " + modulesDirectory.getAbsolutePath());

		return allFiles[0];
	}
	
	protected static File retrievePojoFolderOnModuleDirectory(EtlConfiguration etlConfiguration, DBConnectionInfo app) {
		String pojoFolderOnModule = "";
		
		pojoFolderOnModule += etlConfiguration.getModuleRootDirectory().getAbsoluteFile() + FileUtilities.getPathSeparator();
		pojoFolderOnModule += "epts" + FileUtilities.getPathSeparator();
		pojoFolderOnModule += "etl" + FileUtilities.getPathSeparator();
		pojoFolderOnModule += etlConfiguration.getPojoPackageRelativePath(app);
		
		return new File( pojoFolderOnModule);
	}
}
