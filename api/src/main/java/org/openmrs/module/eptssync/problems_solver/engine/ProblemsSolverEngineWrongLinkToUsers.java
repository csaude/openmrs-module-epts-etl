package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.ProblemsSolverController;
import org.openmrs.module.eptssync.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.eptssync.problems_solver.model.TmpUserVO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * 
 * @author jpboane
 *
 * @see DBQuickMergeController
 */
public class ProblemsSolverEngineWrongLinkToUsers extends Engine {
	
	public static String[] DB_NAMES = {	"openmrs_q3fy22_01_molocue_gruveta",
										"openmrs_q3fy22_01_molocue_nauela",
										"openmrs_q3fy22_01_molocue_sede",
										"openmrs_q3fy22_03_derre_sede",
										"openmrs_q3fy22_04_gile_alto_ligonha",
										"openmrs_q3fy22_04_gile_kayane",
										"openmrs_q3fy22_04_gile_mamala",
										"openmrs_q3fy22_04_gile_moneia",
										"openmrs_q3fy22_04_gile_muiane",
										"openmrs_q3fy22_04_gile_sede",
										"openmrs_q3fy22_04_gile_uape",
										"openmrs_q3fy22_05_gurue_lioma",
										"openmrs_q3fy22_05_gurue_sede",
										"openmrs_q3fy22_06_ile_mugulama",
										"openmrs_q3fy22_06_ile_namanda",
										"openmrs_q3fy22_06_ile_sede",
										"openmrs_q3fy22_06_socone",
										"openmrs_q3fy22_07_inhassunge_bingagira",
										"openmrs_q3fy22_07_inhassunge_cherimane",
										"openmrs_q3fy22_07_inhassunge_gonhane",
										"openmrs_q3fy22_07_inhassunge_mucula",
										"openmrs_q3fy22_07_inhassunge_olinda",
										"openmrs_q3fy22_07_inhassunge_sede",
										"openmrs_q3fy22_09_lugela_mulide",
										"openmrs_q3fy22_09_lugela_munhamade",
										"openmrs_q3fy22_09_lugela_namagoa",
										"openmrs_q3fy22_09_lugela_putine",
										"openmrs_q3fy22_09_lugela_sede",
										"openmrs_q3fy22_11_maganja_costa_alto_mutola",
										"openmrs_q3fy22_11_maganja_costa_cabuir",
										"openmrs_q3fy22_11_maganja_costa_cariua_mapira_muzo",
										"openmrs_q3fy22_11_maganja_costa_mabala",
										"openmrs_q3fy22_11_maganja_costa_moneia",
										"openmrs_q3fy22_11_maganja_costa_muloa",
										"openmrs_q3fy22_11_maganja_costa_namurumo",
										"openmrs_q3fy22_11_maganja_costa_nante",
										"openmrs_q3fy22_11_maganja_costa_sede",
										"openmrs_q3fy22_11_maganja_costa_vila_valdez",
										"openmrs_q3fy22_12_milange_carico",
										"openmrs_q3fy22_12_milange_chitambo",
										"openmrs_q3fy22_12_milange_dachudua",
										"openmrs_q3fy22_12_milange_dulanha",
										"openmrs_q3fy22_12_milange_gurgunha",
										"openmrs_q3fy22_12_milange_hr_milange",
										"openmrs_q3fy22_12_milange_liciro",
										"openmrs_q3fy22_12_milange_muanhambo",
										"openmrs_q3fy22_12_milange_sabelua",
										"openmrs_q3fy22_12_milange_sede",
										"openmrs_q3fy22_12_milange_tengua",
										"openmrs_q3fy22_12_milange_vulalo",
										"openmrs_q3fy22_13_mocuba_16_de_Junho",
										"openmrs_q3fy22_13_mocuba_alto_benfica",
										"openmrs_q3fy22_13_mocuba_caiave",
										"openmrs_q3fy22_13_mocuba_hd_mocuba",
										"openmrs_q3fy22_13_mocuba_intome",
										"openmrs_q3fy22_13_mocuba_magogodo",
										"openmrs_q3fy22_13_mocuba_mocuba_sisal",
										"openmrs_q3fy22_13_mocuba_muanaco",
										"openmrs_q3fy22_13_mocuba_mugeba",
										"openmrs_q3fy22_13_mocuba_muloi",
										"openmrs_q3fy22_13_mocuba_munhiba",
										"openmrs_q3fy22_13_mocuba_namagoa",
										"openmrs_q3fy22_13_mocuba_namanjavira",
										"openmrs_q3fy22_13_mocuba_nhaluanda",
										"openmrs_q3fy22_13_mocuba_padre_usera",
										"openmrs_q3fy22_13_mocuba_pedreira",
										"openmrs_q3fy22_13_mocuba_samora_machel",
										"openmrs_q3fy22_13_mocuba_sede",
										"openmrs_q3fy22_14_mocubela_bajone",
										"openmrs_q3fy22_14_mocubela_gurai",
										"openmrs_q3fy22_14_mocubela_ilha_idugo",
										"openmrs_q3fy22_14_mocubela_maneia",
										"openmrs_q3fy22_14_mocubela_missal",
										"openmrs_q3fy22_14_mocubela_naico",
										"openmrs_q3fy22_14_mocubela_sede",
										"openmrs_q3fy22_14_mocubela_tapata",
										"openmrs_q3fy22_15_molumbo_corromana",
										"openmrs_q3fy22_15_molumbo_namucumua",
										"openmrs_q3fy22_15_molumbo_sede",
										"openmrs_q3fy22_16_mopeia_chimuara",
										"openmrs_q3fy22_16_mopeia_lua_lua",
										"openmrs_q3fy22_16_mopeia_sede",
										"openmrs_q3fy22_17_morrumbala_cumbapo",
										"openmrs_q3fy22_17_morrumbala_megaza",
										"openmrs_q3fy22_17_morrumbala_mepinha",
										"openmrs_q3fy22_17_morrumbala_pinda",
										"openmrs_q3fy22_17_morrumbala_sede",
										"openmrs_q3fy22_19_namacurra_furquia",
										"openmrs_q3fy22_19_namacurra_macuse",
										"openmrs_q3fy22_19_namacurra_malei",
										"openmrs_q3fy22_19_namacurra_mbua",
										"openmrs_q3fy22_19_namacurra_muceliuia",
										"openmrs_q3fy22_19_namacurra_muebele",
										"openmrs_q3fy22_19_namacurra_mugubia",
										"openmrs_q3fy22_19_namacurra_mutange",
										"openmrs_q3fy22_19_namacurra_muxixine",
										"openmrs_q3fy22_22_qlm_04_dezembro",
										"openmrs_q3fy22_22_qlm_17_set",
										"openmrs_q3fy22_22_qlm_24_julho",
										"openmrs_q3fy22_22_qlm_chabeco",
										"openmrs_q3fy22_22_qlm_coalane",
										"openmrs_q3fy22_22_qlm_hospital_geral",
										"openmrs_q3fy22_22_qlm_icidua",
										"openmrs_q3fy22_22_qlm_inhangule",
										"openmrs_q3fy22_22_qlm_ionge",
										"openmrs_q3fy22_22_qlm_madal",
										"openmrs_q3fy22_22_qlm_malanha",
										"openmrs_q3fy22_22_qlm_maquival_rio",
										"openmrs_q3fy22_22_qlm_maquival_sede",
										"openmrs_q3fy22_22_qlm_marrongana",
										"openmrs_q3fy22_22_qlm_micajune",
										"openmrs_q3fy22_22_qlm_namuinho",
										"openmrs_q3fy22_22_qlm_sangarivela",
										"openmrs_q3fy22_22_qlm_varela",
										"openmrs_q3fy22_22_qlm_zalala",
										"openmrs_q3fy22_23_nicoadala_amoro",
										"openmrs_q3fy22_23_nicoadala_domela",
										"openmrs_q3fy22_23_nicoadala_ilalane",
										"openmrs_q3fy22_23_nicoadala_licuane",
										"openmrs_q3fy22_23_nicoadala_namacata",
										"openmrs_q3fy22_23_nicoadala_q_girassol",
										"openmrs_q3fy22_23_nicoadala_sede",
										"openmrs_q3fy22_24_pebane_7_abril",
										"openmrs_q3fy22_24_pebane_alto_maganha",
										"openmrs_q3fy22_24_pebane_impaca",
										"openmrs_q3fy22_24_pebane_magiga",
										"openmrs_q3fy22_24_pebane_malema",
										"openmrs_q3fy22_24_pebane_mulela",
										"openmrs_q3fy22_24_pebane_muligode",
										"openmrs_q3fy22_24_pebane_naburi",
										"openmrs_q3fy22_24_pebane_pele_pele",
										"openmrs_q3fy22_24_pebane_sede",
										"openmrs_q3fy22_24_pebane_tomea"};
	
