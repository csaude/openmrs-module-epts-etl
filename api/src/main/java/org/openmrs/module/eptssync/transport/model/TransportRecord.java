package org.openmrs.module.eptssync.transport.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * @author jpboane
 */
public class TransportRecord extends BaseVO implements SyncRecord {
	private File file;
	private File relatedMinimalInfoFile;
	private File bkpDirectory;
	private File destDirectory;

	public TransportRecord(File file, File destDirectory, File bkpDirectory) {
		this.file = file;
		this.bkpDirectory = bkpDirectory;
		this.destDirectory = destDirectory;
	}

	public File getFile() {
		return file;
	}

	@Override
	public int getObjectId() {
		return 0;
	}

	@Override
	public void setObjectId(int selfId) {
	}

	public File generateRelatedMinimalInfoFile() {
		if (this.relatedMinimalInfoFile == null) {
			String[] parts = this.file.getAbsolutePath().split(".json");
			String minimalFile = parts[0] + "_minimal.json";

			this.relatedMinimalInfoFile = new File(minimalFile);
		}

		return this.relatedMinimalInfoFile;
	}
	
	public File getDestinationFile() {
		return new File(destFileName+".json");
	}
	
	public File getMinimalDestinationFile() {
		return new File(pathToBkpMinimalFile + ".json");
	}
	
	private String destFileName;
	private String pathToBkpMinimalFile;

	public void moveToBackUpDirectory() {
		String pathToBkpFile = "";

		pathToBkpFile += bkpDirectory.getAbsolutePath();
		pathToBkpFile += FileUtilities.getPathSeparator();

		pathToBkpFile += FileUtilities.generateFileNameFromRealPath(this.file.getAbsolutePath());

		FileUtilities.renameTo(this.file.getAbsolutePath(), pathToBkpFile);

		// NOW, MOVE MINIMAL FILE
		pathToBkpMinimalFile = "";
		pathToBkpMinimalFile += this.bkpDirectory.getAbsolutePath();
		pathToBkpMinimalFile += FileUtilities.getPathSeparator();

		pathToBkpMinimalFile += FileUtilities.generateFileNameFromRealPath(generateRelatedMinimalInfoFile().getAbsolutePath());

		FileUtilities.renameTo(generateRelatedMinimalInfoFile().getAbsolutePath(), pathToBkpMinimalFile);
	}
	
	public void transport() {
		try {
			destFileName = "";
			
			destFileName += this.destDirectory.getAbsolutePath();
			destFileName += FileUtilities.getPathSeparator();
			
			//To make a file only avaliable after a copy process terminated, the name must be without extension
			destFileName += FileUtilities.generateFileNameFromRealPathWithoutExtension(this.file.getAbsolutePath());
			
			copy(this.file, new File(destFileName));
			
			String minimalDestFileName = "";

			minimalDestFileName += this.destDirectory.getAbsolutePath();
			minimalDestFileName += FileUtilities.getPathSeparator();
			minimalDestFileName += FileUtilities.generateFileNameFromRealPathWithoutExtension(this.generateRelatedMinimalInfoFile().getAbsolutePath());
			
			copy(this.generateRelatedMinimalInfoFile(), new File(minimalDestFileName));
		
			FileUtilities.renameTo(minimalDestFileName, minimalDestFileName+".json");
			FileUtilities.renameTo(destFileName, destFileName+".json");
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	
	private static void copy(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}

	
	
	private void copy_old(File source, File dest) throws IOException {
		try {
			FileUtilities.copyFile(source, dest);
		} catch (IOException e) {
			if (e.getLocalizedMessage().contains("Failed to copy full contents from")) {
				/*The file is on the creation process
				 * wait and try again
				*/
				TimeCountDown.sleep(10);
				copy(source, dest);
			}
		}
	}
}
