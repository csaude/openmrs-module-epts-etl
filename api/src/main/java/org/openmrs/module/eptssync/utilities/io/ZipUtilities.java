package org.openmrs.module.eptssync.utilities.io;

 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtilities {
    List<String> fileList;
    
    String OUTPUT_ZIP_FILE;
    String SOURCE_FOLDER;
    
    private ZipUtilities(){
    	fileList = new ArrayList<String>();
    }
 
    
    public static List<String> unZip(String zipFile, String outPutFolder){
    	ZipUtilities appZip = new ZipUtilities();
    	appZip.unZipIt(zipFile, outPutFolder);
    	
    	return appZip.fileList;
    }
   
    public static void zip(String pathToDirectory, String pathToGeneratedZip){
    	ZipUtilities appZip = new ZipUtilities();
    
    	appZip.SOURCE_FOLDER = pathToDirectory;
    	appZip.OUTPUT_ZIP_FILE = pathToGeneratedZip;
    
    	appZip.generateFileList(new File(pathToDirectory));
    		
    	appZip.zip();
    	
    	
    }
	 
    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zip(){
     byte[] buffer = new byte[1024];
 
     try{
 
    	FileOutputStream fos = new FileOutputStream(this.OUTPUT_ZIP_FILE);
    	ZipOutputStream zos = new ZipOutputStream(fos);
 
    	System.out.println("Output to Zip : " + this.OUTPUT_ZIP_FILE);
 
    	for(String file : this.fileList){
 
    		System.out.println("File Added : " + file);
    		ZipEntry ze= new ZipEntry(file);
        	zos.putNextEntry(ze);
 
        	FileInputStream in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
 
        	int len;
        	while ((len = in.read(buffer)) > 0) {
        		zos.write(buffer, 0, len);
        	}
 
        	in.close();
    	}
 
    	zos.closeEntry();
    	//remember close it
    	zos.close();
 
    	System.out.println("Done");
    }catch(IOException ex){
       ex.printStackTrace();   
    }
   }
	 
    /**
     * Traverse a directory and get all files,
     * and add the file into fileList  
     * @param node file or directory
     */
    private void generateFileList(File node){
    	if(node.isFile()){
    		fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
    	}
 
		if(node.isDirectory()){
			String[] subNote = node.list();
			
			for(String filename : subNote){
				generateFileList(new File(node, filename));
			}
		}
    }
	 
    /**
     * Format the file path for zip
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file){
    	return file.substring(SOURCE_FOLDER.length()+1, file.length());
    }
    
    
    /**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    public void unZipIt(String zipFile, String outputFolder){
 
     byte[] buffer = new byte[1024];
 
     try{
 
    	 this.fileList = new ArrayList<String>();
    	 
    	//create output directory is not exists
    	File folder = new File(outputFolder);
    	
    	if(!folder.exists()){
    		folder.mkdir();
    	}
 
    	//get the zip file content
    	ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    	//get the zipped file list entry
    	ZipEntry ze = zis.getNextEntry();
    	
    	while(ze!=null){
    		
    	   String fileName = ze.getName();
           File newFile = new File(outputFolder + File.separator + fileName);
 
           System.out.println("file unzip : "+ newFile.getAbsoluteFile());
           
           this.fileList.add(""+newFile.getAbsoluteFile());
           
            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();
 
            FileOutputStream fos = new FileOutputStream(newFile);             
 
            int len;
            while ((len = zis.read(buffer)) > 0) {
            	fos.write(buffer, 0, len);
            }
 
            fos.close();   
            ze = zis.getNextEntry();
    	}
 
        zis.closeEntry();
    	zis.close();
 
    	System.out.println("Done");
    }catch(IOException ex){
       ex.printStackTrace(); 
    }
   }    
}
