package org.openmrs.module.epts.etl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class ReportGenerator {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static void main(String[] synConfigFiles) throws IOException, DBException {
		List<EtlConfiguration> syncConfigs = loadSyncConfig(synConfigFiles);
		
		ProcessController controller = new ProcessController(null, syncConfigs.get(0));
		
		OpenConnection conn = controller.getDefaultApp().openConnection();
		
		generateDataInconsistencyReport(syncConfigs.get(0), conn);
		
		conn.markAsSuccessifullyTerminated();
		conn.finalizeConnection();
	}
	
	private static void generateDataInconsistencyReport(EtlConfiguration etlConfiguration, Connection conn)
	        throws DBException {
		etlConfiguration.fullLoad();
		
		for (TableConfiguration config : etlConfiguration.getConfiguredTables()) {
			produceReport(config, conn);
		}
	}
	
	private static void produceReport(TableConfiguration config, Connection conn) throws DBException {
		int recordsOnMainTable = countRecordsOnMainTable(config, conn);
		int recordsOnStageTable = countRecordsOnStageTable(config, conn);
		int parentMissingRecords = 0;
		
		String report = config.getTableName() + "," + (recordsOnMainTable + recordsOnStageTable) + "," + recordsOnMainTable
		        + "," + recordsOnStageTable;
		
		if (utilities.arrayHasElement(config.getParents())) {
			ParentTable parent = null;
			
			int firstMissingParentPos = 0;
			
			while (parentMissingRecords == 0 && firstMissingParentPos < config.getParents().size()) {
				parent = config.getParents().get(firstMissingParentPos);
				
				parentMissingRecords = countMissingParents(config, parent.getTableName(), conn);
				
				firstMissingParentPos++;
			}
			
			if (parentMissingRecords > 0) {
				report += "," + parent.getTableName() + "," + parentMissingRecords;
				
				for (int i = firstMissingParentPos; i < config.getParents().size(); i++) {
					String nextLine = ",,,";
					
					parent = config.getParents().get(i);
					
					parentMissingRecords = countMissingParents(config, parent.getTableName(), conn);
					
					if (parentMissingRecords > 0) {
						nextLine += "," + parent.getTableName() + "," + parentMissingRecords;
						
						report += "\n" + nextLine;
					}
				}
			} else {
				report += ",,";
			}
		} else {
			report += ",-,";
		}
		
		System.out.println(report);
	}
	
	private static int countRecordsOnMainTable(TableConfiguration config, Connection conn) throws DBException {
		String sql = "SELECT COUNT(*) as value FROM " + config.getTableName();
		
		return BaseDAO.find(SimpleValue.class, sql, null, conn).intValue();
	}
	
	private static int countRecordsOnStageTable(TableConfiguration config, Connection conn) throws DBException {
		String sql = "SELECT COUNT(*) as value FROM " + config.generateFullStageTableName();
		
		return BaseDAO.find(SimpleValue.class, sql, null, conn).intValue();
	}
	
	private static int countMissingParents(TableConfiguration config, String tableName, Connection conn)
	        throws DBException {
		String sql = "SELECT COUNT(*) as value FROM " + config.generateFullStageTableName()
		        + " WHERE last_migration_try_err LIKE '%" + tableName + ":%'";
		
		return BaseDAO.find(SimpleValue.class, sql, null, conn).intValue();
	}
	
	private static List<EtlConfiguration> loadSyncConfig(String[] synConfigFiles)
	        throws ForbiddenOperationException, IOException {
		List<EtlConfiguration> syncConfigs = new ArrayList<EtlConfiguration>(synConfigFiles.length);
		
		for (String confFile : synConfigFiles) {
			File file = new File(confFile);
			
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				
				String[] paths = new String[files.length];
				
				for (int i = 0; i < files.length; i++) {
					paths[i] = files[i].getAbsolutePath();
				}
				
				syncConfigs.addAll(loadSyncConfig(paths));
			} else {
				EtlConfiguration conf = EtlConfiguration.loadFromFile(file);
				
				conf.validate();
				
				if (conf.isAutomaticStart()) {
					if (!conf.existsOnArray(syncConfigs)) {
						syncConfigs.add(conf);
					} else
						throw new ForbiddenOperationException(
						        "The configuration [" + conf.getDesignation() + "] exists in more than one files");
				} else {}
			}
		}
		
		return syncConfigs;
	}
}
