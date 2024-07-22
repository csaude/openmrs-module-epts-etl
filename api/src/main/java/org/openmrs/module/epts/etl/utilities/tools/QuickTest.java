package org.openmrs.module.epts.etl.utilities.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionService;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;
import org.openmrs.module.epts.etl.utilities.tools.model.TmpVO;

public class QuickTest {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	static DBConnectionInfo connInfo_localhost = new DBConnectionInfo("root", "root",
	        "jdbc:mysql://localhost:3306/tmp_qlm_hgq", "com.mysql.cj.jdbc.Driver");
	
	static DBConnectionInfo connInfo_quelimane = new DBConnectionInfo("root", "Fgh397$@Wy$Q7",
	        "jdbc:mysql://10.0.0.22:3307/openmrs_gurue_lioma", "com.mysql.cj.jdbc.Driver");
	
	static DBConnectionInfo connInfo_zambezia = new DBConnectionInfo("root", "root",
	        "jdbc:mysql://10.0.0.24:3307/openmrs_derre", "com.mysql.cj.jdbc.Driver");
	
	static String json = "{\r\n" + "            \"dataBaseUserName\":\"root\",\r\n"
	        + "            \"dataBaseUserPassword\":\"#moZart123#\",\r\n"
	        + "            \"connectionURI\":\"jdbc:mysql://10.10.2.71:3306/mozart_q1_fy24_ariel_cab_consolidated?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true\",\r\n"
	        + "            \"driveClassName\":\"com.mysql.cj.jdbc.Driver\"\r\n" + "         }";
	
	@SuppressWarnings("unused")
	public static OpenConnection openConnection() throws DBException {
		
		DBConnectionInfo connInfo_mozart = DBConnectionInfo.loadFromJson(json);
		
		DBConnectionService service = DBConnectionService.init(connInfo_localhost);
		
		return service.openConnection();
	}
	
	public static void main(String[] args) throws Exception {
		saveToTable();
	}
	
	public static void saveToTable() throws DBException {
		
		EtlConfiguration conf = new EtlConfiguration();
		conf.setSrcConnInfo(connInfo_localhost);
		conf.setDstConnInfo(connInfo_localhost);
		
		EtlItemConfiguration itemConf = new EtlItemConfiguration();
		itemConf.setRelatedEtlConfig(conf);
		
		SrcConf src = new SrcConf();
		src.setParentConf(itemConf);
		src.setRelatedEtlConfig(conf);
		
		src.setTableName("tmp");
		
		OpenConnection conn = conf.openSrcConn();
		
		src.fullLoad(conn);
		
		UniqueKeyInfo uk = new UniqueKeyInfo();
		
		uk.setKeyName("test_key");
		
		uk.addKey(Key.fastCreateValued("uuid", "00980f84-4467-475d-8a3f-4136f1358b66"));
		uk.addKey(Key.fastCreateValued("date", DateAndTimeUtilities.createDate("2016-07-06 ")));
		
		EtlDatabaseObject obj = src.createRecordInstance();
		
		obj.setFieldValue("uk", uk.toString());
		
		DatabaseObjectDAO.insert(obj, src, conn);
		
		conn.markAsSuccessifullyTerminated();
		conn.finalizeConnection();
	}
	
	public static void searchOnDbs(Connection conn) throws IOException, DBException {
		List<String> alldbs = FileUtilities
		        .readAllFileAsListOfString("D:\\PRG\\JEE\\Workspace\\CSaude\\eptssync\\_quelimane\\dbs.txt");
		
		String newLine = "\n";
		
		for (String dbName : alldbs) {
			String sql = "";
			sql += " select count(*) as value " + newLine;
			sql += " from  " + dbName + ".encounter inner join " + dbName + ".location using (location_id)" + newLine;
			sql += " where encounter.uuid in ('dfa96496-54aa-4c83-bce2-3cb8c3e4ffd5', '49d6a0ea-aa71-461d-95f5-45cb9b66878e', '15d0ea8e-a1b6-4e68-8891-88474c4f701f', 'ded002e7-b24d-49e1-9be0-e112e333dc4d', '22403449-77c5-466c-a5c5-071e861d9b43', '7a45dfc1-2baa-446e-951c-440b4f2ee5ab', 'a38ea590-3103-4240-acd7-9aaed6951cfd', 'b3cfac54-56da-4271-8c56-6f7141ed9306', 'ee25ca52-d27f-46dd-b1a2-ed355052bb76', 'beabf800-582d-413a-8d37-5470431870cb', '38ca70df-0713-4539-9c1a-03966dc67bc8', '78c79009-f81f-4282-9eeb-1b6220858c35', '1e65d82b-56ae-41d7-8ad8-94bafec2c308', '95ea6e43-639e-4251-9e30-d89e8ca7ca4d', 'b0173511-b4bf-4975-84ea-721a3f95caa6')";
			
			SimpleValue result = null;
			try {
				result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
				
				if (result != null && result.intValue() > 0) {
					System.out.println("Record found on " + dbName + " Rec: " + result.intValue());
				}
			}
			catch (DBException e) {
				if (!e.getLocalizedMessage().contains("Table '" + dbName + ".encounter'")) {
					throw e;
				}
				
			}
			
		}
		
		System.out.println("Finished ");
	}
	
