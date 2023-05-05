package org.openmrs.module.eptssync.utilities.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class ExportMozatDBs {
	
	static String dir = "/home/eip/bkps/q3fy22";
	
	static DBConnectionService dbService;
	
	public static void main(String[] args) throws SQLException {
		
		DBConnectionInfo dbConnInfo = new DBConnectionInfo();
		dbConnInfo.setConnectionURI(
		    "jdbc:mysql://10.10.2.2:53301/mysql?autoReconnect=true&useSSL=false");
		dbConnInfo.setDataBaseUserName("root");
		dbConnInfo.setDataBaseUserPassword("root");
		dbConnInfo.setDriveClassName("com.mysql.jdbc.Driver");
		
		dbService = DBConnectionService.init(dbConnInfo);
	

		List<Map<String, Object>> dataBaseNames = new ArrayList<>();
		
		if (args == null || args.length < 1) {
			System.err.println("One o all params were not specified! Please specify to params 1. DB Export root directory");
			
			System.exit(1);
		}
		
		String dbExportRootDirectoryPath = args[0];
		
		if (!new File(dbExportRootDirectoryPath).exists()) {
			System.err.println(
			    "The path [" + dbExportRootDirectoryPath + "] for Sites file does not correspond existing file!");
			System.exit(1);
		}
		
		System.out.println("Initializing...");
		System.out.println("Using Sites File: " + dbExportRootDirectoryPath);
		
		for (File file : getDumps(new File(dbExportRootDirectoryPath))) {
			//File Name Pathern: openmrs_site_code
			
			String dumpName = FileUtilities.generateFileNameFromRealPath(file.getAbsolutePath());
			//String siteName = (dumpName.split("openmrs_")[1]).split(".sql")[0];
			String siteName = generateSiteName(dumpName);
			
			String dbName = siteName.toLowerCase();
			
			dataBaseNames.add(fastCreateMap("dbName", dbName, "typeIdLookupExists", false, "qtyRecordsOnTypeIdLookupTable", 0));
			
			String[] cmd = new String[] { "/bin/bash", dbExportRootDirectoryPath + "/db_import.sh", dbName,
			        file.getAbsolutePath() };
			
			try {
				System.out.println("Starting import of dump [" + file.getAbsolutePath() + "] to [" + dbName + "] for site ["
				        + siteName + "]");
				
				Runtime.getRuntime().exec(cmd);
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		
		boolean exportRunning=true;
		
		while (exportRunning) {
			TimeCountDown.sleep(20);
			
			OpenConnection conn = dbService.openConnection();
			
			for (Map<String, Object> db : dataBaseNames) {
				exportRunning = exportIsRunningOnDb(db, conn);
				
				if (exportRunning) {
					break;
				}
			}
			
			conn.finalizeConnection();
			
			System.err.println("The aplication is still working...");
		}
		
		System.out.println("All jobs finished!");
		
	}
	
	static boolean exportIsRunningOnDb(Map<String, Object> db, Connection conn) throws SQLException {
		String dbName = (String) db.get("dbName");
		boolean typeIdLookupExists = (boolean) db.get("typeIdLookupExists");
		int oldQtyRecordsOnTypeIdLookupTable = (int) db.get("qtyRecordsOnTypeIdLookupTable");
		
		if (!typeIdLookupExists) {
			typeIdLookupExists = DBUtilities.isResourceExist(dbName, DBUtilities.RESOURCE_TYPE_TABLE, "type_id_lookup", conn);
			
			if (!typeIdLookupExists) {
				return true;
			}
			else {
				db.put("typeIdLookupExists", true);
			}
		}
		
		String sql = "select count(*) as value from  " + dbName + ".type_id_lookup ";
		
		SimpleValue result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
		
		int qtyRecordsOnTypeIdLookupTable = result.intValue();
		
		if (qtyRecordsOnTypeIdLookupTable == 0) return true;
		
		if (oldQtyRecordsOnTypeIdLookupTable != qtyRecordsOnTypeIdLookupTable) {
			db.put("qtyRecordsOnTypeIdLookupTable", qtyRecordsOnTypeIdLookupTable);
			
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Create a map populated with an initial entries passed by parameter
	 * 
	 * @param params the entries which will populate the map. It's an array which emulate a map entries
	 *            in this format [key1, val1, key2, val2, key3, val3, ..]
	 * @return the generated map
	 * @throws ForbiddenOperationException when the params array length is not odd
	 */
	public static Map<String, Object> fastCreateMap(Object... params) throws ForbiddenOperationException {
		if (params.length % 2 != 0)
			throw new ForbiddenOperationException("The parameters for fastCreatMap must be pars <K1, V1>, <K2, V2>");
		
		Map<String, Object> map = new HashMap<>();
		
		int paramsSize = params.length / 2;
		
		for (int set = 1; set <= paramsSize; set++) {
			int pos = set * 2 - 1;
			
			map.put(((String) params[pos - 1]), params[pos]);
		}
		
		return map;
	}
		
	
	private static String generateSiteName(String dumpName) {
		String[] nameParts = dumpName.split("_");
		
		String siteName = "";
		
		for (int i = 0; i < nameParts.length - 1; i++) {
			
			if (i > 0)
				siteName += "_";
			
			siteName += nameParts[i];
		}
		
		return siteName;
	}
	
	public static List<File> getDumps(File rootDirectory) {
		//Assume-se que, os dumps encontram-se armazenados em directorios representado os distritos correspondentes
		
		List<File> dumps = new ArrayList<File>();
		
		//Loop over the all folder content
		for (File file : rootDirectory.listFiles()) {
			File[] files = null;
			
			if (!file.isDirectory()) {
				if (file.getName().endsWith("sql")) {
					dumps.add(file);
				}
			} else {
				files = file.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith("sql");
					}
				});
				
				if (files.length > 0) {
					dumps.addAll(CommonUtilities.getInstance().parseArrayToList(files));
				}
			}
		}
		
		return dumps;
	}
}
