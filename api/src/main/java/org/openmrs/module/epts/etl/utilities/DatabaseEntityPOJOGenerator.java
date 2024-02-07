
package org.openmrs.module.epts.etl.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.exceptions.SyncExeption;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.PojobleDatabaseObject;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DatabaseEntityPOJOGenerator {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	static final String[] ignorableFields = { "date_changed", "date_created", "uuid" };
	
	public static Class<DatabaseObject> generate(PojobleDatabaseObject pojoble, AppInfo application)
	        throws IOException, SQLException, ClassNotFoundException {
		if (!pojoble.isFullLoaded())
			pojoble.fullLoad();
		
		String pojoRootFolder = pojoble.getPOJOSourceFilesDirectory().getAbsolutePath();
		
		pojoRootFolder += "/org/openmrs/module/epts.etl/model/pojo/";
		
		File sourceFile = new File(
		        pojoRootFolder + pojoble.getClasspackage(application) + "/" + pojoble.generateClassName() + ".java");
		
		String fullClassName = pojoble.generateFullClassName(application);
		
		Class<DatabaseObject> existingCLass = tryToGetExistingCLass(fullClassName, pojoble.getRelatedSyncConfiguration());
		
		if (existingCLass != null) {
			if (!Modifier.isAbstract(existingCLass.getModifiers())) {
				return existingCLass;
			}
		}
		
		String attsDefinition = "";
		String getttersAndSetterDefinition = "";
		String resultSetLoadDefinition = "		";
		
		String insertSQLFieldsWithoutObjectId = "";
		String insertSQLQuestionMarksWithoutObjectId = "";
		
		String updateSQLDefinition = "UPDATE " + pojoble.getObjectName() + " SET ";
		
		String insertParamsWithoutObjectId = "";
		String updateParamsDefinition = "Object[] params = {";
		
		String insertValuesDefinition = "";
		
		AttDefinedElements attElements;
		
		int qtyAttrs = pojoble.getFields().size();
		
		for (int i = 0; i < qtyAttrs - 1; i++) {
			Field field = pojoble.getFields().get(i);
			
			attElements = AttDefinedElements.define(field.getName(), field.getType(), false, pojoble);
			
			if (!isIgnorableField(field.getName())) {
				attsDefinition = utilities.concatStringsWithSeparator(attsDefinition, attElements.getAttDefinition(), "\n");
				getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition,
				    attElements.getSetterDefinition());
				
				getttersAndSetterDefinition += "\n \n";
				getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition,
				    attElements.getGetterDefinition());
				
				getttersAndSetterDefinition += "\n \n";
			}
			
			insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId,
			    attElements.getSqlInsertFirstPartDefinition());
			insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId,
			    attElements.getSqlInsertLastEndPartDefinition());
			
			updateSQLDefinition = utilities.concatStrings(updateSQLDefinition, attElements.getSqlUpdateDefinition());
			
			insertValuesDefinition = utilities.concatStrings(insertValuesDefinition, attElements.getSqlInsertValues());
			
			insertParamsWithoutObjectId = utilities.concatStrings(insertParamsWithoutObjectId,
			    attElements.getSqlInsertParamDefinifion());
			
			updateParamsDefinition = utilities.concatStrings(updateParamsDefinition,
			    attElements.getSqlUpdateParamDefinifion());
			
			resultSetLoadDefinition = utilities.concatStrings(resultSetLoadDefinition,
			    attElements.getResultSetLoadDefinition());
			resultSetLoadDefinition += "\n		";
		}
		
		Field field = pojoble.getFields().get(qtyAttrs - 1);
		
		attElements = AttDefinedElements.define(field.getName(), field.getType(), true, pojoble);
		
		if (!isIgnorableField(field.getName())) {
			attsDefinition = utilities.concatStringsWithSeparator(attsDefinition, attElements.getAttDefinition(), "\n");
			getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition,
			    attElements.getSetterDefinition());
			
			getttersAndSetterDefinition += "\n\n";
			
			getttersAndSetterDefinition += "\n \n";
			getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition,
			    attElements.getGetterDefinition());
		}
		
		updateSQLDefinition += attElements.getSqlUpdateDefinition() + " WHERE " + pojoble.getPrimaryKey() + " = ?;";
		
		updateParamsDefinition += attElements.getSqlUpdateParamDefinifion();
		
		resultSetLoadDefinition += attElements.getResultSetLoadDefinition();
		resultSetLoadDefinition += "\n";
		
		insertParamsWithoutObjectId += attElements.getSqlInsertParamDefinifion();
		
		insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId,
		    attElements.getSqlInsertFirstPartDefinition());
		insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId,
		    attElements.getSqlInsertLastEndPartDefinition());
		
		if (pojoble.getPrimaryKey() != null) {
			updateParamsDefinition += ", this." + pojoble.getPrimaryKeyAsClassAtt() + "};";
		} else {
			updateParamsDefinition += ", null};";
		}
		
		String insertSQLDefinitionWithoutObjectId = "INSERT INTO " + pojoble.getObjectName() + "("
		        + insertSQLFieldsWithoutObjectId + ") VALUES( " + insertSQLQuestionMarksWithoutObjectId + ");";
		String insertParamsWithoutObjectIdDefinition = "Object[] params = {" + insertParamsWithoutObjectId + "};";
		
		String insertSQLDefinitionWithObjectId = "INSERT INTO " + pojoble.getObjectName() + "(" + pojoble.getPrimaryKey()
		        + ", " + insertSQLFieldsWithoutObjectId + ") VALUES(?, " + insertSQLQuestionMarksWithoutObjectId + ");";
		String insertParamsWithObjectIdDefinition = "Object[] params = {this." + pojoble.getPrimaryKeyAsClassAtt() + ", "
		        + insertParamsWithoutObjectId + "};";
		
		insertValuesDefinition += attElements.getSqlInsertValues();
		
		String methodFromSuperClass = "";
		
		String primaryKeyAtt = pojoble.hasPK() ? pojoble.getPrimaryKeyAsClassAtt() : null;
		
		methodFromSuperClass += "	public Integer getObjectId() { \n ";
		if (pojoble.isNumericColumnType() && pojoble.hasPK())
			methodFromSuperClass += "		return this." + primaryKeyAtt + "; \n";
		else
			methodFromSuperClass += "		return 0; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	public void setObjectId(Integer selfId){ \n";
		if (pojoble.isNumericColumnType() && pojoble.hasPK())
			methodFromSuperClass += "		this." + primaryKeyAtt + " = selfId; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	public void load(ResultSet rs) throws SQLException{ \n";
		methodFromSuperClass += "		super.load(rs);\n";
		methodFromSuperClass += resultSetLoadDefinition;
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String generateDBPrimaryKeyAtt(){ \n ";
		methodFromSuperClass += "		return \"" + pojoble.getPrimaryKey() + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String getInsertSQLWithoutObjectId(){ \n ";
		methodFromSuperClass += "		return \"" + insertSQLDefinitionWithoutObjectId + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public Object[]  getInsertParamsWithoutObjectId(){ \n ";
		methodFromSuperClass += "		" + insertParamsWithoutObjectIdDefinition;
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String getInsertSQLWithObjectId(){ \n ";
		methodFromSuperClass += "		return \"" + insertSQLDefinitionWithObjectId + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public Object[]  getInsertParamsWithObjectId(){ \n ";
		methodFromSuperClass += "		" + insertParamsWithObjectIdDefinition;
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public Object[]  getUpdateParams(){ \n ";
		methodFromSuperClass += "		" + updateParamsDefinition;
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String getUpdateSQL(){ \n ";
		methodFromSuperClass += "		return \"" + updateSQLDefinition + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String generateInsertValues(){ \n ";
		methodFromSuperClass += "		return \"\"+" + insertValuesDefinition + "; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public boolean hasParents() {\n";
		
		if (utilities.arrayHasElement(pojoble.getParents())) {
			for (RefInfo refInfo : pojoble.getParents()) {
				if (refInfo.isNumericRefColumn()) {
					methodFromSuperClass += "		if (this." + refInfo.getRefColumnAsClassAttName()
					        + " != 0) return true;\n\n";
				} else {
					methodFromSuperClass += "		if (this." + refInfo.getRefColumnAsClassAttName()
					        + " != null) return true;\n\n";
				}
			}
		}
		
		methodFromSuperClass += "		return false;\n";
		
		methodFromSuperClass += "	}\n\n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public Integer getParentValue(String parentAttName) {";
		
		if (utilities.arrayHasElement(pojoble.getParents())) {
			for (RefInfo refInfo : pojoble.getParents()) {
				if (refInfo.isNumericRefColumn()) {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) return this."
					        + refInfo.getRefColumnAsClassAttName() + ";";
				} else {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) return 0;";
				}
			}
		}
		
		if (utilities.arrayHasElement(pojoble.getConditionalParents())) {
			for (RefInfo refInfo : pojoble.getConditionalParents()) {
				if (refInfo.isNumericRefColumn()) {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) return this."
					        + refInfo.getRefColumnAsClassAttName() + ";";
				} else {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) return Integer.parseInt(this."
					        + refInfo.getRefColumnAsClassAttName() + ");";
				}
			}
		}
		
		methodFromSuperClass += "\n\n";
		
		methodFromSuperClass += "		throw new RuntimeException(\"No found parent for: \" + parentAttName);";
		
		methodFromSuperClass += "	}\n\n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public void changeParentValue(String parentAttName, DatabaseObject newParent) {";
		
		if (utilities.arrayHasElement(pojoble.getParents())) {
			for (RefInfo refInfo : pojoble.getParents()) {
				if (refInfo.isNumericRefColumn()) {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) {\n			this."
					        + refInfo.getRefColumnAsClassAttName()
					        + " = newParent.getObjectId();\n			return;\n		}";
				} else {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) {\n			this."
					        + refInfo.getRefColumnAsClassAttName()
					        + " = \"\" + newParent.getObjectId();\n			return;\n		}";
				}
			}
		}
		
		if (utilities.arrayHasElement(pojoble.getConditionalParents())) {
			for (RefInfo refInfo : pojoble.getConditionalParents()) {
				if (refInfo.isNumericRefColumn()) {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) {\n			this."
					        + refInfo.getRefColumnAsClassAttName()
					        + " = newParent.getObjectId();\n			return;\n		}";
				} else {
					methodFromSuperClass += "		\n		if (parentAttName.equals(\""
					        + refInfo.getRefColumnAsClassAttName() + "\")) {\n			this."
					        + refInfo.getRefColumnAsClassAttName()
					        + " = newParent.getObjectId().toString();\n			return;\n		}";
				}
			}
		}
		
		methodFromSuperClass += "\n\n";
		
		methodFromSuperClass += "		throw new RuntimeException(\"No found parent for: \" + parentAttName);\n";
		
		methodFromSuperClass += "	}\n\n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public void setParentToNull(String parentAttName) {";
		
		if (utilities.arrayHasElement(pojoble.getParents())) {
			for (RefInfo refInfo : pojoble.getParents()) {
				methodFromSuperClass += "		\n		if (parentAttName.equals(\"" + refInfo.getRefColumnAsClassAttName()
				        + "\")) {\n			this." + refInfo.getRefColumnAsClassAttName()
				        + " = null;\n			return;\n		}";
			}
		}
		
		methodFromSuperClass += "\n\n";
		
		methodFromSuperClass += "		throw new RuntimeException(\"No found parent for: \" + parentAttName);\n";
		
		methodFromSuperClass += "	}\n\n";
		
		String classDefinition = "package " + pojoble.generateFullPackageName(application) + ";\n\n";
		
		classDefinition += "import org.openmrs.module.epts.etl.model.pojo.generic.*; \n \n";
		classDefinition += "import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; \n \n";
		classDefinition += "import org.openmrs.module.epts.etl.utilities.AttDefinedElements; \n";
		classDefinition += "import java.sql.SQLException; \n";
		classDefinition += "import java.sql.ResultSet; \n \n";
		classDefinition += "import com.fasterxml.jackson.annotation.JsonIgnore; \n \n";
		
		classDefinition += "public class " + pojoble.generateClassName()
		        + " extends AbstractDatabaseObject implements DatabaseObject { \n";
		classDefinition += attsDefinition + "\n \n";
		classDefinition += "	public " + pojoble.generateClassName() + "() { \n";
		classDefinition += "		this.metadata = " + pojoble.isMetadata() + ";\n";
		classDefinition += "	} \n \n";
		classDefinition += getttersAndSetterDefinition + "\n \n";
		classDefinition += methodFromSuperClass + "\n";
		
		classDefinition += "}";
		
		FileUtilities.tryToCreateDirectoryStructureForFile(sourceFile.getAbsolutePath());
		
		FileWriter writer = new FileWriter(sourceFile);
		
		writer.write(classDefinition);
		
		writer.close();
		
		compile(sourceFile, pojoble, application);
		
		existingCLass = tryToGetExistingCLass(fullClassName, pojoble.getRelatedSyncConfiguration());
		
		if (existingCLass == null)
			throw new SyncExeption("The class for " + pojoble.getObjectName() + " was not created!") {
				
				private static final long serialVersionUID = 1L;
			};
		
		return existingCLass;
	}
	
	private static boolean isIgnorableField(String columnName) {
		
		for (String field : ignorableFields) {
			if (field.equals(columnName))
				return true;
		}
		
		return false;
	}
	
	public static Class<DatabaseObject> generateSkeleton(PojobleDatabaseObject pojoable, AppInfo application)
	        throws IOException, SQLException, ClassNotFoundException {
		if (!pojoable.isFullLoaded())
			pojoable.fullLoad();
		
		String pojoRootPackage = pojoable.getPOJOSourceFilesDirectory().getAbsolutePath();
		
		pojoRootPackage += pojoable.isDestinationInstallationType() ? "/org/openmrs/module/epts.etl/model/pojo/"
		        : "/org/openmrs/module/epts.etl/model/pojo/source/";
		
		File sourceFile = new File(
		        pojoRootPackage + pojoable.getClasspackage(application) + "/" + pojoable.generateClassName() + ".java");
		
		String fullClassName = "org.openmrs.module.epts.etl.model.pojo";
		
		fullClassName += pojoable.isDestinationInstallationType() ? "." : fullClassName + "source.";
		
		fullClassName += pojoable.getClasspackage(application) + "."
		        + FileUtilities.generateFileNameFromRealPathWithoutExtension(sourceFile.getName());
		
		Class<DatabaseObject> existingCLass = tryToGetExistingCLass(fullClassName, pojoable.getRelatedSyncConfiguration());
		
		if (existingCLass != null)
			return existingCLass;
		
		String classDefinition = "package org.openmrs.module.epts.etl.model.pojo.";
		
		classDefinition += pojoable.isDestinationInstallationType() ? "" : "source.";
		
		classDefinition += pojoable.getClasspackage(application) + "; \n \n";
		
		classDefinition += "import org.openmrs.module.epts.etl.model.pojo.generic.*; \n \n";
		
		classDefinition += "public abstract class " + pojoable.generateClassName()
		        + " extends AbstractDatabaseObject implements DatabaseObject { \n";
		classDefinition += "	public " + pojoable.generateClassName() + "() { \n";
		classDefinition += "	} \n \n";
		classDefinition += "}";
		
		FileUtilities.tryToCreateDirectoryStructureForFile(sourceFile.getAbsolutePath());
		
		FileWriter writer = new FileWriter(sourceFile);
		
		writer.write(classDefinition);
		
		writer.close();
		
		compile(sourceFile, pojoable, application);
		
		return tryToGetExistingCLass(fullClassName, pojoable.getRelatedSyncConfiguration());
	}
	
	public static Class<DatabaseObject> tryToGetExistingCLass(String fullClassName, SyncConfiguration syncConfiguration) {
		Class<DatabaseObject> clazz = tryToLoadFromOpenMRSClassLoader(fullClassName);
		
		if (clazz == null) {
			if (syncConfiguration.getModuleRootDirectory() != null)
				clazz = tryToLoadFromClassPath(fullClassName, syncConfiguration.getModuleRootDirectory());
			
			if (clazz == null) {
				clazz = tryToLoadFromClassPath(fullClassName, syncConfiguration.getClassPathAsFile());
			}
			
			if (clazz == null) {
				clazz = tryToLoadFromClassPath(fullClassName, syncConfiguration.getPOJOCompiledFilesDirectory());
			}
		}
		
		return clazz;
	}
	
	public static Class<DatabaseObject> tryToGetExistingCLass(String fullClassName) {
		return tryToLoadFromOpenMRSClassLoader(fullClassName);
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Class<DatabaseObject> tryToLoadFromOpenMRSClassLoader(String fullClassName) {
		try {
			return (Class<DatabaseObject>) DatabaseObject.class.getClassLoader().loadClass(fullClassName);
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Class<DatabaseObject> tryToLoadFromClassPath(String fullClassName, File classPath) {
		
		try {
			URL[] classPaths = new URL[] { classPath.toURI().toURL() };
			
			URLClassLoader loader = URLClassLoader.newInstance(classPaths);
			
			Class<DatabaseObject> c = null;
			
			c = (Class<DatabaseObject>) loader.loadClass(fullClassName);
			
			loader.close();
			
			return c;
		}
		catch (ClassNotFoundException e) {
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	private static void addAllToClassPath(List<File> classPath, File file) {
		classPath.add(file);
		
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				addAllToClassPath(classPath, f);
			}
		}
	}
	
	public static void compile(File sourceFile, PojobleDatabaseObject pojoble, AppInfo app) throws IOException {
		File destinationFile = pojoble.getPOJOCopiledFilesDirectory();
		
		if (!destinationFile.exists())
			FileUtilities.tryToCreateDirectoryStructure(destinationFile.getAbsolutePath());
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(destinationFile));
		
		List<File> classPathFiles = new ArrayList<File>();
		
		classPathFiles.add(destinationFile);
		
		addAllToClassPath(classPathFiles, pojoble.getClassPath());
		
		fileManager.setLocation(StandardLocation.CLASS_PATH, classPathFiles);
		
		compiler.getTask(null, fileManager, null, null, null,
		    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile))).call();
		
		fileManager.close();
		
		ClassPathUtilities.addClassToClassPath(pojoble, app);
	}
	
}
