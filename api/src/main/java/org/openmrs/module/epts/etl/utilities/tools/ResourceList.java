package org.openmrs.module.epts.etl.utilities.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class ResourceList {
	
	@SuppressWarnings("unused")
	private static Collection<String> getResourcesFromDirectory(final File directory) {
		final ArrayList<String> retval = new ArrayList<String>();
		final File[] fileList = directory.listFiles();
		
		for (final File file : fileList) {
			if (file.isDirectory()) {
				retval.addAll(getResourcesFromDirectory(file));
			} else {
				try {
					final String fileName = file.getCanonicalPath();
					
					retval.add(fileName);
				}
				catch (final IOException e) {
					throw new Error(e);
				}
			}
		}
		return retval;
	}
	
	private static void recreateResources(final File directory, final File destinationDirectory) throws IOException {
		final File[] fileList = directory.listFiles();
		
		if (!destinationDirectory.exists())
			FileUtilities.createDirectory(destinationDirectory.getCanonicalPath());
		
		for (final File file : fileList) {
			
			
			if (file.isDirectory()) {
				recreateResources(file, new File(destinationDirectory.getCanonicalPath() + "/" + file.getName()));
			} else {
				copyResource(file, destinationDirectory);
			}
			
			TimeCountDown.sleep(5);
			
		}
	}
	
	static void copyResource(File resource, File directory) throws FileNotFoundException, IOException {
		InputStream s = new FileInputStream(resource.getCanonicalPath());
		
		FileUtilities.write(directory.getCanonicalPath() + "/" + resource.getName(), s);
	}
	
	public static void main(final String[] args) throws IOException {
		ResourceList.recreateResources(new File("D:\\MIDIA\\Audio & Music\\New\\2024\\Afro_PlayList_m3u"), new File("D:\\MIDIA\\Audio & Music\\New\\2024\\Afro_PlayList_m3u\\Afro"));
	}

}