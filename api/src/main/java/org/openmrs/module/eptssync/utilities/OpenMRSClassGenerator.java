
package org.openmrs.module.eptssync.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.openmrs.module.eptssync.controller.conf.ParentRefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class OpenMRSClassGenerator {
	static CommonUtilities utilities = CommonUtilities.getInstance();

	public static Class<OpenMRSObject> generate(SyncTableInfo syncTableInfo, Connection conn) throws IOException, SQLException, ClassNotFoundException {
		
		syncTableInfo.fullLoad();
		
		Path root = Paths.get(".").normalize().toAbsolutePath();

		File sourceFile = new File(root + "/src/main/java/org/openmrs/module/eptssync/model/openmrs/" + syncTableInfo.generateClassName() + ".java");
		File destinationFileLocation = new File(root + "/target/classes/");
		
		String fullClassName = "org.openmrs.module.eptssync.model.openmrs." + FileUtilities.generateFileNameFromRealPathWithoutExtension(sourceFile.getName());
		
		Class<OpenMRSObject> existingCLass = tryToGetExistingCLass(fullClassName);
			
		if (existingCLass != null && !syncTableInfo.mustRecompileTableClass()) return existingCLass;
	
		String attsDefinition = "";
		String getttersAndSetterDefinition = "";
		String resultSetLoadDefinition = "		";
		
		PreparedStatement st = conn.prepareStatement("SELECT * FROM " + syncTableInfo.getTableName() + " WHERE 1 != 1");

		ResultSet rs = st.executeQuery();
		ResultSetMetaData rsMetaData = rs.getMetaData();

		
		String insertSQLStart = "INSERT INTO " + syncTableInfo.getTableName() + "(";
		String insertSQLEnd = "VALUES(";
		
		String insertSQLDefinition;
		
		String updaSQLDefinition = "UPDATE " + syncTableInfo.getTableName() + " SET ";
		
		String insertParamsDefinition = "Object[] params = {";
		String updateParamsDefinition = "Object[] params = {";
		
		AttDefinedElements attElements;
		
		for (int i = 1; i <= rsMetaData.getColumnCount() - 1; i++) {
			attElements = AttDefinedElements.define(rsMetaData.getColumnName(i), rsMetaData.getColumnTypeName(i), false, syncTableInfo);
			
			attsDefinition = utilities.concatStrings(attsDefinition, attElements.getAttDefinition(), "\n");
			getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getSetterDefinition());
			
			getttersAndSetterDefinition += "\n \n";
			getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getGetterDefinition());

			getttersAndSetterDefinition += "\n \n";
			
			insertSQLStart = utilities.concatStrings(insertSQLStart, attElements.getSqlInsertFirstPartDefinition());
			insertSQLEnd = utilities.concatStrings(insertSQLEnd, attElements.getSqlInsertLastEndPartDefinition());
			
			updaSQLDefinition = utilities.concatStrings(updaSQLDefinition, attElements.getSqlUpdateDefinition());
			
			insertParamsDefinition = utilities.concatStrings(insertParamsDefinition, attElements.getSqlInsertParamDefinifion());
			updateParamsDefinition = utilities.concatStrings(updateParamsDefinition, attElements.getSqlUpdateParamDefinifion());
			
			resultSetLoadDefinition = utilities.concatStrings(resultSetLoadDefinition, attElements.getResultSetLoadDefinition());
			resultSetLoadDefinition += "\n		";
		}
	
		attElements = AttDefinedElements.define(rsMetaData.getColumnName(rsMetaData.getColumnCount()), rsMetaData.getColumnTypeName(rsMetaData.getColumnCount()), true, syncTableInfo);
		
		attsDefinition = utilities.concatStrings(attsDefinition, attElements.getAttDefinition(), "\n");
		getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getSetterDefinition());
			
		getttersAndSetterDefinition += "\n\n";
		
		getttersAndSetterDefinition += "\n \n";
		getttersAndSetterDefinition = utilities.concatStrings(getttersAndSetterDefinition, attElements.getGetterDefinition());
		
		insertSQLStart += attElements.getSqlInsertFirstPartDefinition() + ")";
		insertSQLEnd += attElements.getSqlInsertLastEndPartDefinition() + ")";
		
		updaSQLDefinition += attElements.getSqlUpdateDefinition() + " WHERE " + syncTableInfo.getPrimaryKey() + " = ?;";
		
		insertParamsDefinition += attElements.getSqlInsertParamDefinifion() + "};";
		updateParamsDefinition += attElements.getSqlUpdateParamDefinifion();
		updateParamsDefinition += ", this." + syncTableInfo.getPrimaryKeyAsClassAtt() + "};"; 
		
		insertSQLDefinition = insertSQLStart + " " + insertSQLEnd + ";";
		
		String primaryKeyAtt = syncTableInfo.getPrimaryKeyAsClassAtt();
		
		String methodFromSuperClass = "";
		
		methodFromSuperClass += "	public int getObjectId() { \n ";
		methodFromSuperClass += "		return this."+ primaryKeyAtt + "; \n";
		methodFromSuperClass += "	} \n \n";

		methodFromSuperClass += "	public void setObjectId(int selfId){ \n";
		methodFromSuperClass += "		this." + primaryKeyAtt + " = selfId; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	public void load(ResultSet rs) throws SQLException{ \n";
		methodFromSuperClass +=   		resultSetLoadDefinition;
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String generateDBPrimaryKeyAtt(){ \n ";
		methodFromSuperClass += "		return \""+ syncTableInfo.getPrimaryKey() + "\"; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public Object[]  getInsertParams(){ \n ";
		methodFromSuperClass += "		" + insertParamsDefinition;
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
	
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public Object[]  getUpdateParams(){ \n ";
		methodFromSuperClass += "		" + updateParamsDefinition;
		methodFromSuperClass += "		return params; \n";
		methodFromSuperClass += "	} \n \n";
		
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String getInsertSQL(){ \n ";
		methodFromSuperClass += "		return \""+ insertSQLDefinition + "\"; \n";
		methodFromSuperClass += "	} \n \n";
	
		methodFromSuperClass += "	@JsonIgnore\n";
		methodFromSuperClass += "	public String getUpdateSQL(){ \n ";
		methodFromSuperClass += "		return \""+ updaSQLDefinition + "\"; \n";
		methodFromSuperClass += "	} \n \n";
			
		
		/*methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public void setAllParentsToZero(SyncTableInfo tableInfo, Connection conn) {\n";
			for(ParentRefInfo refInfo : syncTableInfo.getParentRefInfo()) {
				methodFromSuperClass += "		this." + refInfo.getReferenceColumnAsClassAttName() + " = 0;\n";
			}
		methodFromSuperClass += "	}\n\n";*/
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public boolean hasParents() {\n";
		
		for(ParentRefInfo refInfo : syncTableInfo.getParentRefInfo()) {		
			methodFromSuperClass += "		if (this." + refInfo.getReferenceColumnAsClassAttName() + " != 0) return true;\n";
		}
	
		methodFromSuperClass += "		return false;\n";
		
		methodFromSuperClass += "	}\n\n";

		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {\n";
			
		ParentRefInfo sharedKeyRefInfo = syncTableInfo.getSharedKeyRefInfo();
		
		if (sharedKeyRefInfo != null) {
			methodFromSuperClass += "		OpenMRSObject parentOnDestination = null;\n \n";
			
			methodFromSuperClass += "		parentOnDestination = loadParent(";
			methodFromSuperClass += sharedKeyRefInfo.getParentFullClassName() + ".class,";
				
			methodFromSuperClass += " this." +  sharedKeyRefInfo.getReferenceColumnAsClassAttName() + ", false, conn); \n";
			methodFromSuperClass += "		return parentOnDestination.getObjectId();\n \n";
		}
		else {
			methodFromSuperClass += "		throw new RuntimeException(\"No PKSharedInfo defined!\");";
		}
		
		methodFromSuperClass += "	}\n\n";
		
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {\n";
		methodFromSuperClass += "		OpenMRSObject parentOnDestination = null;\n \n";
		
		for(ParentRefInfo refInfo : syncTableInfo.getParentRefInfo()) {
			if (refInfo.isMetadata()) continue;
			
			methodFromSuperClass += "		parentOnDestination = loadParent(";
			methodFromSuperClass += refInfo.getParentFullClassName() + ".class,";
			
			boolean ignorable = syncTableInfo.checkIfisIgnorableParentByClassAttName(refInfo.getReferenceColumnAsClassAttName());
			
			methodFromSuperClass += " this." +  refInfo.getReferenceColumnAsClassAttName() + ", " + ignorable + ", conn); \n";
			methodFromSuperClass += "		this." + refInfo.getReferenceColumnAsClassAttName() + " = 0;\n";
			methodFromSuperClass += "		if (parentOnDestination  != null) this." + refInfo.getReferenceColumnAsClassAttName() + " = parentOnDestination.getObjectId();\n \n";
		}
		
		methodFromSuperClass += "	}\n\n";
		
		
		methodFromSuperClass += "	@Override\n";
		methodFromSuperClass += "	public int getParentValue(String parentAttName) {\n";
		
		for(ParentRefInfo refInfo : syncTableInfo.getParentRefInfo()) {
			methodFromSuperClass += "		if (parentAttName.equals(\"" + refInfo.getReferenceColumnAsClassAttName() + "\")) return this."+refInfo.getReferenceColumnAsClassAttName();
		}
		
		methodFromSuperClass += "	}\n\n";
		
		
		String classDefinition ="";
		
		classDefinition += "package org.openmrs.module.eptssync.model.openmrs; \n \n";
		
		classDefinition += "import org.openmrs.module.eptssync.model.openmrs.generic.*; \n \n";
		
		classDefinition += "import org.openmrs.module.eptssync.utilities.db.conn.DBException; \n";
		classDefinition += "import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; \n \n";
		
		classDefinition += "import java.sql.Connection; \n";
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
		
		FileWriter writer = new FileWriter(sourceFile);

		writer.write(classDefinition);

		writer.close();

		compile(sourceFile, destinationFileLocation);
		
		st.close();
		rs.close();
				
		return tryToGetExistingCLass(fullClassName);
	}
	@SuppressWarnings("unchecked")
	public static Class<OpenMRSObject> tryToGetExistingCLass(String fullClassName) {
		try {
			return (Class<OpenMRSObject>) Class.forName(fullClassName);
		} catch (ClassNotFoundException e) {
			return null;
		}
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
