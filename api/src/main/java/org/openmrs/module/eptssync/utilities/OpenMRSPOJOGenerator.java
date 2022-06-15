
package org.openmrs.module.eptssync.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class OpenMRSPOJOGenerator {
	static CommonUtilities utilities = CommonUtilities.getInstance();
	static final String[] ignorableFields = {"date_changed", "date_created", "uuid"};
	
	public static Class<OpenMRSObject> generate(SyncTableConfiguration syncTableInfo, Connection conn) throws IOException, SQLException, ClassNotFoundException {
		if (!syncTableInfo.isFullLoaded()) syncTableInfo.fullLoad();

		String pojoRootFolder = syncTableInfo.getPOJOSourceFilesDirectory().getAbsolutePath();
		
		pojoRootFolder += syncTableInfo.isDestinationInstallationType() ? "/org/openmrs/module/eptssync/model/pojo/" : "/org/openmrs/module/eptssync/model/pojo/source/";
		
		File sourceFile = new File(pojoRootFolder + syncTableInfo.getClasspackage() + "/" + syncTableInfo.generateClassName() + ".java");
		
		String fullClassName = syncTableInfo.generateFullClassName();
		
		Class<OpenMRSObject> existingCLass = tryToGetExistingCLass(fullClassName, syncTableInfo.getRelatedSynconfiguration());
			
		if (existingCLass != null) {
			if (!Modifier.isAbstract(existingCLass.getModifiers())) {
				return existingCLass;
			}
		}
	
		String attsDefinition = "";
		String getttersAndSetterDefinition = "";
		String resultSetLoadDefinition = "		";
		
		PreparedStatement st = conn.prepareStatement("SELECT * FROM " + syncTableInfo.getTableName() + " WHERE 1 != 1");

		ResultSet rs = st.executeQuery();
		ResultSetMetaData rsMetaData = rs.getMetaData();

		
		String insertSQLFieldsWithoutObjectId = "";
		String insertSQLQuestionMarksWithoutObjectId = "";
		
		String updateSQLDefinition = "UPDATE " + syncTableInfo.getTableName() + " SET ";
		
		String insertParamsWithoutObjectId = "";
		String updateParamsDefinition = "Object[] params = {";
		
		String insertValuesDefinition = "";
		
		AttDefinedElements attElements;
		
		int qtyAttrs = rsMetaData.getColumnCount();
		
		for (int i = 1; i <= qtyAttrs - 1; i++) {
			attElements = AttDefinedElements.define(rsMetaData.getColumnName(i), rsMetaData.getColumnTypeName(i), false, syncTableInfo);
			
			if (!isIgnorableField(rsMetaData.getColumnName(i))) {
				attsDefinition = utilities.concatStringsWithSeparator(attsDefinition, attElements.getAttDefinition(), "\n");
				getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getSetterDefinition());
				
				getttersAndSetterDefinition += "\n \n";
				getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getGetterDefinition());
	
				getttersAndSetterDefinition += "\n \n";
			}
			
			insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId, attElements.getSqlInsertFirstPartDefinition());
			insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId, attElements.getSqlInsertLastEndPartDefinition());
			
			updateSQLDefinition = utilities.concatStrings(updateSQLDefinition, attElements.getSqlUpdateDefinition());
			
			insertValuesDefinition = utilities.concatStrings(insertValuesDefinition, attElements.getSqlInsertValues());
			
			insertParamsWithoutObjectId = utilities.concatStrings(insertParamsWithoutObjectId, attElements.getSqlInsertParamDefinifion());
			
			updateParamsDefinition = utilities.concatStrings(updateParamsDefinition, attElements.getSqlUpdateParamDefinifion());
			
			resultSetLoadDefinition = utilities.concatStrings(resultSetLoadDefinition, attElements.getResultSetLoadDefinition());
			resultSetLoadDefinition += "\n		";
		}
	
		attElements = AttDefinedElements.define(rsMetaData.getColumnName(qtyAttrs), rsMetaData.getColumnTypeName(qtyAttrs), true, syncTableInfo);
		
		if (!isIgnorableField(rsMetaData.getColumnName(qtyAttrs))) {
			attsDefinition = utilities.concatStringsWithSeparator(attsDefinition, attElements.getAttDefinition(), "\n");
			getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getSetterDefinition());
				
			getttersAndSetterDefinition += "\n\n";
			
			getttersAndSetterDefinition += "\n \n";
			getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getGetterDefinition());
		}
		
		updateSQLDefinition += attElements.getSqlUpdateDefinition() + " WHERE " + syncTableInfo.getPrimaryKey() + " = ?;";
		
		updateParamsDefinition += attElements.getSqlUpdateParamDefinifion();
		
		resultSetLoadDefinition += attElements.getResultSetLoadDefinition();
		resultSetLoadDefinition += "\n";
		
		insertParamsWithoutObjectId += attElements.getSqlInsertParamDefinifion();
	
		insertSQLFieldsWithoutObjectId = utilities.concatStrings(insertSQLFieldsWithoutObjectId, attElements.getSqlInsertFirstPartDefinition());
		insertSQLQuestionMarksWithoutObjectId = utilities.concatStrings(insertSQLQuestionMarksWithoutObjectId, attElements.getSqlInsertLastEndPartDefinition());
		
		if (syncTableInfo.getPrimaryKey() != null) {
			updateParamsDefinition += ", this." + syncTableInfo.getPrimaryKeyAsClassAtt() + "};"; 
		}
		else {
			updateParamsDefinition += ", null};"; 				
		}
		
		String insertSQLDefinitionWithoutObjectId = "INSERT INTO " + syncTableInfo.getTableName() + "(" + insertSQLFieldsWithoutObjectId + ") VALUES( " + insertSQLQuestionMarksWithoutObjectId + ");";
		String insertParamsWithoutObjectIdDefinition = "Object[] params = {" + insertParamsWithoutObjectId + "};";
		
		String insertSQLDefinitionWithObjectId = "INSERT INTO " + syncTableInfo.getTableName() + "(" + syncTableInfo.getPrimaryKey() + ", " + insertSQLFieldsWithoutObjectId + ") VALUES(?, " + insertSQLQuestionMarksWithoutObjectId + ");";
		String insertParamsWithObjectIdDefinition = "Object[] params = {this." + syncTableInfo.getPrimaryKeyAsClassAtt() + ", "  + insertParamsWithoutObjectId + "};";
		
		insertValuesDefinition += attElements.getSqlInsertValues();
		
		//GENERATE INFO FOR UNAVALIABLE COLUMNS
		//if (!DBUtilities.isColumnExistOnTable(syncTableInfo.getTableName(), "uuid", conn)) {
		//	getttersAndSetterDefinition += generateDefaultGetterAndSetterDefinition("uuid", "String");
		//}
		
		/*
		if (!DBUtilities.isColumnExistOnTable(syncTableInfo.getTableName(), "origin_record_id", conn)) {
			getttersAndSetterDefinition += generateDefaultGetterAndSetterDefinition("originRecordId", "int");
		}
		
		if (!DBUtilities.isColumnExistOnTable(syncTableInfo.getTableName(), "origin_app_location_code", conn)) {
			getttersAndSetterDefinition += generateDefaultGetterAndSetterDefinition("originAppLocationCode", "String");
		}
		
		if (!DBUtilities.isColumnExistOnTable(syncTableInfo.getTableName(), "consistent", conn)) {
			getttersAndSetterDefinition += generateDefaultGetterAndSetterDefinition("consistent", "int");
		}*/
	
		String methodFromSuperClass = "";

		String primaryKeyAtt = syncTableInfo.hasPK() ? syncTableInfo.getPrimaryKeyAsClassAtt() : null;
		
		methodFromSuperClass += "	public Integer getObjectId() { \n ";
		if (syncTableInfo.isNumericColumnType() && syncTableInfo.hasPK()) methodFromSuperClass += "		return this."+ primaryKeyAtt + "; \n";
		else methodFromSuperClass += "		return 0; \n";
		methodFromSuperClass += "	} \n \n";

		methodFromSuperClass += "	public void setObjectId(Integer selfId){ \n";
		if (syncTableInfo.isNumericColumnType() && syncTableInfo.hasPK()) methodFromSuperClass += "		this." + primaryKeyAtt + " = selfId; \n";
		methodFromSuperClass += "	} \n \n";
	
		methodFromSuperClass += "	public void load(ResultSet rs) throws SQLException{ \n";
		methodFromSuperClass += "		super.load(rs);\n";
		methodFromSuperClass +=   		resultSetLoadDefinition;
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String generateDBPrimaryKeyAtt(){ \n ";
		methodFromSuperClass += "		return \""+ syncTableInfo.getPrimaryKey() + "\"; \n";
		methodFromSuperClass += "	} \n \n";

		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String getInsertSQLWithoutObjectId(){ \n ";
		methodFromSuperClass += "		return \""+ insertSQLDefinitionWithoutObjectId + "\"; \n";
		methodFromSuperClass += "	} \n \n";

		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public Object[]  getInsertParamsWithoutObjectId(){ \n ";
		methodFromSuperClass += "		" + insertParamsWithoutObjectIdDefinition;
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String getInsertSQLWithObjectId(){ \n ";
		methodFromSuperClass += "		return \""+ insertSQLDefinitionWithObjectId + "\"; \n";
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
		methodFromSuperClass += "		return \""+ updateSQLDefinition + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String generateInsertValues(){ \n ";
		methodFromSuperClass += "		return \"\"+"+ insertValuesDefinition + "; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public boolean hasParents() {\n";
		
		for(RefInfo refInfo : syncTableInfo.getParents()) {		
			if (refInfo.isNumericRefColumn()) {
				methodFromSuperClass += "		if (this." + refInfo.getRefColumnAsClassAttName() + " != 0) return true;\n\n";
			}
			else {
				methodFromSuperClass += "		if (this." + refInfo.getRefColumnAsClassAttName() + " != null) return true;\n\n";
			}
		}
	
		methodFromSuperClass += "		return false;\n";
		
		methodFromSuperClass += "	}\n\n";
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public Integer getParentValue(String parentAttName) {";
		
		for(RefInfo refInfo : syncTableInfo.getParents()) {
			if (refInfo.isNumericRefColumn()) {
				methodFromSuperClass += "		\n		if (parentAttName.equals(\"" + refInfo.getRefColumnAsClassAttName() + "\")) return this."+refInfo.getRefColumnAsClassAttName() + ";";
			}
			else {
				methodFromSuperClass += "		\n		if (parentAttName.equals(\"" + refInfo.getRefColumnAsClassAttName() + "\")) return 0;";
			}
		}
		
		methodFromSuperClass += "\n\n";
		
		methodFromSuperClass += "		throw new RuntimeException(\"No found parent for: \" + parentAttName);";
		
		methodFromSuperClass += "	}\n\n";
		
	
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {";
		
		for(RefInfo refInfo : syncTableInfo.getParents()) {
			if (refInfo.isNumericRefColumn()) {
				methodFromSuperClass += "		\n		if (parentAttName.equals(\"" + refInfo.getRefColumnAsClassAttName() + "\")) {\n			this."+refInfo.getRefColumnAsClassAttName() + " = newParent.getObjectId();\n			return;\n		}";
			}
			else {
				methodFromSuperClass += "		\n		if (parentAttName.equals(\"" + refInfo.getRefColumnAsClassAttName() + "\")) {\n			this."+refInfo.getRefColumnAsClassAttName() + " = \"\" + newParent.getObjectId();\n			return;\n		}";
			}
		}
		
		methodFromSuperClass += "\n\n";
		
		methodFromSuperClass += "		throw new RuntimeException(\"No found parent for: \" + parentAttName);\n";
		
		methodFromSuperClass += "	}\n\n";
	
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public void setParentToNull(String parentAttName) {";
		
		for(RefInfo refInfo : syncTableInfo.getParents()) {
			methodFromSuperClass += "		\n		if (parentAttName.equals(\"" + refInfo.getRefColumnAsClassAttName() + "\")) {\n			this."+refInfo.getRefColumnAsClassAttName() + " = null;\n			return;\n		}";
		}
		
		methodFromSuperClass += "\n\n";
		
		methodFromSuperClass += "		throw new RuntimeException(\"No found parent for: \" + parentAttName);\n";
		
		methodFromSuperClass += "	}\n\n";
			
		String classDefinition ="package " + syncTableInfo.generateFullPackageName() + ";\n\n";
		
		classDefinition += "import org.openmrs.module.eptssync.model.pojo.generic.*; \n \n";
		classDefinition += "import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; \n \n";
		classDefinition += "import org.openmrs.module.eptssync.utilities.AttDefinedElements; \n";
		classDefinition += "import java.sql.SQLException; \n";
		classDefinition += "import java.sql.ResultSet; \n \n";
		classDefinition += "import com.fasterxml.jackson.annotation.JsonIgnore; \n \n";
		
		classDefinition += "public class " + syncTableInfo.generateClassName() + " extends AbstractOpenMRSObject implements OpenMRSObject { \n";
		classDefinition += 		attsDefinition + "\n \n";
		classDefinition += "	public " + syncTableInfo.generateClassName() + "() { \n";
		classDefinition += "		this.metadata = " + syncTableInfo.isMetadata() + ";\n";
		classDefinition += "	} \n \n";
		classDefinition +=  	getttersAndSetterDefinition + "\n \n";
		classDefinition +=  	methodFromSuperClass + "\n";
		
		classDefinition += "}";
		
		FileUtilities.tryToCreateDirectoryStructureForFile(sourceFile.getAbsolutePath());
		
		FileWriter writer = new FileWriter(sourceFile);

		writer.write(classDefinition);

		writer.close();
		
		compile(sourceFile, syncTableInfo);
		
		st.close();
		rs.close();
				
		return tryToGetExistingCLass(fullClassName, syncTableInfo.getRelatedSynconfiguration());
	}
	
	private static boolean isIgnorableField(String columnName) {
		
		for (String field : ignorableFields) {
			if (field.equals(columnName)) return true;
		}
		
		return false;
	}

	public static Class<OpenMRSObject> generateSkeleton(SyncTableConfiguration syncTableInfo, Connection conn) throws IOException, SQLException, ClassNotFoundException {
		if (!syncTableInfo.isFullLoaded()) syncTableInfo.fullLoad();
			
		String pojoRootPackage = syncTableInfo.getPOJOSourceFilesDirectory().getAbsolutePath();
		
		pojoRootPackage += syncTableInfo.isDestinationInstallationType() ? "/org/openmrs/module/eptssync/model/pojo/" : "/org/openmrs/module/eptssync/model/pojo/source/";
	
		File sourceFile = new File(pojoRootPackage + syncTableInfo.getClasspackage() + "/" + syncTableInfo.generateClassName() + ".java");
		
		String fullClassName  = "org.openmrs.module.eptssync.model.pojo";
		
		fullClassName += syncTableInfo.isDestinationInstallationType() ? "." : fullClassName + "source.";
		
		fullClassName += syncTableInfo.getClasspackage() + "." + FileUtilities.generateFileNameFromRealPathWithoutExtension(sourceFile.getName());
		
		Class<OpenMRSObject> existingCLass = tryToGetExistingCLass(fullClassName, syncTableInfo.getRelatedSynconfiguration());
			
		if (existingCLass != null) return existingCLass;
	
		String classDefinition ="package org.openmrs.module.eptssync.model.pojo.";
		
		classDefinition += syncTableInfo.isDestinationInstallationType() ? "" : "source.";
		
		
		classDefinition += syncTableInfo.getClasspackage() + "; \n \n";
		
		classDefinition += "import org.openmrs.module.eptssync.model.pojo.generic.*; \n \n";
		
		classDefinition += "public abstract class " + syncTableInfo.generateClassName() + " extends AbstractOpenMRSObject implements OpenMRSObject { \n";
		classDefinition += "	public " + syncTableInfo.generateClassName() + "() { \n";
		classDefinition += "	} \n \n";
		classDefinition += "}";
		
		FileUtilities.tryToCreateDirectoryStructureForFile(sourceFile.getAbsolutePath());

		FileWriter writer = new FileWriter(sourceFile);

		writer.write(classDefinition);

		writer.close();

		compile(sourceFile, syncTableInfo);
		
		return tryToGetExistingCLass(fullClassName, syncTableInfo.getRelatedSynconfiguration());
	}
	
	static Logger logger = Logger.getLogger(OpenMRSPOJOGenerator.class);
	
	private static String generateDefaultGetterAndSetterDefinition(String attName, String attType) {
		String getttersAndSetterDefinition = "";
		
		getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, AttDefinedElements.defineDefaultGetterMethod(attName, attType));
		
		getttersAndSetterDefinition += "\n \n";
		getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, AttDefinedElements.defineDefaultSetterMethod(attName, attType));

		getttersAndSetterDefinition += "\n \n";
		
		return getttersAndSetterDefinition;
	}
	
	public static Class<OpenMRSObject> tryToGetExistingCLass(String fullClassName, SyncConfiguration syncConfiguration) {
		Class<OpenMRSObject> clazz = tryToLoadFromOpenMRSClassLoader(fullClassName);
		
		if (clazz == null) {
			if (syncConfiguration.getModuleRootDirectory() != null) clazz = tryToLoadFromClassPath(fullClassName, syncConfiguration.getModuleRootDirectory());
			
			if (clazz == null) {
				clazz = tryToLoadFromClassPath(fullClassName, syncConfiguration.getClassPathAsFile());
			}
		}
		
		return clazz;
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Class<OpenMRSObject> tryToLoadFromOpenMRSClassLoader(String fullClassName) {
		try {
			return (Class<OpenMRSObject>) OpenMRSObject.class.getClassLoader().loadClass(fullClassName);
			//return (Class<OpenMRSObject>) Class.forName(fullClassName);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked"})
	private static Class<OpenMRSObject> tryToLoadFromClassPath(String fullClassName, File classPath) {
		
		try {
			URL[] classPaths = new URL[] {classPath.toURI().toURL()};
			
			URLClassLoader loader = URLClassLoader.newInstance(classPaths);
			
			Class<OpenMRSObject> c = null;
			
			c = (Class<OpenMRSObject>) loader.loadClass(fullClassName);
			
	        loader.close();
	        
	        return c;
		} 
		catch (ClassNotFoundException e) {
			return null;
		} catch (IOException e) {
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
	
	public static void compile(File sourceFile, SyncTableConfiguration tableConfiguration) throws IOException {
		File destinationFile = tableConfiguration.getPOJOCopiledFilesDirectory();
		
		if (!destinationFile.exists()) FileUtilities.tryToCreateDirectoryStructure(destinationFile.getAbsolutePath());
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(destinationFile));
		
		List<File> classPathFiles = new ArrayList<File>();
		
		classPathFiles.add(destinationFile);
		
		addAllToClassPath(classPathFiles, tableConfiguration.getClassPath());
		
		fileManager.setLocation(StandardLocation.CLASS_PATH, classPathFiles);
	
		compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile))).call();
		
		fileManager.close();
	
		ClassPathUtilities.addClassToClassPath(tableConfiguration);
	}
	
	
}