	private AppInfo mainApp;
	private AppInfo remoteApp;
	
	public ProblemsSolverEngineWrongLinkToUsers(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		this.remoteApp = getRelatedOperationController().getConfiguration().find(AppInfo.init("remote"));
	}

	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public ProblemsSolverController getRelatedOperationController() {
		return (ProblemsSolverController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		OpenConnection srcConn = remoteApp.openConnection();
		
		try {
			int i = 1;
			for (SyncRecord record : syncRecords) {
				try {
					
					String startingStrLog = utilities.garantirXCaracterOnNumber(i,
					    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
					
					logDebug(startingStrLog + " STARTING RESOLVE PROBLEMS OF RECORD [" + record + "]");
					
					Class<DatabaseObject> syncRecordClass = getSyncTableConfiguration().getSyncRecordClass(getDefaultApp());
					Class<DatabaseObject> prsonRecordClass = SyncTableConfiguration.init("person", getSyncTableConfiguration().getRelatedSynconfiguration()).getSyncRecordClass(getDefaultApp());
					
					DatabaseObject userOnDestDB = DatabaseObjectDAO.getById(syncRecordClass,
					    ((DatabaseObject) record).getObjectId(), conn);
					
					if (userOnDestDB.getParentValue("personId") != 1) {
						logDebug("SKIPPING THE RECORD BECAUSE IT HAS THE CORRECT PERSON ["
						        + userOnDestDB.getParentValue("personId") + "]");
						continue;
					}
					
					if (userOnDestDB.getObjectId() == 1) {
						logDebug("SKIPPING THE RECORD BECAUSE IT IS THE DEFAULT USER");
						continue;
					}
					
					boolean found = false;
					
					for (String dbName : DB_NAMES) {
						DatabaseObject userOnSrcDB = new GenericDatabaseObject();//DatabaseObjectDAO.getByUuidOnSpecificSchema(syncRecordClass, userOnDestDB.getUuid(), dbName, srcConn);
						
						if (userOnSrcDB != null) {
							
							logDebug("RESOLVING USER PROBLEM USING DATA FROM [" + dbName + "]");
							
							DatabaseObject relatedPersonOnSrcDB = DatabaseObjectDAO.getByIdOnSpecificSchema(prsonRecordClass,
							    userOnSrcDB.getParentValue("personId"), dbName, srcConn);
							
							/*if (relatedPersonOnSrcDB == null) {
								logDebug("RELATED PERSON NOT FOUND ON ON [" + dbName + "]");
								continue;
							}
							
							*/
							
							List<DatabaseObject> relatedPersonOnDestDB = null;//DatabaseObjectDAO.getByUuid(prsonRecordClass, relatedPersonOnSrcDB.getUuid(), conn);
							
							userOnDestDB.changeParentValue("personId", relatedPersonOnDestDB.get(0));
							userOnDestDB.save(getSyncTableConfiguration(), conn);
							
							found = true;
							
							break;
						} else {
							logDebug("USER NOT FOUND ON [" + dbName + "]");
						}
					}
					
					if (!found) {
						//throw new ForbiddenOperationException("THE RECORD [" + record + "] WERE NOT FOUND IN ANY SRC!");
					}
				}
				finally {
					i++;
					((TmpUserVO) record).markAsHarmonized(conn);
				}
			} 
		}
		finally {
			srcConn.finalizeConnection();
		}
	}
	
	protected void resolveDuplicatedUuidOnUserTable(List<SyncRecord> syncRecords, Connection conn) throws DBException, ForbiddenOperationException {
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		int i = 1;
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		for (SyncRecord record: syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i, (""+getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			DatabaseObject rec = (DatabaseObject)record;
			
			List<DatabaseObject> dups = null;//DatabaseObjectDAO.getByUuid(getSyncTableConfiguration().getSyncRecordClass(getDefaultApp()), rec.getUuid(), conn);
				
			logDebug(startingStrLog + " RESOLVING..." + rec);
			
			for (int j = 1; j < dups.size(); j++) {
				DatabaseObject dup = dups.get(j);
				
				dup.setUuid(dup.getUuid() + "_" + j);
				
				dup.save(getSyncTableConfiguration(), conn);
			}
			
			
			i++;
		}
		
		if (utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
			logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
			syncRecords.removeAll(recordsToIgnoreOnStatistics);
		}
		
		logDebug("MERGE DONE ON " + syncRecords.size() + " " + getSyncTableConfiguration().getTableName() + "!");		
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ProblemsSolverSearchParams(this.getSyncTableConfiguration(), null);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}

}
