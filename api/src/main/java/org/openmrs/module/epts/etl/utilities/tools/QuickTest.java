package org.openmrs.module.epts.etl.utilities.tools;

import java.io.IOException;
import java.util.List;

import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionService;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class QuickTest {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static OpenConnection openConnection() {
		DBConnectionInfo connInfo = new DBConnectionInfo("root", "root", "jdbc:mysql://localhost:3306/tmp_qlm_hgq",
		        "com.mysql.cj.jdbc.Driver");
		
		DBConnectionService service = DBConnectionService.init(connInfo);
		
		return service.openConnection();
	}
	
	public static void main(String[] args) throws DBException {
		String query = "select * from ?.community_arv_patient limit 1";
		
		Object[] params = { "community_data" };
		
		//List<CommunityArvPatientVO> c = BaseDAO.search(CommunityArvPatientVO.class, query, params, openConnection());
		
		//System.out.println(c);
		
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
