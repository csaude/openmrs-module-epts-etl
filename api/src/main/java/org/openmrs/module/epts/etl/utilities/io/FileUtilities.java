/**
 * @author J.P Boane
 * 2010.JAN.21
 */
package org.openmrs.module.epts.etl.utilities.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileUtilities {
	
	/**
	 * Operacoes basicas sobre ficheiros
	 */
	
	public static void tryToCreateDirectoryStructureForFile(String fileName) {
		String[] folderHierarchy = null;
		
		try {
			folderHierarchy = fileName.split(getPathSeparator());
		}
		catch (Exception e) {
			//System.out.println("Error: You may be using Windows");
		}
		
		if (folderHierarchy == null)
			folderHierarchy = fileName.split("\\\\"); //If OS is Window
			
		if (folderHierarchy.length == 1)
			return;
		
		String currFolder = folderHierarchy[0];
		
		createDirectory(currFolder);
		
		for (int i = 1; i < folderHierarchy.length - 1; i++) {
			currFolder += getPathSeparator() + folderHierarchy[i];
			
			createDirectory(currFolder);
		}
	}
	
	public static void tryToCreateDirectoryStructure(String fullPathToDirectory) {
		String[] folderHierarchy = null;
		
		try {
			folderHierarchy = fullPathToDirectory.split(getPathSeparator());
		}
		catch (Exception e) {
			//System.out.println("Error: You may be using Windows");
		}
		
		if (folderHierarchy == null)
			folderHierarchy = fullPathToDirectory.split("\\\\"); //If OS is Window
			
		if (folderHierarchy.length == 1)
			return;
		
		String currFolder = folderHierarchy[0];
		
		createDirectory(currFolder);
		
		for (int i = 1; i < folderHierarchy.length; i++) {
			currFolder += getPathSeparator() + folderHierarchy[i];
			
			createDirectory(currFolder);
		}
	}
	
	public static void write(String fileName, List<String> linesToWrite) {
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		try {
			
			FileWriter file = new FileWriter(fileName, true);
			BufferedWriter buffer = new BufferedWriter(file);
			
			for (int i = 0; i < linesToWrite.size(); i++) {
				String str = linesToWrite.get(i);
				
				buffer.write(str);
				
				//Do not write the new line for last line
				if (i < linesToWrite.size() - 1) {
					buffer.newLine();
				}
			}
			buffer.close();
			file.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void write(String fileName, String... linesToWrite) {
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		try {
			
			FileWriter file = new FileWriter(fileName, true);
			BufferedWriter buffer = new BufferedWriter(file);
			
			for (String str : linesToWrite) {
				buffer.write(str);
				buffer.newLine();
			}
			buffer.close();
			file.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static java.io.File createDirectory(String directoryName) {
		java.io.File dir = new java.io.File(directoryName);
		
		if (!dir.exists()) {
			try {
				dir.mkdir();
			}
			catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		
		return dir;
	}
	
	public static void write(String fileName, String lineToWrite) {
		ArrayList<String> linesToWrite = new ArrayList<String>(1);
		
		linesToWrite.add(lineToWrite);
		
		write(fileName, linesToWrite);
	}
	
	public static void write(String fileName, byte[] source) throws IOException {
		FileOutputStream fout = new FileOutputStream(fileName, true);
		
		BufferedOutputStream bout = new BufferedOutputStream(fout);
		
		InputStream bin = new ByteArrayInputStream(source);
		
		int byte_;
		while ((byte_ = bin.read()) != -1) {
			bout.write(byte_);
		}
		
		bout.close();
		bin.close();
	}
	
	public static void write(String fileName, InputStream inputStream) throws IOException {
		FileOutputStream fout = new FileOutputStream(fileName);
		
		BufferedOutputStream bout = new BufferedOutputStream(fout);
		
		BufferedInputStream bin = new BufferedInputStream(inputStream);
		
		int byte_;
		while ((byte_ = bin.read()) != -1) {
			bout.write(byte_);
		}
		
		bout.close();
		bin.close();
	}
	
	public static InputStream createStreamFromByte(byte[] in) throws IOException {
		InputStream input = new ByteArrayInputStream(in);
		
		return input;
	}
	
	public static InputStream createStreamFromRealPath(String pathToFile) throws IOException {
		return new FileInputStream(pathToFile);
	}
	
	public static InputStream createStreamFromFile(File file) throws IOException {
		return new FileInputStream(file);
	}
	
	/*
	public static void write(String fileName, byte[] bytis){
		//FileInputStream inputStream = new FileInputStream(fileName);
		 
		FileOutputStream fout= new FileOutputStream (fileName);
		
	 	byte[] bytes = new byte[1024];
	 	
		int ch;
		while ((ch = inputStream.read(bytes)) != -1) {
			out.write(bytes, 0, ch);
		}
	    
	    out.flush();
	    out.close();
	    inputStream.close();
	}
	*/
	
	public static java.io.File getFile(String pathToFileFromContextClassLoader) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(pathToFileFromContextClassLoader);
		return new java.io.File(url.getPath());
	}
	
	public static java.io.File getFileFromRealPath(String realPathToFile) throws FileNotFoundException {
		return new java.io.File(realPathToFile);
	}
	
	public static FileInputStream getInputStreamFromFile(String realPathToFile) throws IOException {
		return new FileInputStream(realPathToFile);
	}
	
	/**
	 * Retorna o directorio onde esta localizado o ficheiro passado pelo parametro
	 * 
	 * @param realPathToFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public static java.io.File getDirectory(String realPathToFile) throws FileNotFoundException {
		return getFileFromRealPath(realPathToFile).getParentFile();
	}
	
	public static int countLinesOnFile(String pathToFile) throws IOException {
		return countLinesOnFile(createStreamFromRealPath(pathToFile));
	}
	
	public static int countLinesOnFile(File file) throws IOException {
		return countLinesOnFile(createStreamFromFile(file));
	}
	
	public static int countLinesOnFile(InputStream stream) throws IOException {
		int currPos = 0;
		
		InputStreamReader reader = new InputStreamReader(stream);
		
		BufferedReader buffer = new BufferedReader(reader);
		
		while (buffer.ready()) {
			currPos++;
			buffer.readLine();
		}
		
		buffer.close();
		reader.close();
		
		return currPos;
	}
	
	public static List<String> readAllFileAsListOfString(File file, int start, int qtyLinesToRead, int limit)
	        throws IOException {
		return readAllStreamAsListOfString(createStreamFromFile(file), start, qtyLinesToRead, limit);
	}
	
	public static List<String> readAllFileAsListOfString(String pathToFile, int start, int qtyLinesToRead, int limit)
	        throws IOException {
		return readAllStreamAsListOfString(createStreamFromRealPath(pathToFile), start, qtyLinesToRead, limit);
	}
	
	public static List<String> readAllStreamAsListOfString(InputStream stream, int start, int qtyLinesToRead, int limit)
	        throws IOException {
		long currPos = 1;
		long qtyLinesRead = 0;
		
		InputStreamReader reader = new InputStreamReader(stream);
		
		BufferedReader buffer = new BufferedReader(reader);
		
		List<String> lista = new ArrayList<String>();
		
		while (buffer.ready()) {
			if (qtyLinesToRead != 0 && qtyLinesRead >= qtyLinesToRead) {
				break;
			}
			
			String line = buffer.readLine();
			
			if (currPos >= start && (currPos < limit || limit == 0)) {
				lista.add(line);
				qtyLinesRead++;
			}
			
			currPos++;
		}
		
		buffer.close();
		reader.close();
		
		return lista;
		
	}
	
	public static ByteArrayOutputStream convertToByteArrayOutputStream(InputStream input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		// Fake code simulating the copy
		// You can generally do better with nio if you need...
		// And please, unlike me, do something about the Exceptions :D
		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		
		baos.flush();
		
		return baos;
	}
	
	public static List<String> readAllStreamAsListOfString(InputStream toCopy) throws IOException {
		
		ByteArrayOutputStream baos = convertToByteArrayOutputStream(toCopy);
		
		InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
		InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
		
		int qty = countLinesOnFile(is1);
		
		return readAllStreamAsListOfString(is2, 1, qty, 0);
	}
	
	public static List<String> readAllFileAsListOfStringFromClassLoader(String pathToFileFromContextClassLoader)
	        throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream(pathToFileFromContextClassLoader);
		
		return readAllStreamAsListOfString(stream);
	}
	
	public static List<String> readAllFileAsListOfString(String pathToFile) throws IOException {
		InputStream stream = new FileInputStream(pathToFile);
		
		List<String> lines = readAllStreamAsListOfString(stream);
		
		stream.close();
		
		return lines;
	}
	
	public static String realAllFileAsString(File file) throws IOException {
		InputStream b = null;
		
		try {
			b = Files.newInputStream(file.toPath());
			
			return new String(IOUtils.toByteArray(b));
		}
		finally {
			if (b != null) {
				try {
					b.close();
				}
				catch (IOException e) {}
			}
		}
	}
	
	/**
	 * Devolve o ficheiro sobre a forma de array de bytes
	 * 
	 * @param InputStream file - O ficheiro a ser transformado
	 * @return byte[] - O ficheiro em formato de array de bytes
	 * @throws IOException
	 */
	public static byte[] getBytesFromFile(InputStream is) throws IOException {
		// Determina o tamanho do ficheiro
		long length = is.available();
		
		// Cria o array de bytes para colocar o ficheiro
		byte[] bytes = new byte[(int) length];
		
		// Vai ler o ficheiro
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		
		// Verifica se o ficheiro foi lido na sua totalidade
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file ");
		}
		
		// Fecha o input stream do ficheiro e devolve o ficheiro convertido num
		// array de bytes
		is.close();
		
		return bytes;
	}
	
	public static byte[] readFileAsByte(String pathToFile) throws IOException {
		return getBytesFromFile(new FileInputStream(pathToFile));
	}
	
	public static java.io.File makeDirectory(String pathAndName) {
		java.io.File file = new java.io.File(pathAndName);
		
		file.mkdirs();
		
		return file;
	}
	
	public static String getPathSeparator() {
		return java.io.File.separator;
	}
	
	public static void renameTo(String sourceFileName, String destinationFileName) {
		Path sourceFile = Paths.get(sourceFileName);
		Path targetFile = Paths.get(destinationFileName);
		
		try {
			// Move the source file to the target file, replacing the target if it exists
			Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static boolean removeFile(File sourceFile) {
		if (sourceFile.isDirectory()) {
			String[] entries = sourceFile.list();
			for (String s : entries) {
				java.io.File currentFile = new java.io.File(sourceFile.getPath(), s);
				
				if (currentFile.isDirectory()) {
					removeFile(currentFile);
				} else {
					currentFile.delete();
				}
			}
		}
		
		return sourceFile.delete();
	}
	
	public static boolean removeFile(String sourceFileName) {
		return removeFile(new java.io.File(sourceFileName));
	}
	
	public static String determineExtencaoApartirDoNome(String fileName) {
		String[] partiesOfName = fileName.split("\\.");
		
		return partiesOfName[partiesOfName.length - 1];
	}
	
	/**
	 * @param gera o nome do ficheiro a partir do path real do ficheiro
	 * @return
	 */
	public static String generateFileName(File realPath) {
		return generateFileNameFromRealPath(realPath.getAbsolutePath());
	}
	
	/**
	 * @param gera o nome do ficheiro a partir do path real do ficheiro
	 * @return
	 */
	public static String generateFileNameFromRealPath(String realPath) {
		
		String[] folderHierarchy = null;
		
		try {
			folderHierarchy = realPath.split(getPathSeparator());
		}
		catch (Exception e) {
			//System.out.println("Error: You may be using Windows");
		}
		
		if (folderHierarchy == null)
			folderHierarchy = realPath.split("\\\\"); //If OS is Window
			
		/*
		 * //NOTE: You can use File.separator to discover the file separator for the specific OS - JP;
		
		 * String[] folderHierarchy = realPath.split("\\\\"); //If OS is Window
		
		 if (folderHierarchy.length <= 0){
				folderHierarchy = realPath.split("/"); //If OS is UNIX Based
		 }
		*/
		
		/**
		 * As duas linhas abaixo foram acrescentadas para o caso em que há mistura de separadores.
		 * Por exemplo: "C:\home\myfolder/report.xml"
		 */
		String fileName = folderHierarchy[folderHierarchy.length - 1];
		folderHierarchy = fileName.split("/");
		
		return folderHierarchy[folderHierarchy.length - 1];
	}
	
	public static String generateFileNameFromRealPathWithoutExtension(String realPath) {
		String fileName = generateFileNameFromRealPath(realPath);
		
		String extensao = determineExtencaoApartirDoNome(fileName);
		
		int i = fileName.lastIndexOf(extensao);
		
		return i > 1 ? fileName.substring(0, i - 1) : fileName;
	}
	
	public static void copyFile(File source, File destination) throws IOException {
		tryToCreateDirectoryStructureForFile(destination.getAbsolutePath());
		
		FileUtils.copyFile(source, destination);
	}
	
	public static void copyDirectory(File source, File destination) throws IOException {
		tryToCreateDirectoryStructureForFile(destination.getAbsolutePath());
		
		FileUtils.copyDirectory(source, destination);
	}
	
	public static File getParent(File f) {
		return f.getParentFile();
	}
	
	public static void replaceAllInFile(File file, String toFind, String replacement) {
		try {
			Charset charset = StandardCharsets.UTF_8;
			
			String content = IOUtils.toString(new FileInputStream(file), charset);
			content = content.replaceAll(toFind, replacement);
			IOUtils.write(content, new FileOutputStream(file), charset);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isEmpty(File file) {
		return file.length() == 0;
	}
}