	public static void testResourceSharingBetweenConnections() throws SQLException {
		OpenConnection conn1 = openConnection();
		OpenConnection conn2 = openConnection();
		
		conn2.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		
		String taskId1 = "Task-12";
		
		int result = testConcurrency(taskId1, "insert into tmp(name) values(' " + taskId1 + " ')", conn1);
		
		Object[] params = { result };
		
		TmpVO usr1 = DatabaseObjectDAO.find(TmpVO.class, "select * from tmp where id = ?", params, conn1);
		
		TmpVO usr2 = DatabaseObjectDAO.find(TmpVO.class, "select * from tmp where id = ?", params, conn2);
		
		System.err.println(usr1);
		System.err.println(usr2);
		
		conn1.markAsSuccessifullyTerminated();
		conn1.finalizeConnection();
		
		TmpVO usr3 = DatabaseObjectDAO.find(TmpVO.class, "select * from tmp where id = ?", params, conn2);
		System.err.println(usr3);
		
	}
	
	public static void runInConcurrency() throws DBException {
		OpenConnection conn1 = openConnection();
		
		List<CompletableFuture<Void>> tasks = new ArrayList<>(2);
		
		tasks.add(CompletableFuture.runAsync(() -> {
			runTask("Task 1", conn1);
		}));
		
		tasks.add(CompletableFuture.runAsync(() -> {
			runTask("Task 2", conn1);
		}));
		
		// Wait for all tasks to complete
		CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
		allOf.join();
		
		System.out.println("All tasks completed");
		
		conn1.markAsSuccessifullyTerminated();
		conn1.finalizeConnection();
	}
	
