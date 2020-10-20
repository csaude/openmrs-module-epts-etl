package org.openmrs.module.eptssync;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class ClassGenerator {

	public static void main(String[] args) throws IOException {
		generate();
	}
	
	public static void generate() throws IOException {
		String root = "/home/jpboane/working/prg/jee/workspace/w02/dinamicProject";
		
		String packageName = "main";
		String className = "DynamicClass";
		
		String target = root + "/bin";
		
		String classFullName = packageName + "/" + className  + ".java";
		
		String packageLocation = root + "/src/" + packageName;
		
		FileUtilities.tryToCreateDirectoryStructure(packageLocation);
		FileUtilities.tryToCreateDirectoryStructure(target);
		
		File sourceFile = new File(root  + "/src/" + classFullName);
		
		
		FileWriter writer = new FileWriter(sourceFile);

		String classDefinition = "";
	
		classDefinition += "package "+packageName + ";\n";
		classDefinition += "public class " + className + "{"+ "\n";
		classDefinition += "	public void dynamicMethod(){"+ "\n";
		classDefinition += "		System.out.printls(\"Dynamically printed\");"+ "\n";
		classDefinition += "	}"+ "\n";
		classDefinition += "}";
		
		writer.write(classDefinition);

		writer.close();
		
		compile(sourceFile, new File(target));
	}
	
	public static void compile(File sourceFile, File destinationFile) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(destinationFile));

		// Compile the file
		compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile))).call();
		
		fileManager.close();
	}
	
}
