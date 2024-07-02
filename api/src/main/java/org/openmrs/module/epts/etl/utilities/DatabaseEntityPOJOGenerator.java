
package org.openmrs.module.epts.etl.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DatabaseEntityPOJOGenerator {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	static final String[] ignorableFields = { "date_changed", "date_created", "uuid" };
	
	public static Class<EtlDatabaseObject> generate(DatabaseObjectConfiguration pojoble, DBConnectionInfo connInfo)
	        throws IOException, SQLException, ClassNotFoundException {
		if (!pojoble.isFullLoaded())
			pojoble.fullLoad();
		
		String pojoRootFolder = pojoble.getPOJOSourceFilesDirectory().getAbsolutePath();
		
		pojoRootFolder += "/org/openmrs/module/epts.etl/model/pojo/";
		
		File sourceFile = new File(
		        pojoRootFolder + pojoble.getClasspackage(connInfo) + "/" + pojoble.generateClassName() + ".java");
		
		String fullClassName = pojoble.generateFullClassName(connInfo);
		
		Class<EtlDatabaseObject> existingCLass = null;
		
		String attsDefinition = "";
		String gettersAndSetterDefinition = "";
		String resultSetLoadDefinition = "";
		
		String insertSQLFieldsWithoutObjectId = "";
		String insertSQLFieldsWithObjectId = "";
		
		String insertSQLQuestionMarksWithoutObjectId = "";
		String insertSQLQuestionMarksWithObjectId = "";
		
		String updateSQLDefinition = "UPDATE " + pojoble.getObjectName() + " SET ";
		
		String insertParamsWithoutObjectId = "";
		String insertParamsWithObjectId = "";
		
		String updateParamsDefinition = "Object[] params = {";
		
		String insertValuesWithoutObjectIdDefinition = "";
		String insertValuesWithObjectIdDefinition = "";
		
		AttDefinedElements attElements;
		
		int qtyAttrs = pojoble.getFields().size();
		
		for (int i = 0; i < qtyAttrs - 1; i++) {
			Field field = pojoble.getFields().get(i);
			
			attElements = AttDefinedElements.define(field.getName(), field.getType(), false, pojoble);
			
			if (!isIgnorableField(field.getName())) {
				attsDefinition = utilities.concatStringsWithSeparator(attsDefinition, attElements.getAttDefinition(), "\n");
				gettersAndSetterDefinition = utilities.concatStrings(gettersAndSetterDefinition,
				    attElements.getSetterDefinition());
				
				gettersAndSetterDefinition += "\n \n";
				gettersAndSetterDefinition = utilities.concatStrings(gettersAndSetterDefinition,
				    attElements.getGetterDefinition());
				
				gettersAndSetterDefinition += "\n \n";
			}
			
			if (!attElements.isPartOfObjectId()) {
				insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId,
				    attElements.getSqlInsertFirstPartDefinition());
				
				insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId,
				    attElements.getSqlInsertLastEndPartDefinition());
				
				insertValuesWithoutObjectIdDefinition = utilities.concatStrings(insertValuesWithoutObjectIdDefinition,
				    attElements.getSqlInsertValues());
				
				insertParamsWithoutObjectId = utilities.concatStrings(insertParamsWithoutObjectId,
				    attElements.getSqlInsertParamDefinifion());
			}
			
			insertSQLFieldsWithObjectId = utilities.concatStrings(insertSQLFieldsWithObjectId,
			    attElements.getSqlInsertFirstPartDefinition());
			
			insertSQLQuestionMarksWithObjectId = utilities.concatStrings(insertSQLQuestionMarksWithObjectId,
			    attElements.getSqlInsertLastEndPartDefinition());
			
			insertValuesWithObjectIdDefinition = utilities.concatStrings(insertValuesWithObjectIdDefinition,
			    attElements.getSqlInsertValues());
			
			insertParamsWithObjectId = utilities.concatStrings(insertParamsWithObjectId,
			    attElements.getSqlInsertParamDefinifion());
			
			updateSQLDefinition = utilities.concatStrings(updateSQLDefinition, attElements.getSqlUpdateDefinition());
			
			updateParamsDefinition = utilities.concatStrings(updateParamsDefinition,
			    attElements.getSqlUpdateParamDefinifion());
			
			resultSetLoadDefinition = utilities.concatStrings(resultSetLoadDefinition,
			    attElements.getResultSetLoadDefinition());
			
			resultSetLoadDefinition += "\n";
		}
		
		Field field = pojoble.getFields().get(qtyAttrs - 1);
		
		attElements = AttDefinedElements.define(field.getName(), field.getType(), true, pojoble);
		
		if (!isIgnorableField(field.getName())) {
			attsDefinition = utilities.concatStringsWithSeparator(attsDefinition, attElements.getAttDefinition(), "\n");
			gettersAndSetterDefinition = utilities.concatStrings(gettersAndSetterDefinition,
			    attElements.getSetterDefinition());
			
			gettersAndSetterDefinition += "\n\n";
			
			gettersAndSetterDefinition += "\n \n";
			gettersAndSetterDefinition = utilities.concatStrings(gettersAndSetterDefinition,
			    attElements.getGetterDefinition());
		}
		
		//insertValuesWithoutObjectIdDefinition
		
		updateSQLDefinition += attElements.getSqlUpdateDefinition();
		
		updateParamsDefinition += attElements.getSqlUpdateParamDefinifion();
		
		resultSetLoadDefinition += attElements.getResultSetLoadDefinition();
		resultSetLoadDefinition += "\n";
		
		if (!attElements.isPartOfObjectId()) {
			insertParamsWithoutObjectId += attElements.getSqlInsertParamDefinifion();
			
			insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId,
			    attElements.getSqlInsertFirstPartDefinition());
			insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId,
			    attElements.getSqlInsertLastEndPartDefinition());
			
			insertValuesWithoutObjectIdDefinition += attElements.getSqlInsertValues();
		}
		
		insertParamsWithObjectId += attElements.getSqlInsertParamDefinifion();
		
		insertSQLFieldsWithObjectId = utilities.concatStrings(insertSQLFieldsWithObjectId,
		    attElements.getSqlInsertFirstPartDefinition());
		
		insertSQLQuestionMarksWithObjectId = utilities.concatStrings(insertSQLQuestionMarksWithObjectId,
		    attElements.getSqlInsertLastEndPartDefinition());
		
		insertValuesWithObjectIdDefinition += attElements.getSqlInsertValues();
		
		if (pojoble.getPrimaryKey() != null) {
			updateSQLDefinition += " WHERE " + pojoble.getPrimaryKey().parseToParametrizedStringConditionWithAlias();
			
			for (Key key : pojoble.getPrimaryKey().getFields()) {
				updateParamsDefinition += ", this." + key.getNameAsClassAtt();
			}
			
			updateParamsDefinition += "};";
			
		}
		
		String insertSQLDefinitionWithoutObjectId = "INSERT INTO " + pojoble.getObjectName() + "("
		        + insertSQLFieldsWithoutObjectId + ") VALUES( " + insertSQLQuestionMarksWithoutObjectId + ");";
		
		String insertSQLDefinitionWithObjectId = "INSERT INTO " + pojoble.getObjectName() + "(" + insertSQLFieldsWithObjectId
		        + ") VALUES( " + insertSQLQuestionMarksWithObjectId + ");";
		
		String insertParamsWithoutObjectIdDefinition = "Object[] params = {" + insertParamsWithoutObjectId + "};";
		
		String insertParamsWithObjectIdDefinition = "Object[] params = {" + insertParamsWithObjectId + "};";
		
		String methodFromSuperClass = "";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public void load(ResultSet rs) throws SQLException{ \n";
		methodFromSuperClass += "		super.load(rs);\n \n";
		methodFromSuperClass += resultSetLoadDefinition;
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public String getInsertSQLWithoutObjectId(){ \n ";
		methodFromSuperClass += "		return \"" + insertSQLDefinitionWithoutObjectId + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public String getInsertSQLWithObjectId(){ \n ";
		methodFromSuperClass += "		return \"" + insertSQLDefinitionWithObjectId + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public Object[]  getInsertParamsWithoutObjectId(){ \n ";
		methodFromSuperClass += "		" + insertParamsWithoutObjectIdDefinition + "\n";
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public Object[]  getInsertParamsWithObjectId(){ \n ";
		methodFromSuperClass += "		" + insertParamsWithObjectIdDefinition + "\n";
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public Object[]  getUpdateParams(){ \n ";
		
		if (pojoble.getPrimaryKey() != null) {
			methodFromSuperClass += "		" + updateParamsDefinition + "\n";
			methodFromSuperClass += "		return params; \n";
		} else {
			methodFromSuperClass += "		throw new RuntimeException(\"Impossible auto update command! No primary key is defined for table object!\");";
		}
		
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public String getUpdateSQL(){ \n ";
		
		if (pojoble.getPrimaryKey() != null) {
			methodFromSuperClass += "		return \"" + updateSQLDefinition + "\"; \n";
		} else {
			methodFromSuperClass += "		throw new RuntimeException(\"Impossible auto update command! No primary key is defined for table object!\");";
		}
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public String generateInsertValuesWithoutObjectId(){ \n ";
		methodFromSuperClass += "		return \"\"+" + insertValuesWithoutObjectIdDefinition + "; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public String generateInsertValuesWithObjectId(){ \n ";
		methodFromSuperClass += "		return \"\"+" + insertValuesWithObjectIdDefinition + "; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public boolean hasParents() {\n";
		
		if (utilities.arrayHasElement(pojoble.getParentRefInfo())) {
			for (ParentTable refInfo : pojoble.getParentRefInfo()) {
				
				for (RefMapping map : refInfo.getRefMapping()) {
					
					if (map.isPrimitieveRefColumn()) {
						methodFromSuperClass += "		if (this." + map.getChildFieldNameAsAttClass()
						        + " != 0) return true;\n\n";
					} else {
						methodFromSuperClass += "		if (this." + map.getChildFieldNameAsAttClass()
						        + " != null) return true;\n\n";
					}
				}
			}
		}
		
		methodFromSuperClass += "		return false;\n";
		
		methodFromSuperClass += "	}\n\n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public Integer getParentValue(String parentAttName) {";
		
		if (utilities.arrayHasElement(pojoble.getParentRefInfo())) {
			for (ParentTable refInfo : pojoble.getParentRefInfo()) {
				methodFromSuperClass += "		\n		if (parentAttName.equals(\""
				        + refInfo.getChildColumnAsClassAttOnSimpleMapping() + "\")) return this."
				        + refInfo.getChildColumnAsClassAttOnSimpleMapping() + ";";
			}
		}
		
		methodFromSuperClass += "\n\n";
		
		methodFromSuperClass += "		throw new RuntimeException(\"No found parent for: \" + parentAttName);";
		
		methodFromSuperClass += "	}\n\n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public String generateTableName() {\n";
		methodFromSuperClass += "		return " + utilities.quote(pojoble.getObjectName()) + ";\n";
		methodFromSuperClass += "	}\n\n";
		
		String classDefinition = "package " + pojoble.generateFullPackageName(connInfo) + ";\n\n";
		
		classDefinition += "import org.openmrs.module.epts.etl.model.pojo.generic.*; \n \n";
		
		if (pojoble.hasDateFields()) {
			classDefinition += "import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; \n \n";
		}
		
		classDefinition += "import org.openmrs.module.epts.etl.utilities.AttDefinedElements; \n \n";
		
		classDefinition += "import java.sql.SQLException; \n";
		classDefinition += "import java.sql.ResultSet; \n \n";
		classDefinition += "import com.fasterxml.jackson.annotation.JsonIgnore; \n \n";
		
		classDefinition += "public class " + pojoble.generateClassName()
		        + " extends AbstractDatabaseObject implements EtlDatabaseObject { \n";
		classDefinition += attsDefinition + "\n \n";
		classDefinition += "	public " + pojoble.generateClassName() + "() { \n";
		classDefinition += "		this.metadata = " + pojoble.isMetadata() + ";\n";
		classDefinition += "	} \n \n";
		classDefinition += gettersAndSetterDefinition + "\n \n";
		classDefinition += methodFromSuperClass + "\n";
		
		classDefinition += "}";
		
		FileUtilities.tryToCreateDirectoryStructureForFile(sourceFile.getAbsolutePath());
		
		FileWriter writer = new FileWriter(sourceFile);
		
		writer.write(classDefinition);
		
		writer.close();
		
		compile(sourceFile, pojoble, connInfo);
		
		existingCLass = tryToGetExistingCLass(fullClassName, pojoble.getRelatedEtlConf());
		
		if (existingCLass == null)
			throw new EtlExceptionImpl("The class for " + pojoble.getObjectName() + " was not created!") {
				
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
	
	public static Class<EtlDatabaseObject> generateSkeleton(DatabaseObjectConfiguration pojoable, DBConnectionInfo connInfo)
	        throws IOException, SQLException, ClassNotFoundException {
		if (!pojoable.isFullLoaded())
			pojoable.fullLoad();
		
		String pojoRootPackage = pojoable.getPOJOSourceFilesDirectory().getAbsolutePath();
		
		pojoRootPackage += pojoable.isDestinationInstallationType() ? "/org/openmrs/module/epts.etl/model/pojo/"
		        : "/org/openmrs/module/epts.etl/model/pojo/source/";
		
		File sourceFile = new File(
		        pojoRootPackage + pojoable.getClasspackage(connInfo) + "/" + pojoable.generateClassName() + ".java");
		
		String fullClassName = "org.openmrs.module.epts.etl.model.pojo";
		
		fullClassName += pojoable.isDestinationInstallationType() ? "." : fullClassName + "source.";
		
		fullClassName += pojoable.getClasspackage(connInfo) + "."
		        + FileUtilities.generateFileNameFromRealPathWithoutExtension(sourceFile.getName());
		
		Class<EtlDatabaseObject> existingCLass = tryToGetExistingCLass(fullClassName,
		    pojoable.getRelatedEtlConf());
		
		if (existingCLass != null)
			return existingCLass;
		
		String classDefinition = "package org.openmrs.module.epts.etl.model.pojo.";
		
		classDefinition += pojoable.isDestinationInstallationType() ? "" : "source.";
		
		classDefinition += pojoable.getClasspackage(connInfo) + "; \n \n";
		
		classDefinition += "import org.openmrs.module.epts.etl.model.pojo.generic.*; \n \n";
		
		classDefinition += "public abstract class " + pojoable.generateClassName()
		        + " extends AbstractDatabaseObject implements EtlDatabaseObject { \n";
		classDefinition += "	public " + pojoable.generateClassName() + "() { \n";
		classDefinition += "	} \n \n";
		classDefinition += "}";
		
		FileUtilities.tryToCreateDirectoryStructureForFile(sourceFile.getAbsolutePath());
		
		FileWriter writer = new FileWriter(sourceFile);
		
		writer.write(classDefinition);
		
		writer.close();
		
		compile(sourceFile, pojoable, connInfo);
		
		return tryToGetExistingCLass(fullClassName, pojoable.getRelatedEtlConf());
	}
	
	public static Class<EtlDatabaseObject> tryToGetExistingCLass(String fullClassName, EtlConfiguration etlConfiguration) {
		Class<EtlDatabaseObject> clazz = tryToLoadFromOpenMRSClassLoader(fullClassName);
		
		if (clazz == null) {
			if (etlConfiguration.getModuleRootDirectory() != null)
				clazz = tryToLoadFromClassPath(fullClassName, etlConfiguration.getModuleRootDirectory());
			
			if (clazz == null) {
				clazz = tryToLoadFromClassPath(fullClassName, etlConfiguration.getClassPathAsFile());
			}
			
			if (clazz == null) {
				clazz = tryToLoadFromClassPath(fullClassName, etlConfiguration.getPOJOCompiledFilesDirectory());
			}
		}
		
		return clazz;
	}
	
	public static Class<EtlDatabaseObject> tryToGetExistingCLass(String fullClassName) {
		return tryToLoadFromOpenMRSClassLoader(fullClassName);
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Class<EtlDatabaseObject> tryToLoadFromOpenMRSClassLoader(String fullClassName) {
		try {
			return (Class<EtlDatabaseObject>) EtlDatabaseObject.class.getClassLoader().loadClass(fullClassName);
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Class<EtlDatabaseObject> tryToLoadFromClassPath(String fullClassName, File classPath) {
		
		try {
			URL[] classPaths = new URL[] { classPath.toURI().toURL() };
			
			URLClassLoader loader = URLClassLoader.newInstance(classPaths);
			
			Class<EtlDatabaseObject> c = null;
			
			c = (Class<EtlDatabaseObject>) loader.loadClass(fullClassName);
			
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
	
	public static void compile(File sourceFile, DatabaseObjectConfiguration pojoble, DBConnectionInfo connInfo) throws IOException {
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
		
		ClassPathUtilities.addClassToClassPath(pojoble, connInfo);
	}
	
}