	/**
	 * @param conn
	 * @throws RuntimeException
	 */
	public static void runTask(String taskId, Connection conn) throws RuntimeException {
		System.out.println("Starting Task 1");
		
		try {
			int result = testConcurrency(taskId, "insert into tmp(name) values(' " + taskId + " ')", conn);
			
			System.out.println("Thread " + taskId + " result: " + result);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Integer testConcurrency(String threadId, String sql, Connection connection) throws SQLException {
		PreparedStatement st = null;
		
		try {
			st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			System.out.println("Thread " + threadId + " is starting the execution of statment...");
			
			st.execute();
			
			System.out.println("Thread " + threadId + " finished execution of statment...");
			
			ResultSet rs = st.getGeneratedKeys();
			
			if (rs != null && rs.next()) {
				
				System.out.println("Thread " + threadId + " is retrieving the result of statment...");
				
				return rs.getInt(1);
			} else
				return 0;
			
		}
		finally {
			try {
				st.close();
				
				System.out.println("Thread " + threadId + " finalized the db operation!");
				
				st = null;
			}
			catch (NullPointerException e) {
				st = null;
			}
			catch (SQLException e) {
				st = null;
				throw new DBException(e);
			}
			st = null;
		}
	}
	
	public static void selectLinesOnFile() throws IOException, DBException {
		List<String> lines = FileUtilities.readAllFileAsListOfString("D:\\ORG\\C-SAUDE\\PROJECTOS\\EPTS\\etl\\alldbs.txt");
		
		for (String line : lines) {
			if (line.endsWith("new")) {
				FileUtilities.write("D:\\ORG\\C-SAUDE\\PROJECTOS\\EPTS\\etl\\newdbs.txt", line);
			}
		}
	}
	
	public static void calcularIdade(Connection conn) throws IOException, DBException {
		List<String> patients = FileUtilities.readAllFileAsListOfString(
		    "D:/ORG/C-SAUDE/PROJECTOS/Centralizacao/Tickets/Data-Community/Cacum/Analyse/patient_data/not_extracted.txt");
		
		String newLine = "\n";
		
		for (String uuid : patients) {
			String sql = "";
			sql += " select person_id as value " + newLine;
			sql += " from  " + uuid + ".person " + newLine;
			sql += " where uuid = 'd203ee32-e000-11e6-a91f-4485001ec084' ";
			
			SimpleValue result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
			
			if (result != null) {
				System.out.println("Record found on" + uuid);
			}
		}
		
		System.out.println("Finished ");
	}
	
	public static void searchRecords() throws IOException, DBException {
		
		String path = "D:\\ORG\\C-SAUDE\\PROJECTOS\\Mozart\\etl\\conf\\mpozart_etl.json";
		
		EtlConfiguration conf = EtlConfiguration.loadFromFile(new File(path));
		
		EtlItemConfiguration etlConf = conf.getEtlItemConfiguration().get(0);
		
		etlConf.fullLoad(conf.getOperations().get(0));
		
		EtlDatabaseObjectSearchParams searchParams = new EtlDatabaseObjectSearchParams(null, null);
		
		searchParams.setQtdRecordPerSelected(conf.getOperations().get(0).getProcessingBatch());
		
		DBConnectionInfo dstApp = etlConf.getDstConf().get(0).getRelatedConnInfo();
		
		OpenConnection srcConn = conf.getSrcConnInfo().openConnection();
		
		List<EtlDatabaseObject> syncRecords = searchParams.search(null, srcConn, srcConn);
		
		OpenConnection dstConn = dstApp.openConnection();
		
		Map<String, List<LoadRecord>> mergingRecs = new HashMap<>();
		
		try {
			
			for (EtlObject record : syncRecords) {
				EtlDatabaseObject rec = (EtlDatabaseObject) record;
				
				for (DstConf mappingInfo : etlConf.getDstConf()) {
					
					EtlDatabaseObject destObject = null;
					
					destObject = mappingInfo.getTransformerInstance().transform(null, destObject, mappingInfo, srcConn,
					    dstConn);
					
					if (destObject != null) {
						destObject.loadObjectIdData(mappingInfo);
						
						LoadRecord mr = new LoadRecord(rec, destObject, etlConf.getSrcConf(), mappingInfo, null);
						
						if (mergingRecs.get(mappingInfo.getTableName()) == null) {
							mergingRecs.put(mappingInfo.getTableName(), new ArrayList<>(syncRecords.size()));
						}
						
						mergingRecs.get(mappingInfo.getTableName()).add(mr);
					}
				}
			}
			
			//QuickMergeRecord.mergeAll(mergingRecs, srcConn, dstConn);
			
			dstConn.markAsSuccessifullyTerminated();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	public static void copyFileContentExcludingSomeLines() throws IOException {
		File dir = new File(
		        "D:\\ORG\\C-SAUDE\\PROJECTOS\\Centralizacao\\Tickets\\Data-Community\\Cacum\\zbz-extract\\csv\\selected.corrected");
		
		for (File site : dir.listFiles()) {
			
			for (File file : site.listFiles()) {
				File tmpFile = new File(file.getParentFile().getAbsolutePath() + File.separator
				        + FileUtilities.generateFileNameFromRealPathWithoutExtension(file.getAbsolutePath()) + ".tmp");
				
				int i = 0;
				
				for (String line : FileUtilities.readAllFileAsListOfString(file.getAbsolutePath())) {
					String[] linesParth = line.split(",");
					
					if (utilities.isNumeric(linesParth[0]) || i == 0) {
						FileUtilities.write(tmpFile.getAbsolutePath(), line);
					}
					i++;
				}
				
				FileUtilities.renameTo(tmpFile.getAbsolutePath(), file.getAbsolutePath());
			}
			
		}
		
	}
	
	public static void moveContentToRootFolder() throws IOException {
		File dir = new File(
		        "D:\\ORG\\C-SAUDE\\PROJECTOS\\Centralizacao\\Tickets\\Data-Community\\Cacum\\zbz-extract\\csv\\selected");
		
		for (File site : dir.listFiles()) {
			
			if (site.getName().equals("cacum_cs_cololo")) {
				System.out.println("Stop");
			}
			
			Path sitePath = site.toPath();
			
			File dataDir = new File(site.getAbsoluteFile() + File.separator + "data");
			
			if (dataDir.exists()) {
				
				Path dataPath = dataDir.toPath();
				
				// List all files and directories in the child directory
				try (Stream<Path> paths = Files.list(dataPath)) {
					paths.forEach(path -> {
						try {
							// Move each file/directory to the parent directory
							Files.move(path, sitePath.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
						}
						catch (IOException e) {
							throw new RuntimeException("Failed to move " + path + ": " + e.getMessage());
						}
					});
				}
				
				// Optionally, delete the now-empty child directory
				//Files.delete(sitePath);
				System.out.println("All contents moved and child directory deleted.");
			}
			
		}
		
	}
	
	public static void filderTokenToFile() throws IOException {
		List<String> allLines = FileUtilities.readAllFileAsListOfString("D:/ORG/C-SAUDE/PROJECTOS/Mozart/etl/dbs.txt");
		
		for (String str : allLines) {
			if (str.endsWith("new")) {
				FileUtilities.write("D:/ORG/C-SAUDE/PROJECTOS/Mozart/etl/dbs_new.txt", str);
			}
		}
		
	}
	
	public static void countAll() throws IOException, DBException {
		
		Connection conn = openConnection();
		
		List<String> alldbs = FileUtilities.readAllFileAsListOfString("D:/ORG/C-SAUDE/PROJECTOS/Mozart/Analisy/alldbs.txt");
		
		long total = 0;
		
		alldbs = utilities.parseToList("mozart_q1_fy24_icap_consolidated");
		
		for (String dbName : alldbs) {
			
			String sql = "select count(*) as value from  " + dbName + ".patient_state";
			
			SimpleValue result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
			
			total += result.integerValue();
			
			System.out.println(total);
		}
	}
	
	public static void countAllNotExisting(File dbFiles, Connection conn) throws IOException, DBException {
		
		List<String> alldbs = FileUtilities.readAllFileAsListOfString(dbFiles.getAbsolutePath());
		
		long total = 0;
		
		String dstDb = dbFiles.getName();
		
		String newLine = "\n";
		
		System.out.println("Strarting " + dbFiles);
		
		for (String dbName : alldbs) {
			
			String sql = "";
			sql += " select count(*) as value " + newLine;
			sql += " from  " + dbName + ".patient_state src_ " + newLine;
			sql += " where not exists ( select * " + newLine;
			sql += "					from " + dstDb + ".patient_state dst_" + newLine;
			sql += "					where dst_.state_uuid = src_.state_uuid )";
			
			SimpleValue result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
			
			total = result.integerValue();
			
			if (total > 0) {
				System.err.println("missing on " + dbName + " = " + total);
			}
		}
		
		System.out.println("Finished " + dbFiles);
	}
	
	public static void foundMozartIssue() throws DBException, IOException {
		
		Connection conn = openConnection();
		
		File allConsolidatedDir = new File("D:/ORG/C-SAUDE/PROJECTOS/Mozart/Analisy/dbs");
		
		for (File f : allConsolidatedDir.listFiles()) {
			if (!f.getName().equals("mozart_q1_fy24_consolidated_fix")) {
				continue;
			}
			
			countAllNotExisting(f, conn);
		}
	}
	
	public static void testLoadParents()
	        throws IOException, ClassNotFoundException, ForbiddenOperationException, SQLException {
		String path = "D:\\ORG\\C-SAUDE\\PROJECTOS\\Mozart\\etl\\conf\\testing.json";
		
		EtlConfiguration conf = EtlConfiguration.loadFromFile(new File(path));
		
		EtlItemConfiguration etlConf = conf.getEtlItemConfiguration().get(1);
		
		etlConf.fullLoad(conf.getOperations().get(0));
		
		AbstractTableConfiguration tbConf = etlConf.getSrcConf();
		
		//tbConf.getSyncRecordClass(conf.getMainApp());
		
		DatabaseEntityPOJOGenerator.generate(tbConf, conf.getSrcConnInfo());
		
		System.out.println(conf);
	}
	
	public static void main_(String[] args) throws DBException, IOException {
		//testQuickMerge();
		
		int startingColumn = 3;
		
		for (int i = 0; i < 12; i++) {
			int mes = i + 1;
			int mensalidade = startingColumn + 1 + i * 2;
			int pt = startingColumn + 1 + i * 2 + 1;
			
			System.out.println("Mes= " + mes + ", Mensalidade=" + mensalidade + ", PT= " + pt);
		}
	}
	
	public static void testQuickMerge() throws IOException, ForbiddenOperationException, DBException {
		/*String path = "D:/PRG/JEE/Workspace/CSaude/eptssync/_testing/openmrs/conf/db_quick_merge_cs_1_maio.json";
		
		ProcessStarter ps = new ProcessStarter(utilities.parseObjectToArray(path));
		
		ps.init();
		
		EtlConfiguration syncConfig = ps.getCurrentController().getConfiguration();
		
		OpenConnection conn = syncConfig.getMainApp().openConnection();
		
		EtlItemConfiguration tableInfo = syncConfig.find(EtlItemConfiguration.fastCreate("obs"));
		
		EtlController controller = (EtlController) ps.getCurrentController().getOperationsControllers().get(0);
		
		ThreadRecordIntervalsManager limits = new ThreadRecordIntervalsManager(1448341 + 500, 1449340, 20);
		
		EtlDatabaseObjectSearchParams searchParams = new EtlDatabaseObjectSearchParams(tableInfo, limits, null);
		
		List<EtlDatabaseObject> a = SearchParamsDAO.search(searchParams, conn);
		
		System.out.println(a);*/
	}
}
