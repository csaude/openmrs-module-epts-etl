package org.openmrs.module.epts.etl.utilities;

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
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;
import org.openmrs.util.OpenmrsClassLoader;

public class ClassPathUtilities {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static void addFileToZip(File source, File file, String path) {
		File[] files = { file };
		
		addFilesToZip(source, files, path);
	}
	
	public static void addFilesToZip(File source, File[] files, String path) {
		try {
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
			for (int i = 0; i < files.length; i++) {
				InputStream in = new FileInputStream(files[i]);
				out.putNextEntry(new ZipEntry(path + files[i].getName()));
				
				//out.write(FileUtilities.readFileAsByte(files[i].getAbsolutePath()));
				
				for (int read = in.read(buffer); read > -1; read = in.read(buffer)) {
					out.write(buffer, 0, read);
				}
				
				out.closeEntry();
				in.close();
			}
			
			ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
			
			for (ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()) {
				if (!zipEntryMatch(ze.getName(), files, path)) {
					out.putNextEntry(ze);
					for (int read = zin.read(buffer); read > -1; read = zin.read(buffer)) {
						out.write(buffer, 0, read);
					}
					out.closeEntry();
				}
			}
			
			out.close();
			zin.close();
			tmpZip.delete();
		}
		catch (Exception e) {
			// e.printStackTrace();
		}
	}
	
