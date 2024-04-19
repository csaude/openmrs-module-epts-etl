package org.openmrs.module.epts.etl.utilities.tools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionService;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class QuickTest {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static OpenConnection openConnection() {
		@SuppressWarnings("unused")
		DBConnectionInfo connInfo_localhost = new DBConnectionInfo("root", "root", "jdbc:mysql://localhost:3306/tmp_qlm_hgq",
		        "com.mysql.cj.jdbc.Driver");
		
		String json = "{\r\n" + "            \"dataBaseUserName\":\"root\",\r\n"
		        + "            \"dataBaseUserPassword\":\"#moZart123#\",\r\n"
		        + "            \"connectionURI\":\"jdbc:mysql://10.10.2.71:3306/mozart_q1_fy24_ariel_cab_consolidated?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true\",\r\n"
		        + "            \"driveClassName\":\"com.mysql.cj.jdbc.Driver\"\r\n" + "         }";
		
		DBConnectionInfo connInfo_mozart = DBConnectionInfo.loadFromJson(json);
		
		DBConnectionService service = DBConnectionService.init(connInfo_mozart);
		
		return service.openConnection();
	}
	
	public static void main(String[] args) throws Exception {
		countAll();
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
		
		SyncConfiguration conf = SyncConfiguration.loadFromFile(new File(path));
		
		EtlConfiguration etlConf = conf.getEtlConfiguration().get(1);
		
		etlConf.fullLoad();
		
		AbstractTableConfiguration tbConf = etlConf.getSrcConf();
		
		//tbConf.getSyncRecordClass(conf.getMainApp());
		
		DatabaseEntityPOJOGenerator.generate(tbConf, conf.getMainApp());
		
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
		String path = "D:/PRG/JEE/Workspace/CSaude/eptssync/_testing/openmrs/conf/db_quick_merge_cs_1_maio.json";
		
		ProcessStarter ps = new ProcessStarter(utilities.parseObjectToArray(path));
		
		ps.init();
		
		SyncConfiguration syncConfig = ps.getCurrentController().getConfiguration();
		
		OpenConnection conn = syncConfig.getMainApp().openConnection();
		
		EtlConfiguration tableInfo = syncConfig.find(EtlConfiguration.fastCreate("obs"));
		
		DBQuickMergeController controller = (DBQuickMergeController) ps.getCurrentController().getOperationsControllers()
		        .get(0);
		
		RecordLimits limits = new RecordLimits(1448341 + 500, 1449340, 20, null);
		
		DBQuickMergeSearchParams searchParams = new DBQuickMergeSearchParams(tableInfo, limits, controller);
		
		//tableInfo.setExtraConditionForExport("value_datetime is not null");
		
		List<DatabaseObject> a = SearchParamsDAO.search(searchParams, conn);
		
		System.out.println(a);
	}
}
