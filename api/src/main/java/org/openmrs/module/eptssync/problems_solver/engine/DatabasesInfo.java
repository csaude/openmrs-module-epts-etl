package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DatabasesInfo {
	public static String[] DB_NAMES_21 = { "openmrs_ile_mugulama",
			"openmrs_ile_namanda",
			"openmrs_ile_sede",
			"openmrs_ile_socone",
			"openmrs_namacurra_mbaua",
			"openmrs_namacurra_muceliua",
			"openmrs_namacurra_muebele",
			"openmrs_namacurra_mutange",
			"openmrs_namacurra_sede"};

public static String[] DB_NAMES_22 = { "openmrs_derre",
			"openmrs_gurue_lioma",
			"openmrs_gurue_sede",
			"openmrs_inhassunge_bingagira",
			"openmrs_inhassunge_cherimane",
			"openmrs_inhassunge_gonhane",
			"openmrs_inhassunge_olinda",
			"openmrs_inhassunge_palane",
			"openmrs_inhassunge_sede",
			"openmrs_molumbo_corromana",
			"openmrs_molumbo_namucumua",
			"openmrs_molumbo_sede",
			"openmrs_quelimane_17setembro",
			"openmrs_quelimane_24julho",
			"openmrs_quelimane_4dezembro",
			"openmrs_quelimane_chabeco",
			"openmrs_quelimane_coalane",
			"openmrs_quelimane_hgq",
			"openmrs_quelimane_icidua",
			"openmrs_quelimane_inhangulue",
			"openmrs_quelimane_ionge",
			"openmrs_quelimane_madal",
			"openmrs_quelimane_malanha",
			"openmrs_quelimane_maquival_rio",
			"openmrs_quelimane_maquival_sede",
			"openmrs_quelimane_marrongane",
			"openmrs_quelimane_micajune",
			"openmrs_quelimane_namuinho",
			"openmrs_quelimane_sangariveira",
			"openmrs_quelimane_varela",
			"openmrs_quelimane_zalala"};	

public static String[] DB_NAMES_23 = {	"openmrs_lugela_mulide",
			"openmrs_lugela_munhamade",
			"openmrs_lugela_namagoa",
			"openmrs_lugela_puthine",
			"openmrs_lugela_sede",
			"openmrs_molocue_bonifacio_gruveta",
			"openmrs_molocue_nauela",
			"openmrs_molocue_sede",
			"openmrs_mopeia_chimuara",
			"openmrs_mopeia_lualua",
			"openmrs_mopeia_sede",
			"openmrs_morrumbala_cumbapo",
			"openmrs_morrumbala_megaza",
			"openmrs_morrumbala_mepinha",
			"openmrs_morrumbala_pinda",
			"openmrs_morrumbala_sede",
			"openmrs_namacurra_macuse",
			"openmrs_nicoadala_amoro",
			"openmrs_nicoadala_domela",
			"openmrs_nicoadala_ilalane",
			"openmrs_nicoadala_licuar",
			"openmrs_nicoadala_namacata",
			"openmrs_nicoadala_quinta_girassol",
			"openmrs_nicoadala_sede"};

public static String[] DB_NAMES_24 = {	"openmrs_gile_alto_ligonha",
			"openmrs_gile_kayane",
			"openmrs_gile_mamala",
			"openmrs_gile_moneia",
			"openmrs_gile_muiane",
			"openmrs_gile_sede",
			"openmrs_gile_uape",
			"openmrs_maganja_alto_mutola",
			"openmrs_maganja_cabuir",
			"openmrs_maganja_cariua_mapira_muzo",
			"openmrs_maganja_mabala",
			"openmrs_maganja_moneia",
			"openmrs_maganja_muloa",
			"openmrs_maganja_namurrumo",
			"openmrs_maganja_nante",
			"openmrs_maganja_sede",
			"openmrs_maganja_vila_valdez",
			"openmrs_milange_carico",
			"openmrs_milange_chitambo",
			"openmrs_milange_dachudua",
			"openmrs_milange_dulanha_nambuzi",
			"openmrs_milange_hr",
			"openmrs_milange_liciro",
			"openmrs_milange_majaua_gurgunha",
			"openmrs_milange_muanhambo_mongue",
			"openmrs_milange_sabelua",
			"openmrs_milange_sede",
			"openmrs_milange_tengua",
			"openmrs_milange_vulalo",
			"openmrs_mocuba_16junho",
			"openmrs_mocuba_alto_benfica",
			"openmrs_mocuba_caiave_chimbua",
			"openmrs_mocuba_hd",
			"openmrs_mocuba_intome_namabida",
			"openmrs_mocuba_magogodo",
			"openmrs_mocuba_muanaco",
			"openmrs_mocuba_muaquiua_muloi",
			"openmrs_mocuba_mugeba",
			"openmrs_mocuba_munhiba_mataia",
			"openmrs_mocuba_namagoa",
			"openmrs_mocuba_namanjavira",
			"openmrs_mocuba_nhaluanda",
			"openmrs_mocuba_padre_usera",
			"openmrs_mocuba_pedreira",
			"openmrs_mocuba_samora_machel",
			"openmrs_mocuba_sede",
			"openmrs_mocuba_sisal",
			"openmrs_mocubela_bajone",
			"openmrs_mocubela_gurai",
			"openmrs_mocubela_ilha_idugo",
			"openmrs_mocubela_maneia",
			"openmrs_mocubela_missal",
			"openmrs_mocubela_naico",
			"openmrs_mocubela_sede",
			"openmrs_mocubela_tapata",
			"openmrs_namacurra_furquia",
			"openmrs_namacurra_malei",
			"openmrs_namacurra_mixixine",
			"openmrs_namacurra_mugubia",
			"openmrs_pebane_7abril",
			"openmrs_pebane_alto_maganha",
			"openmrs_pebane_impaca",
			"openmrs_pebane_magiga",
			"openmrs_pebane_malema",
			"openmrs_pebane_mulela",
			"openmrs_pebane_muligode",
			"openmrs_pebane_naburi",
			"openmrs_pebane_pele_pele",
			"openmrs_pebane_sede",
			"openmrs_pebane_tomea"};
	
	private String[] db_names;
	private DBConnectionInfo connInfo;
	private OpenConnection conn;
	private DBConnectionService connService;
	private String serverName;
	
	public DatabasesInfo(String serverName, String[] db_names, DBConnectionInfo connInfo) {
		this.db_names = db_names;
		this.connInfo = connInfo;
		this.connService = DBConnectionService.init(this.connInfo);
		this.serverName = serverName;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String[] getDbNames() {
		return db_names;
	}
	
	public DBConnectionInfo getConnInfo() {
		return connInfo;
	}

	public synchronized OpenConnection acquireConnection() throws DBException {
		try {
			if (this.conn != null && this.conn.isValid(30000)) return this.conn;
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		this.conn = this.connService.openConnection();
		
		return this.conn;
	}
	
	static String[] dbsQ3 = {	"openmrs_q3fy22_01_molocue_gruveta",
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
						"openmrs_q3fy22_19_namacurra_sede",
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
	
		static String[] dbsQ4 = {"openmrs_ile_mugulama",
				"openmrs_ile_namanda",
				"openmrs_ile_sede",
				"openmrs_ile_socone",
				"openmrs_namacurra_mbaua",
				"openmrs_namacurra_muceliua",
				"openmrs_namacurra_muebele",
				"openmrs_namacurra_mutange",
				"openmrs_namacurra_sede",
				"openmrs_gurue_lioma",
				"openmrs_gurue_sede",
				"openmrs_inhassunge_bingagira",
				"openmrs_inhassunge_cherimane",
				"openmrs_inhassunge_gonhane",
				"openmrs_inhassunge_olinda",
				"openmrs_inhassunge_palane",
				"openmrs_inhassunge_sede",
				"openmrs_molumbo_corromana",
				"openmrs_molumbo_namucumua",
				"openmrs_molumbo_sede",
				"openmrs_quelimane_17setembro",
				"openmrs_quelimane_24julho",
				"openmrs_quelimane_4dezembro",
				"openmrs_quelimane_chabeco",
				"openmrs_quelimane_coalane",
				"openmrs_quelimane_hgq",
				"openmrs_quelimane_icidua",
				"openmrs_quelimane_inhangulue",
				"openmrs_quelimane_ionge",
				"openmrs_quelimane_madal",
				"openmrs_quelimane_malanha",
				"openmrs_quelimane_maquival_rio",
				"openmrs_quelimane_maquival_sede",
				"openmrs_quelimane_marrongane",
				"openmrs_quelimane_micajune",
				"openmrs_quelimane_namuinho",
				"openmrs_quelimane_sangariveira",
				"openmrs_quelimane_varela",
				"openmrs_quelimane_zalala",
				"openmrs_lugela_mulide",
				"openmrs_lugela_munhamade",
				"openmrs_lugela_namagoa",
				"openmrs_lugela_puthine",
				"openmrs_lugela_sede",
				"openmrs_molocue_bonifacio_gruveta",
				"openmrs_molocue_nauela",
				"openmrs_molocue_sede",
				"openmrs_mopeia_chimuara",
				"openmrs_mopeia_lualua",
				"openmrs_mopeia_sede",
				"openmrs_morrumbala_cumbapo",
				"openmrs_morrumbala_megaza",
				"openmrs_morrumbala_mepinha",
				"openmrs_morrumbala_pinda",
				"openmrs_morrumbala_sede",
				"openmrs_namacurra_macuse",
				"openmrs_nicoadala_amoro",
				"openmrs_nicoadala_domela",
				"openmrs_nicoadala_ilalane",
				"openmrs_nicoadala_licuar",
				"openmrs_nicoadala_namacata",
				"openmrs_nicoadala_quinta_girassol",
				"openmrs_nicoadala_sede",
				"openmrs_gile_alto_ligonha",
				"openmrs_gile_kayane",
				"openmrs_gile_mamala",
				"openmrs_gile_moneia",
				"openmrs_gile_muiane",
				"openmrs_gile_sede",
				"openmrs_gile_uape",
				"openmrs_maganja_alto_mutola",
				"openmrs_maganja_cabuir",
				"openmrs_maganja_cariua_mapira_muzo",
				"openmrs_maganja_mabala",
				"openmrs_maganja_moneia",
				"openmrs_maganja_muloa",
				"openmrs_maganja_namurrumo",
				"openmrs_maganja_nante",
				"openmrs_maganja_sede",
				"openmrs_maganja_vila_valdez",
				"openmrs_milange_carico",
				"openmrs_milange_chitambo",
				"openmrs_milange_dachudua",
				"openmrs_milange_dulanha_nambuzi",
				"openmrs_milange_hr",
				"openmrs_milange_liciro",
				"openmrs_milange_majaua_gurgunha",
				"openmrs_milange_muanhambo_mongue",
				"openmrs_milange_sabelua",
				"openmrs_milange_sede",
				"openmrs_milange_tengua",
				"openmrs_milange_vulalo",
				"openmrs_mocuba_16junho",
				"openmrs_mocuba_alto_benfica",
				"openmrs_mocuba_caiave_chimbua",
				"openmrs_mocuba_hd",
				"openmrs_mocuba_intome_namabida",
				"openmrs_mocuba_magogodo",
				"openmrs_mocuba_muanaco",
				"openmrs_mocuba_muaquiua_muloi",
				"openmrs_mocuba_mugeba",
				"openmrs_mocuba_munhiba_mataia",
				"openmrs_mocuba_namagoa",
				"openmrs_mocuba_namanjavira",
				"openmrs_mocuba_nhaluanda",
				"openmrs_mocuba_padre_usera",
				"openmrs_mocuba_pedreira",
				"openmrs_mocuba_samora_machel",
				"openmrs_mocuba_sede",
				"openmrs_mocuba_sisal",
				"openmrs_mocubela_bajone",
				"openmrs_mocubela_gurai",
				"openmrs_mocubela_ilha_idugo",
				"openmrs_mocubela_maneia",
				"openmrs_mocubela_missal",
				"openmrs_mocubela_naico",
				"openmrs_mocubela_sede",
				"openmrs_mocubela_tapata",
				"openmrs_namacurra_furquia",
				"openmrs_namacurra_malei",
				"openmrs_namacurra_mixixine",
				"openmrs_namacurra_mugubia",
				"openmrs_pebane_7abril",
				"openmrs_pebane_alto_maganha",
				"openmrs_pebane_impaca",
				"openmrs_pebane_magiga",
				"openmrs_pebane_malema",
				"openmrs_pebane_mulela",
				"openmrs_pebane_muligode",
				"openmrs_pebane_naburi",
				"openmrs_pebane_pele_pele",
				"openmrs_pebane_sede",
				"openmrs_pebane_tomea"};
	
		
		
	public static void main(String[] args) {
		CommonUtilities utilities = CommonUtilities.getInstance();
		
		List<String> q3 = utilities.parseArrayToList(dbsQ3);
		List<String> q4 = utilities.parseArrayToList(dbsQ4);
		
		for (String dbNameOnQ3: q3) {
			String[] hfOnQ3Parts = (dbNameOnQ3.split("openmrs_q3fy22_")[1]).split("_");
			
			String hfOnQ3 = "";
			
			for (int i =0; i < hfOnQ3Parts.length; i++) {
				if (i == 0) continue;
				
				if (i > 1) {
					hfOnQ3 += "_";
				}
				
				hfOnQ3 += hfOnQ3Parts[i];
			}
			
			
			boolean existsOnQ4 = false;
			
			for (String dbnameOnQ4 : q4) {
				if (dbnameOnQ4.contains(hfOnQ3)) {
					existsOnQ4 = true;
					break;
				}
			}
			
			if (!existsOnQ4) {
				System.out.println("NOT FOUND: " + dbNameOnQ3.toUpperCase());
			}
			else {
				System.out.println("Found");
			}
		}
	}
	
}