	public static void addFilesToFolder(File folder, File[] files, String path) {
		try {
			for (File file : files) {
				FileUtilities.copyFile(file, new File(folder.getAbsoluteFile() + FileUtilities.getPathSeparator() + path
				        + FileUtilities.getPathSeparator() + file.getName()));
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean zipEntryMatch(String zeName, File[] files, String path) {
		for (int i = 0; i < files.length; i++) {
			if ((path + files[i].getName()).equals(zeName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Identify a epts-etl module file
	 */
	public static File retrieveModuleFile() {
		File modulesDirectory = null;
		
		try {
			modulesDirectory = ModuleUtil.getModuleRepository();
		}
		catch (NoClassDefFoundError e) {
			//e.printStackTrace();
		}
		catch (Error e) {
			//e.printStackTrace();
		}
		
		if (modulesDirectory != null) {
			File[] allFiles = modulesDirectory.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().startsWith("epts-etl") && pathname.getName().endsWith("omod");
				}
			});
			
			if (allFiles == null)
				throw new ForbiddenOperationException(
				        "The modules directory was not located on " + modulesDirectory.getAbsolutePath());
			
			if (allFiles.length == 0)
				throw new ForbiddenOperationException("The application was not able to identify the epts-etlmodule on "
				        + modulesDirectory.getAbsolutePath());
			
			if (allFiles.length > 1)
				throw new ForbiddenOperationException(
				        "There are multiple epts-etl modules on " + modulesDirectory.getAbsolutePath());
			
			return allFiles[0];
		}
		
		return null;
	}
	
	public static File retrieveModuleJar() {
		try {
			File moduleFilesDirectory = new File(OpenmrsClassLoader.getLibCacheFolder().getAbsolutePath()
			        + FileUtilities.getPathSeparator() + FileUtilities.getPathSeparator() + "epts-etl");
			
			return new File(moduleFilesDirectory.getAbsolutePath() + FileUtilities.getPathSeparator() + "epts-etl.jar");
		}
		catch (NoClassDefFoundError e) {
			//e.printStackTrace();
		}
		catch (Error e) {
			//e.printStackTrace();
		}
		
		return null;
	}
	
	public static File retrieveModuleFolder() {
		File moduleJar = retrieveModuleJar();
		
		if (moduleJar != null)
			return new File(retrieveModuleJar().getParent());
		
		return null;
	}
	
	public static File retrieveOpenMRSWebAppFolder() {
		String rootDirectory = Paths.get(".").normalize().toAbsolutePath().toString();
		
		return new File(
		        rootDirectory + FileUtilities.getPathSeparator() + "webapps" + FileUtilities.getPathSeparator() + "openmrs");
	}
	
	public static void copyFileToOpenMRSTagsDirectory(File file) throws IOException {
		File tagDir = new File(retrieveOpenMRSWebAppFolder().getAbsoluteFile() + FileUtilities.getPathSeparator() + "WEB-INF"
		        + FileUtilities.getPathSeparator() + "tags");
		
		FileUtilities.copyFile(file, new File(tagDir.getAbsolutePath() + FileUtilities.getPathSeparator() + file.getName()));
	}
	
	public static void copyModuleTagsToOpenMRS() {
		try {
			ZipFile zipfile = new ZipFile(retrieveModuleJar());
			
			ZipEntry tagEntry = zipfile.getEntry("web/module/tags/syncStatusTab.tag");
			
			File tagDir = new File(retrieveOpenMRSWebAppFolder().getAbsoluteFile() + FileUtilities.getPathSeparator()
			        + "WEB-INF" + FileUtilities.getPathSeparator() + "tags");
			
			File destFile = new File(tagDir.getAbsolutePath() + FileUtilities.getPathSeparator()
			        + FileUtilities.generateFileNameFromRealPath(tagEntry.getName()));
			
			destFile.delete();
			
			FileUtilities.write(destFile.getAbsolutePath(), zipfile.getInputStream(tagEntry));
			
		}
		catch (ZipException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	public static void addClassToClassPath(DatabaseObjectConfiguration pojoble, AppInfo app) {
		String pojoPackageDir = pojoble.getRelatedSyncConfiguration().getPojoPackageAsDirectory(app).getAbsolutePath();
		
		File clazzFile = new File(
		        pojoPackageDir + FileUtilities.getPathSeparator() + pojoble.generateClassName() + ".class");
		
		if (clazzFile.exists()) {
			addClassToClassPath(utilities.parseObjectToArray(clazzFile),
			    pojoble.getRelatedSyncConfiguration().getPojoPackageRelativePath(app),
			    pojoble.getRelatedSyncConfiguration());
		}
	}
	
	public static void addClassToClassPath(File[] clazzFiless, String path, EtlConfiguration etlConfiguration) {
		try {
			if (etlConfiguration.getClassPathAsFile().exists())
				ClassPathUtilities.addFilesToZip(etlConfiguration.getClassPathAsFile(), clazzFiless, path);
		}
		catch (Exception e) {}
		
		try {
			File moduleJar = ClassPathUtilities.retrieveModuleJar();
			
			if (moduleJar != null && moduleJar.exists())
				ClassPathUtilities.addFilesToZip(moduleJar, clazzFiless, path);
		}
		catch (Exception e) {}
		
		try {
			File moduleFile = ClassPathUtilities.retrieveModuleJar();
			
			if (moduleFile != null && moduleFile.exists())
				ClassPathUtilities.addFilesToZip(moduleFile, clazzFiless, path);
		}
		catch (Exception e) {}
		
		try {
			File moduleFolder = ClassPathUtilities.retrieveModuleJar();
			
			if (moduleFolder != null && moduleFolder.exists())
				ClassPathUtilities.addFilesToFolder(moduleFolder, clazzFiless, path);
		}
		catch (Exception e) {}
	}
	
	public static void tryToCopyPOJOToClassPath(EtlConfiguration etlConfiguration, AppInfo app) {
		if (etlConfiguration.getPojoPackageAsDirectory(app).exists()) {
			
			List<File> clazzListFiless = new ArrayList<File>();
			
			for (EtlItemConfiguration config : etlConfiguration.getEtlItemConfiguration()) {
				AbstractTableConfiguration tableConfiguration = config.getSrcConf();
				
				tryToCopyPOJOToClassPath(tableConfiguration, clazzListFiless, app);
				
				for (DstConf dstConf : config.getDstConf()) {
					tryToCopyPOJOToClassPath(dstConf, clazzListFiless, app);
				}
			}
			
			addClassToClassPath(utilities.parseListToArray(clazzListFiless),
			    etlConfiguration.getPojoPackageRelativePath(app), etlConfiguration);
		}
	}
	
	public static void tryToCopyPOJOToClassPath(AbstractTableConfiguration tableConfiguration, List<File> clazzListFiless,
	        AppInfo app) {
		String pojoPackageDir = tableConfiguration.getRelatedSyncConfiguration().getPojoPackageAsDirectory(app)
		        .getAbsolutePath();
		
		File clazzFile = new File(
		        pojoPackageDir + FileUtilities.getPathSeparator() + tableConfiguration.generateClassName() + ".class");
		
		if (clazzFile.exists()) {
			clazzListFiless.add(clazzFile);
		}
	}
}
